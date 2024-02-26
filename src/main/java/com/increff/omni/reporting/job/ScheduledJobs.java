package com.increff.omni.reporting.job;


import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.flow.ReportRequestFlowApi;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ScheduleStatus;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.security.RateLimitingFilter;
import com.increff.commons.springboot.common.ApiException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import jakarta.persistence.OptimisticLockException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.CommonDtoHelper.*;

@Log4j2
@Component
public class ScheduledJobs {

    @Autowired
    private ReportRequestApi api;
    @Autowired
    private ReportRequestFlowApi reportRequestFlowApi;
    @Autowired
    private ReportScheduleApi scheduleApi;
    @Autowired
    private FolderApi folderApi;
    @Autowired
    private ReportScheduleApi reportScheduleApi;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private OrgSchemaApi orgSchemaApi;
    @Autowired
    private JobFactory jobFactory;
    @Autowired
    @Qualifier(value = "userReportRequestExecutor")
    private Executor userReportExecutor;

    @Autowired
    @Qualifier(value = "scheduleReportRequestExecutor")
    private Executor scheduleReportExecutor;

//    @Autowired
//    private RateLimitingFilter rateLimitingFilter;

    @Scheduled(fixedDelay = 1000)
    public void runUserReports() {
        runReports(userReportExecutor, Collections.singletonList(ReportRequestType.USER));
    }

    @Scheduled(fixedDelay = 1000)
    public void runScheduleReports() {
        runReports(scheduleReportExecutor, Collections.singletonList(ReportRequestType.EMAIL));
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void markJobsStuck() {
        List<ReportRequestPojo> stuckRequests = api.getStuckRequests(properties.getStuckReportTime());
        stuckRequests.forEach(s -> {
            try {
                api.markStuck(s);
            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
                log.debug("Error occurred while marking report in progress for request id : " + s.getId(), e);
            }
        });
    }

    @Scheduled(fixedDelay = 2 * 60 * 1000)
    public void deleteOldFiles() {
        folderApi.deleteOlderFiles();
    }

    @Scheduled(fixedDelay = 10000)
    public void addScheduleReportRequests() throws ApiException {
        List<ReportSchedulePojo> schedulePojos = reportScheduleApi.getEligibleSchedules();
        List<ReportSchedulePojo> alreadyExecutedSchedules = new ArrayList<>();
        schedulePojos.forEach(s -> { // mark schedule status to RUNNING to prevent same schedule getting picked bp multiple times by horizontally scaled servers
            try {
                scheduleApi.updateStatusToRunning(s.getId());
            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException | ApiException e) {
                log.trace("Error occurred while marking status " + ScheduleStatus.RUNNING + " for schedule id : " + s.getId() + " " + e.getMessage());
                alreadyExecutedSchedules.add(s);
            }
        });
        schedulePojos.removeAll(alreadyExecutedSchedules);

        log.debug("Eligible schedules : " + schedulePojos.size());
        for(ReportSchedulePojo s : schedulePojos) {
            OrgSchemaVersionPojo orgSchemaVersionPojo = orgSchemaApi.getCheckByOrgId(s.getOrgId());
            ReportPojo reportPojo = reportApi.getByAliasAndSchema(s.getReportAlias(),
                    orgSchemaVersionPojo.getSchemaVersionId(), false);
            Integer reportId = Objects.isNull(reportPojo) ? null : reportPojo.getId();
            ReportRequestPojo reportRequestPojo = convertToReportRequestPojo(s, reportId);
            List<ReportInputParamsPojo> reportInputParamsPojoList = new ArrayList<>();
            List<ReportScheduleInputParamsPojo> scheduleInputParamsPojos = scheduleApi.getScheduleParams(s.getId());
            String timezone = "UTC";
            for (ReportScheduleInputParamsPojo sp : scheduleInputParamsPojos) {
                if(sp.getParamKey().equalsIgnoreCase("timezone"))
                    timezone = getValueFromQuotes(sp.getParamValue());
                ReportInputParamsPojo ip = new ReportInputParamsPojo();
                ip.setDisplayValue(sp.getDisplayValue());
                ip.setParamKey(sp.getParamKey());
                ip.setParamValue(sp.getParamValue());
                reportInputParamsPojoList.add(ip);
            }
            reportRequestFlowApi.requestReportWithoutValidation(reportRequestPojo, reportInputParamsPojoList);
            s.setNextRuntime(getNextRunTime(s.getCron(), timezone));
            s.setStatus(ScheduleStatus.NEW);
            scheduleApi.edit(s);
        }

    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void refreshScheduleStatus() {
        List<ReportSchedulePojo> stuckSchedules = scheduleApi.getStuckSchedules(properties.getStuckScheduleSeconds());
        stuckSchedules.forEach(s -> {
            try {
                scheduleApi.updateStatusToNew(properties.getStuckScheduleSeconds(), s.getId());
            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException | ApiException e) {
                log.trace("Error occurred while refreshing status " + ScheduleStatus.NEW + " for schedule id : " + s.getId() + " " + e.getMessage());
            }
        });
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void refreshRequests() throws ApiException {
        List<ReportRequestPojo> reportRequestPojoList = api.getPendingRequests();
        Map<Integer, List<ReportRequestPojo>> userIdToRequests = groupByUserID(reportRequestPojoList);
        for (Map.Entry<Integer, List<ReportRequestPojo>> e : userIdToRequests.entrySet()) {
            List<Integer> pendingIds = e.getValue().stream().map(ReportRequestPojo::getId).collect(Collectors.toList());
            reportRequestFlowApi.updatePendingRequestStatus(pendingIds, e.getValue(), e.getKey(),
                    new HashMap<>());
        }
    }

    private void runReports(Executor executor, List<ReportRequestType> types) {
        // Get all the tasks pending for execution + Tasks that got stuck in processing
        int limitForEligibleRequest = getLimitForEligibleRequests((ThreadPoolTaskExecutor) executor);
        List<ReportRequestPojo> reportRequestPojoList = api.getEligibleRequests(types, limitForEligibleRequest);
        if (reportRequestPojoList.isEmpty())
            return;

        try {
            sortBasedOnCreatedAt(reportRequestPojoList);

            // Group by orgs
            Map<Integer, List<ReportRequestPojo>> orgToRequests = groupByOrgID(reportRequestPojoList);
            boolean flag = true;
            while (flag) {
                for (Map.Entry<Integer, List<ReportRequestPojo>> e : orgToRequests.entrySet()) {
                    // 1 from an org and then the other org
                    List<ReportRequestPojo> pojoList = new ArrayList<>(e.getValue());
                    Iterator<ReportRequestPojo> itr = pojoList.iterator();
                    if (!itr.hasNext()) {
                        orgToRequests.remove(e.getKey());
                        continue;
                    }
                    ReportRequestPojo reportRequestPojo = itr.next();
                    AbstractTask task = jobFactory.getTask(reportRequestPojo.getType());
                    task.runReportAsync(reportRequestPojo);
                    itr.remove();
                    orgToRequests.put(e.getKey(), pojoList);
                }
                if (orgToRequests.isEmpty())
                    flag = false;
            }
        } catch (Exception e) {
            log.error("Error while running requests ", e);
        }
    }

    private int getLimitForEligibleRequests(ThreadPoolTaskExecutor executor) {
        long poolSize = properties.getUserReportRequestCorePool() - 10; // Just for safety we kept buffer of 10 to core pool
        long currentUsedThreads = executor.getThreadPoolExecutor().getTaskCount()
                - executor.getThreadPoolExecutor().getCompletedTaskCount();
        log.debug("Task Count : " + executor.getThreadPoolExecutor().getTaskCount());
        log.debug("Completed Task Count : " + executor.getThreadPoolExecutor().getCompletedTaskCount());
        log.debug("Current Used threads : " + currentUsedThreads);
        if (currentUsedThreads >= poolSize) {
            log.error("Threshold is Greater than " + poolSize);
        }
        return (int) (poolSize - currentUsedThreads);
    }

    private void sortBasedOnCreatedAt(List<ReportRequestPojo> reportRequestPojoList) {
        reportRequestPojoList.sort((o1, o2) -> {
            if (o1.getCreatedAt().isEqual(o2.getCreatedAt()))
                return 0;
            return o1.getCreatedAt().isAfter(o2.getCreatedAt()) ? 1 : -1;
        });
    }

//    @Scheduled(cron = "0 0 6 * * *")
//    public void clearUserRateLimiterMap() {
//        rateLimitingFilter.clearUserRateLimiterMap();
//    }
}

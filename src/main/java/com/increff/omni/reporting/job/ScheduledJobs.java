package com.increff.omni.reporting.job;


import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.flow.ReportRequestFlowApi;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.persistence.OptimisticLockException;
import java.util.*;
import java.util.concurrent.Executor;

import static com.increff.omni.reporting.dto.CommonDtoHelper.convertToReportRequestPojo;
import static com.increff.omni.reporting.dto.CommonDtoHelper.groupByOrgID;

@Log4j
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
    private ReportTask reportTask;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private OrgSchemaApi orgSchemaApi;
    @Autowired
    @Qualifier(value = "userReportRequestExecutor")
    private Executor userReportExecutor;

    @Autowired
    @Qualifier(value = "scheduleReportRequestExecutor")
    private Executor scheduleReportExecutor;

    @Scheduled(fixedDelay = 1000)
    public void runUserReports() {
        runReports(userReportExecutor, Collections.singletonList(ReportRequestType.USER));
    }

    @Scheduled(fixedDelay = 1000)
    public void runScheduleReports() {
        runReports(scheduleReportExecutor, Arrays.asList(ReportRequestType.EMAIL));
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

    @Scheduled(fixedDelay = 3600 * 1000)
    public void deleteOldFiles() {
        folderApi.deleteFilesOlderThan1Hr();
    }

    @Scheduled(fixedDelay = 1000)
    public void addScheduleReportRequests() throws ApiException {
        List<ReportSchedulePojo> schedulePojos = reportScheduleApi.getEligibleSchedules();
        log.debug("Eligible schedules : " + schedulePojos.size());
        for(ReportSchedulePojo s : schedulePojos) {
            OrgSchemaVersionPojo orgSchemaVersionPojo = orgSchemaApi.getCheckByOrgId(s.getOrgId());
            ReportPojo reportPojo = reportApi.getByNameAndSchema(s.getReportName(),
                    orgSchemaVersionPojo.getSchemaVersionId());
            Integer reportId = Objects.isNull(reportPojo) ? null : reportPojo.getId();
            ReportRequestPojo reportRequestPojo = convertToReportRequestPojo(s, reportId);
            List<ReportInputParamsPojo> reportInputParamsPojoList = new ArrayList<>();
            List<ReportScheduleInputParamsPojo> scheduleInputParamsPojos = scheduleApi.getScheduleParams(s.getId());
            scheduleInputParamsPojos.forEach(sp -> {
                ReportInputParamsPojo ip = new ReportInputParamsPojo();
                ip.setDisplayValue(sp.getDisplayValue());
                ip.setParamKey(sp.getParamKey());
                ip.setParamValue(sp.getParamValue());
                reportInputParamsPojoList.add(ip);
            });
            reportRequestFlowApi.requestReportWithoutValidation(reportRequestPojo, reportInputParamsPojoList);
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
                    switch (reportRequestPojo.getType()) {
                        case USER:
                            reportTask.runUserReportAsync(reportRequestPojo);
                            break;
                        case EMAIL:
                            reportTask.runScheduleReportAsync(reportRequestPojo);
                            break;
                        default:
                            throw new ApiException(ApiStatus.BAD_DATA, "Unknown report request type");
                    }
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
}

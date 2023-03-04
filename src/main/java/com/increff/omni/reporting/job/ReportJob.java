package com.increff.omni.reporting.job;


import com.increff.omni.reporting.api.FolderApi;
import com.increff.omni.reporting.api.ReportRequestApi;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.persistence.OptimisticLockException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.increff.omni.reporting.dto.CommonDtoHelper.groupByOrgID;

@Log4j
@Component
public class ReportJob {

    @Autowired
    private ReportRequestApi api;
    @Autowired
    private FolderApi folderApi;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private ReportTask reportTask;
    @Autowired
    @Qualifier(value = "jobExecutor")
    private Executor executor;

    @Scheduled(fixedDelay = 1000)
    public void runReports() {
        // Get all the tasks pending for execution + Tasks that got stuck in processing
        int limitForEligibleRequest = getLimitForEligibleRequests();
        List<ReportRequestPojo> reportRequestPojoList = api.getEligibleRequests(limitForEligibleRequest);
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
                    reportTask.runAsync(reportRequestPojo);
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

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void deleteOldFiles() {
        folderApi.deleteOlderFiles();
    }

    private int getLimitForEligibleRequests() {
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;
        long poolSize = properties.getCorePoolSize() - 10; // Just for safety we kept buffer of 10 to core pool
        long currentUsedThreads = threadPoolExecutor.getThreadPoolExecutor().getTaskCount()
                - threadPoolExecutor.getThreadPoolExecutor().getCompletedTaskCount();
        log.debug("Task Count : " + threadPoolExecutor.getThreadPoolExecutor().getTaskCount());
        log.debug("Completed Task Count : " + threadPoolExecutor.getThreadPoolExecutor().getCompletedTaskCount());
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

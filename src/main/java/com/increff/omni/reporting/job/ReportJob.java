package com.increff.omni.reporting.job;


import com.increff.omni.reporting.api.FolderApi;
import com.increff.omni.reporting.api.ReportRequestApi;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;

import static com.increff.omni.reporting.job.ReportTaskHelper.groupByOrgID;

public class ReportJob {

    @Autowired
    private ReportRequestApi api;
    @Autowired
    private FolderApi folderApi;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private ReportTask reportTask;

    @Scheduled(fixedDelay = 1000)
    public void run(){
        // Get all the tasks pending for execution + Tasks that got stuck in processing
        List<ReportRequestPojo> reportRequestPojoList = api.getEligibleRequests();
        if(reportRequestPojoList.isEmpty())
            return;
        reportRequestPojoList.sort((o1, o2) -> {
            if (o1.getCreatedAt().isEqual(o2.getCreatedAt()))
                return 0;
            return o1.getCreatedAt().isAfter(o2.getCreatedAt()) ? 1 : -1;
        });
        // Group by orgs
        Map<Integer, List<ReportRequestPojo>> orgToRequests = groupByOrgID(reportRequestPojoList);
        boolean flag = true;
        while(flag) {
            for(Map.Entry<Integer, List<ReportRequestPojo>> e : orgToRequests.entrySet()) {
                // 1 from an org and then the other org
                Iterator<ReportRequestPojo> itr = e.getValue().iterator();
                if(!itr.hasNext())
                    orgToRequests.remove(e.getKey());
                ReportRequestPojo reportRequestPojo = itr.next();
                reportTask.runAsync(reportRequestPojo);
                itr.remove();
            }
            if(orgToRequests.isEmpty())
                flag = false;
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void resetStuckJobs(){
        api.markStuck(properties.getStuckReportTime());
    }

    @Scheduled(fixedDelay = 3600 * 1000)
    public void deleteOldLogsAndFiles() {
        folderApi.deleteFilesOlderThan1Hr();
    }
}

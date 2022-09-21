package com.increff.omni.reporting.job;


import com.increff.omni.reporting.api.ReportRequestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

public class ReportJob {

    @Autowired
    private ReportRequestApi api;


    @Scheduled(fixedDelay = 1000)
    public void run(){

        /*
        Get all the tasks pending for execution + Tasks that got stuck in processing
        Group by orgs
        See total number of pending async threads
        Keep allocating in following way
            The oldest one first
            1 from an org and then the other org
        * */

    }

    @Scheduled(fixedDelay = 1000)
    public void resetStuckJobs(){
        api.markStuck();
    }

}

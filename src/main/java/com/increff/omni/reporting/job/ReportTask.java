package com.increff.omni.reporting.job;

import com.increff.omni.reporting.api.ReportRequestApi;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ReportTask {

    @Autowired
    private ReportRequestApi api;

    @Async
    public void runAsync(ReportRequestPojo pojo){
        Integer id = pojo.getId();
        try{
            api.markProcessingIfEligible(id);
            //mark as processing
        } catch (Exception e) {
            //log as error and can be tried next time
            return;//
        }

        //process - TODO code from webget

        //mark as completed TODO
        /* upload result to cloud
           update status to completed
        * */
    }

}

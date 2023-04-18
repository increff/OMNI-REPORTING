package com.increff.omni.reporting.job;

import com.increff.omni.reporting.model.constants.ReportRequestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class JobFactory {

    @Autowired
    private ApplicationContext appContext;

    public AbstractTask getTask(ReportRequestType type) {
        Class<? extends AbstractTask> jobClass = null;
        switch (type) {
            case USER:
                jobClass = UserReportTask.class;
                break;
            case EMAIL:
                jobClass = ScheduleReportTask.class;
                break;
        }
        return appContext.getBean(jobClass);
    }
}

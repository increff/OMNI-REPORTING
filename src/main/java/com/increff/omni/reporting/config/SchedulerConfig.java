//package com.increff.omni.reporting.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//@Configuration
//public class SchedulerConfig {
//
//    @Autowired
//    private ApplicationProperties properties;
//
//    @Bean
//    public Executor getScheduledThreadPool() {
//        return Executors.newScheduledThreadPool(6);
//    }
//
//    @Bean(name = "userReportRequestExecutor")
//    public Executor getReportRequestAsyncExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(properties.getUserReportRequestCorePool());
//        executor.setMaxPoolSize(properties.getUserReportRequestMaxPool());
//        executor.setQueueCapacity(properties.getUserReportRequestQueueCapacity());
//        executor.setWaitForTasksToCompleteOnShutdown(true);
//        executor.initialize();
//        return executor;
//    }
//
//    @Bean(name = "scheduleReportRequestExecutor")
//    public Executor getReportScheduleAsyncExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(properties.getScheduleReportRequestCorePool());
//        executor.setMaxPoolSize(properties.getScheduleReportRequestMaxPool());
//        executor.setQueueCapacity(properties.getScheduleReportRequestQueueCapacity());
//        executor.initialize();
//        return executor;
//    }
//
//}

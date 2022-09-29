package com.increff.omni.reporting.config;

import com.increff.omni.reporting.api.FolderApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@EnableScheduling
@EnableAsync
@Configuration
public class SchedulerConfig {

    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private FolderApi folderApi;

    @Bean
    public Executor getScheduledThreadPool() {
        return Executors.newScheduledThreadPool(3);
    }

    @Scheduled(fixedDelay = 3600 * 1000)
    public void deleteOldLogsAndFiles() {
        folderApi.deleteFilesOlderThan1Hr();
    }

    @Bean(name = "jobExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.initialize();
        return executor;
    }

}

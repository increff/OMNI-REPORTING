package com.increff.omni.reporting.config;

import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.pojo.DirectoryPojo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class StartupReportsInitialise implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private DirectoryDao dao;

    @Autowired
    private ApplicationProperties properties;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        DirectoryPojo pojo = dao.select("directoryName", properties.getRootDirectory());
        if(Objects.nonNull(pojo))
            return;
        pojo = new DirectoryPojo();
        pojo.setDirectoryName(properties.getRootDirectory());
        pojo.setParentId(0);
        dao.persist(pojo);
    }

}

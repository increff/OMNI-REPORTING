package com.increff.omni.reporting.commons;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public abstract class AbstractWebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    private static final int ONE_MB = 1000000;
    private static final String LOCATION = System.getProperty("java.io.tmpdir");
    private static final long MAX_FILE_SIZE = 10000000L;
    private static final long MAX_REQUEST_SIZE = 100000000L;
    private static final int FILE_SIZE_THRESHOLD = 1000000;

    public AbstractWebInitializer() {
    }

    protected Class<?>[] getRootConfigClasses() {
        return new Class[0];
    }


    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration){
        MultipartConfigElement multipartConfigElement = new MultipartConfigElement(LOCATION, MAX_FILE_SIZE,
                MAX_REQUEST_SIZE, FILE_SIZE_THRESHOLD);
        registration.setMultipartConfig(multipartConfigElement);
    }
}

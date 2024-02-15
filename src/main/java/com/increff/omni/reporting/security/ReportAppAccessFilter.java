package com.increff.omni.reporting.security;

import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.util.UserPrincipalUtil;
import com.nextscm.commons.spring.common.ApiException;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Log4j
@Component
public class ReportAppAccessFilter extends GenericFilterBean {

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private ReportApi reportApi;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Integer reportId = 1; // TODO : Get reportID from request

        try {
            reportApi.getCheckAppAccess(reportId, UserPrincipalUtil.getAccessibleApps());
        } catch (ApiException e) {
            //'doFilter(ServletRequest, ServletResponse, FilterChain)' in 'com.increff.omni.reporting.security.ReportAppAccessFilter' clashes with 'doFilter(ServletRequest, ServletResponse, FilterChain)' in 'javax.servlet.Filter'; overridden method does not throw 'com.nextscm.commons.spring.common.ApiException'
        }

        chain.doFilter(request, response);
    }

}

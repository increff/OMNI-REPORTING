package com.increff.omni.reporting.security;

import com.increff.account.client.SecurityUtil;
import com.increff.omni.reporting.config.ApplicationProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Log4j
@Component
@Order(0)
public class RateLimitingFilter extends GenericFilterBean {

    @Autowired
    private ApplicationProperties properties;

    private ZonedDateTime nextLogTime = ZonedDateTime.now().minusMinutes(1);
    // Can cause heap space issues if the number of users is large!
    private final ConcurrentHashMap<String, Bucket> userRateLimiters = new ConcurrentHashMap<>();

    public Bucket bucketB() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(properties.getTokens(), Refill.intervally(properties.getTokens(), Duration.ofSeconds(properties.getTokensRefillRateSeconds()))))
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if(nextLogTime.isBefore(ZonedDateTime.now())) {
            log.info("RateLimitingFilter.stats: " + " userRateLimitersMap.size " + userRateLimiters.size() + " RateLimiting" + properties.getTokens() + " tokens per " + properties.getTokensRefillRateSeconds() + " seconds");
            nextLogTime = ZonedDateTime.now().plusMinutes(10);
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String user = getUserIdFromRequest();

        if(httpRequest.getRequestURI().matches(".*/dashboards/\\d+/view$") && Objects.nonNull(user)) {
            int dashboard_id = getDashboardId(httpRequest);
            Bucket bucket = userRateLimiters.computeIfAbsent(user + ":" + dashboard_id, k -> bucketB());
            log.debug("RateLimitingFilter.doFilter: " + user + ":" + dashboard_id);
            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);  // If the user is within the rate limit, proceed with the request
            } else {
                setRateLimitResponse((HttpServletResponse) response);
            }

        } else {
            chain.doFilter(request, response); // Proceed with the request
        }
    }

    private void setRateLimitResponse(HttpServletResponse response) throws IOException {
        // If the user has exceeded the rate limit, return a 429 (Too Many Requests) response
        int responseCode = 429;
        HttpServletResponse httpResponse = response;
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.getWriter().write("{\"status\":\"" + responseCode + "\",\"message\":\"Too many requests. Please try again after " + properties.getTokensRefillRateSeconds() + " Second(s)\"}");
        httpResponse.setStatus(responseCode);
    }

    private int getDashboardId(HttpServletRequest httpRequest) {
        String[] urlTokens = httpRequest.getRequestURI().split("/");
        return Integer.parseInt(urlTokens[urlTokens.length - 2]);
    }

    private String getUserIdFromRequest() {
        String user_id = null;
        if(Objects.nonNull(SecurityUtil.getPrincipal()))
            user_id = String.valueOf(SecurityUtil.getPrincipal().getId());
        log.debug("RateLimitingFilter.getUserIdFromRequest: " + user_id);
        return user_id;
    }

    public void clearUserRateLimiterMap() {
        log.debug("RateLimitingFilter.clearUserRateLimiterMap: " + " userRateLimitersMap.size " + userRateLimiters.size() + " RateLimiting" + properties.getTokens() + " tokens per " + properties.getTokensRefillRateSeconds() + " seconds");
        userRateLimiters.entrySet().removeIf(entry -> entry.getValue().getAvailableTokens() == properties.getTokens());
    }

}

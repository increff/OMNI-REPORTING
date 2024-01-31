package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.dao.ReportRequestDao;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static com.increff.omni.reporting.helper.ReportTestHelper.getReportRequestPojo;
import static org.junit.jupiter.api.Assertions.*;

public class ReportRequestApiTest extends AbstractTest {

    @Autowired
    private ReportRequestApi api;
    @Autowired
    private ReportRequestDao dao;

    private ReportRequestPojo commonSetup() {
        ReportRequestPojo pojo1 = getReportRequestPojo(100001, ReportRequestStatus.NEW
                , 100001, 100001, ReportRequestType.USER);
        ReportRequestPojo pojo2 = getReportRequestPojo(100001, ReportRequestStatus.FAILED
                , 100001, 100002, ReportRequestType.USER);
        ReportRequestPojo pojo3 = getReportRequestPojo(100001, ReportRequestStatus.COMPLETED
                , 100002, 100003, ReportRequestType.USER);
        ReportRequestPojo pojo4 = getReportRequestPojo(100002, ReportRequestStatus.STUCK
                , 100001, 100001, ReportRequestType.USER);
        pojo4.setCreatedAt(ZonedDateTime.now().plusDays(1));
        ReportRequestPojo pojo5 = getReportRequestPojo(100002, ReportRequestStatus.IN_PROGRESS
                , 100002, 100003, ReportRequestType.EMAIL);
        pojo5.setUpdatedAt(ZonedDateTime.now().minusMinutes(11));
        api.add(pojo1);
        api.add(pojo2);
        api.add(pojo3);
        api.add(pojo4);
        api.add(pojo5);
        return pojo1;
    }

    @Test
    public void testAdd() throws ApiException {
        ReportRequestPojo pojo1 = commonSetup();
        ReportRequestPojo pojo = api.getCheck(pojo1.getId());
        assertNotNull(pojo);
        assertEquals(ReportRequestStatus.NEW, pojo.getStatus());
        assertEquals(100001, pojo.getReportId().intValue());
        assertEquals(100001, pojo.getOrgId().intValue());
        assertEquals(100001, pojo.getUserId().intValue());
    }

    @Test
    public void testGetPendingByUserId() {
        commonSetup();
        List<ReportRequestPojo> pojoList = api.getPendingByUserId(100001);
        assertEquals(1, pojoList.size());
        assertEquals(ReportRequestStatus.NEW, pojoList.get(0).getStatus());
        assertEquals(100001, pojoList.get(0).getOrgId().intValue());
        assertEquals(100001, pojoList.get(0).getReportId().intValue());
        pojoList = api.getPendingByUserId(100002);
        assertEquals(0, pojoList.size());
    }

    @Test
    public void testGetByUserId() {
        commonSetup();
        List<ReportRequestPojo> pojoList = api.getByUserId(100001, 1);
        assertEquals(1, pojoList.size());
        assertEquals(ReportRequestStatus.STUCK, pojoList.get(0).getStatus());
        assertEquals(100001, pojoList.get(0).getOrgId().intValue());
        assertEquals(100002, pojoList.get(0).getReportId().intValue());
    }

    @Test
    public void testMarkProcessingIfEligible() throws ApiException {
        ReportRequestPojo pojo = commonSetup();
        api.markProcessingIfEligible(pojo.getId());
        ReportRequestPojo p = api.getCheck(pojo.getId());
        assertEquals(ReportRequestStatus.IN_PROGRESS, p.getStatus());
    }

    @Test
    public void testMarkProcessingWithError() throws ApiException {
        ReportRequestPojo pojo = commonSetup();
        api.markProcessingIfEligible(pojo.getId());
        ReportRequestPojo p = api.getCheck(pojo.getId());
        assertEquals(ReportRequestStatus.IN_PROGRESS, p.getStatus());
        try {
            api.markProcessingIfEligible(pojo.getId());
        } catch (ApiException e) {
            assertEquals(ApiStatus.UNKNOWN_ERROR, e.getStatus());
            assertEquals("Task not in eligible state", e.getMessage());
        }
    }

    @Test
    public void testMarkStuck() {
        commonSetup();
        List<ReportRequestPojo> pojoList = api.getStuckRequests( 10);
        api.markStuck(pojoList.get(0));
        pojoList = dao.selectMultiple("status", ReportRequestStatus.STUCK);
        assertEquals(2, pojoList.size());
        assertEquals(100002, pojoList.get(0).getReportId().intValue());
        assertEquals(100001, pojoList.get(0).getOrgId().intValue());
        assertEquals(100001, pojoList.get(0).getUserId().intValue());
        assertEquals(100002, pojoList.get(1).getReportId().intValue());
        assertEquals(100002, pojoList.get(1).getOrgId().intValue());
        assertEquals(100003, pojoList.get(1).getUserId().intValue());
    }

    @Test
    public void testUpdateStatus() throws ApiException {
        ReportRequestPojo pojo = commonSetup();
        api.updateStatus(pojo.getId(), ReportRequestStatus.COMPLETED, "https://fileUrl.com", 2, 0.01, "",
                null);
        ReportRequestPojo p = api.getCheck(pojo.getId());
        assertEquals(ReportRequestStatus.COMPLETED, p.getStatus());
        assertEquals(100001, p.getReportId().intValue());
        assertEquals(100001, p.getOrgId().intValue());
        assertEquals(100001, p.getUserId().intValue());
    }

    @Test
    public void testGetEligibleRequests() {
        commonSetup();
        List<ReportRequestPojo> pojoList = api.getEligibleRequests(Collections.singletonList(ReportRequestType.USER),
                1);
        assertEquals(1, pojoList.size());
        assertEquals(ReportRequestStatus.NEW, pojoList.get(0).getStatus());
        assertEquals(100001, pojoList.get(0).getReportId().intValue());
        assertEquals(100001, pojoList.get(0).getOrgId().intValue());
        assertEquals(100001, pojoList.get(0).getUserId().intValue());
        pojoList = api.getEligibleRequests(Collections.singletonList(ReportRequestType.USER), 0);
        assertEquals(0, pojoList.size());
    }
}

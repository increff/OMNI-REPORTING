//package com.increff.omni.reporting.api;
//
//import com.increff.omni.reporting.config.AbstractTest;
//import com.increff.omni.reporting.model.constants.ValidationType;
//import com.increff.omni.reporting.pojo.ReportValidationGroupPojo;
//import com.nextscm.commons.spring.common.ApiException;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static com.increff.omni.reporting.helper.ReportTestHelper.getReportValidationGroupPojo;
//import static org.junit.Assert.assertEquals;
//
//public class ReportValidationGroupApiTest extends AbstractTest {
//
//    @Autowired
//    private ReportValidationGroupApi api;
//
//    @Test
//    public void testAdd() {
//        ReportValidationGroupPojo pojo1 = getReportValidationGroupPojo(100001, "group1"
//                , ValidationType.MANDATORY, 0, 100001);
//        ReportValidationGroupPojo pojo2 = getReportValidationGroupPojo(100001, "group1"
//                , ValidationType.MANDATORY, 0, 100002);
//        ReportValidationGroupPojo pojo3 = getReportValidationGroupPojo(100002, "group1"
//                , ValidationType.SINGLE_MANDATORY, 0, 100001);
//        List<ReportValidationGroupPojo> pojoList = Arrays.asList(pojo1, pojo2, pojo3);
//        api.addAll(pojoList);
//        List<ReportValidationGroupPojo> pojos = api.getByReportId(100001);
//        assertEquals(2, pojos.size());
//        assertEquals("group1", pojos.get(0).getGroupName());
//        assertEquals(ValidationType.MANDATORY, pojos.get(0).getType());
//        assertEquals(100001, pojos.get(0).getReportControlId().intValue());
//        assertEquals("group1", pojos.get(1).getGroupName());
//        assertEquals(ValidationType.MANDATORY, pojos.get(1).getType());
//        assertEquals(100002, pojos.get(1).getReportControlId().intValue());
//    }
//
//    @Test
//    public void testDeleteByReportIdAndGroupName() throws ApiException {
//        ReportValidationGroupPojo pojo1 = getReportValidationGroupPojo(100001, "group1"
//                , ValidationType.MANDATORY, 0, 100001);
//        ReportValidationGroupPojo pojo2 = getReportValidationGroupPojo(100001, "group1"
//                , ValidationType.MANDATORY, 0, 100002);
//        ReportValidationGroupPojo pojo3 = getReportValidationGroupPojo(100001, "group2"
//                , ValidationType.MANDATORY, 0, 100001);
//        ReportValidationGroupPojo pojo4 = getReportValidationGroupPojo(100002, "group1"
//                , ValidationType.SINGLE_MANDATORY, 0, 100002);
//        List<ReportValidationGroupPojo> pojoList = Arrays.asList(pojo1, pojo2, pojo3, pojo4);
//        api.addAll(pojoList);
//        api.deleteByReportIdAndGroupName(100001, "group1");
//        List<ReportValidationGroupPojo> pojoList1 = api.getByNameAndReportId(100001, "group1");
//        assertEquals(0, pojoList1.size());
//        List<ReportValidationGroupPojo> pojoList2 = api.getByReportId(100001);
//        assertEquals(1, pojoList2.size());
//        assertEquals("group2", pojoList2.get(0).getGroupName());
//        assertEquals(100001, pojoList2.get(0).getReportControlId().intValue());
//        List<ReportValidationGroupPojo> pojoList3 = api.getByReportId(100002);
//        assertEquals(1, pojoList3.size());
//        assertEquals("group1", pojoList3.get(0).getGroupName());
//        assertEquals(100002, pojoList3.get(0).getReportControlId().intValue());
//    }
//
//    @Test
//    public void testDeleteByReportIdAndReportControlId() {
//        ReportValidationGroupPojo pojo1 = getReportValidationGroupPojo(100001, "group1"
//                , ValidationType.MANDATORY, 0, 100001);
//        ReportValidationGroupPojo pojo2 = getReportValidationGroupPojo(100001, "group1"
//                , ValidationType.MANDATORY, 0, 100002);
//        ReportValidationGroupPojo pojo3 = getReportValidationGroupPojo(100001, "group2"
//                , ValidationType.MANDATORY, 0, 100001);
//        ReportValidationGroupPojo pojo4 = getReportValidationGroupPojo(100002, "group1"
//                , ValidationType.SINGLE_MANDATORY, 0, 100001);
//        List<ReportValidationGroupPojo> pojoList = Arrays.asList(pojo1, pojo2, pojo3, pojo4);
//        api.addAll(pojoList);
//        api.deleteByReportIdAndReportControlId(100001, 100001);
//        List<ReportValidationGroupPojo> pojoList2 = api.getByReportId(100001);
//        assertEquals(1, pojoList2.size());
//        assertEquals("group1", pojoList2.get(0).getGroupName());
//        assertEquals(100002, pojoList2.get(0).getReportControlId().intValue());
//        List<ReportValidationGroupPojo> pojoList3 = api.getByReportId(100002);
//        assertEquals(1, pojoList3.size());
//        assertEquals("group1", pojoList3.get(0).getGroupName());
//        assertEquals(100001, pojoList3.get(0).getReportControlId().intValue());
//    }
//}

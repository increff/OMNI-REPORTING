package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.JsonUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SingleMandatoryValidatorTest extends AbstractTest {

    @Autowired
    private SingleMandatoryValidator validator;

    @Test(expected = ApiException.class)
    public void testValidateErrorCase1() throws ApiException {
        List<String> params = Arrays.asList("''", "''");
        List<String> displayNames = Arrays.asList("Client Id", "Item Id");
        try {
            validator.validate(displayNames, params, "Report 1", 0, ReportRequestType.USER);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report 1 failed in validation for key / keys : " +
                            JsonUtil.serialize(displayNames) + " , validation type : " + ValidationType.SINGLE_MANDATORY,
                    e.getMessage());
            throw e;
        }
    }

    @Test
    public void testValidateSuccess() throws ApiException {
        List<String> params = Arrays.asList("''", "'abc'");
        List<String> displayNames = Arrays.asList("Client Id", "Item Id");
        validator.validate(displayNames, params, "Report 1", 0, ReportRequestType.USER);
    }

//    public void testValidateSuccess2() throws ApiException {
//        List<String> params = Arrays.asList("'abc'", "'def'");
//        List<String> displayNames = Arrays.asList("Client Id", "Item Id");
//        try {
//            validator.validate(displayNames, params, "Report 1", 0, ReportRequestType.USER);
//        } catch (ApiException e) {
//            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
//            assertEquals("Report 1 failed in validation for key / keys : " +
//                            JsonUtil.serialize(displayNames) + " , validation type : " + ValidationType.SINGLE_MANDATORY,
//                    e.getMessage());
//            throw e;
//        }
//    } todo : uncomment

    @Test
    public void testAdd() throws ApiException {
        validator.add(new ArrayList<>());
    }
}

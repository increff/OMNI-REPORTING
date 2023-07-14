package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.JsonUtil;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateValidatorTest extends AbstractTest {

    @Autowired
    private DateValidator validator;

    @Test
    public void testValidateErrorCase1() throws ApiException {
        List<String> params = Collections.singletonList("''");
        List<String> displayNames = Arrays.asList("Client Id", "Item Id");
        ApiException e = assertThrows(ApiException.class, () -> {
            validator.validate(displayNames, params, "Report 1", 0, ReportRequestType.USER);
        });
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Exactly 2 Date inputs are required to validate", e.getMessage());
    }

    @Test
    public void testValidateOnly1Date() throws ApiException {
        List<String> params = Arrays.asList("'2022-05-10T10:00:00.000+05:30'", "''");
        List<String> displayNames = Arrays.asList("Client Id", "Item Id");
        ApiException e = assertThrows(ApiException.class, () -> {
            validator.validate(displayNames, params, "Report 1", 10, ReportRequestType.USER);
        });
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Both from and to date should be selected for filters : [\"Client Id\",\"Item Id\"]",
                    e.getMessage());
    }

    @Test
    public void testValidateErrorCase2() throws ApiException {
        List<String> params = Arrays.asList("'2022-05-10T10:00:00.000+05:30'", "'2022-05-21T10:00:00.000+05:30'");
        List<String> displayNames = Arrays.asList("Client Id", "Item Id");
        ApiException e = assertThrows(ApiException.class, () -> {
            validator.validate(displayNames, params, "Report 1", 10, ReportRequestType.USER);
        });
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report 1 failed in validation for key / keys : " +
                    JsonUtil.serialize(displayNames) + " , validation type : " + ValidationType.DATE_RANGE
                    + " message : Date range crossed 10 days", e.getMessage());
    }

    @Test
    public void testValidateErrorCase3() throws ApiException {
        List<String> params = Arrays.asList("'2022-05-21T10:00:00.000+05:30'", "'2022-05-10T10:00:00.000+05:30'");
        List<String> displayNames = Arrays.asList("Client Id", "Item Id");
        ApiException e = assertThrows(ApiException.class, () -> {
            validator.validate(displayNames, params, "Report 1", 10, ReportRequestType.USER);
        });
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report 1 failed in validation for key / keys : " +
                    JsonUtil.serialize(displayNames) + " , validation type : " + ValidationType.DATE_RANGE
                    + " message : Date range crossed 10 days", e.getMessage());
    }

    @Test
    public void testValidateErrorCase4() throws ApiException {
        List<String> params = Arrays.asList("'2022-05-10T10:00:00.000+05:30'", "'2022-05-10T10:00:00.000+05:30'");
        List<String> displayNames = Arrays.asList("Client Id", "Item Id");
        ApiException e = assertThrows(ApiException.class, () -> {
            validator.validate(displayNames, params, "Report 1", 10, ReportRequestType.USER);
        });
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report 1 failed in validation for key / keys : " +
                    JsonUtil.serialize(displayNames) + " , validation type : " + ValidationType.DATE_RANGE
                    + " message : Both dates can't be equal", e.getMessage());
    }

    @Test
    public void testValidateErrorCase5() throws ApiException {
        List<String> params = Arrays.asList("'2022-05-10T10:00:00.000+05:30'", "'a'");
        List<String> displayNames = Arrays.asList("Client Id", "Item Id");
        ApiException e = assertThrows(ApiException.class, () -> {
            validator.validate(displayNames, params, "Report 1", 10, ReportRequestType.USER);
        });
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report 1 failed in validation for key / keys : " +
                    JsonUtil.serialize(displayNames) + " , validation type : " + ValidationType.DATE_RANGE
                    + " message : Date is not in correct format : " + JsonUtil.serialize(params), e.getMessage());
    }

    @Test
    public void testValidateSuccessCase1() throws ApiException {
        List<String> params = Arrays.asList("''", "''");
        List<String> displayNames = Arrays.asList("Client Id", "Item Id");
        validator.validate(displayNames, params, "Report 1", 10, ReportRequestType.USER);
    }

    @Test
    public void testValidateSuccessCase2() throws ApiException {
        List<String> params = Arrays.asList("'2022-05-10T10:00:00.000+05:30'", "'2022-05-12T10:00:00.000+05:30'");
        List<String> displayNames = Arrays.asList("Client Id", "Item Id");
        validator.validate(displayNames, params, "Report 1", 10, ReportRequestType.USER);
    }

    @Test
    public void testAddValidatorErrorCase1() throws ApiException {
        List<InputControlType> types = Arrays.asList(InputControlType.DATE, InputControlType.TEXT);
        ApiException e = assertThrows(ApiException.class, () -> {
            validator.add(types);
        });
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("DATE_RANGE validation can only be applied on DATE / DATE_TIME input controls",
                    e.getMessage());
    }

    @Test
    public void testAddValidatorErrorCase2() throws ApiException {
        List<InputControlType> types =
                Arrays.asList(InputControlType.DATE, InputControlType.DATE, InputControlType.DATE);
        ApiException e = assertThrows(ApiException.class, () -> {
            validator.add(types);
        });
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("DATE_RANGE validation type should have exactly 2 DATE / DATE_TIME input controls",
                    e.getMessage());
    }

    @Test
    public void testAddValidator() throws ApiException {
        List<InputControlType> types = Arrays.asList(InputControlType.DATE, InputControlType.DATE_TIME);
        validator.add(types);
    }
}

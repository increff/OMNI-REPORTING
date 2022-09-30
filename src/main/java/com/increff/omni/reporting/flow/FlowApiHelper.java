package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

import java.util.Arrays;

public class FlowApiHelper {

    public static ReportControlsPojo getReportControlPojo(Integer reportId, Integer controlId, ValidationType validationType) {
        ReportControlsPojo pojo = new ReportControlsPojo();
        pojo.setReportId(reportId);
        pojo.setControlId(controlId);
        pojo.setValidationType(validationType);
        return pojo;
    }

    public static void validateValidationType(InputControlType type, ValidationType validationType) throws ApiException {
        switch (type) {
            case DATE:
                if (!Arrays.asList(ValidationType.DATE, ValidationType.NON_MANDATORY).contains(validationType))
                    throw new ApiException(ApiStatus.BAD_DATA, "Type DATE can have DATE_RANGE or NON_MANDATORY validation type");
            case TEXT:
            case NUMBER:
            case MULTI_SELECT:
                if (!Arrays.asList(ValidationType.MANDATORY, ValidationType.NON_MANDATORY).contains(validationType))
                    throw new ApiException(ApiStatus.BAD_DATA, "Type TEXT, NUMBER or MULTI_SELECT can have MANDATORY or NON_MANDATORY validation type");
            case SINGLE_SELECT:
                if (!Arrays.asList(ValidationType.SINGLE_MANDATORY, ValidationType.NON_MANDATORY).contains(validationType))
                    throw new ApiException(ApiStatus.BAD_DATA, "Type SINGLE_SELECT can have SINGLE_MANDATORY or NON_MANDATORY validation type");
        }
    }
}

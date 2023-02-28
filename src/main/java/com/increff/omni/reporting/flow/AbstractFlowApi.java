package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.AbstractAuditApi;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.omni.reporting.pojo.ReportValidationGroupPojo;
import com.increff.omni.reporting.validators.DateValidator;
import com.increff.omni.reporting.validators.MandatoryValidator;
import com.increff.omni.reporting.validators.SingleMandatoryValidator;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AbstractFlowApi extends AbstractAuditApi {

    @Autowired
    private SingleMandatoryValidator singleMandatoryValidator;
    @Autowired
    private MandatoryValidator mandatoryValidator;
    @Autowired
    private DateValidator dateValidator;

    protected void runValidators(ReportPojo reportPojo, List<ReportValidationGroupPojo> groupPojoList
            , ValidationType type, List<String> paramValues, List<String> displayValues,
                                 ReportRequestType requestType) throws ApiException {
        switch (type) {
            case SINGLE_MANDATORY:
                singleMandatoryValidator.validate(displayValues, paramValues, reportPojo.getName()
                        , groupPojoList.get(0).getValidationValue());
                break;
            case MANDATORY:
                mandatoryValidator.validate(displayValues, paramValues, reportPojo.getName()
                        , groupPojoList.get(0).getValidationValue());
                break;
            case DATE_RANGE:
                dateValidator.validate(displayValues, paramValues, reportPojo.getName()
                        , groupPojoList.get(0).getValidationValue());
                break;
            default:
                throw new ApiException(ApiStatus.BAD_DATA, "Invalid Validation Type");
        }
    }
}

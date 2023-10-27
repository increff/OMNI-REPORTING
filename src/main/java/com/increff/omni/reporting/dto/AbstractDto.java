package com.increff.omni.reporting.dto;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.flow.InputControlFlowApi;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.pojo.*;
import com.increff.service.encryption.EncryptionClient;
import com.increff.service.encryption.common.CryptoCommon;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.client.AppClientException;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;

import static com.increff.omni.reporting.dto.CommonDtoHelper.getValueFromQuotes;

@Log4j
@Component
@Setter
public class AbstractDto extends AbstractDtoApi {

    private static final Integer MAX_LIST_SIZE = 1000;
    @Autowired
    protected EncryptionClient encryptionClient;
    @Autowired
    private ReportControlsApi reportControlsApi;
    @Autowired
    private InputControlApi controlApi;
    @Autowired
    private OrgConnectionApi orgConnectionApi;
    @Autowired
    private OrgSchemaApi orgSchemaApi;
    @Autowired
    private ConnectionApi connectionApi;
    @Autowired
    private InputControlFlowApi inputControlFlowApi;
    @Autowired
    private CustomReportAccessApi customReportAccessApi;


    protected static int getOrgId() {
        return getPrincipal().getDomainId();
    }

    protected static int getUserId() {
        return getPrincipal().getId();
    }

    protected static String getUserName() {
        return getPrincipal().getUsername();
    }

    protected Integer getSchemaVersionId() throws ApiException{
        return orgSchemaApi.getCheckByOrgId(getOrgId()).getSchemaVersionId();
    }

    protected void validateInputParamValues(Map<String, List<String>> inputParams,
                                            Map<String, String> params, int orgId,
                                            Map<String, List<String>> inputDisplayMap,
                                            List<InputControlPojo> inputControlPojoList, ReportRequestType type,
                                            String password) throws ApiException {
        for (InputControlPojo i : inputControlPojoList) {
            if (params.containsKey(i.getParamName())) {
                String value = params.get(i.getParamName());
                if (StringUtil.isEmpty(value) || value.equals("''")) {
                    params.put(i.getParamName(), null);
                    continue;
                }
                List<String> values;
                Map<String, String> allowedValuesMap;
                List<String> displayNames = new ArrayList<>();
                switch (i.getType()) {
                    case TEXT:
                    case MULTI_TEXT:
                        break;
                    case NUMBER:
                        try {
                            value = getValueFromQuotes(value);
                            Integer.parseInt(value);
                        } catch (Exception e) {
                            throw new ApiException(ApiStatus.BAD_DATA, value + " is not a number for filter : "
                                    + i.getDisplayName());
                        }
                        break;
                    case DATE:
                    case DATE_TIME:
                        try {
                            if(type.equals(ReportRequestType.USER)) {
                                value = getValueFromQuotes(value);
                                ZonedDateTime.parse(value);
                            }
                        } catch (Exception e) {
                            throw new ApiException(ApiStatus.BAD_DATA,
                                    value + " is not in valid date format for filter : " + i.getDisplayName());
                        }
                        break;
                    case SINGLE_SELECT:
                        values = inputParams.get(i.getParamName());
                        allowedValuesMap = checkValidValues(i, orgId, password);
                        if (values.size() > 1)
                            throw new ApiException(ApiStatus.BAD_DATA, "Multiple values not allowed for filter : "
                                    + i.getDisplayName());
                        String s = values.get(0);
                        if (!allowedValuesMap.containsKey(s))
                            throw new ApiException(ApiStatus.BAD_DATA, values.get(0) + " is not allowed for filter : "
                                    + i.getDisplayName());
                        displayNames.add(allowedValuesMap.get(s));
                        inputDisplayMap.put(i.getParamName(), displayNames);
                        break;
                    case ACCESS_CONTROLLED_MULTI_SELECT:
                        values = inputParams.get(i.getParamName());
                        allowedValuesMap = checkValidValues(i, orgId, password);
                        for (String v : values) {
                            if (!allowedValuesMap.containsKey(v))
                                throw new ApiException(ApiStatus.BAD_DATA, v + " is not allowed for filter : "
                                        + i.getDisplayName());
                            displayNames.add(allowedValuesMap.get(v));
                        }
                        inputDisplayMap.put(i.getParamName(), displayNames);
                        break;
                    case MULTI_SELECT:
                        values = inputParams.get(i.getParamName());
                        if (values.size() > MAX_LIST_SIZE)
                            throw new ApiException(ApiStatus.BAD_DATA,
                                    i.getDisplayName() + " can't have more than " + MAX_LIST_SIZE + " values in " +
                                            "single request");
                        allowedValuesMap = checkValidValues(i, orgId, password);
                        for (String v : values) {
                            if (!allowedValuesMap.containsKey(v))
                                throw new ApiException(ApiStatus.BAD_DATA, v + " is not allowed for filter : "
                                        + i.getDisplayName());
                            displayNames.add(allowedValuesMap.get(v));
                        }
                        inputDisplayMap.put(i.getParamName(), displayNames);
                        break;
                    default:
                        throw new ApiException(ApiStatus.BAD_DATA, "Invalid Input Control Type");
                }
            } else {
                params.put(i.getParamName(), null);
            }
        }
    }

    protected void validateCustomReportAccess(ReportPojo reportPojo, Integer orgId) throws ApiException {
        if (reportPojo.getType().equals(ReportType.STANDARD))
            return;
        CustomReportAccessPojo customReportAccessPojo =
                customReportAccessApi.getByReportAndOrg(reportPojo.getId(), orgId);
        if (Objects.isNull(customReportAccessPojo)) {
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Organization does not have access to view this report : " + reportPojo.getName());
        }
    }

    protected String getDecryptedPassword(String password) throws ApiException {
        try {
            CryptoCommon form = CommonDtoHelper.convertToCryptoForm(password);
            String decryptedPassword = encryptionClient.decode(form).getValue();
            return Objects.isNull(decryptedPassword) ? password : decryptedPassword;
        } catch (AppClientException e) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error From Crypto Service " + e.getMessage());
        }
    }

    private Map<String, String> checkValidValues(InputControlPojo p, int orgId, String password) throws ApiException {
        Map<String, String> valuesMap = new HashMap<>();
        InputControlQueryPojo queryPojo = controlApi.selectControlQuery(p.getId());
        if (Objects.isNull(queryPojo)) {
            List<InputControlValuesPojo> valuesPojoList =
                    controlApi.selectControlValues(Collections.singletonList(p.getId()));
            for (InputControlValuesPojo pojo : valuesPojoList) {
                valuesMap.put(pojo.getValue(), pojo.getValue());
            }
        } else {
            OrgConnectionPojo orgConnectionPojo = orgConnectionApi.getCheckByOrgId(orgId);
            ConnectionPojo connectionPojo = connectionApi.getCheck(orgConnectionPojo.getConnectionId());
            valuesMap = inputControlFlowApi.getValuesFromQuery(queryPojo.getQuery(), connectionPojo, password);
        }
        return valuesMap;
    }

    private static UserPrincipal getPrincipal() {
        return SecurityUtil.getPrincipal();
    }


}

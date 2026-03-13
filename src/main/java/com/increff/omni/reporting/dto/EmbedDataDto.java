package com.increff.omni.reporting.dto;

import com.increff.commons.springboot.common.ApiException;
import com.increff.omni.reporting.api.EmbedDataApi;
import com.increff.omni.reporting.model.data.EmbedTokenData;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.data.ReportData;
import com.increff.omni.reporting.model.data.ReportQueryData;
import com.increff.omni.reporting.model.data.TestQueryLiveData;
import com.increff.omni.reporting.model.form.EmbedTokenRequestForm;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.util.OrgAccessValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class EmbedDataDto {

    @Autowired
    private EmbedDataApi embedDataApi;
    @Autowired
    private ReportDto reportDto;
    @Autowired
    private InputControlDto inputControlDto;
    @Autowired
    private OrgAccessValidator orgAccessValidator;

    public EmbedTokenData generateEmbedToken(EmbedTokenRequestForm form) throws ApiException {
        String encryptedCredentials = form.getEncryptedCredentials();
        String encryptedToken = embedDataApi.generateEmbedToken(encryptedCredentials);

        EmbedTokenData embedTokenData = new EmbedTokenData();
        embedTokenData.setToken(encryptedToken);
        return embedTokenData;
    }

    public Map<String, Object> verifyToken() throws ApiException {
        return embedDataApi.verifyTokenAndGetContext();
    }

    public ReportData getChartDetails(Integer chartId) throws ApiException {
        return reportDto.get(chartId);
    }

    public ReportQueryData getChartQuery(Integer chartId) throws ApiException {
        return reportDto.getQuery(chartId);
    }

    public List<InputControlData> getChartControls(Integer chartId) throws ApiException {
        Integer orgId = orgAccessValidator.getOrgIdFromToken();
        return inputControlDto.selectForReport(chartId, orgId);
    }

    public TestQueryLiveData executeChartQuery(ReportRequestForm form) throws ApiException, IOException {
        Integer orgId = orgAccessValidator.getOrgIdFromToken();
        return reportDto.testQueryLive(form, orgId);
    }
}
package com.increff.omni.reporting.api;

import com.increff.commons.queryexecutor.QueryExecutorClient;
import com.increff.commons.queryexecutor.data.QueryRequestData;
import com.increff.commons.queryexecutor.form.GetRequestForm;
import com.increff.commons.queryexecutor.form.QueryExecutorForm;
import com.nextscm.commons.spring.client.AppClientException;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
public class QueryExecutorClientApi {

    @Autowired
    private QueryExecutorClient executorClient;

    public List<QueryRequestData> getQueryRequestDataList(GetRequestForm form) throws ApiException {
        try {
            return executorClient.getRequests(form);
        } catch (AppClientException e) {
            log.error(e.getMessage(), e);
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Could not fetch data from external service : " + e.getMessage());
        }
    }

    public void submitExecutionRequest(QueryExecutorForm form) throws ApiException {
        try {
            executorClient.postRequest(form);
        } catch (AppClientException e) {
            log.error(e.getMessage(), e);
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Could not submit request to external service : " + e.getMessage());
        }
    }
}

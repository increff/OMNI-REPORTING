package com.increff.omni.reporting.api;

import com.increff.commons.queryexecutor.QueryExecutorClient;
import com.increff.commons.queryexecutor.data.QueryRequestData;
import com.increff.commons.queryexecutor.form.GetRequestForm;
import com.increff.commons.queryexecutor.form.QueryExecutorForm;
import com.increff.commons.springboot.client.AppClientException;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
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
//        } catch (AppClientException e) {
//            log.error(e.getMessage(), e);
//            throw new ApiException(ApiStatus.BAD_DATA,
//                    "Could not fetch data from external service : " + e.getMessage());
        } catch (com.nextscm.commons.spring.client.AppClientException e) {
            throw new RuntimeException(e);
        }
    }

    public void submitExecutionRequest(QueryExecutorForm form) throws ApiException {
        try {
            executorClient.postRequest(form);
//        } catch (AppClientException e) {
//            log.error(e.getMessage(), e);
//            throw new ApiException(ApiStatus.BAD_DATA,
//                    "Could not submit request to external service : " + e.getMessage());
        } catch (com.nextscm.commons.spring.client.AppClientException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.increff.omni.reporting.job;

import com.increff.omni.reporting.model.SqlParams;
import com.increff.omni.reporting.pojo.*;
import org.apache.commons.text.StringSubstitutor;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportTaskHelper {

    public static SqlParams convert(ConnectionPojo connectionPojo, ReportQueryPojo reportQueryPojo, Map<String, String> inputParamsMap, File file, File errorFile) {
        SqlParams sqlParams = new SqlParams();
        sqlParams.setHost(connectionPojo.getHost());
        sqlParams.setUsername(connectionPojo.getUsername());
        sqlParams.setPassword(connectionPojo.getPassword());
        // Replacing query param with input control values
        String fQuery = StringSubstitutor.replace(reportQueryPojo.getQuery(), inputParamsMap);
        sqlParams.setQuery(fQuery);
        sqlParams.setOutFile(file);
        sqlParams.setErrFile(errorFile);
        return sqlParams;
    }

    public static Map<Integer, List<ReportRequestPojo>> groupByOrgID(List<ReportRequestPojo> reportRequestPojoList) {
        Map<Integer, List<ReportRequestPojo>> orgToRequests = new HashMap<>();
        reportRequestPojoList.forEach(r -> {
            if(orgToRequests.containsKey(r.getOrgId()))
                orgToRequests.get(r.getOrgId()).add(r);
            else
                orgToRequests.put(r.getOrgId(), Collections.singletonList(r));
        });
        return orgToRequests;
    }
}

package com.increff.omni.reporting.util;

import com.increff.omni.reporting.dto.QueryExecutionDto;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.JsonUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Log4j
public class SqlCmd {

    public static String getFinalQuery(Map<String, String> inputParamMap, String query,
                                       Boolean isUserPrincipalAvailable) {
        if(isUserPrincipalAvailable)
            inputParamMap.putAll(UserPrincipalUtil.getAccessControlMap());
        String[] matchingFunctions = StringUtils.substringsBetween(query, "<<", ">>");
        if (Objects.isNull(matchingFunctions)) {
            log.debug("Query formed : " + query);
            return query;
        }
        Map<String, String> functionValueMap = new HashMap<>();
        for (String f : matchingFunctions) {
            String methodName = f.split("\\(")[0].trim();
            String finalString = getValueFromMethod(inputParamMap, f, methodName);
            functionValueMap.put("{{" + f + "}}", finalString);
        }
        for (Map.Entry<String, String> e : functionValueMap.entrySet()) {
            query = query.replace(e.getKey(), e.getValue());
        }
        log.debug("Query formed : " + query);
        return query;
    }

    private static String getValueFromMethod(Map<String, String> inputParamMap, String f, String methodName) {
        String paramKey, paramValue, columnName, operator, condition;
        String finalString = "{{" + f + "}}";
        switch (methodName) {
            case "filter":
                paramKey = f.split("\\(")[1].split(",")[0].trim();
                paramValue = inputParamMap.get(paramKey);
                columnName = f.split("\\(")[1].split(",")[1].trim();
                operator = f.split("\\(")[1].split(",")[2].split("\\)")[0].trim();
                finalString = QueryExecutionDto.filter(columnName, operator, paramValue);
                break;
            case "replace":
                paramKey = StringUtils.substringBetween(f, "(", ")").trim();
                paramValue = inputParamMap.get(paramKey);
                if (Objects.nonNull(paramValue)) {
                    finalString = paramValue;
                }
                break;
            case "filterAppend":
                paramKey = f.split("\\(")[1].split(",")[0].trim();
                paramValue = inputParamMap.get(paramKey);
                columnName = f.split("\\(")[1].split(",")[1].trim();
                operator = f.split("\\(")[1].split(",")[2].trim();
                condition = f.split("\\(")[1].split(",")[3].split("\\)")[0].trim();
                finalString = QueryExecutionDto.filterAppend(columnName, operator, paramValue, condition);
                break;
        }
        return finalString;
    }

    public static Double getValueSum(List<Map<String, String>> result, String valueColumn) throws ApiException {
        double sum = 0;
        for (Map<String, String> map : result) {
            try {
                sum += Double.parseDouble(map.get(valueColumn));
            } catch (NumberFormatException e) {
                throw new ApiException(ApiStatus.BAD_DATA, "Failed to parse to Double. Value: " + map.get(valueColumn) + " Row: " + JsonUtil.serialize(map));
            }
        }
        log.debug("getValueSum sum: " + sum);
        return sum;
    }

}

package com.increff.omni.reporting.util;

import com.increff.omni.reporting.dto.QueryExecutionDto;
import com.increff.omni.reporting.model.form.SqlParams;
import com.nextscm.commons.lang.CmdUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j
public class SqlCmd {

    public static void processQuery(SqlParams sp, Integer maxExecutionTime) throws ApiException {
        processQuery(sp, true, maxExecutionTime);
    }

    public static void processQuery(SqlParams sp, Boolean isUserPrincipalAvailable, Integer maxExecutionTime) throws ApiException {
        if (isUserPrincipalAvailable) addAccessControlMap(sp, maxExecutionTime);
        String[] cmd = getQueryCmd(sp);
        Redirect redirectAll = Redirect.appendTo(sp.getOutFile());
        Redirect errRedirect = Redirect.appendTo(sp.getErrFile());
        try {
            CmdUtil.runCmd(cmd, redirectAll, errRedirect);
        } catch (IOException | InterruptedException e) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error executing query : " + e.getMessage());
        }
    }

    public static String prepareQuery(Map<String, String> inputParamMap, String query, Integer maxExecutionTime) {
        String[] matchingFunctions = StringUtils.substringsBetween(query, "{{", "}}");
        if (Objects.isNull(matchingFunctions)) {
            return massageQuery(query, maxExecutionTime);
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
        return massageQuery(query, maxExecutionTime);
    }

    public static String massageQuery(String query, Integer maxExecutionTime) {
        return "" //
                + "SET SESSION MAX_EXECUTION_TIME=" + maxExecutionTime * 60 * 1000 + ";\n" //
                + query;
    }

    private static void addAccessControlMap(SqlParams sp, Integer maxExecutionTime) {
        Map<String, String> accessControlMap = UserPrincipalUtil.getAccessControlMap();
        String nQuery = prepareQuery(accessControlMap, sp.getQuery(), maxExecutionTime);
        sp.setQuery(nQuery);
    }

    // Commands
    private static String[] getQueryCmd(SqlParams sp) {
        String[] cmd = new String[]{ //
                "mysql", //
                "--quick", //
                "--connect-timeout=5", //
                "--host=" + sp.getHost(), //
                "--user=" + sp.getUsername(), //
                "--password=" + sp.getPassword(), //
                "-e", //
                escape(sp.getQuery()) //
        };
        log.debug("Query formed : " + sp.getQuery());
        return cmd;
    }

    private static String getValueFromMethod(Map<String, String> inputParamMap, String f, String methodName) {
        String paramKey;
        String paramValue;
        String finalString = "{{" + f + "}}";
        switch (methodName) {
            case "filter":
                paramKey = f.split("\\(")[1].split(",")[0].trim();
                paramValue = inputParamMap.get(paramKey);
                String columnName = f.split("\\(")[1].split(",")[1].trim();
                String operator = f.split("\\(")[1].split(",")[2].split("\\)")[0].trim();
                finalString = QueryExecutionDto.filter(columnName, operator, paramValue);
                break;
            case "replace":
                paramKey = StringUtils.substringBetween(f, "(", ")").trim();
                paramValue =  inputParamMap.get(paramKey);
                if (Objects.nonNull(paramValue)) {
                    finalString = paramValue;
                }
                break;
        }
        return finalString;
    }

    private static String escape(String str) {
        String os = getOs();
        if (os.equals("windows")) {
            return "\"" + str + "\"";
        }
        return str;
    }

    private static String getOs() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        } else {
            return "linux";
        }
    }

}

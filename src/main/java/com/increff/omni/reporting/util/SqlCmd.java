package com.increff.omni.reporting.util;

import com.increff.omni.reporting.dto.QueryExecutionDto;
import com.increff.omni.reporting.model.form.SqlParams;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j
public class SqlCmd {

    public static void processQuery(SqlParams sp, Double maxExecutionTime)
            throws IOException, InterruptedException {
        processQuery(sp, true, maxExecutionTime);
    }

    public static void processQuery(SqlParams sp, Boolean isUserPrincipalAvailable, Double maxExecutionTime)
            throws IOException, InterruptedException {
        if (isUserPrincipalAvailable) addAccessControlMap(sp, maxExecutionTime);
        String[] cmd = getQueryCmd(sp);
        Redirect redirectAll = Redirect.appendTo(sp.getOutFile());
        Redirect errRedirect = Redirect.appendTo(sp.getErrFile());
        log.info("Thread in process query : " + Thread.currentThread().getId() + " " +Thread.currentThread().getName());

        runCmd(cmd, redirectAll, errRedirect);
        log.info("Thread in process query after cmd : " + Thread.currentThread().getId() + " " +Thread.currentThread().getName());

    }

    public static String prepareQuery(Map<String, String> inputParamMap, String query, Double maxExecutionTime) {
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

    public static String massageQuery(String query, Double maxExecutionTime) {
        int maxTime = (int) (maxExecutionTime * 60 * 1000);
        return "SET SESSION MAX_EXECUTION_TIME=" + maxTime + ";\n" + query;
    }

    private static void addAccessControlMap(SqlParams sp, Double maxExecutionTime) {
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

    private static void runCmd(String[] cmd, Redirect out, Redirect error) throws IOException, InterruptedException {
        Process p = runCmdProcess(cmd, out, error);
        int exitValue = p.exitValue();
        log.info("Thread in run cmd : " + Thread.currentThread().getId() + " " +Thread.currentThread().getName());

        p.destroy();
        log.info("Thread in run cmd after destroy : " + Thread.currentThread().getId() + " " +Thread.currentThread().getName());

        if (exitValue == 0) {
            return;
        }
        String cmdName = cmd[0];
        throw new IOException("Error running command: " + cmdName + ", exitValue: " + exitValue);
    }

    private static Process runCmdProcess(String[] cmd, Redirect out, Redirect error)
            throws IOException, InterruptedException {
        Process p = null;
        ProcessBuilder b = new ProcessBuilder(cmd);
        if (error != null) {
            b.redirectError(error);
        }
        if (out != null) {
            b.redirectOutput(out);
        }
        p = b.start();
        log.info("Thread in process start : " + Thread.currentThread().getId() + " " +Thread.currentThread().getName());

        p.waitFor();
        Thread.sleep(40000);
        return p;
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

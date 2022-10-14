package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.SqlParams;
import com.nextscm.commons.lang.CmdUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

@Log4j
public class SqlCmd {

    /*
    * https://www.baeldung.com/java-string-formatting-named-placeholders
    * */

    public static void processQuery(SqlParams sp) throws ApiException {
        String[] cmd = getQueryCmd(sp);
        Redirect redirectAll = Redirect.appendTo(sp.getOutFile());
        Redirect errRedirect = Redirect.appendTo(sp.getErrFile());
        try {
            CmdUtil.runCmd(cmd, redirectAll, errRedirect);
        } catch (IOException | InterruptedException e) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error executing query : " + e.getMessage());
        }
    }

    // Commands
    public static String[] getQueryCmd(SqlParams sp) {
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
        log.info("Query formed =" +sp.getQuery());
        return cmd;
    }

    public static String escape(String str) {
        String os = getOs();
        if (os.equals("windows")) {
            return "\"" + str + "\"";
        }
        return str;
    }

    public static String getOs() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        } else {
            return "linux";
        }
    }

}

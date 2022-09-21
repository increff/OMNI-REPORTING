package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.SqlParams;
import com.nextscm.commons.lang.CmdUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

public class SqlCmd {
    public static final Logger logger = Logger.getLogger(SqlCmd.class);

    /*
    * https://www.baeldung.com/java-string-formatting-named-placeholders
    * */

    public static void processToTsv(SqlParams sp) throws ApiException {
        String[] cmd = getQueryCmd(sp);
        Redirect redirectAll = Redirect.appendTo(sp.tsvFile);
        Redirect errRedirect = Redirect.appendTo(sp.errFile);
        try {
            CmdUtil.runCmd(cmd, redirectAll, errRedirect);
        } catch (IOException | InterruptedException e) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error executing MYSQL query");
        }
    }

    // Commands
    public static String[] getQueryCmd(SqlParams sp) {
        String[] cmd = new String[]{ //
                "mysql", //
                "--quick", //
                "--connect-timeout=5", //
                "--host=" + sp.host, //
                "--user=" + sp.username, //
                "--password=" + sp.password, //
                "-e", //
                escape(sp.query) //
        };
        logger.info("Query formed =" +sp.query);
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
        if (os.indexOf("win") >= 0) {
            return "windows";
        } else {
            return "linux";
        }
    }

}

package com.increff.omni.reporting.api;

import com.increff.omni.reporting.model.SqlParams;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.increff.omni.reporting.util.SqlCmd;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class QueryRunApi {

    public static final Logger logger = Logger.getLogger(QueryRunApi.class);

    private static String toString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }


    private File processToTsv(ConnectionPojo connPojo, String query) throws ApiException, IOException, InterruptedException {
        /* Get the correct DB connection
        * Get Query
        * */
        //TODO need to use crypto service here
        String password = connPojo.getPassword();
        SqlParams sqlp = new SqlParams();
        File tsvFile = null;
        File errFile = null;
        sqlp.tsvFile = tsvFile;
        sqlp.errFile = errFile;
        try {
            SqlCmd.processToTsv(sqlp);
        } catch (ApiException e) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error running query" + e.getMessage());
            //TODO do we need this ?
            //return errFile;
        }

        return tsvFile;
    }


    //TODO how to avoid the case where integer and strings are getting replaced as expected
    @Transactional
    public File processToTsv(ReportRequestPojo pojo, Map<String, String> params) throws ApiException, IOException, InterruptedException {
        File file = new File("");
        /*
        * Get the query
        * Replace the params
        * Get
        * Call internal processToTsv
        *
        * */



        try {
            //file = processToTsv(queryLogPojo, propertyApi.getMaxNumberRows(), propertyApi.getMaxDataSize());
            logger.debug("File created");

        } catch (Exception e) {
            toString(e);
            logger.error("Error creating file" + e.getMessage());
            throw e;
        } finally {
            //
        }
        return file;
    }

}
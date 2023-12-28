package com.increff.omni.reporting.job;

import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.increff.commons.springboot.common.ApiException;

public abstract class AbstractTask {

    protected abstract void runReportAsync(ReportRequestPojo pojo) throws ApiException;
}

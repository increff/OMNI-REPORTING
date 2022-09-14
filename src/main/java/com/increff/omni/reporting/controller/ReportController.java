package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.ReportDto;
import com.increff.omni.reporting.model.data.ReportData;
import com.increff.omni.reporting.model.data.ReportQueryData;
import com.increff.omni.reporting.model.form.ReportForm;
import com.increff.omni.reporting.model.form.ReportQueryForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.ApiErrorResponses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
@RequestMapping(value = "/reports")
public class ReportController {

    @Autowired
    private ReportDto dto;

    @ApiOperation(value = "Add Report")
    @ApiErrorResponses
    @RequestMapping(method = RequestMethod.POST)
    public ReportData add(@RequestBody ReportForm form) throws ApiException {
        return dto.add(form);
    }

    @ApiOperation(value = "Edit Report")
    @ApiErrorResponses
    @RequestMapping(value = "/{reportId}", method = RequestMethod.PUT)
    public ReportData edit(@PathVariable Integer reportId, @RequestBody ReportForm form) throws ApiException {
        return dto.edit(reportId, form);
    }

    @ApiOperation(value = "Edit Report")
    @ApiErrorResponses
    @RequestMapping(value = "/selectByOrgId/{orgId}", method = RequestMethod.GET)
    public List<ReportData> selectByOrgId(@PathVariable Integer orgId) throws ApiException {
        return dto.selectAll(orgId);
    }

    @ApiOperation(value = "Add/Edit Report Query")
    @ApiErrorResponses
    @RequestMapping(value = "/{reportId}/query", method = RequestMethod.POST)
    public ReportQueryData addQuery(@PathVariable Integer reportId, @RequestBody ReportQueryForm form) throws ApiException {
        return dto.upsertQuery(reportId, form);
    }

    @ApiOperation(value = "Map control to a report")
    @ApiErrorResponses
    @RequestMapping(value = "/{reportId}/controls/{controlId}", method = RequestMethod.POST)
    public void mapReportToControl(@PathVariable Integer reportId, @PathVariable Integer controlId) throws ApiException {
        dto.mapToControl(reportId, controlId);
    }

}

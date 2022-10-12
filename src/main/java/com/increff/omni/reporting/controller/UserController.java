package com.increff.omni.reporting.controller;


import com.increff.omni.reporting.dto.ReportRequestDto;
import com.increff.omni.reporting.dto.UserDto;
import com.increff.omni.reporting.model.data.ReportRequestData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.util.FileUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.ApiErrorResponses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Api
@RestController
@RequestMapping(value = "/standard")
public class UserController {

    @Autowired
    private UserDto userDto;
    @Autowired
    private ReportRequestDto reportRequestDto;

    @ApiOperation(value = "Request Report")
    @ApiErrorResponses
    @RequestMapping(value = "/request-report", method = RequestMethod.POST)
    public void requestReport(@RequestBody ReportRequestForm form) throws ApiException {
        userDto.requestReport(form);
    }

    @ApiOperation(value = "Get All Request data")
    @ApiErrorResponses
    @RequestMapping(value = "/request-report", method = RequestMethod.GET)
    public List<ReportRequestData> getAll(@RequestParam(required = false) Integer days) throws ApiException {
        return reportRequestDto.getAll(days);
    }

    @ApiOperation(value = "Get Result of Request")
    @ApiErrorResponses
    @RequestMapping(value = "/request-report/{requestId}",method = RequestMethod.GET)
    public void getFile(@PathVariable Integer requestId, HttpServletResponse response) throws ApiException, IOException {
        File file = reportRequestDto.getReportFile(requestId);
        FileUtil.createFileResponse(file,response);
    }

    // todo tsv, excel download
}

package com.increff.omni.reporting.controller;


import com.increff.account.client.AuthClient;
import com.increff.account.client.Params;
import com.increff.account.model.QueryUserData;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.util.FileUtil;
import com.nextscm.commons.spring.client.AppClientException;
import com.nextscm.commons.spring.common.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@CrossOrigin
@Api
@RestController
@RequestMapping(value = "/api/standard")
public class StandardController {

    @Autowired
    private ReportRequestDto reportRequestDto;
    @Autowired
    private InputControlDto inputControlDto;
    @Autowired
    private ReportDto reportDto;
    @Autowired
    private DirectoryDto directoryDto;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private AuthClient authClient;

    @ApiOperation(value = "Get all available timezones")
    @RequestMapping(value = "/timeZones", method = RequestMethod.GET)
    public List<TimeZoneData> getAllAvailableTimeZones() throws ApiException {
        return reportRequestDto.getAllAvailableTimeZones();
    }

    @ApiOperation(value = "Set Account server cookie")
    @RequestMapping(value = "/jump", method = RequestMethod.GET)
    public RedirectView setCookie(@RequestParam String authToken) {
        try {
            QueryUserData data = authClient.veriftyToken(authToken);
            if (Objects.nonNull(data) && data.isStatus()) {
                Cookie c = new Cookie(Params.AUTH_TOKEN, authToken);
                HttpDto.setCookie(c);
            }
        } catch (AppClientException ignored) {

        }
        return new RedirectView(properties.getUiHomePagePath(), false);
    }

    @ApiOperation(value = "Select controls for a report")
    @RequestMapping(value = "/reports/{reportId}/controls", method = RequestMethod.GET)
    public List<InputControlData> selectByReportId(@PathVariable Integer reportId) throws ApiException {
        return inputControlDto.selectForReport(reportId);
    }

    @ApiOperation(value = "Get Reports")
    @RequestMapping(value = "/reports", method = RequestMethod.GET)
    public List<ReportData> selectByOrgId() throws ApiException {
        return reportDto.selectByOrg();
    }

    @ApiOperation(value = "Get validation group")
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.GET)
    public List<ValidationGroupData> getValidationGroups(@PathVariable Integer reportId) {
        return reportDto.getValidationGroups(reportId);
    }

    @ApiOperation(value = "Get All Directories")
    @RequestMapping(value = "/directories", method = RequestMethod.GET)
    public List<DirectoryData> selectAllDirectories() throws ApiException {
        return directoryDto.getAllDirectories();
    }

    @ApiOperation(value = "Request Report")
    @RequestMapping(value = "/request-report", method = RequestMethod.POST)
    public void requestReport(@RequestBody ReportRequestForm form) throws ApiException {
        reportRequestDto.requestReport(form);
    }

    @ApiOperation(value = "Get All Request data")
    @RequestMapping(value = "/request-report", method = RequestMethod.GET)
    public List<ReportRequestData> getAll() throws ApiException, IOException {
        return reportRequestDto.getAll();
    }

    @ApiOperation(value = "Get Result of Request")
    @RequestMapping(value = "/request-report/{requestId}", method = RequestMethod.GET)
    public void getFile(@PathVariable Integer requestId, HttpServletResponse response) throws
            ApiException, IOException {
        File file = reportRequestDto.getReportFile(requestId);
        FileUtil.createFileResponse(file, response);
        FileUtil.delete(file);
    }

    @ApiOperation(value = "View CSV of Request")
    @RequestMapping(value = "/request-report/{requestId}/view", method = RequestMethod.GET)
    public List<Map<String, String>> viewFile(@PathVariable Integer requestId) throws ApiException, IOException {
        return reportRequestDto.getJsonFromCsv(requestId);
    }

}

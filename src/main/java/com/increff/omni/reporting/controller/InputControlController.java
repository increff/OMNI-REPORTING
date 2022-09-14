package com.increff.omni.reporting.controller;


import com.increff.omni.reporting.dto.InputControlDto;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.form.InputControlForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.ApiErrorResponses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
@RequestMapping(value = "/controls")
public class InputControlController {

    @Autowired
    private InputControlDto dto;

    @ApiOperation(value = "Add Input Control")
    @ApiErrorResponses
    @RequestMapping(method = RequestMethod.POST)
    public InputControlData add(@RequestBody InputControlForm form) throws ApiException {
        return dto.add(form);
    }

    @ApiOperation(value = "Select all global controls")
    @ApiErrorResponses
    @RequestMapping(value = "/global", method = RequestMethod.GET)
    public List<InputControlData> selectAllGlobal(){
        return dto.selectAllGlobal();
    }

    @ApiOperation(value = "Select controls for a report")
    @ApiErrorResponses
    @RequestMapping(method = RequestMethod.GET)
    public List<InputControlData> selectByReportId(@RequestParam Integer reportId){
        return dto.selectForReport(reportId);
    }



}

package com.increff.omni.reporting.controller;


import com.increff.omni.reporting.dto.UserDto;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.ApiErrorResponses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserDto userDto;

    @ApiOperation(value = "Request Report")
    @ApiErrorResponses
    @RequestMapping(method = RequestMethod.POST)
    public void requestReport(@RequestBody ReportRequestForm form) throws ApiException {
        userDto.requestReport(form);
    }

}

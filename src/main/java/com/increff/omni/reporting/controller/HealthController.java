package com.increff.omni.reporting.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(value = "/health")
public class HealthController {

    @ApiOperation(value = "For Health Check")
    @RequestMapping(method = RequestMethod.GET)
    public void healthCheck(){
    }

}

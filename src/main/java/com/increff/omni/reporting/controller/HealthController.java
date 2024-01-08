package com.increff.omni.reporting.controller;

//import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/health")
public class HealthController {

    @Operation(summary = "For Health Check")
    @RequestMapping(method = RequestMethod.GET)
    public void healthCheck(){
        System.out.println("Hello Health check");
    }

}

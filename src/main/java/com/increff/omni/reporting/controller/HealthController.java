package com.increff.omni.reporting.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/health")
public class HealthController {

    @Operation(summary = "For Health Check")
    @GetMapping()
    public void healthCheck(){
        System.out.println("Hello Health check");
    }

}

package com.increff.omni.reporting.controller;


import com.nextscm.commons.spring.server.ApiErrorResponses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(value = "/test")

public class TestController extends AbstractRestController {

	// User
	@ApiOperation(value = "Test API")
	@ApiErrorResponses
	@RequestMapping(method = RequestMethod.GET)
	public String test() {
		return "Test success";
	}

}

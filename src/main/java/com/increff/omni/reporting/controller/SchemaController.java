package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.SchemaDto;
import com.increff.omni.reporting.model.data.SchemaData;
import com.increff.omni.reporting.model.form.SchemaForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.ApiErrorResponses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api
@RestController
@RequestMapping(value = "/schema")
public class SchemaController {

    @Autowired
    private SchemaDto dto;

    @ApiOperation(value = "Add Schema")
    @ApiErrorResponses
    @RequestMapping(method = RequestMethod.POST)
    public SchemaData add(@RequestBody SchemaForm form) throws ApiException {
        return dto.add(form);
    }

    @ApiOperation(value = "Get All Schema")
    @ApiErrorResponses
    @RequestMapping(value = "/selectAll", method = RequestMethod.GET)
    public List<SchemaData> selectAll(){
        return dto.selectAll();
    }

}

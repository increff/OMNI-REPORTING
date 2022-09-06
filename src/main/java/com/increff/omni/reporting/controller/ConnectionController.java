package com.increff.omni.reporting.controller;


import com.increff.omni.reporting.dto.ConnectionDto;
import com.increff.omni.reporting.model.data.ConnectionData;
import com.increff.omni.reporting.model.form.ConnectionForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.ApiErrorResponses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
@RequestMapping(value = "/connections")
public class ConnectionController {

    @Autowired
    private ConnectionDto dto;

    @ApiOperation(value = "Add Connection")
    @ApiErrorResponses
    @RequestMapping(method = RequestMethod.POST)
    public ConnectionData add(@RequestBody ConnectionForm form) throws ApiException {
        return dto.add(form);
    }

    @ApiOperation(value = "Update Connection")
    @ApiErrorResponses
    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    public ConnectionData update(@PathVariable Integer id, @RequestBody ConnectionForm form) throws ApiException {
        return dto.update(id, form);
    }

    @ApiOperation(value = "Update Connection")
    @ApiErrorResponses
    @RequestMapping(value = "/selectAll",method = RequestMethod.GET)
    public List<ConnectionData> selectAll(){
        return dto.selectAll();
    }

}

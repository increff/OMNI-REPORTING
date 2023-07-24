package com.increff.omni.reporting.controller;

import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ErrorData;
import com.nextscm.commons.spring.server.AbstractRestappControllerAdvice;
import lombok.extern.log4j.Log4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Log4j
public class RestControllerAdvice extends AbstractRestappControllerAdvice {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Throwable.class})
    @ResponseBody
    public ErrorData handleUnknownException(HttpServletRequest req, Throwable t) {
        ErrorData data = new ErrorData();
        data.setCode(ApiStatus.UNKNOWN_ERROR);
        data.setMessage("Internal error");
//        data.setDescription(fromThrowable(t));
        log.error("Internal Server Error", t);
        return data;
    }
}

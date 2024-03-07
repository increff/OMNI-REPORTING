package com.increff.omni.reporting.controller;

import com.increff.commons.springboot.common.ApiStatus;
import com.increff.commons.springboot.common.ErrorData;
import com.increff.commons.springboot.server.RestAppControllerAdvice;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
@Log4j2
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestControllerAdvice extends RestAppControllerAdvice {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Throwable.class})
    @ResponseBody
    public ErrorData handleUnknownException(HttpServletRequest req, Throwable t) {
        ErrorData data = new ErrorData();
        data.setCode(ApiStatus.UNKNOWN_ERROR);
        data.setMessage("Internal error");
        data.setDescription(fromThrowable(t));
        log.error("Internal Server Error", t);
        return data;
    }

    // Moved from AbstractRestappControllerAdvice to here as it was removed in the next version
    public static String fromThrowable(Throwable t) {
        if (t == null) {
            return null;
        }
        // Return stack trace as a string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}

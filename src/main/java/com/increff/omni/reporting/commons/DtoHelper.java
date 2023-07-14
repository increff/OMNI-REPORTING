package com.increff.omni.reporting.commons;

import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.FieldErrorData;
//import com.nextscm.commons.spring.common.ValidationUtil;
import jakarta.validation.ConstraintViolation;

import java.util.*;

public class DtoHelper {
    public DtoHelper() {
    }

    public static void unsupportedMethod() {
        throw new UnsupportedOperationException("This method is not supported right now");
    }

    public static void checkValid(Collection<String> collection, String message) throws ApiException {
        if (!collection.isEmpty()) {
            String data = "[" + String.join(",", collection) + "]";
            throw new ApiException(ApiStatus.BAD_DATA, message + " " + data);
        }
    }

    public static <T> void checkValid(T obj) throws ApiException {
        Set<ConstraintViolation<T>> violations = ValidationUtil.validate(obj);
        if (!violations.isEmpty()) {
            List<FieldErrorData> errorList = new ArrayList(violations.size());
            Iterator var3 = violations.iterator();

            while(var3.hasNext()) {
                ConstraintViolation<T> violation = (ConstraintViolation)var3.next();
                FieldErrorData error = new FieldErrorData();
                error.setCode("");
                error.setField(violation.getPropertyPath().toString());
                error.setMessage(violation.getMessage());
                errorList.add(error);
            }

            throw new ApiException(ApiStatus.BAD_DATA, "Input validation failed", errorList);
        }
    }

    public static void checkErrors(Collection<String> collection, String message) throws ApiException {
        if (!collection.isEmpty()) {
            String data = "[" + String.join(",", collection) + "]";
            throw new ApiException(ApiStatus.BAD_DATA, message + " " + data);
        }
    }
}

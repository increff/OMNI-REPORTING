package com.increff.omni.reporting.commons;

import com.nextscm.commons.spring.common.ApiException;

import java.util.Collection;

public class AbstractDtoApi {
    public AbstractDtoApi() {
    }

    protected void authorizeAndExecute() {
    }

    protected static void unsupportedMethod() {
        DtoHelper.unsupportedMethod();
    }

    protected static void checkValid(Collection<String> collection, String message) throws ApiException {
        DtoHelper.checkValid(collection, message);
    }

    protected static <T> void checkValid(T obj) throws ApiException {
        DtoHelper.checkValid(obj);
    }

    protected static void checkErrors(Collection<String> collection, String message) throws ApiException {
        DtoHelper.checkErrors(collection, message);
    }
}

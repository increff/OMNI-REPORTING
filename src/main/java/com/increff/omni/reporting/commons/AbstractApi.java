package com.increff.omni.reporting.commons;

import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

public class AbstractApi {
    public AbstractApi() {
    }

    public static void checkNotNull(Object obj, String message) throws ApiException {
        if (obj == null) {
            throw new ApiException(ApiStatus.BAD_DATA, message);
        }
    }

    public static void checkNull(Object obj, String message) throws ApiException {
        if (obj != null) {
            throw new ApiException(ApiStatus.BAD_DATA, message);
        }
    }

    public static void unsupportedMethod(String message) {
        throw new UnsupportedOperationException(message);
    }
}

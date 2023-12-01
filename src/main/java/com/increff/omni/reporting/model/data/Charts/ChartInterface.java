package com.increff.omni.reporting.model.data.Charts;

import com.increff.omni.reporting.model.constants.ChartType;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface ChartInterface {
    Object transform(List<Map<String, String>> data) throws ApiException;

    default void validateNormalize(List<Map<String, String>> result, ChartType type) throws ApiException {
        validate(result, type);
        normalize(result, type);
    }

    default void validate(List<Map<String, String>> result, ChartType type) throws ApiException {
        if(Objects.nonNull(type.getROW_COUNT_VALIDATION()) && (result.size() != type.getROW_COUNT_VALIDATION()) )
            throw new ApiException(ApiStatus.BAD_DATA, "Invalid row count for type: " + type + ". Expected: " + type.getROW_COUNT_VALIDATION() + " Received: " + result.size());
        if(Objects.nonNull(type.getCOL_COUNT_VALIDATION()) && (result.get(0).size() != type.getCOL_COUNT_VALIDATION()) )
            throw new ApiException(ApiStatus.BAD_DATA, "Invalid column count for type: " + type + ". Expected: " + type.getCOL_COUNT_VALIDATION() + " Received: " + result.get(0).size());
    }

    default void normalize(List<Map<String, String>> result, ChartType type) throws ApiException {
        // Placeholder for future use
        return;
    }



}

package com.increff.omni.reporting.model.data.Charts;

import com.increff.omni.reporting.model.constants.ChartType;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.increff.omni.reporting.util.SqlCmd.getValueSum;

public interface ChartInterface {
    Object transform(List<Map<String, String>> data) throws ApiException;

    default void validate(List<Map<String, String>> result, ChartType type) throws ApiException {
        if(Objects.nonNull(type.getROW()) && (result.size() != type.getROW()) )
            throw new ApiException(ApiStatus.BAD_DATA, "Invalid row count for type: " + type + ". Expected: " + type.getROW() + " Received: " + result.size());
        if(Objects.nonNull(type.getCOL()) && (result.get(0).size() != type.getCOL()) )
            throw new ApiException(ApiStatus.BAD_DATA, "Invalid column count for type: " + type + ". Expected: " + type.getCOL() + " Received: " + result.get(0).size());

        if(Objects.nonNull(type.getVALUE_SUM()) && (!Objects.equals(getValueSum(result), type.getVALUE_SUM())) )
            throw new ApiException(ApiStatus.BAD_DATA, "Invalid value sum for type: " + type + ". Expected: " + type.getVALUE_SUM() + " Received: " + getValueSum(result));


    }

}

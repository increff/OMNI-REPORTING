package com.increff.omni.reporting.model.data.Charts;

import com.increff.omni.reporting.model.constants.ChartType;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.apache.log4j.Level;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.log4j.Logger;

import static com.increff.omni.reporting.util.SqlCmd.getValueSum;


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

        if(Objects.nonNull(type.getVALUE_SUM_VALIDATION()) && (!Objects.equals(getValueSum(result), type.getVALUE_SUM_VALIDATION())) )
            throw new ApiException(ApiStatus.BAD_DATA, "Invalid value sum for type: " + type + ". Expected: " + type.getVALUE_SUM_VALIDATION() + " Received: " + getValueSum(result));
    }

    default void normalize(List<Map<String, String>> result, ChartType type) throws ApiException {
        // Normalize pie chart values so that their sum is 100
        Logger logger = Logger.getRootLogger(); // Get Logger here as annotation @Log4j does not work in interfaces
        if(Objects.equals(type, ChartType.PIE) || Objects.equals(type, ChartType.DOUGHNUT)){

            double sum = getValueSum(result);
            if(sum == 0)
                return;
            for(Map.Entry<String, String> e: result.get(0).entrySet()){
                if(!Objects.equals(e.getKey(), "label"))
                    e.setValue(getNormalizedValue(e.getValue(), sum));
            }

            double finalSum = getValueSum(result);
            double difference = 100 - finalSum;
            logger.debug("difference: " + difference);
            if(difference != 0){ // As the final sum can be between(99.01 to 100.99) due to precision, add the offset to firstColumnValue
                Map<String, String> firstRow = result.get(0);
                String firstColumnName = firstRow.keySet().iterator().next();
                firstRow.put(firstColumnName, String.format("%.2f", Double.parseDouble(firstRow.get(firstColumnName)) + difference));
            }
        }
    }

    default String getNormalizedValue(String value, Double sum){
        Logger logger = Logger.getRootLogger();
        logger.debug("getNormalizedValue value: " + value + " sum: " + sum);
        return String.format("%.2f", (Double.parseDouble(value)/sum)*100); // returns rounding off to 2 decimals
    }

}

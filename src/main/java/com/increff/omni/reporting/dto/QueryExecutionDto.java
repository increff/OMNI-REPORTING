package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.model.constants.QueryOperators;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.stereotype.Component;

@Component
public class QueryExecutionDto extends AbstractDto {

    public String replaceOrRemove(String columnName, String operator, String paramValue) throws ApiException {
        if (paramValue == null)
            return "1=1";
        String s = "";
        switch (QueryOperators.valueOf(operator)) {
            case GE:
                s = columnName + " >= " + paramValue;
                break;
            case GT:
                s = columnName + " > " + paramValue;
                break;
            case LE:
                s = columnName + " <= " + paramValue;
                break;
            case LT:
                s = columnName + " < " + paramValue;
                break;
            case EQS:
                s = columnName + " = " + paramValue;
                break;
            case INS:
                s = columnName + " in (" + paramValue + ")";
                break;
            default:
                throw new ApiException(ApiStatus.BAD_DATA, "Unknown Operator : " + operator);
        }
        return s;
    }
}

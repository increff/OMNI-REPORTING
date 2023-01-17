package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.model.constants.QueryOperator;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import org.springframework.stereotype.Component;

import java.util.Objects;

public class QueryExecutionDto extends AbstractDto {
    
    private final static String ALWAYS_TRUE = "1=1";

    public static String filterAppend(String columnName, String operator, String paramValue, String condition) {
        String filter = filter(columnName, operator, paramValue);
        if(filter.equals(ALWAYS_TRUE))
            return ALWAYS_TRUE;
        else
            return filter.concat(" ").concat(condition);
    }

    public static String filter(String columnName, String operator, String paramValue) {
        if (Objects.isNull(paramValue))
            return ALWAYS_TRUE;
        StringBuilder s = new StringBuilder();
        if(Objects.isNull(QueryOperator.valueOfLabel(operator)))
            return s.toString();
        switch (Objects.requireNonNull(QueryOperator.valueOfLabel(operator))) {
            case GE:
                s.append(columnName).append(" ").append(QueryOperator.GE.getValue()).append(" ").append(paramValue);
                break;
            case GT:
                s.append(columnName).append(" ").append(QueryOperator.GT.getValue()).append(" ").append(paramValue);
                break;
            case LE:
                s.append(columnName).append(" ").append(QueryOperator.LE.getValue()).append(" ").append(paramValue);
                break;
            case LT:
                s.append(columnName).append(" ").append(QueryOperator.LT.getValue()).append(" ").append(paramValue);
                break;
            case EQS:
                s.append(columnName).append(" ").append(QueryOperator.EQS.getValue()).append(" ").append(paramValue);
                break;
            case NE:
                s.append(columnName).append(" ").append(QueryOperator.NE.getValue()).append(" ").append(paramValue);
                break;
            case INS:
                s.append(columnName).append(" ").append(QueryOperator.INS.getValue()).append(" (").append(paramValue).append(")");
                break;
            default:
                // Do nothing
        }
        return s.toString();
    }
}

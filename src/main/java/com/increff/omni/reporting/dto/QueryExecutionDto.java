package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.model.constants.QueryOperator;

import java.util.Objects;

public class QueryExecutionDto extends AbstractDto {
    
    private final static String ALWAYS_TRUE = "1=1";
    private final static String MONGO_ALWAYS_TRUE = "{}";

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

    public static String mongoFilter(String filterJson, String paramKey, String paramValue, Boolean keepQuotes) {
        if (Objects.isNull(paramValue))
            return MONGO_ALWAYS_TRUE;
        if(!keepQuotes) // Unlike sql, error when using single quotes for numbers
            paramValue = removeUnescapedSingleQuotes(paramValue);// do not remove escaped single quotes which comes from input

        filterJson = filterJson.replace("#" + paramKey, paramValue);
        return filterJson;
    }

    /**
     * Regex explained:
     * (?<!\\\\)': This is a negative lookbehind assertion ((?<!...)). It ensures that the single quote (') is not preceded by a backslash (\). The \\\\ part represents a double backslash (\\) because in Java regex, backslashes need to be escaped twice within string literals. So, \\\\ matches a single backslash. Therefore, (?<!\\\\) ensures that the single quote is not preceded by a backslash.
     *
     * '': This is the replacement string, which is empty (""). It means that any matched single quotes will be replaced with nothing, effectively removing them from the string.
     */
    private static String removeUnescapedSingleQuotes(String paramValue) {
        paramValue = paramValue.replaceAll("(?<!\\\\)'", "");
        return paramValue;
    }
}

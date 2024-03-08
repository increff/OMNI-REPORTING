package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.model.constants.QueryOperator;

import java.util.Objects;

public class QueryExecutionDto extends AbstractDto {

    private final static String ALWAYS_TRUE = "1=1";
    private final static String MONGO_ALWAYS_TRUE = "{}";

    private final static String MONGO_FILTER_PARAM_VALUE_REPLACE_IDENTIFIER = "#";

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

        /*
            For SQL, the value was just added at the end after col_name and operator (Eg. col_name = value)
            For mongo, the value has to be replaced somewhere between the json.
            Identifier is needed to know at which position the value has to be replaced.
            So, we replace the #paramKey with the paramValue in the filterJson
         */
        filterJson = filterJson.replace(MONGO_FILTER_PARAM_VALUE_REPLACE_IDENTIFIER + paramKey, paramValue);
        return filterJson;
    }

    public static String mongoReplace(String paramValue, Boolean keepQuotes) {
        if(!keepQuotes) // Unlike sql, error when using single quotes for numbers
            paramValue = removeUnescapedSingleQuotes(paramValue);// do not remove escaped single quotes which comes from input
        return paramValue;
    }

    /**
     * Regex explained:
     * (?<!\\\\)': This is a negative lookbehind assertion ((?<!...)). It ensures that the single quote (') is not preceded by a backslash (\). The \\\\ part represents a double backslash (\\) because in Java regex, backslashes need to be escaped twice within string literals. So, \\\\ matches a single backslash. Therefore, (?<!\\\\) ensures that the single quote is not preceded by a backslash.
     * <p>
     * '': This is the replacement string, which is empty (""). It means that any matched single quotes will be replaced with nothing, effectively removing them from the string.
     */
    private static String removeUnescapedSingleQuotes(String paramValue) {
        paramValue = paramValue.replaceAll("(?<!\\\\)'", "");
        return paramValue;
    }
}

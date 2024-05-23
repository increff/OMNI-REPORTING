package com.increff.omni.reporting.model.constants;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import static com.increff.omni.reporting.util.ConstantsUtil.USER_TIMEZONE;

@Log4j2
@Getter
public enum DynamicDate {

    // todo : rem display name if not required as it exists in UI
    NOW("convert_tz(now(), " + USER_TIMEZONE + ", \"UTC\")", "Now"),
    TODAY("convert_tz(timestamp(curdate()), " + USER_TIMEZONE + ", \"UTC\")", "Today"),
    YESTERDAY("convert_tz(timestamp(date_sub(curdate(), interval 1 day)), " + USER_TIMEZONE + ", \"UTC\")", "Yesterday"),
    ONE_WEEK("convert_tz(timestamp(date_sub(curdate(), interval 7 day)), " + USER_TIMEZONE + ", \"UTC\")", "1 Week"),
    FIFTEEN_DAYS("convert_tz(timestamp(date_sub(curdate(), interval 15 day)), " + USER_TIMEZONE + ", \"UTC\")", "15 Days"),
    CURRENT_MONTH("convert_tz(timestamp(DATE_FORMAT(curdate(),'%Y-%m-01')), " + USER_TIMEZONE + ", \"UTC\")", "1st of Current Month"),
    LAST_MONTH_1ST("convert_tz(timestamp(DATE_FORMAT(date_sub(curdate(), INTERVAL 1 MONTH),'%Y-%m-01')), " + USER_TIMEZONE + ", \"UTC\")", "1st of Last Month");

    private final String query;
    private final String displayName;

    DynamicDate(String query, String displayName) {
        this.query = query;
        this.displayName = displayName;
    }

    public static DynamicDate queryToEnum(String query) {
        for (DynamicDate dynamicDate : DynamicDate.values()) {
            String queryBeforeUserTimezone = dynamicDate.getQuery().substring(0, dynamicDate.getQuery().indexOf(USER_TIMEZONE));
            log.trace("queryBeforeUserTimezone : " + queryBeforeUserTimezone + " query : " + query);
            if (query.startsWith(queryBeforeUserTimezone)) {
                return dynamicDate;
            }
        }
        // todo : ques : why not return actual exception instead of throwing ApiException?
        // We should throw relevant exceptions instead of combining everything.
        // Moreover, this exception wont be seen be the user as enum values are hardcoded in UI
        throw new IllegalArgumentException("Query to Dynamic Date conversion failed for " + query);
    }

    public static String enumToQuery(String name) {
        return DynamicDate.valueOf(name).query;
    }

}

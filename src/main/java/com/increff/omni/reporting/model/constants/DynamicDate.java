package com.increff.omni.reporting.model.constants;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import static com.increff.omni.reporting.util.ConstantsUtil.*;

@Log4j2
@Getter
public enum DynamicDate {

    NOW("addtime(convert_tz(now(), " + USER_TIMEZONE + ", \"UTC\"), " + ADD_TIME + ")", TIME_START_STRING),
    TODAY("addtime(convert_tz(timestamp(curdate()), " + USER_TIMEZONE + ", \"UTC\"), " + ADD_TIME + ")", TIME_START_STRING),
    YESTERDAY("addtime(convert_tz(timestamp(date_sub(curdate(), interval 1 day)), " + USER_TIMEZONE + ", \"UTC\"), " + ADD_TIME + ")", TIME_START_STRING),
    ONE_WEEK("addtime(convert_tz(timestamp(date_sub(curdate(), interval 7 day)), " + USER_TIMEZONE + ", \"UTC\"), " + ADD_TIME + ")", TIME_START_STRING),
    FIFTEEN_DAYS("addtime(convert_tz(timestamp(date_sub(curdate(), interval 15 day)), " + USER_TIMEZONE + ", \"UTC\"), " + ADD_TIME + ")", TIME_START_STRING),
    CURRENT_MONTH("addtime(convert_tz(timestamp(DATE_FORMAT(curdate(),'%Y-%m-01')), " + USER_TIMEZONE + ", \"UTC\"), " + ADD_TIME + ")", TIME_END_STRING),
    LAST_MONTH_1ST("addtime(convert_tz(timestamp(DATE_FORMAT(date_sub(curdate(), INTERVAL 1 MONTH),'%Y-%m-01')), " + USER_TIMEZONE + ", \"UTC\"), " + ADD_TIME + ")", TIME_START_STRING);

    private final String query;
    private final String endTimeString; // Add 23:59:59 sometimes in end date for schedulers to include/exclude end date

    DynamicDate(String query, String endTimeString) {
        this.query = query;
        this.endTimeString = endTimeString;
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

}

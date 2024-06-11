package com.increff.omni.reporting.model.constants;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.time.ZonedDateTime;

@Log4j2
@Getter
public enum DynamicDate {

    NOW(),
    TODAY(),
    YESTERDAY(),
    ONE_WEEK(),
    FIFTEEN_DAYS(),
    CURRENT_MONTH(false),
    LAST_MONTH_1ST();

    private Boolean addTimeEndDate = true; // add 23:59 if date type is END_DATE to include end date

    DynamicDate() {
    }

    DynamicDate(Boolean addTimeEndDate) {
        this.addTimeEndDate = addTimeEndDate;
    }


    public static ZonedDateTime parse(DynamicDate dynamicDate, ZonedDateTime zdt) {
        ZonedDateTime dynamicZdtUtc = getDynamicZdt(dynamicDate, zdt);
        return dynamicZdtUtc;
    }

    public static ZonedDateTime getDynamicZdt(DynamicDate dynamicDate, ZonedDateTime zdt) {
        return switch (dynamicDate) {
            case NOW -> zdt;
            case TODAY -> zdt.withHour(0).withMinute(0).withSecond(0).withNano(0);
            case YESTERDAY -> zdt.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case ONE_WEEK -> zdt.minusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case FIFTEEN_DAYS -> zdt.minusDays(15).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case CURRENT_MONTH -> zdt.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case LAST_MONTH_1ST ->
                    zdt.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            default -> throw new IllegalArgumentException("Dynamic Date unsupported value: " + dynamicDate);
        };
    }

}

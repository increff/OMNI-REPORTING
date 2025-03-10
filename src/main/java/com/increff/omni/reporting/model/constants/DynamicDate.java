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
    LAST_WEEK(),
    FIFTEEN_DAYS_AGO(),
    CURRENT_MONTH(false),
    LAST_MONTH_1ST(),

    TOMORROW(),
    NEXT_WEEK(),
    FIFTEEN_DAYS_LATER(),
    NEXT_MONTH_1ST(),

    @Deprecated(since = "2.2.1", forRemoval = true)
    ONE_WEEK(),
    @Deprecated(since = "2.2.1", forRemoval = true)
    FIFTEEN_DAYS();

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
            case LAST_WEEK -> zdt.minusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case FIFTEEN_DAYS_AGO -> zdt.minusDays(15).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case CURRENT_MONTH -> zdt.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case LAST_MONTH_1ST ->
                    zdt.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case TOMORROW -> zdt.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case NEXT_WEEK -> zdt.plusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case FIFTEEN_DAYS_LATER -> zdt.plusDays(15).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case NEXT_MONTH_1ST -> zdt.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case ONE_WEEK, FIFTEEN_DAYS -> throw new IllegalArgumentException("Deprecated Dynamic date value, Migration needed: " + dynamicDate);
            default -> throw new IllegalArgumentException("Dynamic Date unsupported value: " + dynamicDate);
        };
    }

}

package com.increff.omni.reporting.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtil {

    public static ZonedDateTime getTimeInTz(ZonedDateTime zdt, String tz) {
        return zdt.withZoneSameInstant(ZoneId.of(tz));
    }

    public static String getISO8601(ZonedDateTime zdt) {
        return zdt.toOffsetDateTime().toString();
    }

}

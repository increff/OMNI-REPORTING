package com.increff.omni.reporting.util;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConstantsUtil {
    public static final String DASHBOARD_COPY_IGNORE_PREFIX = "test";

    public static Integer MAX_RETRY_COUNT;

    public static Integer SCHEDULE_FILE_SIZE_ZIP_AFTER;

    public static String USER_TIMEZONE = "#user_timezone"; // Replaced by user timezone in add/edit schedules
    public static String ADD_TIME = "#add_time"; // todo : rem
    public static final String TIME_START_STRING = "\"00:00:00\"";
    public static final String TIME_END_STRING = "\"23:59:59\"";

}


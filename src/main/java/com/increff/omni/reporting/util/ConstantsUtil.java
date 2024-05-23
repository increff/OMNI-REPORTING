package com.increff.omni.reporting.util;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConstantsUtil {
    public static Integer MAX_RETRY_COUNT;

    public static Integer SCHEDULE_FILE_SIZE_ZIP_AFTER;

    public static String USER_TIMEZONE = "#user_timezone"; // Replaced by user timezone in add/edit schedules
    public static String ADD_TIME = "#add_time";
}


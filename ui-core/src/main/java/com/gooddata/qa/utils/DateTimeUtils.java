package com.gooddata.qa.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    public final static String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static String parseDateTime(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String parseToTimeStampFormat(LocalDateTime dateTime) {
        return parseDateTime(dateTime, TIMESTAMP_FORMAT);
    }
}

package com.bin.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author P-bin
 * @since 2023/10/14 16:17
 * 日期工具类
 */
public class DateUtils {
    /**
     * 格式化日期为指定格式的字符串
     * @param date    需要格式化的日期
     * @param pattern 日期格式模式
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 将字符串解析为日期对象
     * @param dateStr 需要解析的日期字符串
     * @param pattern 日期格式模式
     * @return 解析后的日期对象
     * @throws ParseException 如果无法解析日期字符串，则抛出异常
     */
    public static Date parseDate(String dateStr, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(dateStr);
    }
}

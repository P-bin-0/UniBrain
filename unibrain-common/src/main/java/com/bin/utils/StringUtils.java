package com.bin.utils;

/**
 * @author P-bin
 * @since 2023/10/14 16:17
 * 字符串工具类
 */
public class StringUtils {
    /**
     * 判断字符串是否为空或null
     *
     * @param str 需要判断的字符串
     * @return 如果字符串为null或空字符串，则返回true
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 将字符串的第一个字符转换为大写
     *
     * @param s 需要转换的字符串
     * @return 转换后的字符串
     */
    public static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }

    /**
     * 将字符串的第一个字符转换为小写
     *
     * @param s 需要转换的字符串
     * @return 转换后的字符串
     */
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
    }

    /**
     * 检查字符串是否包含空白字符
     *
     * @param str 需要检查的字符串
     * @return 如果字符串包含空白字符，则返回true
     */
    public static boolean containsWhitespace(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }
}

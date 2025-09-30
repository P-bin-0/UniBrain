package com.bin.util;

/**
 * 生成reids中的key
 */
public class RedisKeyUtil {
    public static String keyLoginSysuser(String id) {
        return "loginsysuser_" + id ;
    }
    public static String keyLoginSysuser(Integer id) {
        return "loginsysuser_" + id ;
    }
    public static String keyLoginUser(String id) {
        return "loginuser_" + id ;
    }
    public static String keyLoginUser(Long id) {
        return "loginuser_" + id ;
    }
    public static String keyVerifycode(String uuid) {
        return "verifyCode_" + uuid ;
    }
    public static String keyBook(String uuid) {return "book_"+uuid ;}
    public static String keyAllBooks(String uuid) {return "allBooks_"+uuid ;}
}
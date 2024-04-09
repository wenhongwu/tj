package com.tianji.learning.utils;

/**
 * @author whw
 * @title: TableInfoContext
 * @projectName tjxt
 * @description: TODO
 * @date 2024/4/9 13:37
 */
public class TableInfoContext {
    private static final ThreadLocal<String> TL = new ThreadLocal<>();

    public static void setInfo(String info) {
        TL.set(info);
    }

    public static String getInfo() {
        return TL.get();
    }

    public static void remove() {
        TL.remove();
    }
}

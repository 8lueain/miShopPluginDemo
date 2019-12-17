package com.xiaomi.shop.build.gradle.plugins.utils

class Log {
    static void i(String tag, String msg) {
        System.out.println("[INFO][" + tag + "] " + msg);
    }

    static void e(String tag, String msg) {
        System.err.println("[ERROR][" + tag + "] " + msg);
    }
}
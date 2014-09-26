package io.github.izzyleung.zhihudailypurify.support;

import android.util.Log;

/**
 * Thank you, cundong. :-D
 * Checkout <a href="https://github.com/cundong/ZhihuPaper/blob/master/src/com/cundong/izhihu/util/Logger.java"></a>
 * for the original version.
 */

public class Logger {
    private static String logTag = "ZhihuDailyPurify";

    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null) {
            return null;
        }

        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            if (st.getClassName().equals(Logger.class.getName())) {
                continue;
            }

            return "[" + Thread.currentThread().getName()
                    + "(" + Thread.currentThread().getId() + "): "
                    + st.getFileName() + ":" + st.getLineNumber() + "]";
        }

        return null;
    }

    private static String createMessage(String msg) {
        String functionName = getFunctionName();
        return (functionName == null ? msg : (functionName + " - " + msg));
    }

    public static void i(String msg) {
        Log.i(logTag, createMessage(msg));
    }

    public static void v(String msg) {
        Log.v(logTag, createMessage(msg));
    }

    public static void d(String msg) {
        Log.d(logTag, createMessage(msg));
    }

    public static void e(String msg) {
        Log.e(logTag, createMessage(msg));
    }

    public static void w(String msg) {
        Log.w(logTag, createMessage(msg));
    }

    public static void e(Throwable e) {
        StringBuilder sb = new StringBuilder();
        String name = getFunctionName();
        StackTraceElement[] sts = e.getStackTrace();

        if (name != null) {
            sb.append(name).append(" - ").append(e).append("\r\n");
        } else {
            sb.append(e).append("\r\n");
        }
        if (sts != null && sts.length > 0) {
            for (StackTraceElement st : sts) {
                if (st != null) {
                    sb.append("[ ").append(st.getFileName()).append(":")
                            .append(st.getLineNumber()).append(" ]").append("\r\n");
                }
            }
        }

        Log.e(logTag, sb.toString());
    }
}

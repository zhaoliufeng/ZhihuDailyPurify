package io.github.izzyleung.zhihudailypurify.support;

import android.content.Intent;
import android.content.pm.PackageManager;
import io.github.izzyleung.zhihudailypurify.ZhihuDailyPurifyApplication;

public final class HelperMethods {
    private HelperMethods() {

    }

    public static boolean isZhihuClientInstalled() {
        PackageManager pm = ZhihuDailyPurifyApplication.getInstance().getPackageManager();
        boolean isZhihuClientInstalled = false;

        try {
            if (pm != null) {
                pm.getPackageInfo("com.zhihu.android", PackageManager.GET_ACTIVITIES);
                isZhihuClientInstalled = true;
            }
        } catch (PackageManager.NameNotFoundException ignored) {

        }

        return isZhihuClientInstalled;
    }

    public static boolean isIntentSafe(Intent intent) {
        PackageManager packageManager = ZhihuDailyPurifyApplication.getInstance().getPackageManager();
        return packageManager.queryIntentActivities(intent, 0).size() > 0;
    }
}

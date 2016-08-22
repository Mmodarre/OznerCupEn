package com.ozner.yiquan.mycenter.CheckForUpdate;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.ozner.yiquan.BuildConfig;

/**
 * Created by ozner_67 on 2016/4/12.
 */
public class LogUtilsLC {
    public static boolean APP_DBG = false;//是否是debug模式

    public static void init(Context context) {
        APP_DBG = isApkdebugable(context);
    }

    public static boolean isApkdebugable(Context context) {
        try {
            ApplicationInfo appInfo = context.getApplicationInfo();
            return (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }

    public static void E(String tag, String content) {
        if (BuildConfig.DEBUG)
            Log.e(tag, content);
    }

    public static void I(String tag, String content) {
        if (BuildConfig.DEBUG)
            Log.i(tag, content);
    }

    public static void D(String tag, String content) {
        if (BuildConfig.DEBUG)
            Log.d(tag, content);
    }

    public static void W(String tag, String content) {
        if (BuildConfig.DEBUG)
            Log.w(tag, content);
    }

    public static void V(String tag, String content) {
        if (BuildConfig.DEBUG)
            Log.v(tag, content);
    }
}

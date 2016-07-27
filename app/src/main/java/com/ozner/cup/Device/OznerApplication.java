package com.ozner.cup.Device;

import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.mycenter.CheckForUpdate.LogUtilsLC;

import java.util.Locale;

/**
 * Created by mengdongya on 2015/11/26.
 */
public class OznerApplication extends OznerBaseApplication {
    public static final String ACTION_ServiceInit = "ozner.service.init";
    public static Typeface numFace, textFace;
//    private boolean isLoginPhone = true;

    @Override
    protected void onBindService() {
        String userid = UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.UserId, "CsirOzner");
        String token = OznerPreference.UserToken(getBaseContext());
        getService().getDeviceManager().setOwner(userid, token);
        this.sendBroadcast(new Intent(ACTION_ServiceInit));
    }

    @Override
    public void onCreate() {
//        ShareSDK.initSDK(getApplicationContext());
        numFace = Typeface.createFromAsset(getAssets(), "font/shuzi.otf");
        textFace = Typeface.createFromAsset(getAssets(), "font/wenzi.otf");
        LogUtilsLC.init(getApplicationContext());
//        isLoginPhone = Boolean.getBoolean(OznerPreference.GetValue(getApplicationContext(), OznerPreference.IsLoginPhone, "true"));
        super.onCreate();
    }

    public static void changeTextFont(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof TextView) {
                ((TextView) v).setTypeface(textFace);
                ((TextView) v).getPaint().setFakeBoldText(false);
            } else if (v instanceof Button) {
                ((Button) v).setTypeface(textFace);
                ((Button) v).getPaint().setFakeBoldText(false);
            } else if (v instanceof EditText) {
                ((EditText) v).setTypeface(textFace);
                ((EditText) v).getPaint().setFakeBoldText(false);
            } else if (v instanceof ViewGroup) {
                changeTextFont((ViewGroup) v);
            }
        }
    }

    public static void setControlTextFace(View v) {
        if (textFace != null) {
            if (v instanceof TextView) {
                ((TextView) v).setTypeface(textFace);
            } else if (v instanceof Button) {
                ((Button) v).setTypeface(textFace);
            } else if (v instanceof EditText) {
                ((EditText) v).setTypeface(textFace);
            }
        }
    }

    public static void setControlNumFace(View v) {
        if (numFace != null) {
            if (v instanceof TextView) {
                ((TextView) v).setTypeface(numFace);
            } else if (v instanceof Button) {
                ((Button) v).setTypeface(numFace);
            } else if (v instanceof EditText) {
                ((EditText) v).setTypeface(numFace);
            }
        }
    }

    /**
     * 设置手机号登录
     */
    public void setIsPhone() {
        Log.e("lingchen", "setIsPhone: true");
//        isLoginPhone = true;
//        OznerPreference.SetValue(getApplicationContext(), OznerPreference.IsLoginPhone,"true");
        OznerPreference.setIsLoginPhone(getApplicationContext(), true);
    }

    /**
     * 设置，邮箱登录
     */
    public void setIsEmail() {
        Log.e("lingchen", "setIsPhone: false");
//        isLoginPhone = false;
//        OznerPreference.SetValue(getApplicationContext(), OznerPreference.IsLoginPhone, "false");
        OznerPreference.setIsLoginPhone(getApplicationContext(), false);
    }

    /**
     * 是否是用手机号登录
     *
     * @return true:手机登录；fasle:邮箱登录；默认true
     */
    public boolean isLoginPhone() {
//        isLoginPhone = Boolean.getBoolean(OznerPreference.GetValue(getApplicationContext(), OznerPreference.IsLoginPhone, "true"));
//        return isLoginPhone;

        return OznerPreference.isLoginPhone(getApplicationContext());
    }

    /**
     * 系统语言是否是中文
     *
     * @return true:中文版；false:英文版
     */
    public boolean isLanguageCN() {
        try {
            if (Locale.getDefault().getLanguage().endsWith("zh")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }
    }

    @Override
    public void onTerminate() {
//        ShareSDK.stopSDK();
        super.onTerminate();
    }
}

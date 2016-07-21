package com.ozner.cup.Device;

import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.mycenter.CheckForUpdate.LogUtilsLC;

/**
 * Created by mengdongya on 2015/11/26.
 */
public class OznerApplication extends OznerBaseApplication {
    public static final String ACTION_ServiceInit = "ozner.service.init";
    public static Typeface numFace, textFace;
    private boolean isCN = true;

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
        isCN = Boolean.getBoolean(OznerPreference.GetValue(getApplicationContext(), OznerPreference.IsCN, "true"));
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

    public void setIsCN() {
        isCN = true;
        OznerPreference.SetValue(getApplicationContext(), OznerPreference.IsCN, "true");
    }

    public void setIsEN() {
        isCN = false;
        OznerPreference.SetValue(getApplicationContext(), OznerPreference.IsCN, "true");
    }

    /**
     * 是否需要转英文版
     * 邮箱登录的是英文版，手机验证码登录的是中文版
     * 需要在登录的时候判断
     * 默认是中文版
     *
     * @return true:中文版；false:英文版
     */
    public boolean isLanguageCN() {
        isCN = Boolean.getBoolean(OznerPreference.GetValue(getApplicationContext(), OznerPreference.IsCN, "true"));
        return isCN;
//        if (Locale.getDefault().getLanguage().endsWith("zh")) {
//            return true;
//        } else {
//            return false;
//        }
    }

    @Override
    public void onTerminate() {
//        ShareSDK.stopSDK();
        super.onTerminate();
    }
}

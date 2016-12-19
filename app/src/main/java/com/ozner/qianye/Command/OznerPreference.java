package com.ozner.qianye.Command;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by C-sir@hotmail.com on 2015/11/23.
 */
public class OznerPreference {
    public final static String Ozner = "ozner";
    public final static String IsLogin = "islogin";
    public final static String UserToken = "usertoken";
    public final static String ServerAddress = "serveraddress";
    public final static String IsLoginPhone = "isLoginPhone";

    private static SharedPreferences Init(Context context) {
        if (context != null)
            return context.getSharedPreferences(Ozner, Context.MODE_PRIVATE);
        else return null;
    }

    private static SharedPreferences.Editor InitEditor(Context context) {
        if (context != null)
            return context.getSharedPreferences(Ozner, Context.MODE_PRIVATE).edit();
        else
            return null;
    }

    public static boolean isLoginPhone(Context context) {
        SharedPreferences ozner = Init(context);
        Boolean isloginphone = ozner.getBoolean(IsLoginPhone, true);
        return isloginphone;
    }

    public static void setIsLoginPhone(Context myContext, Boolean isPhone) {
        SharedPreferences.Editor ozner = InitEditor(myContext);
        ozner.putBoolean(IsLoginPhone, isPhone);
        ozner.commit();
    }

    public static boolean IsLogin(Context myContext) {
        SharedPreferences ozner = Init(myContext);
        Boolean islogin = ozner.getBoolean(IsLogin, false);
        return islogin;
    }

    public static String UserToken(Context myContext) {
        if (myContext != null) {
            SharedPreferences ozner = Init(myContext);
            String usertoken = ozner.getString(UserToken, null);
            return usertoken;
        } else
            return null;
    }

    public static void setIsLogin(Context myContext, Boolean islogin) {
        SharedPreferences.Editor ozner = InitEditor(myContext);
        ozner.putBoolean(IsLogin, islogin);
        ozner.commit();
    }

    public static void setUserToken(Context myContext, String userToken) {
        SharedPreferences.Editor ozner = InitEditor(myContext);
        ozner.putString(UserToken, userToken);
        ozner.commit();
    }

    public static void SetValue(Context mycontex, String key, String value) {
        SharedPreferences.Editor ozner = InitEditor(mycontex);
        ozner.putString(key, value);
        ozner.commit();
    }

    public static String GetValue(Context mycontext, String key, String value) {
        SharedPreferences ozner = Init(mycontext);
        return ozner.getString(key, value);
    }

    public static String ServerAddress(Context mycontext) {
        SharedPreferences ozner = Init(mycontext);
        if (ozner != null) {
            String server = ozner.getString(ServerAddress, null);
            if (server == null) {
                SharedPreferences.Editor etozner = InitEditor(mycontext);
//                etozner.putString(ServerAddress, "http://app.ozner.net:888/");
                etozner.putString(ServerAddress, "http://192.168.173.241/");
//                return "http://app.ozner.net:888/";
                return "http://192.168.173.241/";
            } else
                return server;
        }
//        return "http://app.ozner.net:888/";
        return "http://192.168.173.241/";
    }
}

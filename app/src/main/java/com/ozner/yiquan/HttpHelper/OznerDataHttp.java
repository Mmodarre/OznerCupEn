package com.ozner.yiquan.HttpHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Login.LoginActivity;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by C-sir@hotmail.com  on 2015/12/4.
 * 浩泽获取网络数据对象
 */
public class OznerDataHttp {
    /*
    * 获取服务器用户信息
    * */
    public static NetUserInfo RefreshUserInfo(Context context) {
        NetUserInfo netUserInfo = new NetUserInfo();
        netUserInfo.state = -1;
        String usertoken = OznerPreference.UserToken(context);
        if (usertoken == null || usertoken.length() <= 0) {
            return netUserInfo;
        }
        netUserInfo.state = 0;
        String url = OznerPreference.ServerAddress(context) + "/OznerServer/GetUserInfo";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, usertoken));
        params.add(new BasicNameValuePair("", ""));
        org.json.JSONObject response = CsirHttp.postGetJsonObject(url, params, 3000);
        if (response != null) {
            try {
                netUserInfo.state = Integer.parseInt(response.get("state").toString());
                netUserInfo.userinfo = response;
            } catch (Exception ex) {
                netUserInfo.state = 0;
            }
        }
        return netUserInfo;
    }

    /*
  * 获取服务器用户信息
  * */
    public static NetUserInfo RefreshUserInfo(Context context, String usertoken) {
        NetUserInfo netUserInfo = new NetUserInfo();
        netUserInfo.state = 0;
        String url = OznerPreference.ServerAddress(context) + "/OznerServer/GetUserInfo";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, usertoken));
        org.json.JSONObject response = CsirHttp.postGetJsonObject(url, params, 3000);
        if (response != null) {
            try {
                netUserInfo.state = Integer.parseInt(response.get("state").toString());
                netUserInfo.userinfo = response;
            } catch (Exception ex) {
                netUserInfo.state = 0;
            }

        }
        return netUserInfo;
    }

    /*
    * 获取服务器设备列表
    * */
    public static NetDeviceList RefreshDeviceList(Context context) {
        NetDeviceList netUserInfo = new NetDeviceList();
        netUserInfo.state = 0;
        String usertoken = OznerPreference.UserToken(context);
        if (usertoken == null || usertoken.length() <= 0) {
            return netUserInfo;
        }
        String url = OznerPreference.ServerAddress(context) + "/OznerServer/GetDeviceList";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, usertoken));
        params.add(new BasicNameValuePair("", ""));
        org.json.JSONObject response = CsirHttp.postGetJsonObject(url, params, 3000);
        if (response != null) {
            try {
                netUserInfo.state = Integer.parseInt(response.get("state").toString());
                netUserInfo.setDevicelist(response.getJSONArray("data").toString());
            } catch (Exception ex) {
                netUserInfo.state = 0;
            }
        }
        return netUserInfo;
    }

    /*
    *
    * 删除设备
    * */
    public static int DeleteDevice(Context context, String mac) {
        int state = 0;
        String usertoken = OznerPreference.UserToken(context);
        if (usertoken == null || usertoken.length() <= 0) {
            return 0;
        }
        String url = OznerPreference.ServerAddress(context) + "/OznerServer/DeleteDevice";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, usertoken));
        params.add(new BasicNameValuePair("Mac", mac));
        org.json.JSONObject response = CsirHttp.postGetJsonObject(url, params, 3000);
        if (response != null) {
            try {
                state = Integer.parseInt(response.get("state").toString());
            } catch (Exception ex) {
                state = 0;
            }
        }
        return state;
    }

    /*
    * 对应服务器接口 获取JSONObject对象
    * */
    public static NetJsonObject OznerWebServer(Context context, String url, List<NameValuePair> params) {
        NetJsonObject result = new NetJsonObject();
        result.state = 0;
//        }
        String response = CsirHttp.postGetString(url, params, 10000);
        if (response != null) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                result.state = Integer.parseInt(jsonObject.get("state").toString());
                result.value = response;
                return result;
            } catch (Exception ex) {
                result.state = 0;
                return result;
            }
        }
        switch (result.state) {
            case -10007:        //Token验证失败
                ((Activity) context).finish();
                OznerPreference.SetValue(context, UserDataPreference.UserId, null);
                Intent mainIntent = new Intent(((Activity) context), LoginActivity.class);
                ((Activity) context).startActivity(mainIntent);
                ((Activity) context).finish();
                break;
        }
        return result;
    }

    /*
  * 对应服务器接口 获取JSONObject对象
  * */
    public static NetJsonObject OznerWebServerForFilter(Context context, String url, List<NameValuePair> params, int timeout_mills) {
        NetJsonObject result = new NetJsonObject();
        result.state = 0;
//        }
        String response = CsirHttp.postGetString(url, params, timeout_mills);
        if (response != null) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                result.state = Integer.parseInt(jsonObject.get("state").toString());
                result.value = response;
                return result;
            } catch (Exception ex) {
                result.state = 0;
                return result;
            }
        }
        switch (result.state) {
            case -10007:        //Token验证失败
                ((Activity) context).finish();
                OznerPreference.SetValue(context, UserDataPreference.UserId, null);
                Intent mainIntent = new Intent(((Activity) context), LoginActivity.class);
                ((Activity) context).startActivity(mainIntent);
                ((Activity) context).finish();
                break;
        }
        return result;
    }
}
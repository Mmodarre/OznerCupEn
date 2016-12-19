package com.ozner.qianye.Command;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by C-sir@hotmail.com on 2015/12/3.
 */
public class UserDataPreference {
    //用户缓存网络任务
    public final static String NetWorks = "networks";

    public final static String UserData = "userdata";
    public final static String UserId = "UserId";
    public final static String ClientId = "ClientId";
    public final static String Mobile = "Mobile";
    public final static String NickName = "NickName";
    public final static String Sex = "Sex";
    public final static String Birthday = "Birthday";
    public final static String Height = "Height";
    public final static String Weight = "Weight";
    public final static String ImgPath = "ImgPath";
    public final static String IsEasySweat = "IsEasySweat";
    public final static String IsBind = "IsBind";
    public final static String UserTalkCode = "UserTalkCode";
    public final static String Version = "Version";
    public final static String WaterInTake = "WaterInTake";//饮水量
    //
    public final static String NetCahceWorkTableName = "NetCahceWorkTableName";

    //今日状态
    public final static String GanMao = "GanMao";
    public final static String SportSweat = "SportSweat";
    public final static String HotWeather = "HotWeather";
    public final static String MenstrualComing = "MenstrualComing";
    public final static String MobileRemind = "MobileRemind";
    //百度推送相关信息
    public final static String Device_ID = "device_id";
    public final static String APP = "5";
    public final static String BaiduDeviceId = "BaiduDeviceId";//百度设备号
    public final static String hasUpdateUserInfo = "hasUpdateUserInfo";//是否已经更新用户信息
    public final static String NewChatmsgCount = "newChatmsgCount";//未处理的聊天数量
    //咨询
    public final static String ChatUserKfId = "ChatUserKfId";//咨询客服id
    public final static String ChatKfIdSaveTime = "ChatKfIdSaveTime";//上次客服id保存的时间
    //个人中心
    public final static String CenterNotify = "centernotify";//个人中心通知状态

    public final static String MAC = "MAC";//滤芯状态信息

    public final static String TempUnit = "tempUnit";//温度计量单位
    public final static String VolUnit = "volUnit";//水量计量单位
    public final static String isAllowPushMsg = "isAllowPushMsg";//是否允许推送消息
    public final static String ClassifiedRankList = "ClassifiedRankList";//个人中心我的排名

    public static SharedPreferences Init(Context context) {
        if (context != null) {
            String file = OznerPreference.GetValue(context, UserDataPreference.UserId, "Oznerser");
            return context.getSharedPreferences(file, Context.MODE_PRIVATE);
        } else
            return null;
    }

    public static void SaveUserData(Context context, JSONObject jsonObject) {
        if (context != null) {
            SharedPreferences sp = Init(context);
            SharedPreferences.Editor et = sp.edit();
            if (jsonObject != null) {
                Iterator it = jsonObject.keys();
                while (it.hasNext()) {
                    try {
                        String key = (String) it.next();

                        String value = jsonObject.getString(key);
                        if (value.equals("null"))
                            value = "";
                        et.putString(key, value);
                        if (key.equals("Icon")) {
                            OznerCommand.SaveHeadImageNetCacheWork(context, value);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            et.commit();
        }
    }

    public static String GetUserData(Context context, String key, String value) {
        if (context != null) {
            SharedPreferences sp = Init(context);
            String value2 = sp.getString(key, value);
            if (value2 != null) {
                if (value2.equals("null"))
                    return value;
                else
                    return value2;
            }
            return value2;
        } else {
            return value;
        }
    }

    public static void SetUserData(Context context, String key, String value) {
        if (context != null) {
            SharedPreferences sp = Init(context);
            SharedPreferences.Editor et = sp.edit();
            et.putString(key, value);
            et.commit();
        }
    }
}

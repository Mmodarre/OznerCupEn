package com.ozner.cup.Command;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ozner.cup.ACSqlLite.CCacheWorking;
import com.ozner.cup.ACSqlLite.CSqlCommand;
import com.ozner.cup.HttpHelper.NetDevice;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.NetUserHeadImg;
import com.ozner.cup.HttpHelper.NetUserVfMessage;
import com.ozner.cup.HttpHelper.NetWeather;
import com.ozner.cup.HttpHelper.OznerDataHttp;
import com.ozner.cup.Login.LoginActivity;
import com.ozner.cup.R;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by C-sir@hotmail.com on 2015/12/3.
 * 公用操作方法，公用接口访问方法
 */
public class OznerCommand {

    public static boolean isAppRunBackound(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.processName.equals(context.getPackageName())) {
                if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;

    }

    /**
     * 判断某个界面是否在前台
     *
     * @param context
     * @param className 某个界面名称
     */
    public static boolean isAppRunBackound(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }

        return false;
    }

    public static void setAppRunForegroud(Context context) {
        Log.e("tag", "设置为前台模式");
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.processName.equals(context.getPackageName())) {
                if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    appProcessInfo.importance = ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
                    break;
                }
            }
        }

    }

    /**
     * 获取库Phon表字段
     **/
    private static final String[] PHONES_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Contacts.Photo.PHOTO_ID, ContactsContract.CommonDataKinds.Phone.CONTACT_ID};

    private static void ShowLoginPage(Activity activity) {
        OznerPreference.SetValue(activity, UserDataPreference.UserId, null);
        Intent mainIntent = new Intent(activity, LoginActivity.class);
        activity.startActivity(mainIntent);
        activity.finish();
    }

    public static void LoginOut(Activity activity) {
        OznerPreference.SetValue(activity, UserDataPreference.UserId, null);
        OznerPreference.SetValue(activity, OznerPreference.UserToken, null);
        ShowLoginPage(activity);
    }

    public static void NotfiyTixing(String mac) {

    }

    public static void CheckTokenAndInitUserData(final Activity activity) {
        //执行网络请求
        //Post参数对象
        String usertoken = OznerPreference.UserToken(activity);
        if (usertoken == null || usertoken.length() <= 0) {
            ShowLoginPage(activity);
            return;
        }
        RequestParams params = new RequestParams();
        params.put(OznerPreference.UserToken, usertoken);
        //网络请求对象
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(1);
        String url = OznerPreference.ServerAddress(activity) + "/OznerServer/GetUserInfo";
        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(activity, activity.getString(R.string.access_net_err), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, org.json.JSONObject response) {
                //解析结果
                int state = 0;
                try {
                    state = Integer.parseInt(response.get("state").toString());
                } catch (Exception ex) {
                    state = 0;
                }
                //网络操作成功
                if (state > 0) {
                    try {
                        JSONObject userinfo = response.getJSONObject("userinfo");
                        String userid = userinfo.getString(UserDataPreference.UserId);
                        OznerPreference.SetValue(activity, UserDataPreference.UserId, userid);
                        UserDataPreference.SaveUserData(activity, userinfo);
                    } catch (Exception ex) {
                        ex.getMessage();
                    }

                } else {
                }
            }
        });
        //结束网络请求
    }

    /**
     * 绑定设备到网络  缓存网络任务
     */
    public static void CNetCacheBindDeviceTask(final Context context, OznerDevice device) {
        try {
            //执行缓存任务标识ACTION
            NetCacheWork netCacheWork = new NetCacheWork();
            netCacheWork.action = CCacheWorking.WorkAction.AddDevice;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Mac", device.Address());
            jsonObject.put("Name", device.getName());
            jsonObject.put("DeviceType", device.Type());
            jsonObject.put("Settings", device.Setting().toString());
            jsonObject.put("AppData", device.getDeviceAppdata());
            //执行任务所需要的约定数据
            netCacheWork.data = jsonObject.toString();
            //添加到缓存网络任务池
            CSqlCommand.getInstance().AddNetCacheWorks(context, netCacheWork);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 解除设备到网络  缓存网络任务
     */
    public static void CNetCacheDeleteBindDeviceTask(final Context context, OznerDevice device) {
        try {
            //执行缓存任务标识ACTION
            NetCacheWork netCacheWork = new NetCacheWork();
            netCacheWork.action = CCacheWorking.WorkAction.DeleteDevice;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Mac", device.Address());
            //执行任务所需要的约定数据
            netCacheWork.data = jsonObject.toString();
            //添加到缓存网络任务池
            CSqlCommand.getInstance().AddNetCacheWorks(context, netCacheWork);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
    * 获取用户头像用户信息
    * */
    public static NetUserHeadImg InitUserHeadImg(final Activity activity) {
        NetUserHeadImg netUserHeadImg = new NetUserHeadImg();
        String Mobile = UserDataPreference.GetUserData(activity, UserDataPreference.Mobile, null);
        if (Mobile != null) {
            String url = OznerPreference.ServerAddress(activity) + "/OznerServer/GetUserNickImage";
            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
            params.add(new BasicNameValuePair("jsonmobile", Mobile));
            NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
            if (netJsonObject.state > 0) {
                JSONObject result = netJsonObject.getJSONObject();
                UserDataPreference.SaveUserData(activity, result);
                netUserHeadImg.fromJSONobject(result);
                return netUserHeadImg;
            }
        }
        netUserHeadImg.fromPreference(activity);
        return netUserHeadImg;
    }

    //*
    // 获取手机验证码
    // *//
    public static NetJsonObject GetPhoneCode(final Activity activity, String phone) {
        String url = OznerPreference.ServerAddress(activity) + "/OznerServer/GetPhoneCode";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", phone));
        return OznerDataHttp.OznerWebServer(activity, url, params);
    }

    /*
    * 获取本地通讯录好友
    * */
    public static List<NetUserHeadImg> InitLocalPhoneHeadImg(final Activity activity, String phone) {
        if (phone == null) {
            Log.e("tag", "InitLocalPhoneHeadImg");
            phone = GetLocalPhoneNumber(activity);
            Log.e("tag", "InitLocalPhoneHeadImg_phone:" + phone);
        }
        if (phone != null) {
            String url = OznerPreference.ServerAddress(activity) + "/OznerServer/GetUserNickImage";
            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
            params.add(new BasicNameValuePair("jsonmobile", phone));
            NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
            if (netJsonObject.state > 0) {
                try {
                    JSONObject result = netJsonObject.getJSONObject();
                    JSONArray data = result.getJSONArray("data");
                    List<NetUserHeadImg> listnet = new ArrayList<NetUserHeadImg>();
                    for (int i = 0; i < data.length(); i++) {
                        NetUserHeadImg netUserHeadImg = new NetUserHeadImg();
                        netUserHeadImg.fromJSONobject(data.getJSONObject(i));
                        listnet.add(netUserHeadImg);
                    }
                    return listnet;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 接受用户的验证消息
     */
    public static NetJsonObject AcceptUserVerif(final Activity activity, NetUserVfMessage netUserVfMessage) {
        List<NetUserVfMessage> listresult = new ArrayList<NetUserVfMessage>();
        String url = OznerPreference.ServerAddress(activity) + "/OznerServer/AcceptUserVerif";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
        params.add(new BasicNameValuePair("id", String.valueOf(netUserVfMessage.ID)));
        NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
        return netJsonObject;
    }

    /**
     * 发送验证消息
     * 需要填充部分NetUserVfMessage 成员
     */
    public static NetJsonObject AddFriend(final Activity activity, NetUserVfMessage netUserVfMessage) {
        List<NetUserVfMessage> listresult = new ArrayList<NetUserVfMessage>();
        String url = OznerPreference.ServerAddress(activity) + "/OznerServer/AddFriend";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
        params.add(new BasicNameValuePair("mobile", String.valueOf(netUserVfMessage.FriendMobile)));
        params.add(new BasicNameValuePair("content", String.valueOf(netUserVfMessage.RequestContent)));
        NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
        return netJsonObject;
    }

    /**
     * 获取验证消息列表
     */
    public static List<NetUserVfMessage> GetUserVerifMessage(final Activity activity) {
        List<NetUserVfMessage> listresult = new ArrayList<NetUserVfMessage>();
        String url = OznerPreference.ServerAddress(activity) + "/OznerServer/GetUserVerifMessage";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
        NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
        if (netJsonObject.state > 0) {
//            Log.e("tag", "verifyMsgResult:" + netJsonObject.value);
            try {
                JSONArray data = netJsonObject.getJSONObject().getJSONArray("msglist");
                for (int i = 0; i < data.length(); i++) {
                    try {
                        NetUserVfMessage netUserVfMessage = new NetUserVfMessage();
                        JSONObject jsonObject = data.getJSONObject(i);
                        if (netUserVfMessage.fromJSONobject(jsonObject)) {
                            listresult.add(netUserVfMessage);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        }
        return listresult;
    }

    /**
     * 更新用户信息
     * 调此方法前应将信息切入@UserDataPreference
     */
    public static NetJsonObject UpdateUserInfo(final Activity activity) {
        String url = OznerPreference.ServerAddress(activity) + "/OznerServer/UpdateUserInfo";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
        params.add(new BasicNameValuePair(UserDataPreference.Device_ID, UserDataPreference.GetUserData(activity, UserDataPreference.Device_ID, "")));
        params.add(new BasicNameValuePair("channel_id", "5"));
        NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
        return netJsonObject;
    }

    /**
     * 上传TDS获取排名
     */
    public static NetJsonObject TDSSensor(final Context activity, String mac, String type, String tds) {

        List<NetUserVfMessage> listresult = new ArrayList<NetUserVfMessage>();
        String url = OznerPreference.ServerAddress(activity) + "/OznerDevice/TDSSensor";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
        params.add(new BasicNameValuePair("mac", mac));
        params.add(new BasicNameValuePair("type", type));
        params.add(new BasicNameValuePair("tds", tds));
        NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
        return netJsonObject;
    }

    /**
     * 上传水探头的tds值
     */
    public static NetJsonObject TapTDSSensor(final Context activity, String mac, String type, String tds) {
        List<NetUserVfMessage> listresult = new ArrayList<NetUserVfMessage>();
        String url = OznerPreference.ServerAddress(activity) + "GetPost/OznerDevice/AsyncRecordTapTds";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
        params.add(new BasicNameValuePair("mac", mac));
        params.add(new BasicNameValuePair("type", type));
        params.add(new BasicNameValuePair("tds", tds));
        NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
        return netJsonObject;
    }

    //上传固件版本号获取下载地址
    public static NetJsonObject GetFirmwareUrl(final Context activity, String type, String version) {
        List<NetUserVfMessage> listresult = new ArrayList<NetUserVfMessage>();
        String url = OznerPreference.ServerAddress(activity) + "/OznerDevice/GetDeviceUpdate";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("type", type));
        params.add(new BasicNameValuePair("version", version));
        NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
        return netJsonObject;
    }

    /**
     * 获取滤芯服务
     */
    public static NetJsonObject FilterService(final Activity activity, String mac) {
        List<NetUserVfMessage> listresult = new ArrayList<NetUserVfMessage>();
        String url = OznerPreference.ServerAddress(activity) + "/OznerDevice/FilterService";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
        params.add(new BasicNameValuePair("mac", mac));
        NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
        return netJsonObject;
    }

    /**
     * 绑定设备
     */
    public static NetJsonObject AddDevice(final Activity activity, String Mac, String Name, String DeviceType, String DeviceAddress, String Weight) {
        List<NetUserVfMessage> listresult = new ArrayList<NetUserVfMessage>();
        String url = OznerPreference.ServerAddress(activity) + "/OznerServer/AddDevice";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
        params.add(new BasicNameValuePair("Mac", Mac));
        params.add(new BasicNameValuePair("DeviceType", DeviceType));
        params.add(new BasicNameValuePair("DeviceAddress", DeviceAddress));
        params.add(new BasicNameValuePair("Weight", Weight));
        NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
        return netJsonObject;
    }

    /**
     * 绑定设备
     */
    public static NetJsonObject AddDeviceV2(final Context activity, String Mac, String Name, String DeviceType, String Settings, String AppData) {
        List<NetUserVfMessage> listresult = new ArrayList<NetUserVfMessage>();
        String url = OznerPreference.ServerAddress(activity) + "/OznerServer/AddDevice";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
        params.add(new BasicNameValuePair("Mac", Mac));
        params.add(new BasicNameValuePair("Name", Name));
        params.add(new BasicNameValuePair("DeviceType", DeviceType));
        params.add(new BasicNameValuePair("Settings", Settings));
        params.add(new BasicNameValuePair("AppData", AppData));
        NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
        return netJsonObject;
    }

    /**
     * 解除绑定设备
     */
    public static NetJsonObject DeleteDevice(final Context context, String Mac) {
        List<NetUserVfMessage> listresult = new ArrayList<NetUserVfMessage>();
        String url = OznerPreference.ServerAddress(context) + "/OznerServer/DeleteDevice";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(context)));
        params.add(new BasicNameValuePair("Mac", Mac));
        return OznerDataHttp.OznerWebServer(context, url, params);
    }

    /**
     * 得到手机通讯录联系人信息
     **/
//    protected static String GetLocalPhoneNumber(final Activity activity){
//        StringBuilder sb = new StringBuilder();
//        ContentResolver resolver = activity.getContentResolver();
//        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,PHONES_PROJECTION,null,null,null);
//        if(phoneCursor!=null){
//            while (phoneCursor.moveToNext()){
//                String phoneNumber = phoneCursor.getString(1);
//                sb.append(phoneNumber+",");
//            }
//        }
//        return null;
//    }
    protected static String GetLocalPhoneNumber(final Activity activity) {
        try {
            PackageManager pm = activity.getPackageManager();
            boolean permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.READ_CONTACTS", activity.getPackageName()));
            Log.e("tag", "ReadContacts:" + pm.checkPermission("android.permission.READ_CONTACTS", activity.getPackageName()));
            if (permission) {
                StringBuilder sb = new StringBuilder();
                ContentResolver resolver = activity.getContentResolver();
                Uri uri = Uri.parse("content://com.android.contacts/contacts");
                Cursor cursor = resolver.query(uri, new String[]{"_id"}, null, null, null);
                while (cursor.moveToNext()) {
                    int contractID = cursor.getInt(0);
                    uri = Uri.parse("content://com.android.contacts/contacts/" + contractID + "/data");
                    Cursor cursor1 = resolver.query(uri, new String[]{"mimetype", "data1", "data2"}, null, null, null);
                    while (cursor1.moveToNext()) {
                        String data1 = cursor1.getString(cursor1.getColumnIndex("data1"));
                        String mimeType = cursor1.getString(cursor1.getColumnIndex("mimetype"));

                        if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) { //手机
                            sb.append(data1.replace(" ", "") + ",");
                        }
                    }
                    cursor1.close();
                }
                cursor.close();

                if (sb != null && sb.toString().length() > 0) {
                    return sb.toString().substring(0, sb.toString().length() - 1);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    * 获取天气
    * */
    public static NetWeather GetWeather(final Activity activity) {

        String url = OznerPreference.ServerAddress(activity) + "/OznerServer/GetWeather";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
        NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
        NetWeather weather = new NetWeather();
        try {
            JSONObject j = netJsonObject.getJSONObject();
            if (weather.fromJSONObject(j)) {
                return weather;
            } else {
                return null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * @context context
     * @mac 设备地址
     * @type 设备类型
     * @volume 饮水量
     */
    public static NetJsonObject VolumeSensor(final Context context, final String mac, final String type, final int volume) {
        NetJsonObject jsonObject = new NetJsonObject();
        if (context != null) {
            String url = OznerPreference.ServerAddress(context) + "/OznerDevice/VolumeSensor";
            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(context)));
            params.add(new BasicNameValuePair("mac", mac));
            params.add(new BasicNameValuePair("type", type));
            params.add(new BasicNameValuePair("volume", String.valueOf(volume)));
            return OznerDataHttp.OznerWebServer(context, url, params);
        } else {
            jsonObject.state = 0;
            return jsonObject;
        }
    }

    /**
     * 登陆
     */
    public static NetJsonObject Login(final Activity activity, String phone, String code) {
        String url = OznerPreference.ServerAddress(activity) + "/OznerServer/Login";
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("UserName", phone));
        params.add(new BasicNameValuePair("PassWord", code));
        params.add(new BasicNameValuePair("miei", getImie(activity)));
        params.add(new BasicNameValuePair("devicename", android.os.Build.MANUFACTURER));
        return OznerDataHttp.OznerWebServer(activity, url, params);
    }

    /**
     * 获取邮箱验证码
     *
     * @param context
     * @param email
     * @param callback
     */
    public static void getEmailCode(final Context context, String email, HttpCallback callback) {
        String url = OznerPreference.ServerAddress(context) + "OznerServer/GetEmailCode";
        List<NameValuePair> httpParms = new ArrayList<>();
        httpParms.add(new BasicNameValuePair("email", email));
        new NormalAsyncTask(context, url, httpParms, callback).execute();
    }

    /**
     * 邮箱注册
     *
     * @param context
     * @param email
     * @param password
     * @param code
     * @param callback
     */
    public static void mailRegister(final Context context, String email, String password, String code, HttpCallback callback) {
        String url = OznerPreference.ServerAddress(context) + "OznerServer/MailRegister";
        List<NameValuePair> httpParms = new ArrayList<>();
        httpParms.add(new BasicNameValuePair("username", email));
        httpParms.add(new BasicNameValuePair("password", password));
        httpParms.add(new BasicNameValuePair("code", code));

        new NormalAsyncTask(context, url, httpParms, callback).execute();
    }

    /**
     * 重置密码
     *
     * @param context
     * @param email
     * @param password
     * @param code
     * @param callback
     */
    public static void resetPwd(final Context context, String email, String password, String code, HttpCallback callback) {
        String url = OznerPreference.ServerAddress(context) + "OznerServer/ResetPassword";
        List<NameValuePair> httpParms = new ArrayList<>();
        httpParms.add(new BasicNameValuePair("username", email));
        httpParms.add(new BasicNameValuePair("password", password));
        httpParms.add(new BasicNameValuePair("code", code));

        new NormalAsyncTask(context, url, httpParms, callback).execute();
    }

    /**
     * 邮箱登录
     *
     * @param context
     * @param email
     * @param password
     */
    public static NetJsonObject mailLogin(final Context context, String email, String password) {
        String url = OznerPreference.ServerAddress(context) + "OznerServer/MailLogin";
        List<NameValuePair> httpParms = new ArrayList<>();
        httpParms.add(new BasicNameValuePair("username", email));
        httpParms.add(new BasicNameValuePair("password", password));
        httpParms.add(new BasicNameValuePair("miei", getImie(context)));
        httpParms.add(new BasicNameValuePair("devicename", android.os.Build.MANUFACTURER));
//        new NormalAsyncTask(context, url, httpParms, callback).execute();
        return OznerDataHttp.OznerWebServer(context, url, httpParms);
    }

    /**
     * 获取网络设备列表
     */
    public static List<NetDevice> GetNetDeviceList(final Context context) {
        NetJsonObject jsonObject = new NetJsonObject();
        List<NetDevice> netDeviceList = new ArrayList<NetDevice>();
        if (context != null) {
            String url = OznerPreference.ServerAddress(context) + "/OznerServer/GetDeviceList";
            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(context)));
            jsonObject = OznerDataHttp.OznerWebServer(context, url, params);
        }
        if (jsonObject != null && jsonObject.state > 0) {
            JSONArray devices = null;
            try {
                devices = jsonObject.getJSONObject().getJSONArray("data");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (devices != null) {
                for (int i = 0; i < devices.length(); i++) {
                    NetDevice netDevice = new NetDevice();
                    JSONObject d = null;
                    try {

                        d = devices.getJSONObject(i);
                        netDevice.Mac = d.getString("Mac");
                        netDevice.DeviceType = d.getString("DeviceType");
                        if (d.get("AppData") != null) {
                            netDevice.AppData = d.getString("AppData");
                        }
                        if (d.get("Settings") != null) {
                            netDevice.Settings = d.getString("Settings");
                        }
                        netDeviceList.add(netDevice);
                    } catch (Exception ex) {
                        continue;
                    }

                }
            }
        }
        return netDeviceList;
    }

    /**
     * DP CONVERT TO PX
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * Umeng TEST
     */
    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = null;
            try {
                device_id = tm.getDeviceId();
            } catch (Exception e) {
            }

            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);

            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }

            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            }

            json.put("device_id", device_id);

            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设备和网络数据库同步
     */
    public static void DeviceNetWorkAsync(final Context context) {
        OznerDevice[] localDevice = OznerDeviceManager.Instance().getDevices();
        List<NetDevice> netDeviceList = GetNetDeviceList(context);
        if (netDeviceList.size() > 0) {
            for (NetDevice netDevice : netDeviceList) {

                Boolean localhas = false;
                for (OznerDevice oznerDevice : localDevice) {
                    if (oznerDevice.Address().equals(netDevice.Mac)) {
                        localhas = true;
                    }
                }
                if (!localhas) {

                    OznerDeviceManager.Instance().save(netDevice.Mac, netDevice.DeviceType, netDevice.Settings, netDevice.AppData);
                }
            }
        }
    }

    //**缓存网络任务
    public static JSONArray GetCacheNetWorks(final Context context) {
        JSONArray jsonworks = null;
        String value = UserDataPreference.GetUserData(context, UserDataPreference.NetWorks, null);
        if (value != null) {
            try {
                jsonworks = new JSONArray(value);
            } catch (Exception ex) {
                jsonworks = null;
            }

        }
        return jsonworks;
    }

    //执行网络任务
    public static void DoCacheNetWorks(final Context context) {
        synchronized (CSqlCommand.getInstance()) {
            if (context != null) {
                //获取缓存网络任务
                List<NetCacheWork> listnetworks = CSqlCommand.getInstance().GetNetCacheWorks(context);
                if (listnetworks != null) {
                    for (NetCacheWork work : listnetworks) {
                        //Action 为操作标识
                        switch (work.action) {
                            case "BindDevice":
                                //此处应该调用异步，或者多线程执行网络操作

                                //网络操作执行结束而且执行成功，删除本地缓存网络任务
                                CSqlCommand.getInstance().RemoveNetCacheWorks(context, work);
                                break;
                            case "DeleteDevice":
                                break;
                        }

                    }
                }

            }
        }
    }


    //
    public static void SaveHeadImageNetCacheWork(final Context context, String url) {
        NetCacheWork netCacheWork = new NetCacheWork();
        netCacheWork.action = CCacheWorking.WorkAction.GetImage;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("url", url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        netCacheWork.data = jsonObject.toString();
        CSqlCommand.getInstance().AddNetCacheWorks(context, netCacheWork);
    }

    public static Bitmap GetUserHeadImage(final Context context) {
        try {
            if (context != null) {

                File tempFile = new File(Environment.getExternalStorageDirectory()
                        .getPath() + "/OznerImage/Cache" + "/"
                        + UserDataPreference.GetUserData(context, UserDataPreference.UserId, "OznerUser") + ".jpg"); // 以时间秒为文件名
                if (tempFile.exists()) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(tempFile.getPath(), opts);
                    try {
                        return BitmapFactory.decodeFile(tempFile.getPath(), opts);
                    } catch (OutOfMemoryError e) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.default_head);
                        return bitmapDrawable.getBitmap();
                    }
                } else {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.default_head);
                    return bitmapDrawable.getBitmap();
                }
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 获取手机mac地址<br/>
     * 错误返回12个0
     */
    public static String getImie(Context context) {
        // 获取mac地址：
        try {
            TelephonyManager telephonemanage = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonemanage.getDeviceId();
        } catch (Exception ex) {
            return UUID.randomUUID().toString();
        }
    }

    public interface HttpCallback {
        void HandleResult(NetJsonObject result);
    }

    //通用异步请求任务
    private static class NormalAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        private Context mContext;
        private HttpCallback httpCallback;
        private String httpUrl;
        private List<NameValuePair> httpParms;

        public NormalAsyncTask(Context context, String httpUrl, List<NameValuePair> httpParms, HttpCallback callback) {
            this.mContext = context;
            this.httpCallback = callback;
            this.httpUrl = httpUrl;
            this.httpParms = httpParms;
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            return OznerDataHttp.OznerWebServer(mContext, httpUrl, httpParms);
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (httpCallback != null) {
                httpCallback.HandleResult(netJsonObject);
            }
        }
    }
}

package com.ozner.cup.Guide;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.Window;

import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.HttpHelper.NetDeviceList;
import com.ozner.cup.Login.LoginActivity;
import com.ozner.cup.Login.LoginEnActivity;
import com.ozner.cup.MainActivity;
import com.ozner.cup.R;

/**
 * Created by taoran on 2015/11/16.欢迎页
 * Modify by C-sir@hotmail.com
 */


public class WelcomeActivity extends Activity {
    private final int SPLASH_DISPLAY_LENGHT = 500;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isFirst;//程序是否第一次加载的标记
    String deviceid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.guideColor));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.guideColor));
        }
//        ShareSDK.initSDK(this);
//        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, getString(R.string.Baidu_Push_ApiKey));
//        PushSettings.enableDebugMode(getApplicationContext(), true);
//
//        Log.e("BaiduPush", "isPushEnabled:" + PushManager.isPushEnabled(this));
//        deviceid = UserDataPreference.GetUserData(this, UserDataPreference.BaiduDeviceId, null);
//        Log.e("BaiduPush", "deviceid:" + deviceid);
//        if(deviceid==null){
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (null == deviceid) {
//                    PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, getString(R.string.Baidu_Push_ApiKey));
//                }
//            }
//        }, 2000);
//        }
//        if (deviceid != null && UserDataPreference.GetUserData(this, UserDataPreference.hasUpdateUserInfo, "false").equals("false")) {
//            sendBroadcast(new Intent(OznerBroadcastAction.UpdateUserInfo));
//        }
//        sharedPreferences = getSharedPreferences("MyState", MODE_PRIVATE);
//        editor = sharedPreferences.edit();
//        isFirst = sharedPreferences.getBoolean("isFirst", true);
//        try {
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (isFirst) {
//                        editor.putBoolean("isFirst", false);
//                        editor.commit();
//                        ShowGuidePage();
//                    } else {
//                        Thread t = new Thread(new Runnable() {
//                            public void run() {
//                                NetUserInfo netUserInfo = OznerDataHttp.RefreshUserInfo(getBaseContext());
//                                //初始化用户头像
//                                OznerCommand.InitUserHeadImg(WelcomeActivity.this);
//                                //网络操作成功
//                                if (netUserInfo.state > 0) {
//                                    try {
//                                        JSONObject userinfo = netUserInfo.userinfo.getJSONObject("userinfo");
//                                        String userid = userinfo.getString(UserDataPreference.UserId);
//                                        OznerPreference.SetValue(getBaseContext(), UserDataPreference.UserId, userid);
//                                        UserDataPreference.SaveUserData(getBaseContext(), userinfo);
//                                        try {
//                                            Handler handler=new Handler(Looper.getMainLooper())
//                                            {
//                                                @Override
//                                                public void handleMessage(Message msg) {
//                                                    String token = OznerPreference.UserToken(getBaseContext());
//                                                    OznerDeviceManager.Instance().setOwner(msg.obj.toString(),token);
//                                                    try {
//                                                        CSqlCommand.getInstance().SetTableName(getBaseContext(), "N" + msg.obj.toString().replace("-",""));
//                                                    }catch (Exception ex)
//                                                    {
//                                                        ex.printStackTrace();
//                                                    }
//                                                    super.handleMessage(msg);
//                                                }
//                                            };
//                                            Message m=new Message();
//                                            m.obj=userid;
//                                            handler.sendMessage(m);
//                                        }catch (Exception ex)
//                                        {ex.printStackTrace();}
//                                        //初始化用户设备列表
////                                        NetDeviceList netDeviceList=OznerDataHttp.RefreshDeviceList(getBaseContext());
//                                        ShowMainPage(null);
//                                        stopService(new Intent(WelcomeActivity.this,LoginService.class));
//                                    } catch (Exception ex) {
//                                        ex.getMessage();
//                                        ShowLoginPage();
//                                    }
//                                } else if(netUserInfo.state==0){
//                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(getBaseContext(), "网络请求失败,请检查网络状态", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                                    ShowMainPage(null);
//                                }
//                                else {
//                                    ShowLoginPage();
//                                    return;
//                                }
//                            }
//                        });
//                        t.start();
//                    }
//
//                }
//
//            }, SPLASH_DISPLAY_LENGHT);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                ShowMainPage(null);
                if (OznerApplication.isLanguageCN()) {
                    ShowLoginPage();
                } else {
                    ShowLoginEnPage();

                }

            }
        }, 2000);
    }

    /*
    *中文版登录界面
     */
    private void ShowLoginPage() {
        OznerPreference.SetValue(getBaseContext(), UserDataPreference.UserId, null);
        Intent mainIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
        WelcomeActivity.this.startActivity(mainIntent);
        WelcomeActivity.this.finish();
    }

    /*
    *英文版登录界面
     */
    private void ShowLoginEnPage() {
        OznerPreference.SetValue(getBaseContext(), UserDataPreference.UserId, null);
        Intent mainIntent = new Intent(WelcomeActivity.this, LoginEnActivity.class);
        WelcomeActivity.this.startActivity(mainIntent);
        WelcomeActivity.this.finish();
    }

    private void ShowGuidePage() {
        OznerPreference.SetValue(getBaseContext(), UserDataPreference.UserId, null);
        Intent mainIntent = new Intent(WelcomeActivity.this, GuideActivity.class);
        WelcomeActivity.this.startActivity(mainIntent);
        WelcomeActivity.this.finish();
    }

    private void ShowMainPage(NetDeviceList devicejson) {
        Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);
//        mainIntent.putExtra("devicelist", (Serializable) devicejson);
        WelcomeActivity.this.startActivity(mainIntent);
        WelcomeActivity.this.finish();
    }
}

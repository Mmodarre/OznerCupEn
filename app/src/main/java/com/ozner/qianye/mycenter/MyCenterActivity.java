package com.ozner.qianye.mycenter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ozner.qianye.BaiduPush.OznerBroadcastAction;
import com.ozner.qianye.Command.ImageHelper;
import com.ozner.qianye.Command.OznerPreference;
import com.ozner.qianye.Command.PageState;
import com.ozner.qianye.Command.UserDataPreference;
import com.ozner.qianye.Device.OznerApplication;
import com.ozner.qianye.HttpHelper.NetJsonObject;
import com.ozner.qianye.HttpHelper.NetUserHeadImg;
import com.ozner.qianye.HttpHelper.OznerDataHttp;
import com.ozner.qianye.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class MyCenterActivity extends AppCompatActivity implements View.OnClickListener {
    private String userid;
    ImageView userImage;
    TextView userName;
    private final int USER_HEAD_INFO = 1;//
    MyCenterHandle uihandle = new MyCenterHandle();
    MyLoadImgListener imageLoadListener = new MyLoadImgListener();

    BroadcastReceiver monitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(OznerBroadcastAction.Logout)) {
                MyCenterActivity.this.finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.main_bgcolor));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.main_bgcolor));
        }
        setContentView(R.layout.activity_my_center);

        findViewById(R.id.person_infor_edit).setOnClickListener(this);
        findViewById(R.id.center_my_device).setOnClickListener(this);
        findViewById(R.id.private_message_layout).setOnClickListener(this);
        findViewById(R.id.setting_layout).setOnClickListener(this);
        userImage = (ImageView) findViewById(R.id.iv_person_photo);
        userName = (TextView) findViewById(R.id.tv_name);
        userid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, null);
        if (!((OznerApplication) getApplication()).isLoginPhone() && userid != null && userid.length() > 0) {
            String nickname = UserDataPreference.GetUserData(this, "Nickname", null);
            if (nickname != null && nickname.length() > 0) {
                userName.setText(nickname);
            } else {
                nickname = UserDataPreference.GetUserData(this, "Email", null);
                if (nickname != null && nickname.length() > 0) {
                    userName.setText(nickname);
                }
            }
        }

        IntentFilter filter = new IntentFilter(OznerBroadcastAction.Logout);
        registerReceiver(monitor, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initHeadImg();
    }

    class MyLoadImgListener extends SimpleImageLoadingListener {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(MyCenterActivity.this, loadedImage));
            super.onLoadingComplete(imageUri, view, loadedImage);
        }
    }

    //初始化个人信息
    private void initHeadImg() {
        final String url = OznerPreference.ServerAddress(this) + "/OznerServer/GetUserNickImage";
        loadUserHeadImg(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetUserHeadImg netUserHeadImg = centerInitUserHeadImg(MyCenterActivity.this, url);
                Message message = new Message();
                message.what = USER_HEAD_INFO;
                message.obj = netUserHeadImg;
                uihandle.sendMessage(message);
            }
        }).start();
    }

    private void loadUserHeadImg(final Activity activity) {
        NetUserHeadImg netUserHeadImg = new NetUserHeadImg();
        netUserHeadImg.fromPreference(activity);
        if (netUserHeadImg != null) {
            Message message = new Message();
            message.what = USER_HEAD_INFO;
            message.obj = netUserHeadImg;
            uihandle.sendMessage(message);
        }
    }

    public static NetUserHeadImg centerInitUserHeadImg(final Activity activity, final String inituserHeadUrl) {
        NetUserHeadImg netUserHeadImg = new NetUserHeadImg();
        String Mobile = UserDataPreference.GetUserData(activity, UserDataPreference.Mobile, null);
        if (Mobile != null) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(activity)));
            params.add(new BasicNameValuePair("jsonmobile", Mobile));
            NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, inituserHeadUrl, params);
            if (netJsonObject.state > 0) {
//                UserDataPreference.SetUserData(activity, inituserHeadUrl, netJsonObject.value);
                try {
                    JSONArray jarry = netJsonObject.getJSONObject().getJSONArray("data");
                    if (jarry.length() > 0) {
                        JSONObject jo = (JSONObject) jarry.get(0);
                        netUserHeadImg.fromJSONobject(jo);
                        UserDataPreference.SaveUserData(activity, jo);
                    } else {
                        netUserHeadImg.fromPreference(activity);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    netUserHeadImg.fromPreference(activity);
                }
            }
        }
        netUserHeadImg.fromPreference(activity);
        return netUserHeadImg;
    }


    class MyCenterHandle extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case USER_HEAD_INFO:
                    ImageHelper imageHelper = new ImageHelper(MyCenterActivity.this);
                    imageHelper.setImageLoadingListener(imageLoadListener);
                    NetUserHeadImg netUserHeadImg = (NetUserHeadImg) msg.obj;
                    if (netUserHeadImg != null) {
                        if (netUserHeadImg.headimg != null && netUserHeadImg.headimg.length() > 0) {
                            imageHelper.loadImage(userImage, netUserHeadImg.headimg);
                        } else {
                            //imageHelper.loadImage(iv_person_photo, "http://a.hiphotos.baidu.com/zhidao/wh%3D600%2C800/sign=10284cd567380cd7e64baaeb9174810c/63d9f2d3572c11df09ba0c46612762d0f703c268.jpg");
                            userImage.setImageResource(R.mipmap.icon_default_headimage);
                        }
                    } else {
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.person_infor_edit:
//                intent.setClass();
                break;
            case R.id.center_my_device:
                intent.setClass(this, MyFriendsActivity.class);
                startActivityForResult(intent, 0x2134);
                break;
            case R.id.private_message_layout:
                intent.setClass(this, AdviseActivity.class);
                startActivityForResult(intent, 0x2135);
                break;
            case R.id.setting_layout:
                intent.setClass(this, CenterSetupActivity.class);
                startActivityForResult(intent, 0x2136);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x2134) {
            switch (resultCode) {
                case PageState.CenterDeviceClick:
                    String centerAddress = data.getStringExtra(PageState.CENTER_DEVICE_ADDRESS);
                    Log.e("tag", "英文版_centerAddress: " + centerAddress);
                    Intent intent = new Intent(OznerBroadcastAction.EN_Center_Click);
                    intent.putExtra("Address", centerAddress);
                    sendBroadcast(intent);
                    finish();
                    break;
            }
        }
    }

    public void backUp(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(monitor);
        super.onDestroy();
    }
}

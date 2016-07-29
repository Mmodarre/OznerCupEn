package com.ozner.cup.mycenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.ozner.cup.BaiduPush.OznerBroadcastAction;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.R;

public class MyCenterActivity extends AppCompatActivity implements View.OnClickListener {
    private String userid;
    ImageView userImage;
    TextView userName;

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
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(OznerBroadcastAction.Logout)) {
                    MyCenterActivity.this.finish();
                }
            }
        }, filter);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.person_infor_edit:
//                intent.setClass();
                break;
            case R.id.center_my_device:
                intent.setClass(this, MyDeviceActivity.class);
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
}

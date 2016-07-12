package com.ozner.cup.mycenter;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.ozner.cup.R;

public class MyCenterActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView userImage;
    TextView userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.add_device));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.add_device));
        }
        setContentView(R.layout.activity_my_center);

        findViewById(R.id.person_infor_edit).setOnClickListener(this);
        findViewById(R.id.center_my_device).setOnClickListener(this);
        findViewById(R.id.private_message_layout).setOnClickListener(this);
        findViewById(R.id.setting_layout).setOnClickListener(this);
        userImage = (ImageView) findViewById(R.id.iv_person_photo);
        userName = (TextView) findViewById(R.id.tv_name);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.person_infor_edit:
//                intent.setClass();
                break;
            case R.id.center_my_device:
                intent.setClass(this,MyDeviceActivity.class);
                startActivityForResult(intent,0x2134);
                break;
            case R.id.private_message_layout:
                intent.setClass(this,AdviseActivity.class);
                startActivityForResult(intent,0x2135);
                break;
            case R.id.setting_layout:
                intent.setClass(this,CenterSetupActivity.class);
                startActivityForResult(intent,0x2136);
                break;
        }
    }
}

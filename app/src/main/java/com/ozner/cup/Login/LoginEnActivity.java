package com.ozner.cup.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.cup.MainEnActivity;
import com.ozner.cup.R;

import java.util.HashMap;

import cn.sharesdk.facebook.Facebook;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.google.GooglePlus;
import cn.sharesdk.linkedin.LinkedIn;

public class LoginEnActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginEnActivity";
    EditText et_username, et_password;
    TextView tv_register;
    Button btn_login;
    ImageButton ibtn_facebook, ibtn_googleplus, ibtn_linkedin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_en);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.guideColor));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.guideColor));
        }
        initViews();
    }

    private void initViews() {
        tv_register = (TextView) findViewById(R.id.tv_register);
        btn_login = (Button) findViewById(R.id.btn_login);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        ibtn_facebook = (ImageButton) findViewById(R.id.ibtn_facebook);
        ibtn_googleplus = (ImageButton) findViewById(R.id.ibtn_googleplus);
        ibtn_linkedin = (ImageButton) findViewById(R.id.ibtn_linkedin);
        setListener();
    }

    private void setListener() {
        tv_register.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        ibtn_linkedin.setOnClickListener(this);
        ibtn_facebook.setOnClickListener(this);
        ibtn_googleplus.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (et_username.getText().length() > 0) {
                    if (et_password.getText().length() > 0) {
                        Intent mainIntent = new Intent(LoginEnActivity.this, MainEnActivity.class);
                        LoginEnActivity.this.startActivity(mainIntent);
                        LoginEnActivity.this.finish();
                    } else {
                        Toast.makeText(LoginEnActivity.this, getString(R.string.input_password), Toast.LENGTH_SHORT).show();
                        et_password.requestFocus();
                    }
                } else {
                    Toast.makeText(LoginEnActivity.this, getString(R.string.input_username), Toast.LENGTH_SHORT).show();
                    et_username.requestFocus();
                }
                break;
            case R.id.tv_register:
                startActivity(new Intent(LoginEnActivity.this, SignUpActivity.class));
                LoginEnActivity.this.finish();
//                Toast.makeText(LoginEnActivity.this, "注册账号", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ibtn_facebook:
                ssoLogin(Facebook.NAME);
                break;
            case R.id.ibtn_googleplus:
                ssoLogin(GooglePlus.NAME);
                break;
            case R.id.ibtn_linkedin:
                ssoLogin(LinkedIn.NAME);
                break;
        }
    }

    /*
    *第三方登录
     */
    private void ssoLogin(final String platformName) {
        //初始化SDK
        ShareSDK.initSDK(this);
        ShareLoginApi ssoApi = new ShareLoginApi();
        ssoApi.setPlatform(platformName);
        ssoApi.setOnLoginListener(new OnLoginListener() {
            @Override
            public boolean onLogin(String platform, HashMap<String, Object> res) {
                // TODO: 2016/7/5 判断登录返回信息是否在用户数据库存在，存在就登录成功，否则登录失败

                Platform loginPlat = ShareSDK.getPlatform(platformName);
                logSSOLoginInfo(platform, res);
                if (loginPlat.getDb().getUserId() != null && !loginPlat.getDb().getUserId().isEmpty()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean onRegister(UserInfo info) {
                return true;
            }
        });
        ssoApi.login(this);
    }


    private void logSSOLoginInfo(String paltname, HashMap<String, Object> res) {
        Platform platform = ShareSDK.getPlatform(paltname);

        Log.e(TAG, "onComplete: UserName:" + platform.getDb().getUserName());
        Log.e(TAG, "onComplete: UserID:" + platform.getDb().getUserId());
        Log.e(TAG, "onComplete: UserIcon:" + platform.getDb().getUserIcon());
        Log.e(TAG, "onComplete: UserGender:" + platform.getDb().getUserGender());
        if (res != null) {
            for (String key : res.keySet()) {
                Log.e(TAG, String.format("handleMessage_请求结果集: %s---->%s", key, res.get(key)));
            }
        }
    }
}

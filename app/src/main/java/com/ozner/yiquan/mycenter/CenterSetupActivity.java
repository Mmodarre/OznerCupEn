package com.ozner.yiquan.mycenter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.yiquan.BaiduPush.OznerBroadcastAction;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.Device.SlidButton;
import com.ozner.yiquan.Login.LoginActivity;
import com.ozner.yiquan.Login.LoginEnActivity;
import com.ozner.yiquan.R;

/*
* Created by xinde on 2015/12/10
 */

public class CenterSetupActivity extends AppCompatActivity implements View.OnClickListener {
    RelativeLayout rlay_utilsetup, rlay_aboutOzner, rlay_Logout, allow_push;
    SlidButton sb_switch;
    //    private SharedPreferences sharePre;
//    private SharedPreferences.Editor editor;
    boolean isAllowPushMsg = true;
    private TextView toolbar_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center_setup);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
        }
//        UserDataPreference.Init(CenterSetupActivity.this);
        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_text.setText(getString(R.string.Center_Setup));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.MyCenter_ToolBar, null));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.MyCenter_ToolBar));
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CenterSetupActivity.this.finish();
            }
        });

        isAllowPushMsg = Boolean.parseBoolean(UserDataPreference.GetUserData(CenterSetupActivity.this, UserDataPreference.isAllowPushMsg, "true"));
//        rlay_back = (RelativeLayout) findViewById(R.id.rlay_back);
        rlay_utilsetup = (RelativeLayout) findViewById(R.id.rlay_utilsetup);
        rlay_aboutOzner = (RelativeLayout) findViewById(R.id.rlay_aboutOzner);
        rlay_Logout = (RelativeLayout) findViewById(R.id.rlay_Logout);
        allow_push = (RelativeLayout) findViewById(R.id.allow_push);
//        if (!((OznerApplication)getApplication()).isLanguageCN()){
        allow_push.setVisibility(View.GONE);
//        }
        sb_switch = (SlidButton) findViewById(R.id.sb_switch);
//        rlay_back.setOnClickListener(this);
        rlay_Logout.setOnClickListener(this);
        rlay_utilsetup.setOnClickListener(this);
        rlay_aboutOzner.setOnClickListener(this);
        sb_switch.SetSlidButtonState(isAllowPushMsg);
        sb_switch.SetOnChangedListener(new SlidButton.OnChangedListener() {
            @Override
            public void OnChanged(boolean checkState) {
                UserDataPreference.SetUserData(CenterSetupActivity.this, UserDataPreference.isAllowPushMsg, String.valueOf(checkState));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlay_utilsetup:
                Intent unitIntent = new Intent(this, SetUnitActivity.class);
                startActivity(unitIntent);
                break;
            case R.id.rlay_aboutOzner:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
//            case R.id.rlay_back:
//                onBackPressed();
//                break;
            case R.id.rlay_Logout:
                //   int i=3/0;
//                VibratorUtil.Vibrate(getBaseContext(),3000);
                if (OznerPreference.IsLogin(this)) {
                    AlertDialog logoutDialog = new AlertDialog.Builder(CenterSetupActivity.this).create();
                    logoutDialog.setMessage(getString(R.string.Center_ToLogOut));
                    logoutDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.ensure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            OznerPreference.SetValue(CenterSetupActivity.this, UserDataPreference.UserId, "");
                            OznerPreference.setUserToken(CenterSetupActivity.this, "");
                            ((OznerApplication) getApplication()).setIsPhone();
                            if (((OznerApplication) getApplication()).isLanguageCN()) {
                                Intent loginIntent = new Intent(CenterSetupActivity.this, LoginActivity.class);
                                startActivity(loginIntent);
                            } else {
                                Intent loginIntent = new Intent(CenterSetupActivity.this, LoginEnActivity.class);
//                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(loginIntent);
                            }
                            Intent logoutBroadIntent = new Intent(OznerBroadcastAction.Logout);
                            logoutBroadIntent.putExtra("Address", "logout");
                            sendBroadcast(logoutBroadIntent);
                            CenterSetupActivity.this.finish();
                        }
                    });
                    logoutDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    logoutDialog.show();
                } else {
                    Toast.makeText(this, getString(R.string.ShouldLogin), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

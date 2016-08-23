package com.ozner.yiquan.mycenter;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import com.ozner.yiquan.Command.Contants;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.R;
import com.ozner.yiquan.mycenter.CheckForUpdate.OznerUpdateManager;

/*
* Created by xinde on 2015/12/10
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout rlay_mark, rlay_checkVersion;
    //    RelativeLayout rlay_back;
    TextView tv_Version, tv_freeItem;
    private TextView toolbar_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        //修改字体
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
        }
        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_text.setText(getString(R.string.Center_About));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.this.finish();
            }
        });
//        rlay_back = (RelativeLayout) findViewById(R.id.rlay_back);
        tv_Version = (TextView) findViewById(R.id.tv_Version);
        tv_freeItem = (TextView) findViewById(R.id.tv_freeItem);
        rlay_mark = (RelativeLayout) findViewById(R.id.rlay_mark);
        rlay_checkVersion = (RelativeLayout) findViewById(R.id.rlay_checkVersion);

        if (!((OznerApplication) getApplication()).isLanguageCN()) {
            tv_freeItem.setVisibility(View.GONE);
        }
//        rlay_back.setOnClickListener(this);
        tv_freeItem.setOnClickListener(this);
        rlay_mark.setOnClickListener(this);
        tv_Version.setText(getVersion());
        rlay_checkVersion.setOnClickListener(this);
    }

    private String getVersion() {
        try {
            PackageManager pgManager = getPackageManager();
            PackageInfo pgInfo = pgManager.getPackageInfo(getPackageName(), 0);
            return pgInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return getString(R.string.Center_Default_Version);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_freeItem:
                Intent webIntent = new Intent();
                webIntent.setClass(AboutActivity.this, WebActivity.class);
                webIntent.putExtra("URL", Contants.URL_BASE_EXCEPTIONS);
                webIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(webIntent);
                break;
            case R.id.rlay_mark:
                String str = "market://details?id=" + getPackageName();
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(str));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(AboutActivity.this, "找不到应用，无法评分", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.rlay_checkVersion:
                //测试中，更新酷狗音乐apk
                new OznerUpdateManager(AboutActivity.this, true).checkUpdate();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}

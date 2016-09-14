package com.ozner.yiquan.AirPurifier;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.AirPurifier.AirPurifier_Bluetooth;
import com.ozner.yiquan.Command.CenterUrlContants;
import com.ozner.yiquan.Command.OznerCommand;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.PageState;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Device.OznerApplication;

import com.ozner.yiquan.R;
import com.ozner.yiquan.UIView.IndicatorProgressBar;
import com.ozner.yiquan.mycenter.WebActivity;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by taoran on 2015/12/30.
 * modify by mengdongya
 */
public class AirFilterActivity extends AppCompatActivity implements View.OnClickListener {
    private IndicatorProgressBar progressBar;
    private TextView tv_filter;
    private ImageView iv_airpm_introduce;
    private String Mac;
    private OznerDevice device;
    Toolbar toolbar;
    private AirPurifier_Bluetooth airPurifier_bluetooth;
    private TextView toolbarText, tv_pm_value;
    private LinearLayout air_zx_layout, air_health_buy_layout, air_filtercz_layout;
    private Date currentDate, proDate, stopDate;
    private int lvXin;
    int screenWide = 0, pm25 = 0, margin = 0;
    private int workTime; //台式空净的实际运行时间
    RelativeLayout tds_health_layout;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Calendar calendar = Calendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mac = getIntent().getStringExtra("MAC");
        device = OznerDeviceManager.Instance().getDevice(Mac);
        if (AirPurifierManager.IsBluetoothAirPurifier(device.Type())) {
            airPurifier_bluetooth = (AirPurifier_Bluetooth) device;
        }
        setContentView(R.layout.air_room_details_desk);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        margin = OznerCommand.dip2px(this, 30);
        screenWide = dm.widthPixels - margin * 2;
        currentDate = new Date();
        initView();
        setData();
        getFilter();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.cup_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbarText = (TextView) findViewById(R.id.cup_toolbar_text);
        toolbarText.setText(getString(R.string.room_air_detail));
        progressBar = (IndicatorProgressBar) findViewById(R.id.pb);
        tv_pm_value = (TextView) findViewById(R.id.tv_pm_value);
//        tv_air_cleans_value = (TextView) findViewById(R.id.tv_air_cleans_value);
        tv_filter = (TextView) findViewById(R.id.tv_air_room_filter);
        iv_airpm_introduce = (ImageView) findViewById(R.id.iv_airpm_introduce);
        iv_airpm_introduce.setOnClickListener(this);
        air_zx_layout = (LinearLayout) findViewById(R.id.air_zx_layout);
        air_zx_layout.setOnClickListener(this);
        air_health_buy_layout = (LinearLayout) findViewById(R.id.air_health_buy_layout);
        air_health_buy_layout.setOnClickListener(this);
//        getFilterMsg();
        air_filtercz_layout = (LinearLayout) findViewById(R.id.air_filtercz_layout);
        air_filtercz_layout.setOnClickListener(this);
        if (airPurifier_bluetooth != null) {
            air_filtercz_layout.setVisibility(View.VISIBLE);
        } else {
            air_filtercz_layout.setVisibility(View.INVISIBLE);
        }

        tds_health_layout = (RelativeLayout) findViewById(R.id.tds_health_layout);
        if (!((OznerApplication)getApplication()).isLoginPhone()) {
            tds_health_layout.setVisibility(View.GONE);
        }
    }

    private void getFilterMsg() {
        proDate = airPurifier_bluetooth.sensor().FilterStatus().lastTime;
        stopDate = airPurifier_bluetooth.sensor().FilterStatus().stopTime;
        sdf.format(proDate);
        sdf.format(stopDate);
        sdf.format(currentDate);
        calendar.setTime(proDate);
        long a = calendar.getTimeInMillis();
        calendar.setTime(currentDate);
        long b = calendar.getTimeInMillis();
        calendar.setTime(stopDate);
        long c = calendar.getTimeInMillis();
        long data = (b - a) / (1000 * 24 * 3600);
        long Times = (c - a) / (1000 * 24 * 3600);
        try {
            lvXin = Math.round((Times - data) * 100 / Times);
            if (lvXin < 0 | lvXin > 100) {
                lvXin = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getFilter() {
        workTime = airPurifier_bluetooth.sensor().FilterStatus().workTime;
        if (workTime > 60000) {
            workTime = 60000;
        }

    }

    private void setData() {
        try {
            pm25 = airPurifier_bluetooth.sensor().PM25();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pm25 == 65535) {
            tv_pm_value.setText(getString(R.string.not_connected));
        } else if (pm25 <= 0 || pm25 > 1000) {
            tv_pm_value.setText(getString(R.string.null_text));//数据异常的情况 -14021  6419
        } else {
            OznerApplication.setControlNumFace(tv_pm_value);
            tv_pm_value.setText(pm25 + "");
            tv_pm_value.setTextSize(53.0f);
        }
        OznerApplication.setControlNumFace(tv_filter);
        if (-1 == workTime) {
            tv_filter.setText(getString(R.string.text_null));
        } else {
            tv_filter.setText(GetFilterTime.getFilter(workTime) + "");
        }

        progressBar.setMaxProgress(100);
        progressBar.setProgress(100 - GetFilterTime.getFilter(workTime));
        progressBar.setThumb(R.drawable.filter_status_thumb);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.iv_airpm_introduce:
                intent.setClass(AirFilterActivity.this, AirPmIntroduceAcitivity.class);
                startActivityForResult(intent, 0x2312);
                break;
            case R.id.iv_airvoc_introduce:
                intent.setClass(AirFilterActivity.this, AirVocIntroduceAcitivity.class);
                startActivityForResult(intent, 0x2313);
                break;
            case R.id.air_health_buy_layout:
                String mobile = UserDataPreference.GetUserData(AirFilterActivity.this, UserDataPreference.Mobile, null);
                String usertoken = OznerPreference.UserToken(AirFilterActivity.this);
                Intent buyFilterIntent = new Intent(AirFilterActivity.this, WebActivity.class);
                Log.e("tag", "KjShopUrl:" + CenterUrlContants.formatKjShopUrl(mobile, usertoken, "zh", "zh"));
                buyFilterIntent.putExtra(WebActivity.URL, CenterUrlContants.formatKjShopUrl(mobile, usertoken, "zh", "zh"));
                startActivity(buyFilterIntent);
                break;
            case R.id.air_zx_layout:
//                intent.putExtra(PageState.FilterStatusChat + "", device.Address());
//                setResult(PageState.FilterStatusChat, intent);
//                this.finish();
                OznerApplication.callSeviceChat(AirFilterActivity.this);
                break;
            case R.id.air_filtercz_layout:
                if (airPurifier_bluetooth.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                    //点击弹出对话框确认是否重置滤芯
                    new AlertDialog.Builder(AirFilterActivity.this).setMessage(getString(R.string.filter_air_change)).setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(AirFilterActivity.this,"滤芯重置",Toast.LENGTH_LONG).show();
                            airPurifier_bluetooth.ResetFilter(new OperateCallback() {
                                @Override
                                public void onSuccess(Object var1) {
//                                Toast.makeText(AirFilterActivity.this, "滤芯重置success", Toast.LENGTH_LONG).show();
                                    if (airPurifier_bluetooth != null) {
                                        Log.e("trDraw", proDate + "=====pro" + stopDate + "===========stop");
                                        progressBar.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressBar.setMaxProgress(100);
                                                progressBar.setProgress(0);
                                                progressBar.setThumb(R.drawable.filter_status_thumb);
                                                tv_filter.setText("100");
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(Throwable var1) {
                                }
                            });
                            dialog.dismiss();
                        }
                    }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();
                } else {
                    Toast.makeText(this, getString(R.string.device_null), Toast.LENGTH_SHORT).show();
                }


                break;
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        setData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
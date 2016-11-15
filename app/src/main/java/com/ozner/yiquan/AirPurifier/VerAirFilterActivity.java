package com.ozner.yiquan.AirPurifier;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.yiquan.Command.CenterUrlContants;
import com.ozner.yiquan.Command.OznerCommand;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.PageState;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.R;
import com.ozner.yiquan.UIView.FilterProgressView;
import com.ozner.yiquan.UIView.IndicatorProgressBar;
import com.ozner.yiquan.mycenter.WebActivity;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class VerAirFilterActivity extends AppCompatActivity implements View.OnClickListener {
    private IndicatorProgressBar progressBar;
    private TextView tv_filter;
    private ImageView iv_airpm_introduce, iv_airvoc_introduce;
    private String Mac;
    private OznerDevice device;
    Toolbar toolbar;
    private AirPurifier_MXChip airPurifier_mxChip;
    private TextView toolbarText, tv_pm_value, tv_voc_value, tv_air_cleans_value;
    private LinearLayout airVoc, air_zx_layout, air_health_buy_layout;
    private RelativeLayout tds_distribution_layout, tds_health_layout;
    private int lvXin;
    int pm25 = 0, voc = 0;
    int totalClean = 0, screenWide = 0, margin = 0;
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private int workTime, maxWorktime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mac = getIntent().getStringExtra("MAC");
        device = OznerDeviceManager.Instance().getDevice(Mac);
        if (AirPurifierManager.IsWifiAirPurifier(device.Type())) {
            airPurifier_mxChip = (AirPurifier_MXChip) device;
        }

        UiUpdateAsyncTask uiUpdateAsyncTask = new UiUpdateAsyncTask();
        uiUpdateAsyncTask.execute("airverasd");
        setContentView(R.layout.air_room_details);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.tdsBackground));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.tdsBackground));
        }
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        margin = OznerCommand.dip2px(this, 30);
        screenWide = dm.widthPixels - margin * 2;
        initView();
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
        tv_voc_value = (TextView) findViewById(R.id.tv_voc_value);
        tv_air_cleans_value = (TextView) findViewById(R.id.tv_air_cleans_value);
        tv_filter = (TextView) findViewById(R.id.tv_air_room_filter);
        airVoc = (LinearLayout) findViewById(R.id.air_value_layout1);
        tds_distribution_layout = (RelativeLayout) findViewById(R.id.tds_distribution_layout);
        tds_health_layout = (RelativeLayout) findViewById(R.id.tds_health_layout);
        if (!((OznerApplication) (getApplication())).isLoginPhone()) {
            tds_health_layout.setVisibility(View.GONE);
        }

        iv_airpm_introduce = (ImageView) findViewById(R.id.iv_airpm_introduce);
        iv_airvoc_introduce = (ImageView) findViewById(R.id.iv_airvoc_introduce);
        iv_airpm_introduce.setOnClickListener(this);
        iv_airvoc_introduce.setOnClickListener(this);

        air_zx_layout = (LinearLayout) findViewById(R.id.air_zx_layout);
        air_zx_layout.setOnClickListener(this);
        air_health_buy_layout = (LinearLayout) findViewById(R.id.air_health_buy_layout);
        air_health_buy_layout.setOnClickListener(this);
        workTime = airPurifier_mxChip.sensor().FilterStatus().workTime;
        maxWorktime = airPurifier_mxChip.sensor().FilterStatus().maxWorkTime;
        if (maxWorktime == 0) {
            maxWorktime = 129600;
        }
        int lvxin = Math.round(100 - workTime * 100 / maxWorktime);
        tv_filter.setText(lvxin+"");
        progressBar.setMaxProgress(100);
        progressBar.setProgress(workTime * 100 / maxWorktime);
        progressBar.setThumb(R.drawable.filter_status_thumb);
    }

    private void initData() {
        try {
            pm25 = airPurifier_mxChip.sensor().PM25();
            voc = airPurifier_mxChip.sensor().VOC();
            totalClean = airPurifier_mxChip.sensor().TotalClean() / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setDate() {
        if (pm25 == 65535) {
            tv_pm_value.setText(getString(R.string.loding_null));
        } else if (pm25 <= 0 || pm25 > 1000) {
            tv_pm_value.setText(getString(R.string.null_text));//数据异常的情况 -14021  6419
        } else {
            OznerApplication.setControlNumFace(tv_pm_value);
            tv_pm_value.setText(pm25 + "");
            tv_pm_value.setTextSize(53.0f);
        }
        OznerApplication.setControlNumFace(tv_filter);

        OznerApplication.setControlNumFace(tv_air_cleans_value);
        if (totalClean == 65535 || totalClean <= 0) {
            tv_air_cleans_value.setText("0");
        } else {
            tv_air_cleans_value.setText(totalClean + "");
        }

        if (pm25 != 65535) {
            switch (voc) {
                case -1:
                    tv_voc_value.setText(getString(R.string.query_checking));
                    break;
                case 0:
                    tv_voc_value.setText(getString(R.string.excellent));
                    break;
                case 1:
                    tv_voc_value.setText(getString(R.string.good));
                    break;
                case 2:
                    tv_voc_value.setText(getString(R.string.ordinary));
                    break;
                case 3:
                    tv_voc_value.setText(getString(R.string.bads));
                    break;
            }
        } else {
            tv_voc_value.setText(getString(R.string.text_null));
        }

        if (AirPurifierManager.IsWifiAirPurifier(device.Type())) {
            airVoc.setVisibility(View.VISIBLE);
        } else {
            tds_distribution_layout.setVisibility(View.GONE);
            airVoc.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.iv_airpm_introduce:
                intent.setClass(this, AirPmIntroduceAcitivity.class);
                startActivityForResult(intent, 0x2314);
                break;
            case R.id.iv_airvoc_introduce:
                intent.setClass(this, AirVocIntroduceAcitivity.class);
                startActivityForResult(intent, 0x2315);
                break;
            case R.id.air_health_buy_layout:
                String mobile = UserDataPreference.GetUserData(this, UserDataPreference.Mobile, null);
                String usertoken = OznerPreference.UserToken(this);
                Intent buyFilterIntent = new Intent(this, WebActivity.class);
                Log.e("tag", "KjShopUrl:" + CenterUrlContants.formatKjShopUrl(mobile, usertoken, "zh", "zh"));
                buyFilterIntent.putExtra(WebActivity.URL, CenterUrlContants.formatKjShopUrl(mobile, usertoken, "zh", "zh"));
                startActivity(buyFilterIntent);
                break;
            case R.id.air_zx_layout:
//                intent.putExtra(PageState.FilterStatusChat + "", device.Address());
//                setResult(PageState.FilterStatusChat, intent);
//                this.finish();
                OznerApplication.callSeviceChat(VerAirFilterActivity.this);
                break;
        }

    }

    private class UiUpdateAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            initData();
            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {
        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            setDate();
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        new UiUpdateAsyncTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

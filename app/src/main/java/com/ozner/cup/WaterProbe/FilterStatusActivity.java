package com.ozner.cup.WaterProbe;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.cup.Command.CenterUrlContants;
import com.ozner.cup.Command.CustomToast;
import com.ozner.cup.Command.NetErrDecode;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.OznerDataHttp;
import com.ozner.cup.QRCodeScan.activity.CaptureActivity;
import com.ozner.cup.R;
import com.ozner.cup.UIView.FilterProgressView;
import com.ozner.cup.UIView.UIZGridView;
import com.ozner.cup.mycenter.CenterBean.RankType;
import com.ozner.cup.mycenter.WebActivity;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
/*
* Created by xinde on 2015/12/10
 */

public class FilterStatusActivity extends AppCompatActivity implements View.OnClickListener {
    private final static int SCANNIN_GREQUEST_CODE = 0x03;
    private static int INIT_WARRANTY = 30;// 默认有效期
    //    private static int WATER_WARRANTY = 365;//净水器默认有效期
    private static final String SaveStr = "FilterStatus";
    private static final String WaterPurifierStr = "WaterPurifierFilter";//净水器滤芯
    //    private String MAC = "34:23:23:23:23:56";
    private String MAC = "";
    private String waterPuriferUrl = "", isShowewm = "";
    private String usertoken = "";
    FilterProgressView filter_progress;
    TextView tv_remainPre, tv_remainTime,tv_ro_filterRest,tv_ro_filter;
    RelativeLayout rlay_back;
    LinearLayout llay_QRCodeScan, llay_Chat, llay_buyFilter, llay_moreService, llay_scan,laly_ro,laly_water;
    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//    private int totalYearDays = 0;

    private UIZGridView uiz_moreProject, uiz_onzerService;
    private ArrayList<HashMap<String, Object>> projectList, serviceList;
    private SimpleAdapter projectAdapter, serviceAdapter;
    private UpdateFilterAsyncTask tapFilterTask;
    private UpdateWaterPurifierFilterTask purifierFilterTask;

    //更多产品
    private int[] projectImgs;// = {R.drawable.filter_status_tap, R.drawable.filter_status_purifier, R.drawable.filter_status_cup};
    private String[] projectStr;// = {"浩泽智能水探头", "金色伊泉系列", "浩泽智能杯"};
    //浩泽服务
    private int[] serviceImgs;
    private String[] serviceUpStr;
    private String[] serviceDownStr;
    private String deviceType = RankType.TapType;
    private OznerDevice device;
    private String mMobile = "";
    private String musertoken = "";
    private String mUserid = "";

    private String buyRourl = "www.baidu.com";//购买滤芯
    //RO文字呼吸灯
    private int index = 0;
    private boolean isOpen = true;
    private Timer timer;
    private TextView tv_rolxa,tv_rolxb,tv_rolxc;
    private String fit_a,fit_b,fit_c;
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_status);
        MAC = getIntent().getStringExtra("MAC");
        //RO水机滤芯状态
        fit_a=getIntent().getStringExtra("Fit_a");
        fit_b=getIntent().getStringExtra("Fit_b");
        fit_c=getIntent().getStringExtra("Fit_c");
        waterPuriferUrl = getIntent().getStringExtra("buylinkurl");
        Log.e("filter", "waterPufifierUrl:" + waterPuriferUrl);
        isShowewm = getIntent().getStringExtra("isShowewm");
        Log.e("filter", "waterPufifierUrl:" + isShowewm);
        mMobile = UserDataPreference.GetUserData(this, UserDataPreference.Mobile, "");
        musertoken = OznerPreference.UserToken(this);
        projectImgs = new int[]{R.drawable.filter_status_tap, R.drawable.filter_status_purifier, R.drawable.filter_status_cup};
        projectStr = new String[]{getString(R.string.Filter_Project1), getString(R.string.Filter_Project2), getString(R.string.Filter_Project3)};
        filter_progress = (FilterProgressView) findViewById(R.id.filter_progress);
        uiz_moreProject = (UIZGridView) findViewById(R.id.uiz_moreProject);
        uiz_onzerService = (UIZGridView) findViewById(R.id.uiz_onzeService);
        tv_remainPre = (TextView) findViewById(R.id.tv_remainPre);
        tv_remainTime = (TextView) findViewById(R.id.tv_remainTime);
        rlay_back = (RelativeLayout) findViewById(R.id.rlay_back);
        llay_QRCodeScan = (LinearLayout) findViewById(R.id.llay_QRCodeScan);
        llay_scan = (LinearLayout) findViewById(R.id.llay_scan);

        llay_moreService = (LinearLayout) findViewById(R.id.llay_moreService);
        llay_Chat = (LinearLayout) findViewById(R.id.tds_health_know_layout);
        llay_buyFilter = (LinearLayout) findViewById(R.id.tds_health_buy_layout);
        rlay_back.setOnClickListener(this);
        llay_QRCodeScan.setOnClickListener(this);
        llay_Chat.setOnClickListener(this);
        llay_buyFilter.setOnClickListener(this);

        //RO水机的界面
        laly_ro=(LinearLayout) findViewById(R.id.laly_ro);
        laly_water=(LinearLayout) findViewById(R.id.laly_water);
        device = OznerDeviceManager.Instance().getDevice(MAC);
        if (RankType.ROWaterType.equals(device.Type())){
            laly_ro.setVisibility(View.VISIBLE);
            laly_water.setVisibility(View.GONE);
            llay_scan.setVisibility(View.GONE);
            llay_moreService.setVisibility(View.VISIBLE);
        }else{
            laly_ro.setVisibility(View.GONE);
            laly_water.setVisibility(View.VISIBLE);
        }
        tv_ro_filterRest=(TextView)findViewById(R.id.tv_ro_filterRest);
        tv_ro_filterRest.setOnClickListener(this);
        tv_ro_filter=(TextView) findViewById(R.id.tv_ro_filter);
        tv_rolxa=(TextView)findViewById(R.id.tv_rolxa);
        tv_rolxb=(TextView)findViewById(R.id.tv_rolxb);
        tv_rolxc=(TextView)findViewById(R.id.tv_rolxc);
        Log.e("trfitt",fit_a+"&&&"+fit_b);
        if(fit_a!=null&&Integer.parseInt(fit_a)!=0){
            tv_rolxa.setText(fit_a+"%");
        }else{
            tv_rolxa.setText(getString(R.string.text_null));
        }
        if(fit_b!=null&&Integer.parseInt(fit_b)!=0){
            tv_rolxb.setText(fit_b+"%");
        }else{
            tv_rolxb.setText(getString(R.string.text_null));
        }
        if(fit_c!=null&&Integer.parseInt(fit_c)!=0){
            tv_rolxc.setText(fit_c+"%");
        }else{
            tv_rolxc.setText(getString(R.string.text_null));
        }
        //文字呼吸灯
        try {
            if ((Integer.parseInt(fit_a) < 30) || Integer.parseInt(fit_b)<30||Integer.parseInt(fit_c)<30) {
                timer();
            }
        }catch(Exception e){
            e.getStackTrace();
        }



        if (!((OznerApplication) getApplication()).isLoginPhone()) {
            findViewById(R.id.ll_en_no).setVisibility(View.GONE);
        } else {
            uiz_moreProject.setOnItemClickListener(new ProjectItemClickListener());
        }
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        OznerApplication.setControlNumFace(tv_remainTime);
        OznerApplication.setControlNumFace(tv_remainPre);
        init();
    }
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    tv_ro_filter.clearAnimation();
                    tv_ro_filter.setAnimation(getLoad());
                    break;
                case 2:
                    tv_ro_filter.clearAnimation();
                    tv_ro_filter.setAnimation(getOut());
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Animation getLoad() {
        return AnimationUtils.loadAnimation(FilterStatusActivity.this,
                R.anim.push_in);
    }

    private Animation getOut() {
        return AnimationUtils.loadAnimation(FilterStatusActivity.this,
                R.anim.push_out);
    }
    private void timer() {
        timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (isOpen) {
                    if (index == 2) {
                        index = 0;
                    }
                    index++;
                    Message message = new Message();
                    message.what = index;
                    handler.sendMessage(message);
                }
            }
        };
        timer.schedule(task, 0, 1000); // 延时0ms后执行，1000ms执行一次
    }




    public class ProjectItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {//水探头
                Intent tapIntent = new Intent(parent.getContext(), WebActivity.class);
                String tapUrl = CenterUrlContants.formatFilterTapUrl(mMobile, musertoken, "zh", "zh");
                tapIntent.putExtra(WebActivity.URL, tapUrl);
                startActivity(tapIntent);
            } else if (position == 1) {//金色伊泉
                Intent goldIntent = new Intent(parent.getContext(), WebActivity.class);
                String goldUrl = CenterUrlContants.formatFilterGoldSpringUrl(mMobile, musertoken, "zh", "zh");
                goldIntent.putExtra(WebActivity.URL, goldUrl);
                startActivity(goldIntent);
            } else if (position == 2) {//浩泽智能杯
                Intent cupIntent = new Intent(parent.getContext(), WebActivity.class);
                String cupUrl = CenterUrlContants.formatFilterCupUrl(mMobile, musertoken, "zh", "zh");
                cupIntent.putExtra(WebActivity.URL, cupUrl);
                startActivity(cupIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initFilter() {
        if (null != MAC && "" != MAC) {
            Log.e("tag", "Filter_Mac:" + MAC);
            try {
                device = OznerDeviceManager.Instance().getDevice(MAC);
                if (device != null && device instanceof WaterPurifier) {
                    deviceType = RankType.WaterType;
                    if ("0".equals(isShowewm)) {
                        llay_scan.setVisibility(View.GONE);
                    } else {
                        llay_scan.setVisibility(View.VISIBLE);
                    }
                    llay_moreService.setVisibility(View.VISIBLE);
                }
// else if(device != null && device instanceof WaterPurifier_RO_BLE){
//                    deviceType = RankType.ROWaterType;
//                    llay_scan.setVisibility(View.GONE);
//                    llay_moreService.setVisibility(View.VISIBLE);
//                }
                else {
                    deviceType = RankType.TapType;
                    llay_moreService.setVisibility(View.GONE);
                }
//            initFilterFromLocal();
//            new UpdateFilterAsyncTask(FilterStatusActivity.this, usertoken).execute(MAC);
                initFilterStatus(deviceType);
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("tag", "initFilter_Ex:" + ex.getMessage());
            }
        } else {
            String errStr = getString(R.string.Filter_ParmErr);
            new AlertDialog.Builder(FilterStatusActivity.this).setMessage(errStr)
                    .setPositiveButton(getResources().getString(R.string.ensure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            FilterStatusActivity.this.finish();
                        }
                    }).show();
        }
    }

    private void init() {
        usertoken = OznerPreference.UserToken(FilterStatusActivity.this);
        serviceImgs = new int[]{R.drawable.filter_status_00, R.drawable.filter_status_01,
                R.drawable.filter_status_10, R.drawable.filter_status_11,
                R.drawable.filter_status_20, R.drawable.filter_status_21,
                R.drawable.filter_status_30, R.drawable.filter_status_31
        };
        serviceUpStr = new String[]{getString(R.string.Filter_Service_up_00), getString(R.string.Filter_Service_up_01),
                getString(R.string.Filter_Service_up_10), getString(R.string.Filter_Service_up_11),
                getString(R.string.Filter_Service_up_20), getString(R.string.Filter_Service_up_21),
                getString(R.string.Filter_Service_up_30), getString(R.string.Filter_Service_up_31)
        };
        serviceDownStr = new String[]{getString(R.string.Filter_Service_down_00), getString(R.string.Filter_Service_down_01),
                getString(R.string.Filter_Service_down_10), getString(R.string.Filter_Service_down_11),
                getString(R.string.Filter_Service_down_20), "",
                "", ""
        };

        projectList = new ArrayList<HashMap<String, Object>>();
        serviceList = new ArrayList<HashMap<String, Object>>();
        reloadProjectData();
        reloadServiceData();
        filter_progress.setThumb(R.drawable.filter_status_thumb);
        initFilter();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlay_back:
                onBackPressed();
                break;
            case R.id.llay_QRCodeScan:
                Intent scanIntent = new Intent(FilterStatusActivity.this, CaptureActivity.class);
                startActivityForResult(scanIntent, SCANNIN_GREQUEST_CODE);
                break;
            case R.id.tds_health_know_layout://咨询
                Intent intent = new Intent();
                intent.putExtra(PageState.CENTER_DEVICE_ADDRESS, MAC);
                setResult(PageState.FilterStatusChat, intent);
                FilterStatusActivity.this.finish();
                break;
            case R.id.tds_health_buy_layout://购买滤芯
                String mobile = UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.Mobile, null);
                String usertoken = OznerPreference.UserToken(getBaseContext());
                Intent buyFilterIntent = new Intent(FilterStatusActivity.this, WebActivity.class);
//                buyFilterIntent.putExtra(WebActivity.IsHideTitle, false);
                String shopUrl = CenterUrlContants.formatTapShopUrl(mobile, usertoken, "zh", "zh");
                if (deviceType.equals(RankType.WaterType)) {
//                    shopUrl = CenterUrlContants.formatDeskPurifierUrl( mobile, usertoken, "zh", "zh");
                    if (waterPuriferUrl != null && waterPuriferUrl != "") {
                        shopUrl = CenterUrlContants.formatUrl(waterPuriferUrl, mobile, usertoken, "zh", "zh");
//                        shopUrl += waterPuriferUrl;
                    } else {
                        shopUrl = CenterUrlContants.formatSecurityServiceUrl(mobile, usertoken, "zh", "zh");
                    }
                }
                Log.e("filter", "shopUrl:" + shopUrl);
//                Log.e("tag", "tapShopUrl:" + CenterUrlContants.formatTapShopUrl(mobile, usertoken, "zh", "zh"));
//                buyFilterIntent.putExtra(WebActivity.URL, CenterUrlContants.formatTapShopUrl(mobile, usertoken, "zh", "zh"));
                Log.e("tag", "tapShopUrl:" + shopUrl);
                buyFilterIntent.putExtra(WebActivity.URL, shopUrl);
                startActivity(buyFilterIntent);
                break;
            case R.id.tv_ro_filterRest://ro水机滤芯复位
                new AlertDialog.Builder(FilterStatusActivity.this)
                        .setMessage(getString(R.string.rofilter_need_change))
                        .setPositiveButton(getString(R.string.buy_air_lvxin), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                buyFilter();
                                dialog.dismiss();
                            }
                        }).setNegativeButton(getString(R.string.airOutside_know), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
        }
    }
    private void buyFilter() {
        String mobile = UserDataPreference.GetUserData(getApplicationContext(), UserDataPreference.Mobile, null);
        String usertoken = OznerPreference.UserToken(getApplicationContext());
        Intent buyFilterIntent = new Intent(FilterStatusActivity.this, WebActivity.class);
        String shopUrl = CenterUrlContants.formatTapShopUrl(mobile, usertoken, "zh", "zh");
        if (buyRourl != null && buyRourl != "") {
            shopUrl = CenterUrlContants.formatUrl(buyRourl, mobile, usertoken, "zh", "zh");
        }
        buyFilterIntent.putExtra("URL", shopUrl);
        Log.e("123456", shopUrl);
        startActivity(buyFilterIntent);
    }
    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }


    private void initFilterStatus(String deType) {
        if (deType.equals(RankType.WaterType)) {
            initWaterPurifierFilterLocal();
        } else if(deType.equals(RankType.TapType)) {
            filter_progress.setVisibility(View.VISIBLE);
            initFilterFromLocal();
            tapFilterTask = new UpdateFilterAsyncTask(FilterStatusActivity.this, usertoken);
            tapFilterTask.execute(MAC);
//            new UpdateFilterAsyncTask(FilterStatusActivity.this, usertoken).execute(MAC);
        }else{

        }
    }

    /*
    *水探头滤芯服务时间本地初始化
     */
    private void initFilterFromLocal() {
        String modifytime = UserDataPreference.GetUserData(FilterStatusActivity.this, SaveStr + MAC, "");
        Log.e("Filter", "modifytime:" + modifytime);
        if ("" != modifytime) {
            try {
                Date startTime = dataFormat.parse(modifytime);
                Date today = new Date(System.currentTimeMillis());
                long useday = (today.getTime() - startTime.getTime()) / (24 * 3600 * 1000);
                if (useday <= INIT_WARRANTY) {
                    tv_remainTime.setText(String.valueOf(INIT_WARRANTY - useday));
                    tv_remainPre.setText(String.valueOf((int) (((float) (INIT_WARRANTY - useday)) / INIT_WARRANTY * 100)));
                }
                filter_progress.initTime(startTime, INIT_WARRANTY);
                filter_progress.update(useday);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    *净水器滤芯服务时间本地初始化
     */
    private void initWaterPurifierFilterLocal() {
        boolean isHasUpdate = false;
        String purifierSaveValue = UserDataPreference.GetUserData(FilterStatusActivity.this, WaterPurifierStr + MAC, "");
        Log.e("Filter", "purifierSaveValue:" + purifierSaveValue);
        if (!"".equals(purifierSaveValue)) {
            showWaterFilterData(purifierSaveValue);
            try {
                JSONObject jsonObject = new JSONObject(purifierSaveValue);
                String nowtime = jsonObject.getString("nowtime");
                Date nowDate = new Date(nowtime);
                Calendar nowCal = Calendar.getInstance();
                nowCal.setTime(nowDate);
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                if (sf.format(nowCal.getTime()).equals(sf.format(Calendar.getInstance().getTime()))) {
                    isHasUpdate = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!isHasUpdate) {
            new UpdateWaterPurifierFilterTask(FilterStatusActivity.this, usertoken).execute(MAC);
        }
    }

    /*
    *刷新净水器滤芯当前状态
     */
    class UpdateWaterPurifierFilterTask extends AsyncTask<String, Void, NetJsonObject> {
        private Context mContext;
        private ProgressDialog dialog;
        private String token;
        private String mac;

        public UpdateWaterPurifierFilterTask(Context context, String usertoken) {
            this.mContext = context;
            this.token = usertoken;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(mContext, "", getString(R.string.Center_Loading));
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    UpdateWaterPurifierFilterTask.this.cancel(true);
                }
            });
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params.length > 0) {
                mac = params[0];
                List<NameValuePair> reqPars = new ArrayList<NameValuePair>();
                reqPars.add(new BasicNameValuePair("usertoken", token));
                reqPars.add(new BasicNameValuePair("mac", mac));
                String filterUrl = OznerPreference.ServerAddress(mContext) + "/OznerDevice/GetMachineLifeOutTime";
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServerForFilter(mContext, filterUrl, reqPars, 2000);
                return netJsonObject;
            }
            return null;
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (netJsonObject != null) {
                if (netJsonObject.state > 0) {
                    UserDataPreference.SetUserData(FilterStatusActivity.this, WaterPurifierStr + mac, netJsonObject.value);
                    showWaterFilterData(netJsonObject.value);

                } else {
                    showExecptionWaterFilter();
                }
                Log.e("tag", "净水器滤芯:" + netJsonObject.value);
            } else {
                Log.e("tag", "刷新净水器滤芯状态失败！");
                showExecptionWaterFilter();
            }
        }
    }

    /*
    *time:结束时间
    * nowtime:服务器当前时间
    * 逻辑：1、当前时间早于结束时间一年的，净水器不会过期，有效时间100%；
    * 2、当前时间在结束时间前一年内的，正常显示，有效期按一年计算
     */
    private void showWaterFilterData(String waterPurifierStr) {
        try {
            JSONObject jsonObject = new JSONObject(waterPurifierStr);
            String time = jsonObject.getString("time");
            String nowtime = jsonObject.getString("nowtime");
            Date nowDate = new Date(nowtime);
            Date endDate = new Date(time);
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTime(nowDate);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            Calendar verifyCal = (Calendar) endCal.clone();
            verifyCal.add(Calendar.YEAR, -1);
            if (endDate.getTime() < nowDate.getTime()) {//当前时间已经超过有效期
                tv_remainPre.setText("0");
                tv_remainTime.setText("0");
                filter_progress.initTime(verifyCal.getTime(), endDate);
                filter_progress.update(nowDate);
            } else if (verifyCal.getTimeInMillis() > nowCal.getTimeInMillis()) {//当前时间早于有效期一年
                float reday = (endCal.getTimeInMillis() - nowCal.getTimeInMillis()) / 1000.0f / 3600 / 24;
                tv_remainTime.setText(String.valueOf((int) Math.ceil(reday)));
                tv_remainPre.setText("100");
                filter_progress.initTime(nowCal.getTime(), endDate);
                filter_progress.update(nowCal.getTime());
            } else {//当前时间在一年有效期内
                float remainDay = (endCal.getTimeInMillis() - nowCal.getTimeInMillis()) / 1000.0f / 3600 / 24;
                tv_remainTime.setText(String.valueOf((int) Math.ceil(remainDay)));
                endCal.add(Calendar.YEAR, -1);
                long totalDays = (endDate.getTime() - endCal.getTimeInMillis()) / 1000 / 3600 / 24;
                Log.e("tag", "剩余：" + remainDay + ", 全部：" + totalDays);
                float pre = remainDay / totalDays * 100;
                tv_remainPre.setText(String.valueOf((int) Math.ceil(pre)));
                filter_progress.initTime(endCal.getTime(), endDate);
                filter_progress.update(nowDate);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("tag", "异步刷新净水器_Ex_one:" + ex.getMessage());
            showExecptionWaterFilter();
        }
    }

    private void showExecptionWaterFilter() {
        try {
            Calendar startCal = Calendar.getInstance();
            Calendar nowCal = (Calendar) startCal.clone();
            startCal.add(Calendar.YEAR, -1);
            float totalDays = (nowCal.getTimeInMillis() - startCal.getTimeInMillis()) / 1000.0f / 3600 / 24;
            tv_remainTime.setText(String.valueOf((int) Math.ceil(totalDays)));
            tv_remainPre.setText("100");
            filter_progress.initTime(startCal.getTime(), nowCal.getTime());
            filter_progress.update(startCal.getTime());
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("tag", "异步刷新净水器_Ex:" + ex.getMessage());
        }
    }

    //刷新水探头滤芯当前显示状态
    class UpdateFilterAsyncTask extends AsyncTask<String, Void, NetJsonObject> {
        private Context mContext;
        private ProgressDialog dialog;
        private String token;
        private String mac;

        public UpdateFilterAsyncTask(Context context, String usertoken) {
            this.mContext = context;
            this.token = usertoken;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(mContext, "", mContext.getString(R.string.Center_Loading));
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    UpdateFilterAsyncTask.this.cancel(true);
                }
            });
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params.length > 0) {
                mac = params[0];
                List<NameValuePair> reqPars = new ArrayList<NameValuePair>();
                reqPars.add(new BasicNameValuePair("usertoken", token));
                reqPars.add(new BasicNameValuePair("mac", mac));
                String filterUrl = OznerPreference.ServerAddress(mContext) + "/OznerDevice/FilterService";
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(mContext, filterUrl, reqPars);
                return netJsonObject;
            }
            return null;
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (netJsonObject != null) {
                if (netJsonObject.state > 0) {
                    Log.e("tag", "刷新成功:" + netJsonObject.value);
                    try {
                        JSONObject jo = netJsonObject.getJSONObject();
                        String modifytime = jo.getString("modifytime");
                        UserDataPreference.SetUserData(FilterStatusActivity.this, SaveStr + mac, modifytime);
                        int useday = jo.getInt("useday");
                        Log.e("tag", "useday:" + useday);
                        if (useday <= INIT_WARRANTY) {
                            tv_remainTime.setText(String.valueOf(INIT_WARRANTY - useday));
                            tv_remainPre.setText(String.valueOf((int) (((float) (INIT_WARRANTY - useday)) / INIT_WARRANTY * 100)));
                        } else {
                            CustomToast.showToastCenter(mContext, mContext.getString(R.string.Filter_Warranty_err));
                        }
                        Date startTime = dataFormat.parse(modifytime);
                        filter_progress.initTime(startTime, INIT_WARRANTY);
                        filter_progress.update(useday);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                Log.e("Filter", "刷新成功:" + netJsonObject.value);
            } else {
                Log.e("Filter", "刷新水探头滤芯状态失败！");
            }
        }
    }

    //更新滤芯服务时间
    class RenewFilterAsyncTask extends AsyncTask<String, Void, NetJsonObject> {
        private Context mContext;
        private String mToken;
        private ProgressDialog dialog;
        private String scanCode;
        private String mac;
        private String devicetype;

        public RenewFilterAsyncTask(Context context, String token, String devicetype) {
            this.mContext = context;
            this.mToken = token;
            this.devicetype = devicetype;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(mContext, "", mContext.getString(R.string.Center_Loading));
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params.length == 2) {
                mac = params[0];
                scanCode = params[1];
                String renewUrl = OznerPreference.ServerAddress(mContext) + "/OznerDevice/RenewFilterTime";
                List<NameValuePair> pairs = new ArrayList<>();
                pairs.add(new BasicNameValuePair("usertoken", mToken));
                pairs.add(new BasicNameValuePair("mac", mac));
                pairs.add(new BasicNameValuePair("devicetype", devicetype));
                pairs.add(new BasicNameValuePair("code", scanCode));
                NetJsonObject result = OznerDataHttp.OznerWebServer(getApplicationContext(), renewUrl, pairs);
                return result;
            }
            return null;
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (netJsonObject != null) {
                if (netJsonObject.state > 0) {
                    //重新刷新滤芯显示状态
                    new UpdateFilterAsyncTask(mContext, mToken).execute(mac);
                } else {
                    NetErrDecode.ShowErrMsgDialog(mContext, netJsonObject.state, getString(R.string.refresh_filter_failure));
                }

            } else {
                CustomToast.showToastCenter(mContext, mContext.getString(R.string.Filter_UpdateErr));
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString("result");
                    if (null != scanResult && "" != scanResult) {
                        new RenewFilterAsyncTask(FilterStatusActivity.this, usertoken, deviceType).execute(MAC, scanResult);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void reloadProjectData() {
        projectList.clear();
        for (int i = 0; i < projectImgs.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImg", projectImgs[i]);
            map.put("itemText", projectStr[i]);
            projectList.add(map);
        }
        projectAdapter = new SimpleAdapter(this, projectList, R.layout.more_product_item,
                new String[]{"itemImg", "itemText"}, new int[]{R.id.iv_more_product_img, R.id.tv_more_product_text});

        uiz_moreProject.setAdapter(projectAdapter);
    }

    private void reloadServiceData() {
        serviceList.clear();
        for (int i = 0; i < serviceImgs.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImg", serviceImgs[i]);
            map.put("itemTextUp", serviceUpStr[i]);
            map.put("itemTextDown", serviceDownStr[i]);
            serviceList.add(map);
        }
        serviceAdapter = new SimpleAdapter(this, serviceList, R.layout.more_ozner_service_item,
                new String[]{"itemImg", "itemTextUp", "itemTextDown"},
                new int[]{R.id.iv_more_service_img, R.id.tv_more_service_up_text, R.id.tv_more_service_down_text});

        uiz_onzerService.setAdapter(serviceAdapter);
    }
    @Override
    protected void onDestroy() {
        isOpen = false;
        if (timer != null) {
            timer.cancel();// 退出计时器
        }
        timer = null;
        super.onDestroy();
    }
}

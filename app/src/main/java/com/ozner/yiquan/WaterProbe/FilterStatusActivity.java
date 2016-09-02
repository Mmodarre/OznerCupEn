package com.ozner.yiquan.WaterProbe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.yiquan.Command.CenterUrlContants;
import com.ozner.yiquan.Command.CustomToast;
import com.ozner.yiquan.Command.NetErrDecode;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.PageState;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.HttpHelper.NetJsonObject;
import com.ozner.yiquan.HttpHelper.OznerDataHttp;
import com.ozner.yiquan.QRCodeScan.activity.CaptureActivity;
import com.ozner.yiquan.R;
import com.ozner.yiquan.UIView.FilterProgressView;
import com.ozner.yiquan.UIView.UIZGridView;
import com.ozner.yiquan.mycenter.CenterBean.RankType;
import com.ozner.yiquan.mycenter.WebActivity;
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
    TextView tv_remainPre, tv_remainTime;
    RelativeLayout rlay_back;
    LinearLayout llay_QRCodeScan, llay_Chat, llay_buyFilter, llay_moreService, llay_scan;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_status);
        MAC = getIntent().getStringExtra("MAC");
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
//                    llay_moreService.setVisibility(View.VISIBLE);
                } else {
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
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }


    private void initFilterStatus(String deType) {
        if (deType.equals(RankType.WaterType)) {
            initWaterPurifierFilterLocal();
        } else {
            filter_progress.setVisibility(View.VISIBLE);
            initFilterFromLocal();
            tapFilterTask = new UpdateFilterAsyncTask(FilterStatusActivity.this, usertoken);
            tapFilterTask.execute(MAC);
//            new UpdateFilterAsyncTask(FilterStatusActivity.this, usertoken).execute(MAC);
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

}

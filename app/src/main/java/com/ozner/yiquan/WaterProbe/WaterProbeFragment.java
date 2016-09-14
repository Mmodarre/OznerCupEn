package com.ozner.yiquan.WaterProbe;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.cup.CupRecord;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.Tap;
import com.ozner.tap.TapRecord;
import com.ozner.tap.TapSensor;
import com.ozner.yiquan.Command.FootFragmentListener;
import com.ozner.yiquan.Command.ImageHelper;
import com.ozner.yiquan.Command.OznerCommand;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.PageState;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.Device.SetupWaterProbeActivity;
import com.ozner.yiquan.HttpHelper.NetJsonObject;
import com.ozner.yiquan.HttpHelper.OznerDataHttp;
import com.ozner.yiquan.MainActivity;
import com.ozner.yiquan.MainEnActivity;
import com.ozner.yiquan.R;
import com.ozner.yiquan.UIView.ChartAdapter;
import com.ozner.yiquan.UIView.TapTDSChartView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;


/**
 * A simple {@link Fragment} subclass.
 */
public class WaterProbeFragment extends Fragment implements View.OnClickListener, FootFragmentListener {
    private static final String SaveStr = "FilterStatus";
    TapTDSChartView tdsChartView = null;
    RelativeLayout rlay_filterStatus, rlay_menu, rlay_top1;
    private static int INIT_WARRANTY = 30;// 默认有效期
    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private RotateAnimation animation;
    String Mac;
    int tdsOld = 0, tdsNew = 0;
    int ranking;
    private LinearLayout lay_tdsShort;
    WaterDetailProgress waterProcess;
    private TextView tv_name, tv_tdsValue, tv_batteryTem, tv_filterStatus, tv_tdsShort, tv_tdsLevelText, tv_data_loading;
    private TextView tv_tapHealthPre, tv_tapGenericPre, tv_tapBadPre, tv_filiteText;
    private Tap tap;
    int[] data = new int[31];
    ImageView iv_probe_setting, iv_data_loading, iv_tdsLevelImg, iv_filterState, iv_battery;
    UpdateFilterAsyncTask filter = null;
    //    UiUpdateAsyncTask asyncTask = null;
    ChartAdapter adapter = new ChartAdapter() {

        @Override
        public int count() {
            return data.length;
        }

        @Override
        public int getValue(int Index) {
            return data[Index];
        }

        @Override
        public int getMax() {
            return CupRecord.TDS_Bad_Value * 2 - CupRecord.TDS_Good_Value;
        }

        @Override
        public ViewMode getViewMode() {
            return ViewMode.Month;
        }
    };

    private Handler rhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    int rank = msg.arg1;
                    OznerApplication.setControlNumFace(tv_tdsShort);
                    tv_tdsShort.setText(rank + "%");
                    break;
            }
        }
    };


    public WaterProbeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tap_detail, container, false);
        // Inflate the layout for this fragment
        InitView(view);
        OznerApplication.changeTextFont((ViewGroup) view);
        return view;
    }

    @Override
    public void onDestroyView() {
//        if (asyncTask != null) {
//            Log.e("TAG", "C-" + "Task rmove .");
//            asyncTask.cancel(false);
//            asyncTask = null;
//        }
        if (filter != null) {
            Log.e("TAG", "C-" + "Task rmove .");
            filter.cancel(false);
            filter = null;
        }
        System.gc();
        super.onDestroyView();
    }

    public void InitView(View view) {
//        if (((OznerApplication) getActivity().getApplication()).isLoginPhone()) {
//            view.findViewById(R.id.llay_cupHolder).setVisibility(View.VISIBLE);
//        } else {
//            view.findViewById(R.id.llay_cupHolder).setVisibility(View.GONE);
//        }
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tdsChartView = (TapTDSChartView) view.findViewById(R.id.tdsChartView);
        rlay_filterStatus = (RelativeLayout) view.findViewById(R.id.rlay_filterStatus);
        lay_tdsShort = (LinearLayout) view.findViewById(R.id.lay_tdsShort);
        tv_tdsValue = (TextView) view.findViewById(R.id.tv_tdsValue);
        tv_tdsShort = (TextView) view.findViewById(R.id.tv_tdsShort);
        waterProcess = (WaterDetailProgress) view.findViewById(R.id.waterProcess);
        tv_batteryTem = (TextView) view.findViewById(R.id.tv_batteryTem);
        iv_battery = (ImageView) view.findViewById(R.id.iv_battery);
        tv_filterStatus = (TextView) view.findViewById(R.id.tv_filterStatus);
        tv_filiteText = (TextView) view.findViewById(R.id.tv_filiteText);
        iv_probe_setting = (ImageView) view.findViewById(R.id.iv_probe_setting);
        rlay_menu = (RelativeLayout) view.findViewById(R.id.rlay_menu);
        rlay_filterStatus.setOnClickListener(this);
        iv_probe_setting.setOnClickListener(this);
        rlay_menu.setOnClickListener(this);
        tv_tdsLevelText = (TextView) view.findViewById(R.id.tv_tdsLevelText);
        iv_filterState = (ImageView) view.findViewById(R.id.iv_filterState);
        rlay_top1 = (RelativeLayout) view.findViewById(R.id.rlay_top1);
        tv_data_loading = (TextView) view.findViewById(R.id.tv_data_loading);
        iv_data_loading = (ImageView) view.findViewById(R.id.iv_data_loading);
        animation = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatCount(-1);
        LinearInterpolator li = new LinearInterpolator();
        animation.setInterpolator(li);
        animation.setFillAfter(false);
        animation.setDuration(1000);
        iv_data_loading.setAnimation(animation);
        iv_tdsLevelImg = (ImageView) view.findViewById(R.id.iv_tdsLevelImg);//水质tds图标
        tv_tapHealthPre = (TextView) view.findViewById(R.id.tv_tapHealthPre);
        tv_tapGenericPre = (TextView) view.findViewById(R.id.tv_tapGenericPre);
        tv_tapBadPre = (TextView) view.findViewById(R.id.tv_tapBadPre);

        initImageViewBitmap(view);
    }

    private void initImageViewBitmap(View initView) {
        WeakReference<Context> refContext = new WeakReference<Context>(getContext());
        if (refContext != null) {
            ((LinearLayout) initView.findViewById(R.id.llay_tapBg)).setBackground(ImageHelper.loadResDrawable(refContext.get(), R.drawable.cupdetail_bg));
            iv_battery.setImageBitmap(ImageHelper.loadResBitmap(refContext.get(), R.drawable.battery30));
            iv_filterState.setImageBitmap(ImageHelper.loadResBitmap(refContext.get(), R.drawable.filter_state0));
            iv_probe_setting.setImageBitmap(ImageHelper.loadResBitmap(refContext.get(), R.drawable.setting));
        }
    }

    /*
    private class UiUpdateAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... params) {
            InitData();
            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {
        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            if (WaterProbeFragment.this.isAdded() && !WaterProbeFragment.this.isDetached() && !WaterProbeFragment.this.isRemoving()) {
                RefreshBindDataView();
                if (adapter != null) {
                    tdsChartView.setAdapter(adapter);
                }
                changeState();
            }
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {

        }
    }
*/

    /*
    //上传tds值获取月数据
    private class UpdateTdsAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            UploadTds();

            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {
        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {

        }
    }
*/


    /**
     * 显示滤芯状态
     *
     * @param pre 滤芯剩余百分比 pre%
     */
    private void displayFilterStatus(String pre) {
        tv_filterStatus.setText(pre + "%");
        tv_filiteText.setText(getString(R.string.filter_status));
        //滤芯状态图片的更改
        int filterPre = Integer.parseInt(pre);
        if (filterPre > 100) filterPre = 100;
        if (filterPre < 0) filterPre = 0;
        if (filterPre == 0) {
            iv_filterState.setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.filter_state0));
            tv_filiteText.setText(getString(R.string.filter_need_change));
        } else if (filterPre > 0 && filterPre <= 30) {
            tv_filiteText.setText(getString(R.string.filter_need_change));
            iv_filterState.setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.filter_state1));
        } else if (filterPre > 30 && filterPre <= 60) {
            iv_filterState.setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.filter_state2));
        } else if (filterPre > 60 && filterPre <= 100) {
            iv_filterState.setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.filter_state3));
        } else {
            iv_filterState.setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.filter_state3));
        }
    }

    private class UpdateFilterAsyncTask extends AsyncTask<String, Integer, String> {
        private Boolean shouldtoupdate;
        private String fiflterday = "0";

        @Override
        protected void onPreExecute() {
            String modifytime = UserDataPreference.GetUserData(getContext(), SaveStr + Mac, "");
            Log.e("tag", "modifytime:" + modifytime);
            if ("" != modifytime) {
                try {
                    Date startTime = dataFormat.parse(modifytime);
                    Date today = new Date(System.currentTimeMillis());
                    long useday = (today.getTime() - startTime.getTime()) / (24 * 3600 * 1000);
                    if (useday <= INIT_WARRANTY) {
                        String pre = String.valueOf((int) (((float) (INIT_WARRANTY - useday)) / INIT_WARRANTY * 100));
                        displayFilterStatus(pre);
//                        tv_filterStatus.setText(pre + "%");
//                        tv_filiteText.setText(getString(R.string.filter_status));
////                        滤芯状态图片的更改
//                        if (Integer.parseInt(pre) >= 0 && Integer.parseInt(pre) <= 30) {
//                            tv_filiteText.setText(getString(R.string.filter_need_change));
//                            iv_filterState.setImageResource(R.drawable.filter_state1);
//                        } else if (Integer.parseInt(pre) > 30 && Integer.parseInt(pre) <= 60) {
//                            iv_filterState.setImageResource(R.drawable.filter_state2);
//                        } else if (Integer.parseInt(pre) > 60 && Integer.parseInt(pre) <= 100) {
//                            iv_filterState.setImageResource(R.drawable.filter_state3);
//                        } else {
//                            iv_filterState.setImageResource(R.drawable.filter_state0);
//                        }
                    } else {
                        tv_filterStatus.setText("0%");
                        tv_filiteText.setText(getString(R.string.filter_need_change));

                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
//
//            String usedayStr = UserDataPreference.GetUserData(getContext(), TapFilterUseDay + Mac, "");
//            Log.e("tag", "onPreExecute:" + usedayStr);
//            if ("" != usedayStr) {
//                try {
////                    Date startTime = dataFormat.parse(modifytime);
////                    Date today = new Date(System.currentTimeMillis());
////                    long useday = (today.getTime() - startTime.getTime()) / (24 * 3600 * 1000);
//                    int useday = Integer.parseInt(usedayStr);
//                    String pre = "0";
//                    if (useday < INIT_WARRANTY) {
//                        pre = String.valueOf((int) (((float) (INIT_WARRANTY - useday)) / INIT_WARRANTY * 100));
//                        tv_filterStatus.setText(pre + "%");
//                        tv_filiteText.setText(getString(R.string.filter_status));
//                        //滤芯状态图片的更改
//                        if (Integer.parseInt(pre) > 0 && Integer.parseInt(pre) <= 30) {
//                            tv_filiteText.setText(getString(R.string.filter_need_change));
//                            iv_filterState.setImageResource(R.drawable.filter_state1);
//                        } else if (Integer.parseInt(pre) > 30 && Integer.parseInt(pre) <= 60) {
//                            iv_filterState.setImageResource(R.drawable.filter_state2);
//                        } else if (Integer.parseInt(pre) > 60 && Integer.parseInt(pre) <= 100) {
//                            iv_filterState.setImageResource(R.drawable.filter_state3);
//                        } else {
//                            iv_filterState.setImageResource(R.drawable.filter_state0);
//                        }
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
//            if (params.length > 0) {
            Log.e("TAG", "UPDATE FIFTER TIME");
            shouldtoupdate = true;
            List<NameValuePair> reqPars = new ArrayList<NameValuePair>();
            reqPars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(getContext())));
            reqPars.add(new BasicNameValuePair("mac", Mac));
            String filterUrl = OznerPreference.ServerAddress(getContext()) + "/OznerDevice/FilterService";
            NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(getContext(), filterUrl, reqPars);
//            Log.e("tag", "doInBackground:" + netJsonObject.value);
            if (netJsonObject.state > 0) {
                try {
                    JSONObject jo = netJsonObject.getJSONObject();
                    String modifytime = jo.getString("modifytime");
                    int useday = jo.getInt("useday");
//                    UserDataPreference.SetUserData(getContext(), TapFilterUseDay + Mac, String.valueOf(useday));
                    UserDataPreference.SetUserData(getContext(), SaveStr + Mac, modifytime);
                    if (useday <= INIT_WARRANTY) {
                        return String.valueOf((int) (((float) (INIT_WARRANTY - useday)) / INIT_WARRANTY * 100));
                    } else {
                        return "0";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "-1";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "-1";
                }
            }
            return "-1";
//            }
//            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {

        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            Log.e("tag", "onPostExecute:" + result);
            if (WaterProbeFragment.this.isAdded() && !WaterProbeFragment.this.isDetached() && !WaterProbeFragment.this.isRemoving() && !result.equals("-1")) {

                tap.setAppdata(PageState.FilterUsePre, result);
                tap.setAppdata(PageState.FilterUpdateTime, System.currentTimeMillis());
                displayFilterStatus(result);
//                if (Integer.parseInt(result) >= 0)
//                    tv_filterStatus.setText(result + "%");
//                tv_filiteText.setText(getString(R.string.filter_status));
//                //滤芯状态的更改
//                if (Integer.parseInt(result) >= 0 && Integer.parseInt(result) <= 30) {
//                    tv_filiteText.setText(getString(R.string.filter_need_change));
//                    iv_filterState.setImageResource(R.drawable.filter_state1);
//                } else if (Integer.parseInt(result) > 30 && Integer.parseInt(result) <= 60) {
//                    iv_filterState.setImageResource(R.drawable.filter_state2);
//                } else if (Integer.parseInt(result) > 60 && Integer.parseInt(result) <= 100) {
//                    iv_filterState.setImageResource(R.drawable.filter_state3);
//                } else {
//                    iv_filterState.setImageResource(R.drawable.filter_state0);
//                }
                if (bad >= 0 && bad <= 100 && nor >= 0 && nor <= 100 && good >= 0 && good <= 100) {
                    tv_tapBadPre.setText(getResources().getString(R.string.tap_legend_bad) + "(" + bad + "%)");
                    tv_tapGenericPre.setText(getResources().getString(R.string.tap_legend_normal) + "(" + nor + "%)");
                    tv_tapHealthPre.setText(getResources().getString(R.string.tap_legend_health) + "(" + good + "%)");
                }

            }
//            tv_tapBadPre.setText(getResources().getString(R.string.tap_legend_bad)+"("+bad+"%)");
//            tv_tapGenericPre.setText(getResources().getString(R.string.tap_legend_normal)+"("+nor+"%)");
//            tv_tapHealthPre.setText(getResources().getString(R.string.tap_legend_health)+"("+good+"%)");
        }


        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {

        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            Mac = getArguments().getString("MAC");
            OznerDevice oznerDevice = OznerDeviceManager.Instance().getDevice(Mac);
            tap = (Tap) oznerDevice;
        } catch (Exception ex) {
            ex.printStackTrace();
            tap = null;
        }


//         int i= 7/0;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (asyncTask == null) {
//            asyncTask = new UiUpdateAsyncTask();
//            asyncTask.execute("s");
//        } else {
//            asyncTask.cancel(false);
//            asyncTask = null;
//            asyncTask = new UiUpdateAsyncTask();
//            asyncTask.execute();
//        }

        refreshUIData();

//        if (tap.getAppValue(PageState.FilterUpdateTime) != null) {
//            long filterUpdateTime = (long) tap.getAppValue(PageState.FilterUpdateTime);
//            Calendar calSave = Calendar.getInstance();
//            calSave.setTimeInMillis(filterUpdateTime);
//            Calendar calNow = Calendar.getInstance();
//            Log.e("CSIR", "SHOULD TO UPDATE FIFTER TIME" + (String) tap.getAppValue(PageState.FilterUsePre));
//            if (calNow.getTime() == calSave.getTime()) {
//                String pre = (String) tap.getAppValue(PageState.FilterUsePre);
////                tv_filterStatus.setText(pre + "%");
//                if (Integer.parseInt(pre) > 0) {
//                    tv_filterStatus.setText(pre + "%");
//                    new UpdateFilterAsyncTask().execute();
//                } else {
//                    tv_filterStatus.setText("暂无1");
//                    tv_tdsValue.setText(getResources().getString(R.string.probe_null));
//                }
//            } else {
//                Log.e("CSIR", "SHOULD TO UPDATE FIFTER TIME ON TIME OUT");
//                new UpdateFilterAsyncTask().execute();
////            }
//        } else {
//            Log.e("CSIR", "SHOULD TO UPDATE FIFTER TIME ON FIFTER FIRST");
        new UpdateFilterAsyncTask().execute();
//        }
    }

    //页面相关数据
    private int tds, tapTds;
    private int battery;
    private String name;
    private TapRecord[] tapRecords;
    private TapRecord[] records;
    private int count = 1, bad_count = 0, nor_count = 0, good_count = 0;
    private int bad = 0, nor = 0, good = 0;

    /**
     * 刷新水探头UI数据
     */
    private void refreshUIData() {
        try {
            InitData();
            changeState();
            if (WaterProbeFragment.this.isAdded() && !WaterProbeFragment.this.isDetached() && !WaterProbeFragment.this.isRemoving()) {
                RefreshBindDataView();
                if (adapter != null) {
                    tdsChartView.setAdapter(adapter);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("WaterProbe", "reloadUIData_Ex: " + ex.getMessage());
        }
    }


    /*
     * 初始化数据
     * */
    public void InitData() {
        name = tap.getName();
        TapSensor tapSensor = tap.Sensor();
        if (tapSensor != null) {
            tds = tapSensor.TDSFix;
            battery = tapSensor.Battery;
            Log.e("battery", battery + "");
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date time = new Date(cal.getTime().getTime() / 86400000 * 86400000);
        tapRecords = tap.TapRecordList().getRecordsByDate(time);
//        Log.e("tag",tapRecords.length+"");
        if (tapRecords != null) {
            count = tapRecords.length;
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < tapRecords.length; j++) {
                    data[tapRecords[j].time.getDate() - 1] = tapRecords[j].TDS;
                }
            }
            bad_count = 0;
            nor_count = 0;
            good_count = 0;
            count = 0;
            for (int j = 0; j < data.length; j++) {
                if (data[j] > 0) {
//                    Log.e("Cup", "data_" + j + ":" + data[j]);
                    count++;
                    if (0 < data[j] && data[j] <= CupRecord.TDS_Good_Value) {
                        good_count++;
                    } else if (CupRecord.TDS_Good_Value < data[j] && data[j] <= CupRecord.TDS_Bad_Value) {
                        nor_count++;
                    } else if (data[j] > CupRecord.TDS_Bad_Value) {
                        bad_count++;
                    }
                }
            }
            if (count > 0) {
                bad = bad_count * 100 / count;
                nor = nor_count * 100 / count;
                good = 100 - (bad + nor);
            } else {
                bad = 0;
                nor = 0;
                good = 0;
            }
        } else {
            count = 1;
            bad_count = 0;
            nor_count = 0;
            good_count = 0;
        }

//        try {
//            if (Mac != null) {
//                OznerDevice oznerDevice = OznerDeviceManager.Instance().getDevice(Mac);
//                if (oznerDevice != null) {
//                    tap = (Tap) oznerDevice;
//                    if (tap != null) {
//                        RefreshBindDataView(tap);
    }

    public void Upload() {

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        NetJsonObject netJsonObject = OznerCommand.TDSSensor(getActivity(), Mac, tap.Type(), String.valueOf(tds));
                        if (netJsonObject.state > 0) {
                            try {
                                int rank = netJsonObject.getJSONObject().getInt("rank");
                                int total = netJsonObject.getJSONObject().getInt("total");
                                ranking = (total - rank) * 100 / total;
                                Message message = new Message();
                                message.arg1 = ranking;
                                message.what = 1;
                                try {
                                    Thread.sleep(2000);
                                    rhandler.sendMessage(message);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }).start();
        } catch (Exception ex) {
//                tdsOld = 0;
        }
    }

    //region 这里好像没什么用，先注释掉
    /*
    public void UploadTds() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        NetJsonObject netJsonObject = OznerCommand.TapTDSSensor(getActivity(), Mac, tap.Type(), String.valueOf(tapTds));
                        Log.e("tagtds", netJsonObject.value + "");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }).start();
        } catch (Exception ex) {
        }
    }
    */
    //endregion

    public void RefreshBindDataView() {
        tv_name.setText(name);
        final int value = tds;
        if (value == 65535) {

            OznerApplication.setControlTextFace(tv_tdsValue);
            tv_tdsValue.setTextSize(45);
            tv_tdsValue.setText(getResources().getString(R.string.text_null));
            tv_tdsLevelText.setText(getResources().getString(R.string.text_null));
            iv_tdsLevelImg.setVisibility(View.GONE);
            lay_tdsShort.setVisibility(View.GONE);
        } else {
            if (OznerPreference.isLoginPhone(getContext())) {
                lay_tdsShort.setVisibility(View.VISIBLE);
            } else {
                lay_tdsShort.setVisibility(View.GONE);
            }

            //tds的状态
            if (value > 0 && value <= CupRecord.TDS_Good_Value) {
                tv_tdsLevelText.setText(getResources().getString(R.string.health));
                iv_tdsLevelImg.setImageResource(R.drawable.lianghao);
                iv_tdsLevelImg.setVisibility(View.VISIBLE);
            } else if (value > CupRecord.TDS_Good_Value && value <= CupRecord.TDS_Bad_Value) {
                tv_tdsLevelText.setText(getResources().getString(R.string.generic));
                iv_tdsLevelImg.setImageResource(R.drawable.yiban);
                iv_tdsLevelImg.setVisibility(View.VISIBLE);
            } else {
                if (value == 0) {
                    tv_tdsLevelText.setText(getResources().getString(R.string.text_null));
                    iv_tdsLevelImg.setVisibility(View.GONE);
                    lay_tdsShort.setVisibility(View.GONE);
                    tv_tdsValue.setText(getResources().getString(R.string.text_null));
                    OznerApplication.setControlTextFace(tv_tdsValue);
                    tv_tdsValue.setTextSize(45);
                } else {
                    tv_tdsLevelText.setText(getResources().getString(R.string.bad));
                    iv_tdsLevelImg.setImageResource(R.drawable.jingbao);
                    iv_tdsLevelImg.setVisibility(View.VISIBLE);
                }
            }
            //数字跑马灯效果

            if (value != 0) {
                if (OznerPreference.isLoginPhone(getContext())) {
                    lay_tdsShort.setVisibility(View.VISIBLE);
                } else {
                    lay_tdsShort.setVisibility(View.GONE);
                }
                OznerApplication.setControlNumFace(tv_tdsValue);
                tv_tdsValue.setTextSize(50);
                if (tdsOld != value) {
                    final ValueAnimator animator = ValueAnimator.ofInt(tdsOld, value);
                    Upload();
                    animator.setDuration(500);
                    animator.setInterpolator(new LinearInterpolator());//线性效果变化
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            Integer integer = (Integer) animator.getAnimatedValue();
                            OznerApplication.setControlNumFace(tv_tdsValue);
                            tv_tdsValue.setText("" + integer);

                        }
                    });
                    animator.start();
                    if (value > 250) {
                        waterProcess.update(100);
                    } else {
                        double s = (value / 250.00) * 100;
                        waterProcess.update((int) s);
                    }
                } else {
                    OznerApplication.setControlNumFace(tv_tdsValue);
                    tv_tdsValue.setTextSize(50);
                    if (tdsNew != 0) {
                        tv_tdsValue.setText(String.valueOf(tdsNew));
                        if (OznerPreference.isLoginPhone(getContext())) {
                            lay_tdsShort.setVisibility(View.VISIBLE);
                        } else {
                            lay_tdsShort.setVisibility(View.GONE);
                        }
                    } else {
                        OznerApplication.setControlTextFace(tv_tdsValue);
                        tv_tdsValue.setTextSize(45);
                        tv_tdsValue.setText(getResources().getString(R.string.text_null));
                        lay_tdsShort.setVisibility(View.GONE);
                    }
                }
            } else {
                OznerApplication.setControlTextFace(tv_tdsValue);
                tv_tdsValue.setTextSize(45);
                tv_tdsValue.setText(getResources().getString(R.string.text_null));
                lay_tdsShort.setVisibility(View.GONE);
                tv_tdsLevelText.setText(getResources().getString(R.string.text_null));
                iv_tdsLevelImg.setVisibility(View.GONE);
                waterProcess.update(0);
            }
        }
        try {

            int power = Math.round(tap.Sensor().getPower() * 100);
            Log.e("power", "power: " + power + " ,battery:" + tap.Sensor().BatteryFix);
            if (power == 100) {
                iv_battery.setImageResource(R.drawable.battery100);
                tv_batteryTem.setText(String.valueOf(power) + "%");
            } else if (power < 100 && power >= 50) {
                iv_battery.setImageResource(R.drawable.battery70);
                tv_batteryTem.setText(String.valueOf(power) + "%");
            } else if (power < 50 && power > 0) {
                iv_battery.setImageResource(R.drawable.battery30);
                tv_batteryTem.setText(String.valueOf(power) + "%");
            } else if (power == 0) {
                iv_battery.setImageResource(R.drawable.battery0);
                tv_batteryTem.setText(getResources().getString(R.string.text_null));
            }
//            tv_batteryTem.setText(String.valueOf((int) (tap.Sensor().getPower() * 100)) + "%");
            tv_batteryTem.setTextColor(getResources().getColor(R.color.white));
        } catch (Exception ex) {
            tv_batteryTem.setText("-");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlay_filterStatus:
                Intent filterStatusIntent = new Intent(getContext(), FilterStatusActivity.class);
                filterStatusIntent.putExtra(PageState.MAC, this.Mac);
                startActivityForResult(filterStatusIntent, 0x134);
                break;
            case R.id.iv_probe_setting:
                ShowSettingPage();
                break;
            case R.id.rlay_menu:
                if (((OznerApplication) getActivity().getApplication()).isLoginPhone()) {
                    ((MainActivity) getActivity()).myOverlayDrawer.toggleMenu();
                } else {
                    ((MainEnActivity) getActivity()).myOverlayDrawer.toggleMenu();
                }
                break;
        }
    }

    private void ShowSettingPage() {
        Intent setting = new Intent(getContext(), SetupWaterProbeActivity.class);
        setting.putExtra(PageState.MAC, this.Mac);
        startActivityForResult(setting, 0x1111);
    }

    private FootFragmentListener mFootFragmentListener;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mFootFragmentListener = (FootFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void ShowContent(int i, String mac) {
    }

    @Override
    public void ChangeRawRecord() {
    }

    @Override
    public void CupSensorChange(String address) {
        if (WaterProbeFragment.this.isAdded()) {
            if (this.Mac != null && this.Mac.equals(address)) {
                refreshUIData();

                //region 这里好像没什么用，先注释掉
                /*
                Log.e("CSIR", "TDS-WATER-TAP " + this.tap.Sensor().TDSFix);
                Calendar calTap = Calendar.getInstance();
                calTap.set(Calendar.DAY_OF_MONTH, 1);
                Date timeTap = new Date(calTap.getTime().getTime() / 86400000 * 86400000);
                records = tap.TapRecordList().getNoSyncItemDay(timeTap);
                if (records != null) {
                    for (int j = 0; j < records.length; j++) {
                        tapTds = records[j].TDS;
                    }
                    UploadTds();
                }
                */
                //endregion
            }
        }
    }

    @Override
    public void DeviceDataChange() {
    }

    @Override
    public void ContentChange(String mac, String state) {
        if (this.Mac.equals(mac) && WaterProbeFragment.this.isAdded() && !WaterProbeFragment.this.isDetached() && !WaterProbeFragment.this.isRemoving()) {
            try {
                switch (state) {
                    //正在链接中
                    case BaseDeviceIO.ACTION_DEVICE_CONNECTING:
//                        iv_data_loading.setImageResource(R.drawable.air_loding);
                        iv_data_loading.setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.air_loding));
                        tv_data_loading.setText(getResources().getString(R.string.loding_now));
                        if (iv_data_loading.getAnimation() != null)
                            iv_data_loading.getAnimation().start();
                        rlay_top1.setVisibility(View.VISIBLE);
                        //已经连接
                    case BaseDeviceIO.ACTION_DEVICE_CONNECTED:

                        if (iv_data_loading.getAnimation() != null) {
                            iv_data_loading.getAnimation().cancel();
                        }
                        rlay_top1.setVisibility(View.GONE);

                        break;
                    //已经断开连接
                    case BaseDeviceIO.ACTION_DEVICE_DISCONNECTED:
                        Log.e("tag3", "ACTION_DEVICE_DISCONNECTED");
                        if (WaterProbeFragment.this != null && WaterProbeFragment.this.isAdded()) {
//                            iv_data_loading.setImageResource(R.drawable.air_loding_fair);
                            iv_data_loading.setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.air_loding_fair));
                            tv_data_loading.setText(getResources().getString(R.string.loding_fair));
                        }
                        break;
                }
//                changeState();
                refreshUIData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    @Override
    public void RecvChatData(String data) {

    }

    public void changeState() {
        BaseDeviceIO.ConnectStatus stateIo = tap.connectStatus();
        if (stateIo == BaseDeviceIO.ConnectStatus.Connecting) {
//            iv_data_loading.setImageResource(R.drawable.air_loding);
            iv_data_loading.setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.air_loding));
            tv_data_loading.setText(getResources().getString(R.string.loding_now));
            if (iv_data_loading.getAnimation() != null)
                iv_data_loading.getAnimation().start();
            rlay_top1.setVisibility(View.VISIBLE);
        }
        if (stateIo == BaseDeviceIO.ConnectStatus.Connected) {
            try {
                if (iv_data_loading.getAnimation() != null)
                    iv_data_loading.getAnimation().cancel();
                rlay_top1.setVisibility(View.GONE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (stateIo == BaseDeviceIO.ConnectStatus.Disconnect) {
            if (WaterProbeFragment.this.isAdded()) {
                if (iv_data_loading.getAnimation() != null) {
//                    iv_data_loading.setImageResource(R.drawable.air_loding_fair);
                    iv_data_loading.setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.air_loding_fair));
                    iv_data_loading.getAnimation().cancel();
                    tv_data_loading.setText(getResources().getString(R.string.loding_fair));
                }
            }
        }
    }
}

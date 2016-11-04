package com.ozner.yiquan.WaterProbe;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.cup.CupRecord;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDeviceManager;
import com.ozner.wifi.ayla.AylaIO;
import com.ozner.yiquan.Command.FootFragmentListener;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.Device.SetupWaterPurifierActivity;
import com.ozner.yiquan.HttpHelper.NetJsonObject;
import com.ozner.yiquan.HttpHelper.NetUserVfMessage;
import com.ozner.yiquan.HttpHelper.OznerDataHttp;
import com.ozner.yiquan.MainActivity;
import com.ozner.yiquan.MainEnActivity;
import com.ozner.yiquan.R;
import com.ozner.yiquan.UIView.NetHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;


/**
 * A simple {@link Fragment} subclass.
 */
public class WaterPurifierFragment extends Fragment implements View.OnClickListener, OperateCallback<Void>, FootFragmentListener {
    boolean isPowerOn = false;
    boolean isCoolOn = false;
    boolean isHotOn = false;
    boolean state = false;
    boolean isOffLine = false;
    RelativeLayout rlay_hot, rlay_cool, rlay_power, rlay_purifier_tds, rlay_filterStatus, rlay_menu, rlay_purifier_tdsdata;
    ImageView iv_hot, iv_cool, iv_power, iv_data_loading, iv_tdsLevelImg;
    int tds1, tds2, tdsMid;
    private RotateAnimation animation;
    private LinearLayout laly_phone_nonet;
    TextView tv_hot, tv_cool, tv_power, tv_name, tv_afterValue, tv_preValue, tv_tdsLevelText, tv_filterStatus, tv_data_loading, tv_spec, tv_filiteText, tv_purifier_type;
    WaterPurifier waterPurifier;
    PurifierDetailProgress waterProcess;
    ImageView iv_purifierSetBtn = null, iv_filterState;
    private String MAC;
    private String Name;
    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static int INIT_WARRANTY = 365;// 默认有效期
    PurifierTDSFragment purifierTDSFragment;
    private static final String WaterPurifierStr = "WaterPurifierFilter";//净水器滤芯
    private static int WATER_WARRANTY = 365;// 默认有效期
    private int tdsPre = 0;//保存上传时净花前的值
    private int tdsAfter = 0;//保存上传时净化后的值

    private String cool = "";//制冷
    private String hot = "";//加热
    private String MachineType = "";//设备型号
    private String smlinkurl = "";//提示的文字
    private String buylinkurl = "";//购买滤芯
    private String tips = "";//说明书
    private String isShowewm = "";//是否显示二维码
    //    private String day="";//过期提醒时间
    private int IsShowDueDay = 0;
    private TextView tv_phone_nonet, tv_detail_nonet, purifier_nonet;
    private int isNet;
    private boolean isFirst = true;
    private boolean hasType = false;
    private boolean isZero = false;
    private String dsn = "";

    public WaterPurifierFragment() {
        // Required empty public constructor
    }

    //金泉

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MAC = getArguments().getString("MAC");
        try {
            waterPurifier = (WaterPurifier) OznerDeviceManager.Instance().getDevice(MAC);
        } catch (Exception ex) {
            ex.printStackTrace();
            waterPurifier = null;

            if (waterPurifier.IO() instanceof AylaIO) {
                try {
                    dsn = ((AylaIO) waterPurifier.IO()).DSN();
                } catch (Exception e) {
                    e.printStackTrace();
                    dsn = "";
                }
            }
        }
        View view = inflater.inflate(R.layout.purifier_detail, container, false);
        initView(view);

        if (waterPurifier != null) {
            Log.e("1234", "净水器类型:" + MAC);
//           String type = waterPurifier.info().Model;
//            new GetMachineTypeAsyncTask().execute();
        }
        OznerApplication.changeTextFont((ViewGroup) view);
//        isFirst = true;
        if (isFirst) {
            if (((OznerApplication) getActivity().getApplication()).isLanguageCN()) {
                new GetLvxinTimeAsyncTask().execute();
            }
            isFirst = false;
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UiUpdateAsyncTask asyncTask = new UiUpdateAsyncTask();
        asyncTask.execute("s");

        UiPowerUpdateAsyncTask uiPowerUpdateAsyncTask = new UiPowerUpdateAsyncTask();
        uiPowerUpdateAsyncTask.execute("ss");

        initWaterPurifierFilter();
        OznerApplication.setControlNumFace(tv_spec);
    }

    private void initView(View view) {
//        if (((OznerApplication) (getActivity().getApplication())).isLoginPhone()) {
//            view.findViewById(R.id.llay_cupHolder).setVisibility(View.VISIBLE);
//        } else {
            view.findViewById(R.id.llay_cupHolder).setVisibility(View.GONE);
//        }
        rlay_menu = (RelativeLayout) view.findViewById(R.id.rlay_menu);
        rlay_purifier_tdsdata = (RelativeLayout) view.findViewById(R.id.rlay_purifier_tdsdata);
        iv_purifierSetBtn = (ImageView) view.findViewById(R.id.iv_purifierSetBtn);
        rlay_power = (RelativeLayout) view.findViewById(R.id.rlay_powerswitch);
        rlay_cool = (RelativeLayout) view.findViewById(R.id.rlay_coolswitch);
        rlay_hot = (RelativeLayout) view.findViewById(R.id.rlay_hotswitch);
        laly_phone_nonet = (LinearLayout) view.findViewById(R.id.laly_phone_nonet);
        rlay_purifier_tds = (RelativeLayout) view.findViewById(R.id.rlay_purifier_tds);
        rlay_filterStatus = (RelativeLayout) view.findViewById(R.id.rlay_filterStatus);
        waterProcess = (PurifierDetailProgress) view.findViewById(R.id.waterProcess);
        tv_afterValue = (TextView) view.findViewById(R.id.tv_afterValue);
        tv_preValue = (TextView) view.findViewById(R.id.tv_preValue);
        iv_hot = (ImageView) view.findViewById(R.id.iv_hotswitch);
        tv_hot = (TextView) view.findViewById(R.id.tv_hotswitch);
        iv_cool = (ImageView) view.findViewById(R.id.iv_coolswitch);
        tv_cool = (TextView) view.findViewById(R.id.tv_coolswitch);
        iv_power = (ImageView) view.findViewById(R.id.iv_powerswitch);
        tv_power = (TextView) view.findViewById(R.id.tv_powerswitch);
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_spec = (TextView) view.findViewById(R.id.tv_spec);
        iv_filterState = (ImageView) view.findViewById(R.id.iv_filterState);
        tv_filiteText = (TextView) view.findViewById(R.id.tv_filiteText);
        rlay_menu.setOnClickListener(this);
        rlay_power.setOnClickListener(this);
        rlay_cool.setOnClickListener(this);
        rlay_hot.setOnClickListener(this);
        iv_purifierSetBtn.setOnClickListener(this);
        rlay_filterStatus.setOnClickListener(this);
        rlay_purifier_tds.setOnClickListener(this);
        rlay_purifier_tds.setClickable(false);
        tv_tdsLevelText = (TextView) view.findViewById(R.id.tv_tdsLevelText);
        tv_filterStatus = (TextView) view.findViewById(R.id.tv_filterStatus);
//        rlay_top1 = (RelativeLayout) view.findViewById(R.id.rlay_top1);
//        tv_data_loading = (TextView) view.findViewById(R.id.tv_data_loading);
//        iv_data_loading = (ImageView) view.findViewById(R.id.iv_data_loading);
//        animation = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        animation.setRepeatCount(-1);
//        LinearInterpolator li = new LinearInterpolator();
//        animation.setInterpolator(li);
//        animation.setFillAfter(false);
//        animation.setDuration(1000);
//        iv_data_loading.setAnimation(animation);
        iv_tdsLevelImg = (ImageView) view.findViewById(R.id.iv_tdsLevelImg);
//        tv_purifier_type=(TextView)view.findViewById(R.id.tv_purifier_type);

//        tv_phone_nonet = (TextView) view.findViewById(R.id.tv_phone_nonet);
//        tv_detail_nonet = (TextView) view.findViewById(R.id.tv_detail_nonet);
        purifier_nonet = (TextView) view.findViewById(R.id.purifier_nonet);
        purifier_nonet.setOnClickListener(this);
        try {
            this.Name = waterPurifier.getName();
            if (Name == null) {
                tv_name.setText(waterPurifier.getName());
            } else {
                tv_name.setText(Name);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void InitData() {
        isOffLine = waterPurifier.isOffline();
        if (waterPurifier.sensor().TDS1() > 0 && waterPurifier.sensor().TDS2() > 0) {
            isZero = false;
            tds1 = waterPurifier.sensor().TDS1();
            tds2 = waterPurifier.sensor().TDS2();
            if (tds1 < tds2) {
                tdsMid = tds1;
                tds1 = tds2;
                tds2 = tdsMid;
            }
        } else {
            tds1 = waterPurifier.sensor().TDS1();
            tds2 = waterPurifier.sensor().TDS2();
            isZero = true;
        }
        isPowerOn = waterPurifier.status().Power();
    }

    //
    public void BindDataToView() {
        if (isNet != 0 && !isOffLine) {
            laly_phone_nonet.setVisibility(View.INVISIBLE);
            purifier_nonet.setVisibility(View.INVISIBLE);
            rlay_purifier_tdsdata.setVisibility(View.VISIBLE);
        } else if (isNet == 0) {
            laly_phone_nonet.setVisibility(View.VISIBLE);
            purifier_nonet.setVisibility(View.VISIBLE);
            rlay_purifier_tdsdata.setVisibility(View.INVISIBLE);
        } else if (isOffLine) {
            laly_phone_nonet.setVisibility(View.INVISIBLE);
            rlay_purifier_tdsdata.setVisibility(View.INVISIBLE);
            purifier_nonet.setVisibility(View.VISIBLE);
        } else {
            purifier_nonet.setVisibility(View.INVISIBLE);
        }
        if (MachineType == null) {
//            tv_purifier_type.setText(getResources().getString(R.string.text_null));
        } else {
//            tv_purifier_type.setText(MachineType);
        }

        switchPower(isPowerOn);
        switchCool(isCoolOn);
        switchHot(isHotOn);

        if (isPowerOn) {
//            iv_tdsLevelImg.setVisibility(View.VISIBLE);
            int tds1New = tds1;
            int tds2New = tds2;

            int tds1Old = 0;
            int tds2Old = 0;
            try {
                tds1Old = Integer.parseInt(tv_preValue.getText().toString());
                tds2Old = Integer.parseInt(tv_afterValue.getText().toString());
            } catch (Exception ex) {
                tds1Old = 0;
                tds2Old = 0;
            }
            if (tds1 != 65535) {
                OznerApplication.setControlNumFace(tv_preValue);
                if (isZero) {
                    OznerApplication.setControlTextFace(tv_preValue);
                    OznerApplication.setControlTextFace(tv_afterValue);
                    tv_preValue.setText(getResources().getString(R.string.text_null));
                    tv_afterValue.setText(getString(R.string.text_null));
                    rlay_purifier_tds.setClickable(false);
                } else {
                    rlay_purifier_tds.setClickable(true);
                    tv_preValue.setText(tds1 + "");
                }
//                if (tds1 < 0) {
//                    tv_preValue.setText(getResources().getString(R.string.text_null));
//                    tv_afterValue.setText(getString(R.string.text_null));
//                    rlay_purifier_tds.setClickable(false);
//                } else {
//                    rlay_purifier_tds.setClickable(true);
//                    tv_preValue.setText(tds1 + "");
//                }
//            tv_preValue.setTextSize(45);
            } else {
                OznerApplication.setControlTextFace(tv_preValue);
                tv_preValue.setText(getResources().getString(R.string.text_null));
//            tv_preValue.setTextSize(30);
            }

            if (tds2 != 65535) {
                OznerApplication.setControlNumFace(tv_afterValue);
                if (isZero) {
                    OznerApplication.setControlTextFace(tv_preValue);
                    OznerApplication.setControlTextFace(tv_afterValue);
                    tv_preValue.setText(getResources().getString(R.string.text_null));
                    tv_afterValue.setText(getString(R.string.text_null));
                    rlay_purifier_tds.setClickable(false);
                } else {
                    rlay_purifier_tds.setClickable(true);
                    tv_preValue.setText(tds1 + "");
                }
//                if (tds2<0) {
//                    tv_preValue.setText(getResources().getString(R.string.text_null));
//                    tv_afterValue.setText(getString(R.string.text_null));
//                    rlay_purifier_tds.setClickable(false);
//                } else {
//                    rlay_purifier_tds.setClickable(true);
//                    tv_preValue.setText(tds2 + "");
//                }
//            tv_afterValue.setTextSize(45);
            } else {
                OznerApplication.setControlTextFace(tv_afterValue);
                tv_afterValue.setText(getResources().getString(R.string.text_null));
//            tv_afterValue.setTextSize(30);
            }
            if (tds1 != 65535 && tds2 != 65535) {
                if (isZero) {
                    OznerApplication.setControlTextFace(tv_preValue);
                    OznerApplication.setControlTextFace(tv_afterValue);
                    tv_preValue.setText(getResources().getString(R.string.text_null));
                    tv_afterValue.setText(getString(R.string.text_null));
                    rlay_purifier_tds.setClickable(false);
                } else {
                    rlay_purifier_tds.setClickable(true);
                    tv_preValue.setText(tds1 + "");
                    tv_afterValue.setText(tds2 + "");

                    if (tds1Old != tds1New) {
                        final ValueAnimator animator = ValueAnimator.ofInt(tds1Old, tds1New);
                        animator.setDuration(500);
                        animator.setInterpolator(new LinearInterpolator());//线性效果变化
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                Integer integer = (Integer) animator.getAnimatedValue();
                                tv_preValue.setText("" + integer);
                                OznerApplication.setControlNumFace(tv_preValue);
//                        tv_preValue.setTextSize(45);
                            }
                        });
                        animator.start();
                        if (tds1 > 250) {
                            waterProcess.update(100, Math.round((tds2 / 250f) * 100));
                        } else {
                            try {
                                waterProcess.update(Math.round((tds1 / 250f) * 100), Math.round((tds2 / 250f) * 100));
//                        Log.e("TAG", "WateProcess.Update" + Math.round((value / 400f) * 100));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                waterProcess.update(0, 0);
                            }
                        }
                    } else {
                        OznerApplication.setControlNumFace(tv_preValue);
                        tv_preValue.setText(String.valueOf(tds1New));
                        tds1Old = Integer.parseInt(tv_preValue.getText().toString());
                    }
                    if (tds2Old != tds2New) {
                        final ValueAnimator animator = ValueAnimator.ofInt(tds2Old, tds2New);
                        animator.setDuration(500);
                        animator.setInterpolator(new LinearInterpolator());//线性效果变化
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                Integer integer = (Integer) animator.getAnimatedValue();
                                OznerApplication.setControlNumFace(tv_afterValue);
                                tv_afterValue.setText("" + integer);
//                        tv_afterValue.setTextSize(45);
                            }
                        });
                        animator.start();
                        if (tds2 >= 250) {
                            waterProcess.update(Math.round((tds1 / 250f) * 100), 100);
                        } else {
                            try {
                                waterProcess.update(Math.round((tds1 / 250f) * 100), Math.round((tds2 / 250f) * 100));
//                        Log.e("TAG", "WateProcess.Update" + Math.round((value / 400f) * 100));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                waterProcess.update(0, 0);
                            }
                        }
                    } else {
                        OznerApplication.setControlNumFace(tv_afterValue);
                        tv_afterValue.setText(String.valueOf(tds2New));
                        tds2Old = Integer.parseInt(tv_afterValue.getText().toString());
                    }
                    rlay_purifier_tds.setClickable(true);

                    //上传净水器净化前后的TDS值
                    if (waterPurifier.sensor().TDS1() > 0 && waterPurifier.sensor().TDS2() > 0) {
                        if (waterPurifier.sensor().TDS1() > waterPurifier.sensor().TDS2()) {
                            if (waterPurifier.sensor().TDS1() != tdsPre || waterPurifier.sensor().TDS2() != tdsAfter) {
                                tdsPre = waterPurifier.sensor().TDS1();
                                tdsAfter = waterPurifier.sensor().TDS2();
                                uploadTds(MAC, waterPurifier.Type(), tdsPre, tdsAfter);
                            }
                        } else {
                            if (waterPurifier.sensor().TDS2() != tdsPre || waterPurifier.sensor().TDS1() != tdsAfter) {
                                tdsPre = waterPurifier.sensor().TDS2();
                                tdsAfter = waterPurifier.sensor().TDS1();
                                uploadTds(MAC, waterPurifier.Type(), tdsPre, tdsAfter);
                            }
                        }
                    }
                    if (tds2 > 0 && tds2 <= CupRecord.TDS_Good_Value) {
                        tv_tdsLevelText.setText(getResources().getString(R.string.health));
                        iv_tdsLevelImg.setImageResource(R.drawable.lianghao);
                        iv_tdsLevelImg.setVisibility(View.VISIBLE);
                        tv_tdsLevelText.setVisibility(View.VISIBLE);
                    } else if (tds2 > CupRecord.TDS_Good_Value && tds2 <= CupRecord.TDS_Bad_Value) {
                        tv_tdsLevelText.setText(getResources().getString(R.string.generic));
                        iv_tdsLevelImg.setImageResource(R.drawable.yiban);
                        iv_tdsLevelImg.setVisibility(View.VISIBLE);
                        tv_tdsLevelText.setVisibility(View.VISIBLE);
                    } else {
                        if (tds2 == 0) {
                            tv_tdsLevelText.setVisibility(View.GONE);
                            iv_tdsLevelImg.setVisibility(View.GONE);
                        } else {
                            tv_tdsLevelText.setText(getResources().getString(R.string.bad));
                            iv_tdsLevelImg.setImageResource(R.drawable.jingbao);
                            iv_tdsLevelImg.setVisibility(View.VISIBLE);
                        }
                    }
                }
            } else {
                rlay_purifier_tds.setClickable(false);
            }
        } else {
            OznerApplication.setControlTextFace(tv_preValue);
            tv_preValue.setText(getResources().getString(R.string.text_null));
            OznerApplication.setControlTextFace(tv_afterValue);
            tv_afterValue.setText(getResources().getString(R.string.text_null));
            tv_tdsLevelText.setText(getResources().getString(R.string.text_null));
            iv_tdsLevelImg.setVisibility(View.GONE);
            waterProcess.update(0, 0);
            rlay_purifier_tds.setClickable(false);
        }
    }

    private void uploadTds(final String mac, final String type, final int pre, final int after) {
        try {
            final Activity activity = getActivity();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<NetUserVfMessage> listresult = new ArrayList<NetUserVfMessage>();
                    String url = OznerPreference.ServerAddress(activity) + "/OznerDevice/TDSSensor";
                    List<NameValuePair> params = new LinkedList<NameValuePair>();
                    params.add(new BasicNameValuePair(OznerPreference.UserToken, OznerPreference.UserToken(activity)));
                    params.add(new BasicNameValuePair("mac", mac));
                    params.add(new BasicNameValuePair("type", type));
                    params.add(new BasicNameValuePair("tds", String.valueOf(after)));
                    params.add(new BasicNameValuePair("beforetds", String.valueOf(pre)));
                    params.add(new BasicNameValuePair("dsn", dsn));
                    NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, url, params);
                    Log.e("tag", "上传净水器tds：" + netJsonObject.value);
                }
            }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("tag", "上传净水器tds_异常：" + ex.getMessage());
        }
    }


    private class UiPowerUpdateAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        @Override
        protected void onPreExecute() {
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected NetJsonObject doInBackground(String... params) {


            if (isNet != 0) {
                if (params != null) {
                    List<NameValuePair> pars = new ArrayList<>();
                    pars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(getContext())));
                    pars.add(new BasicNameValuePair("type", MAC));
                    String filterUrl = OznerPreference.ServerAddress(getContext()) + "OznerServer/GetMachineType";
//                    Log.e("123456", "doInBackground: url+" + filterUrl);
                    NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(getContext(), filterUrl, pars);
//                    Log.e("123456", "doInBackground: +" + netJsonObject.value);
                    return netJsonObject;
                }
            }
            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {

        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (netJsonObject != null && netJsonObject.state > 0) {
                hasType = true;
                try {
//                    Log.e("net", "value==="+netJsonObject.value);
                    JSONObject jsonObject = netJsonObject.getJSONObject().getJSONObject("data");
//                    Log.e("net", "value==="+jsonObject.toString());
                    MachineType = jsonObject.getString("MachineType");
                    smlinkurl = jsonObject.getString("smlinkurl");
                    buylinkurl = jsonObject.getString("buylinkurl").trim();
                    tips = jsonObject.getString("tips");
                    IsShowDueDay = Integer.parseInt(jsonObject.getString("days"));
                    isShowewm = jsonObject.getString("boolshow");
                    Log.e("net", "isShowewm===" + isShowewm);
                    String Attr = jsonObject.getString("Attr");
                    if (!Attr.equals("null")) {
                        cool = Attr.split(",")[0].split(":")[1];
                        hot = Attr.split(",")[1].split(":")[1];

                    }
//                    initHotCool();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("1234", "coolhot");
                }
            } else {
                hasType = false;
            }


//            if (WaterPurifierFragment.this != null && WaterPurifierFragment.this.isAdded()) {
////                rlay_purifier_tds.setClickable(false);
//                BindDataToView();
//            }
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {

        }
    }


    private class UiUpdateAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            InitData();
            initHotCool();
            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {

        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String string) {
            if (WaterPurifierFragment.this != null && WaterPurifierFragment.this.isAdded()) {
//                if (isZero) {
//                    rlay_purifier_tds.setClickable(false);
//                    tv_preValue.setText(getString(R.string.text_null));
//                    tv_afterValue.setText(getString(R.string.text_null));
//                } else {
                BindDataToView();
                switchCool(isCoolOn);
                switchHot(isHotOn);
//                }
            }
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {

        }
    }


    //初始化加热制冷
    private void initHotCool() {
        if ("true".equals(cool)) {
            isCoolOn = waterPurifier.status().Cool();
            Log.e("trcool", "sssssssssssssss");

        }
        if ("true".equals(hot)) {
            isHotOn = waterPurifier.status().Hot();
            Log.e("trcool", "dddddddddddddddddddd");

        }
    }

    //    cc
    private class GetLvxinTimeAsyncTask extends AsyncTask<String, Void, NetJsonObject> {

        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params != null) {
                List<NameValuePair> pars = new ArrayList<>();
//                pars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(getContext())));
                pars.add(new BasicNameValuePair("mac", MAC));
                String filterUrl = OznerPreference.ServerAddress(getContext()) + "OznerDevice/GetMachineLifeOutTime";
//                Log.e("123456", "doInBackground: url+" + filterUrl);
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(getContext(), filterUrl, pars);
//                Log.e("123456", "GetLvxinTimeAsyncTask: +" + netJsonObject.value);
                return netJsonObject;
            }
            return null;
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (netJsonObject != null && netJsonObject.state > 0) {
                try {
                    String date = netJsonObject.getJSONObject().getString("time");
                    try {
                        Date lvxinTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(date);
                        if (lvxinTime.getTime() + IsShowDueDay * 24 * 1000 * 3600 < new Date().getTime()) {
                            told();
                        }
//                        Log.e("123456",lvxinTime.getTime()+"==========" + new Date().getTime());
                    } catch (Exception e) {
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void told() {
        new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.filter_need_change))
//                .setPositiveButton(getString(R.string.buy_air_lvxin), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        buyFilter();
//                        dialog.dismiss();
//                    }
//                })
                .setNegativeButton(getString(R.string.airOutside_know), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    /*
*净水器滤芯服务时间本地初始化
*/
    private void initWaterPurifierFilter() {
        boolean isHasUpdate = false;
        String purifierSaveValue = UserDataPreference.GetUserData(getContext(), WaterPurifierStr + MAC, "");
        Log.e("tag", "WaterPurifier_Filter_local:" + purifierSaveValue);
        if (!"".equals(purifierSaveValue)) {
            try {
                JSONObject jsonObject = new JSONObject(purifierSaveValue);
                String time = jsonObject.getString("time");
                String nowtime = jsonObject.getString("nowtime");
                Date nowDate = new Date(nowtime);
                Date endDate = new Date(time);
                Calendar nowCal = Calendar.getInstance();
                nowCal.setTime(nowDate);
                Calendar starCal = Calendar.getInstance();
                starCal.setTime(endDate);
                float remainDay = (starCal.getTimeInMillis() - nowCal.getTimeInMillis()) / 1000.0f / 3600 / 24;
                starCal.add(Calendar.YEAR, -1);
                long totalDays = (endDate.getTime() - starCal.getTimeInMillis()) / 1000 / 3600 / 24;
                float pre = remainDay / totalDays * 100;
                setFilterStatusPre((int) Math.ceil(pre));
                if (nowCal.getTime().equals(Calendar.getInstance().getTime())) {
                    isHasUpdate = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!isHasUpdate) {
            new UpdateWaterPurifierFilterTask(getContext(), OznerPreference.UserToken(getContext())).execute(MAC);
        }
    }

    private class UpdateWaterPurifierFilterTask extends AsyncTask<String, Integer, NetJsonObject> {
        private Context mContext;
        private String token;
        private String mac;

        public UpdateWaterPurifierFilterTask(Context context, String usertoken) {
            this.mContext = context;
            this.token = usertoken;
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params.length > 0) {
                mac = params[0];
                List<NameValuePair> reqPars = new ArrayList<NameValuePair>();
                reqPars.add(new BasicNameValuePair("usertoken", token));
                reqPars.add(new BasicNameValuePair("mac", mac));
                String filterUrl = OznerPreference.ServerAddress(mContext) + "/OznerDevice/GetMachineLifeOutTime";
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(mContext, filterUrl, reqPars);
                return netJsonObject;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (netJsonObject != null) {
                if (netJsonObject.state > 0) {
                    UserDataPreference.SetUserData(getContext(), WaterPurifierStr + mac, netJsonObject.value);
                    try {
                        JSONObject jsonObject = new JSONObject(netJsonObject.value);
                        String time = jsonObject.getString("time");
                        String nowtime = jsonObject.getString("nowtime");
                        Date nowDate = new Date(nowtime);
                        Date endDate = new Date(time);
                        long nt = nowDate.getTime();
                        long et = endDate.getTime();
                        if (et > nt) {
                            float remainDay = (et - nt) / (1000.0f * 3600 * 24);
                            Calendar startCal = Calendar.getInstance();
                            startCal.setTime(endDate);
                            startCal.add(Calendar.YEAR, -1);
                            float totalDays = (endDate.getTime() - startCal.getTimeInMillis()) / 1000.0f / 3600 / 24;
                            float pre = (remainDay / totalDays) * 100;
                            setFilterStatusPre((int) Math.ceil(pre));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.e("tag", "WaterPurifier_Filter_Ex;" + ex.getMessage());
                    }
                }
                Log.e("tag", "WaterPurifier_Filter_Net:" + netJsonObject.value);
            }
            super.onPostExecute(netJsonObject);
        }
    }

    void setFilterStatusPre(int fitPre) {
        if (fitPre > 100)
            fitPre = 100;
        if (fitPre > 0) {
            //滤芯状态图片的更改
            if (fitPre <= 8) {
                iv_filterState.setImageResource(R.drawable.filter_state1);
                tv_filiteText.setText(R.string.filter_need_change);
            } else if (fitPre > 8 && fitPre <= 60) {
                tv_filiteText.setText(R.string.filter_status);
                iv_filterState.setImageResource(R.drawable.filter_state2);
            } else if (fitPre > 60 && fitPre <= 100) {
                tv_filiteText.setText(R.string.filter_status);
                iv_filterState.setImageResource(R.drawable.filter_state3);
            }
            String pre = String.valueOf(fitPre);
            tv_filterStatus.setText(pre + "%");
        } else {
            tv_filterStatus.setText("0%");
            tv_filiteText.setText(R.string.filter_need_change);
            iv_filterState.setImageResource(R.drawable.filter_state0);
        }

    }

    private void showDialog() {
        final Dialog airDialog = new Dialog(getContext(), R.style.dialog_style);
        airDialog.setContentView(R.layout.device_nonet_notice2);
        TextView purifier_tip = (TextView) airDialog.findViewById(R.id.purifier_tip);
        purifier_tip.setText("3" + getString(R.string.device_nonet_notice2));
        airDialog.getWindow().setGravity(Gravity.BOTTOM);
        WindowManager windowManager = getActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = airDialog.getWindow().getAttributes();
        lp.width = display.getWidth(); // 设置宽度
        Window window = airDialog.getWindow();
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.dialogstyle);
        airDialog.findViewById(R.id.tv_air_know).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                airDialog.dismiss();
            }
        });
        airDialog.show();
    }

    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        bundle.putString("MAC", MAC);
        switch (v.getId()) {
            case R.id.purifier_nonet:
                showDialog();
                break;
            case R.id.rlay_menu:
                if (((OznerApplication) (getActivity().getApplication())).isLoginPhone()) {
                    ((MainActivity) getActivity()).myOverlayDrawer.toggleMenu();
                } else {
                    ((MainEnActivity) getActivity()).myOverlayDrawer.toggleMenu();
                }
                break;
            case R.id.rlay_powerswitch:
                isPowerOn = !waterPurifier.status().Power();
                switchPower(isPowerOn);
//                 waterPurifier.status().setPower(waterPurifier.status().Power(),this);
                waterPurifier.status().setPower(isPowerOn, this);
                break;
            case R.id.rlay_coolswitch:
                if (hasType) {
                    if ("true".equals(cool)) {
                        if (isPowerOn) {
                            switchCool(!isCoolOn);
                            waterPurifier.status().setCool(!waterPurifier.status().Cool(), this);
                            Log.e("TR", waterPurifier.status().Cool() + "dddddddddd");
                        } else {
                            Toast.makeText(getContext(), getString(R.string.please_open_power), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!"".equals(tips)) {
//                            if ("不支持此功能".equals(tips)){
                            if (tips.contains("不支持此功能")) {
                                Toast.makeText(getActivity(), getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), tips, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.get_waterpurifiertype_fail), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rlay_hotswitch:
                if (hasType) {
                    if ("true".equals(hot)) {
                        if (isPowerOn) {
                            switchHot(!isHotOn);
                            waterPurifier.status().setHot(!waterPurifier.status().Hot(), this);
                        } else {
                            Toast.makeText(getContext(), getString(R.string.please_open_power), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!"".equals(tips)) {
//                            if ("不支持此功能".equals(tips)){
                            if (tips.contains("不支持此功能")) {
                                Toast.makeText(getActivity(), getString(R.string.not_support), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), tips, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.get_waterpurifiertype_fail), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iv_purifierSetBtn:
                Intent intentSetting = new Intent(getContext(), SetupWaterPurifierActivity.class);
                intentSetting.putExtra("MAC", MAC);
                if (smlinkurl != null) {
                    intentSetting.putExtra("smlinkurl", smlinkurl);

                } else {
                    intentSetting.putExtra("smlinkurl", "");

                }
                startActivityForResult(intentSetting, 111);
                break;
            case R.id.rlay_purifier_tds:
                purifierTDSFragment = new PurifierTDSFragment();
                purifierTDSFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.framen_main_con, purifierTDSFragment).addToBackStack("Two").commitAllowingStateLoss();
//                getActivity().getSupportFragmentManager().executePendingTransactions();
                break;
            case R.id.rlay_filterStatus:
                Intent filterStatusIntent = new Intent(getContext(), FilterStatusActivity.class);
                filterStatusIntent.putExtra("MAC", MAC);

                if (buylinkurl != null) {
                    filterStatusIntent.putExtra("buylinkurl", buylinkurl);
                    filterStatusIntent.putExtra("isShowewm", isShowewm);
                } else {
                    filterStatusIntent.putExtra("buylinkurl", "");
                    filterStatusIntent.putExtra("isShowewm", "");
                }
                startActivityForResult(filterStatusIntent, 526);
                break;
        }
    }
//    依泉系列去除商城相关一切
//    private void buyFilter() {
//        String mobile = UserDataPreference.GetUserData(getActivity().getApplicationContext(), UserDataPreference.Mobile, null);
//        String usertoken = OznerPreference.UserToken(getActivity().getApplicationContext());
//        Intent buyFilterIntent = new Intent(getActivity(), WebActivity.class);
//        String shopUrl = CenterUrlContants.formatTapShopUrl(mobile, usertoken, "zh", "zh");
//        if (buylinkurl != null && buylinkurl != "") {
//            shopUrl = CenterUrlContants.formatUrl(buylinkurl, mobile, usertoken, "zh", "zh");
//        }
//        buyFilterIntent.putExtra("URL", shopUrl);
//        Log.e("123456", shopUrl);
//        startActivity(buyFilterIntent);
//    }

    @Override
    public void onFailure(Throwable var1) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
//                InitData();


                if (isWaterPuriferAdd())

                    Toast.makeText(getContext(), getString(R.string.send_status_fail), Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void onSuccess(Void var1) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
//                InitData();
                if (isWaterPuriferAdd())
                    Toast.makeText(getContext(), getString(R.string.send_status_success), Toast.LENGTH_SHORT);
            }
        });
    }

    private boolean isWaterPuriferAdd() {
        return !WaterPurifierFragment.this.isRemoving() && !WaterPurifierFragment.this.isDetached() && WaterPurifierFragment.this.isAdded();
    }


    public void switchPower(boolean isOn) {
        if (!isOn && isCoolOn) {
            switchCool(isOn);
        }
        if (!isOn && isHotOn) {
            switchHot(isOn);
        }
        iv_power.setSelected(isOn);
        rlay_power.setSelected(isOn);
        tv_power.setSelected(isOn);
//        waterPurifier.status().setPower(isOn, this);
        isPowerOn = isOn;
    }

    public void switchCool(boolean isOn) {
        rlay_cool.setSelected(isOn);
        iv_cool.setSelected(isOn);
        tv_cool.setSelected(isOn);
        isCoolOn = isOn;
    }

    public void switchHot(boolean isOn) {
        rlay_hot.setSelected(isOn);
        iv_hot.setSelected(isOn);
        tv_hot.setSelected(isOn);
        isHotOn = isOn;
    }

    private FootFragmentListener mFootFragmentListener;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mFootFragmentListener = (FootFragmentListener) activity;
            isNet = NetHelper.checkNetwork(getContext());
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
        if (this.MAC.equals(address)) {
            //此处应该执行数据变换异步操作
            new UiUpdateAsyncTask().execute();
//            Toast.makeText(getContext(),"jsq",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void DeviceDataChange() {

    }

    @Override
    public void ContentChange(String mac, String state) {
//        if(isNet!=0){
        if (this.MAC.equals(mac) && WaterPurifierFragment.this.isAdded() && !WaterPurifierFragment.this.isDetached() && !WaterPurifierFragment.this.isRemoving()) {
            new UiUpdateAsyncTask().execute();
        }
//        }else{
//            Log.e("trnet","ffff");
//        }
    }

    @Override
    public void RecvChatData(String data) {

    }

    public void changeState() {
//        BaseDeviceIO.ConnectStatus stateIo = waterPurifier.connectStatus();
//        if (stateIo == BaseDeviceIO.ConnectStatus.Connecting) {
//            iv_data_loading.setImageResource(R.drawable.air_loding);
//            tv_data_loading.setText(getResources().getString(R.string.loding_now));
//            if (iv_data_loading.getAnimation() != null)
//                iv_data_loading.getAnimation().start();
//            rlay_top1.setVisibility(View.VISIBLE);
//        }
//        if (stateIo == BaseDeviceIO.ConnectStatus.Connected) {
//            if (iv_data_loading.getAnimation() != null)
//                iv_data_loading.getAnimation().cancel();
//            rlay_top1.setVisibility(View.GONE);
//        }
//        if (stateIo == BaseDeviceIO.ConnectStatus.Disconnect) {
//            iv_data_loading.setImageResource(R.drawable.air_loding_fair);
//            if (iv_data_loading.getAnimation() != null)
//                iv_data_loading.getAnimation().cancel();
//            tv_data_loading.setText(getResources().getString(R.string.loding_fair));
//        }

    }
}

package com.ozner.yiquan.WaterProbe;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.ozner.bluetooth.BluetoothIO;
import com.ozner.yiquan.Command.DeviceData;
import com.ozner.yiquan.Command.FootFragmentListener;
import com.ozner.yiquan.Command.OznerCommand;
import com.ozner.yiquan.Command.PageState;
import com.ozner.cup.Cup;
import com.ozner.cup.CupRecord;
import com.ozner.cup.CupRecordList;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.Device.SetupGlassActivity;
import com.ozner.yiquan.HttpHelper.NetJsonObject;
import com.ozner.yiquan.Main.BaseMainActivity;
import com.ozner.yiquan.R;
import com.ozner.yiquan.TeachGuide.TeachGuideForCupActivity;
import com.ozner.yiquan.UIView.FirmwareUpgrade;
import com.ozner.yiquan.WaterCup.TDSFragment;
import com.ozner.yiquan.WaterCup.WaterQuantityFragment;
import com.ozner.yiquan.WaterCup.WaterTemperatureFragment;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import org.json.JSONException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ALL")
public class WaterCupFragment extends Fragment implements View.OnClickListener, FootFragmentListener {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private DeviceData deviceData;
    private boolean isCupGuideFirst;
    LinearLayout llay_waterVolum, llay_waterTem, lay_tdsShort;
    RelativeLayout rlay_tdsdetail, rlay_menu, rlay_top1, layout_cup_detail;
    ImageView iv_battery, iv_cupSet, iv_menu, iv_waterValum, iv_waterTemp;
    ImageView iv_tdsLevelImg, iv_data_loading;
    TextView tv_tdsLevelText, tv_tdsValue, tv_tdsShort;
    TextView tv_batteryText, tv_name, tv_newDevice, tv_waterTarget;
    TextView tv_waterTemText, tv_waterVolum, tv_waterTempTip, tv_data_loading;
    WaterDetailProgress waterProcess;
    TextView tv_tdsTitle, tv_per, tv_after;
    private Cup cup;
    //获取饮水量
    WaterQuantityFragment waterQuantityFragment;
    Date time;

    int[] volum;

    //获取水温

    WaterTemperatureFragment waterTemperatureFragment;

    //tds
    TDSFragment tdsFragment;
    private String Mac;
    private RotateAnimation animation;
    int tdsOld, tdsNew;
    int ranking;
    //    ValueAnimator animator;
    private OznerDevice device;
    private float POWER;
    private String firmwarNum;//固件版本号
    /* 下载保存路径 */
    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    /*记录文件总大小*/
    private int totalLen;
    private int readLen;

    private Handler tdshandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    int rank = msg.arg1;
                    tv_tdsShort.setText(rank + "%");
                    ranking = rank;
                    break;
                case 2:
                    if (msg.arg2 > 0) {
                        String url = (String) msg.obj;
                        downloadFile(url);
                    } else {
                        Log.e("error", "nullPath");
                    }
                    break;
            }
        }
    };

    public WaterCupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Mac = getArguments().getString("MAC");
            Log.e("ffState", Mac + "==========mac");
            device = OznerDeviceManager.Instance().getDevice(Mac);
            getFirmwarNum(device);
        } catch (Exception e) {
        }
    }

    private void getFirmwarNum(OznerDevice device) {
        if (device instanceof Cup) {
            cup = (Cup) device;
            BluetoothIO io = (BluetoothIO) cup.IO();
            if (io != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                firmwarNum = sdf.format(new Date(io.getFirmware()));
            }
            cuptype = cup.Type();
            Log.e("ffState", cuptype + "==========");
            if (cup != null && (cup.connectStatus() == BaseDeviceIO.ConnectStatus.Connected)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                if (!sdf.format(new Date()).equals(cup.getAppValue("checkUpdate"))) {
                    if (firmwarNum != null) {
                        Log.e("ffState", firmwarNum + "==========");
                        uploadFirmwarUrl(cuptype, firmwarNum);//更新固件的方法
                    }
                }
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cup_detail, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        layout_cup_detail = (RelativeLayout) view.findViewById(R.id.layout_cup_detail);
        if (((OznerApplication) getActivity().getApplication()).isLoginPhone()) {
            view.findViewById(R.id.llay_cupHolder).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.llay_cupHolder).setVisibility(View.GONE);
        }

        waterProcess = (WaterDetailProgress) view.findViewById(R.id.waterProcess);//水质圆弧
        //饮水量和水温的布局
        llay_waterVolum = (LinearLayout) view.findViewById(R.id.llay_waterVolum);//饮水量详情
        llay_waterVolum.setOnClickListener(this);
        llay_waterTem = (LinearLayout) view.findViewById(R.id.llay_waterTem);//水温详情
        rlay_menu = (RelativeLayout) view.findViewById(R.id.rlay_menu);
        iv_menu = (ImageView) view.findViewById(R.id.iv_menu);//侧边栏菜单
        tv_newDevice = (TextView) view.findViewById(R.id.tv_newDevice);//新增设备Tips
        tv_name = (TextView) view.findViewById(R.id.tv_name);//设备名称
        iv_tdsLevelImg = (ImageView) view.findViewById(R.id.iv_tdsLevelImg);//水质tds图标
        tv_tdsLevelText = (TextView) view.findViewById(R.id.tv_tdsLevelText);//水质tds提示
//        View tvXX=view.findViewById(R.id.tv_tdsValue);
        tv_tdsValue = (TextView) view.findViewById(R.id.tv_tdsValue);//水质tds值
        tv_tdsShort = (TextView) view.findViewById(R.id.tv_tdsShort);//水质tds排名
        rlay_tdsdetail = (RelativeLayout) view.findViewById(R.id.rlay_tdsdetail);//tds详情信息
        iv_battery = (ImageView) view.findViewById(R.id.iv_battery);//电池电量图标
        tv_batteryText = (TextView) view.findViewById(R.id.tv_batteryText);//电池电量数值,百分比
        iv_cupSet = (ImageView) view.findViewById(R.id.iv_cupSet);//设置按钮
        tv_waterVolum = (TextView) view.findViewById(R.id.tv_waterVolum);//饮水量
        tv_waterTarget = (TextView) view.findViewById(R.id.tv_waterTarget);//饮水目标
        iv_waterValum = (ImageView) view.findViewById(R.id.iv_waterValum);//水量图标
        iv_waterTemp = (ImageView) view.findViewById(R.id.iv_waterTemp);//水温图标
        tv_waterTemText = (TextView) view.findViewById(R.id.tv_waterTemText);//水温情况
        tv_waterTempTip = (TextView) view.findViewById(R.id.tv_waterTempTip);//水温提示语
        lay_tdsShort = (LinearLayout) view.findViewById(R.id.lay_tdsShort);

        tv_tdsTitle = (TextView) view.findViewById(R.id.tv_tdsTitle);
        tv_per = (TextView) view.findViewById(R.id.tv_per);
        tv_after = (TextView) view.findViewById(R.id.tv_after);
//
        layout_cup_detail.setOnClickListener(this);
        iv_cupSet.setOnClickListener(this);
        rlay_menu.setOnClickListener(this);
        sharedPreferences = getContext().getSharedPreferences("MyState", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isCupGuideFirst = sharedPreferences.getBoolean("isCupGuideFirst", true);
        if (isCupGuideFirst) {
            layout_cup_detail.setOnClickListener(this);
        }

        rlay_top1 = (RelativeLayout) view.findViewById(R.id.rlay_top1);
        iv_data_loading = (ImageView) view.findViewById(R.id.iv_data_loading);
        tv_data_loading = (TextView) view.findViewById(R.id.tv_data_loading);
        animation = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatCount(-1);
        LinearInterpolator li = new LinearInterpolator();
        animation.setInterpolator(li);
        animation.setFillAfter(false);
        animation.setDuration(1000);
        iv_data_loading.setAnimation(animation);


        if (!isCupGuideFirst) {
            layout_cup_detail.setOnClickListener(this);
            iv_cupSet.setOnClickListener(this);
            rlay_menu.setOnClickListener(this);
        }
        OznerApplication.changeTextFont((ViewGroup) view);
        initTypeFace();
        if (((OznerApplication) getActivity().getApplication()).isLoginPhone()) {
            lay_tdsShort.setVisibility(View.VISIBLE);
        } else {
            lay_tdsShort.setVisibility(View.GONE);
        }
    }


    private void initTypeFace() {
        tv_tdsShort.setTypeface(OznerApplication.numFace);
        tv_tdsShort.getPaint().setFakeBoldText(false);
        tv_waterVolum.setTypeface(OznerApplication.numFace);
        tv_waterVolum.getPaint().setFakeBoldText(false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        InitData();
        try {
            int battery = (int) (POWER * 100 + 0.5);
            if (battery == 100) {
                tv_batteryText.setText(battery + "%");
                iv_battery.setImageResource(R.drawable.battery100);
            } else if (battery < 100 && battery >= 50) {
                tv_batteryText.setText(battery + "%");
                iv_battery.setImageResource(R.drawable.battery70);
            } else if (battery < 50 && battery > 0) {
                tv_batteryText.setText(battery + "%");
                iv_battery.setImageResource(R.drawable.battery30);
            } else if (battery == 0) {
                tv_batteryText.setText(battery + "%");
                iv_battery.setImageResource(R.drawable.battery0);
            } else {
                tv_batteryText.setText(getString(R.string.text_null));
                iv_battery.setImageResource(R.drawable.battery0);
            }
        } catch (Exception ex) {
            tv_batteryText.setText(getString(R.string.text_null));
        }
        try {
            if (65535 == TemperatureFix)
                tv_waterTemText.setText(getResources().getString(R.string.text_null));
            else {
                llay_waterTem.setOnClickListener(this);
                if (TemperatureFix > 0 && TemperatureFix <= CupRecord.Temperature_Low_Value) {
                    tv_waterTemText.setText(getResources().getString(R.string.good_temperature));
                    iv_waterTemp.setImageResource(R.drawable.temp_low);
                    tv_waterTempTip.setText(getResources().getString(R.string.cupDetail_waterDrinkNo));
                } else if (TemperatureFix > CupRecord.Temperature_Low_Value && TemperatureFix <= CupRecord.Temperature_High_Value) {
                    tv_waterTemText.setText(getResources().getString(R.string.normal_temperature));
                    iv_waterTemp.setImageResource(R.drawable.temp_mid);
                    tv_waterTempTip.setText(getResources().getString(R.string.cupDetail_waterDrinkOk));
                } else if (TemperatureFix > CupRecord.Temperature_High_Value && TemperatureFix <= 100) {
                    tv_waterTemText.setText(getResources().getString(R.string.bad_temperature));
                    iv_waterTemp.setImageResource(R.drawable.temp_high);
                    tv_waterTempTip.setText(getResources().getString(R.string.cupDetail_waterDrinkNo));
                } else {
                    tv_waterTemText.setText(getResources().getString(R.string.text_null));
                    tv_waterTempTip.setText(getResources().getString(R.string.temp_null));
                }
            }
        } catch (Exception ex) {
            tv_waterTemText.setText(getString(R.string.text_null));
            tv_waterTempTip.setText(getResources().getString(R.string.temp_null));
        }


        if (WaterCupFragment.this != null && WaterCupFragment.this.isAdded()) {
            try {
                if (TDSFix > 5000) {
                    setTDSTextFace();
                    tv_tdsValue.setText(getResources().getString(R.string.null_text));
                    tv_tdsLevelText.setText(getResources().getString(R.string.text_null));
                    lay_tdsShort.setVisibility(View.GONE);
                    iv_tdsLevelImg.setVisibility(View.GONE);
                    rlay_tdsdetail.setEnabled(false);
                } else {
                    RefreshBindDataView();
                    tv_name.setText(device.getName());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                RefreshBindVolume();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            changeState();
        }

    }

    private String cuptype;
    private int TDSFix = 0;
    private int TemperatureFix;
    private CupRecordList cupRecordLists;
    private CupRecord cupRecord;

    /*
    * 初始化数据
    * */
    public void InitData() {
        TDSFix = cup.Sensor().TDSFix;
        Log.e("TDS", TDSFix + "");
        cupRecordLists = cup.Volume();
        TemperatureFix = cup.Sensor().TemperatureFix;
        POWER = cup.Sensor().getPower();
        Log.e("power", cup.Sensor().getPower() + "==========");
        if (cupRecordLists != null) {
            Date time = new Date(new Date().getTime() / 86400000 * 86400000);
            cupRecord = cupRecordLists.getRecordByDate(time);
        }
    }

    private void RefreshBindVolume() {

        if (cupRecord != null) {
            try {
                tv_waterVolum.setText(String.valueOf(cupRecord.Volume));
                if (cupRecord.Volume > 0 && cupRecord.Volume <= 1000) {
                    iv_waterValum.setImageResource(R.drawable.water_volum_low);
                } else if (cupRecord.Volume > 1000 && cupRecord.Volume <= 2000) {
                    iv_waterValum.setImageResource(R.drawable.water_volum_mid);
                } else {
                    iv_waterValum.setImageResource(R.drawable.water_volum_hight);
                }
            } catch (Exception ex) {
            }
        }
    }

    private void setTDSTextFace() {
        tv_tdsValue.setTypeface(OznerApplication.textFace);
        tv_tdsValue.getPaint().setFakeBoldText(false);
        tv_tdsValue.setTextSize(45);
    }

    private void setTDSnumFace() {
        tv_tdsValue.setTypeface(OznerApplication.numFace);
        tv_tdsValue.getPaint().setFakeBoldText(false);
        tv_tdsValue.setTextSize(50);   //修改位置
    }

    public void RefreshBindDataView() {
        tv_name.setText(device.getName());
        if (TDSFix == 0) {
            lay_tdsShort.setVisibility(View.GONE);
        }
        if (cup.getAppValue(PageState.DRINK_GOAL) != null) {
            tv_waterTarget.setText(cup.getAppValue(PageState.DRINK_GOAL).toString());
        }
        if (65535 == TDSFix) {
            setTDSTextFace();
            tv_tdsValue.setText(getResources().getString(R.string.null_text));
            tv_tdsLevelText.setText(getResources().getString(R.string.text_null));
            lay_tdsShort.setVisibility(View.GONE);
            iv_tdsLevelImg.setVisibility(View.GONE);
            rlay_tdsdetail.setEnabled(false);
        } else {
            if (TDSFix == 0) {
                lay_tdsShort.setVisibility(View.GONE);
            }
            rlay_tdsdetail.setOnClickListener(this);
            //tds的状态
            if (TDSFix > 0 && TDSFix <= CupRecord.TDS_Good_Value) {
                tv_tdsLevelText.setText(getResources().getString(R.string.health));
                iv_tdsLevelImg.setImageResource(R.drawable.lianghao);
                iv_tdsLevelImg.setVisibility(View.VISIBLE);

            } else if (TDSFix > CupRecord.TDS_Good_Value && TDSFix <= CupRecord.TDS_Bad_Value) {
                tv_tdsLevelText.setText(getResources().getString(R.string.generic));
                iv_tdsLevelImg.setImageResource(R.drawable.yiban);
                iv_tdsLevelImg.setVisibility(View.VISIBLE);
            } else {
                if (TDSFix == 0) {
                    tv_tdsLevelText.setText(getResources().getString(R.string.text_null));
                    iv_tdsLevelImg.setVisibility(View.GONE);
                } else {
                    tv_tdsLevelText.setText(getResources().getString(R.string.bad));
                    iv_tdsLevelImg.setImageResource(R.drawable.jingbao);
                    iv_tdsLevelImg.setVisibility(View.VISIBLE);
                }
            }
//            tdsNew = TDSFix;
            try {
                tdsOld = Integer.parseInt(tv_tdsValue.getText().toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Context context = getContext();
                        if (context != null && TDSFix != 0) {
                            NetJsonObject netJsonObject = OznerCommand.TDSSensor(context, Mac, cuptype, String.valueOf(tdsOld));
                            if (netJsonObject.state > 0) {
                                try {
                                    int rank = netJsonObject.getJSONObject().getInt("rank");
                                    int total = netJsonObject.getJSONObject().getInt("total");
                                    int ranking = (total - rank) * 100 / total;
                                    Message message = new Message();
                                    message.arg1 = ranking;
                                    message.what = 1;
//                                        Thread.sleep(2000);
                                    tdshandler.sendMessage(message);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();
            } catch (Exception ex) {
//                lay_tdsShort.setVisibility(View.INVISIBLE);
            }
            if (TDSFix != 0) {

//                if(OznerPreference.isLoginPhone(getContext())){
                    lay_tdsShort.setVisibility(View.VISIBLE);
//                }else{
//                    lay_tdsShort.setVisibility(View.GONE);
//                }
                rlay_tdsdetail.setEnabled(true);
                if (tdsOld != TDSFix) {
                    final ValueAnimator animator = ValueAnimator.ofInt(tdsOld, TDSFix);
                    animator.setDuration(500);
                    animator.setInterpolator(new LinearInterpolator());//线性效果变化
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            Integer integer = (Integer) animator.getAnimatedValue();
                            setTDSnumFace();
                            OznerApplication.setControlNumFace(tv_tdsValue);
                            tv_tdsValue.setText("" + integer);
//                                rlay_tdsdetail.setEnabled(true);
                        }
                    });
                    animator.start();
                    if (TDSFix > 250) {
                        waterProcess.update(100);
                    } else {
                        double s = (TDSFix / 250.00) * 100;
                        waterProcess.update((int) s);
                    }
                } else {
//                animator.cancel();
                    if (TDSFix != 0) {
                        setTDSnumFace();
                        tv_tdsValue.setText(String.valueOf(TDSFix));
                        tdsOld = Integer.parseInt(tv_tdsValue.getText().toString());
                        if (TDSFix > 250) {
                            waterProcess.update(100);
                        } else {
                            double s = (TDSFix / 250.00) * 100;
                            waterProcess.update((int) s);
                        }
                    }
                }
            } else {
                setTDSTextFace();
                tv_tdsValue.setText(getResources().getString(R.string.text_null));
                rlay_tdsdetail.setEnabled(false);
                lay_tdsShort.setVisibility(View.GONE);
                tv_tdsLevelText.setText(getResources().getString(R.string.text_null));
                iv_tdsLevelImg.setVisibility(View.GONE);
                waterProcess.update(0);
            }
        }
        try {
            if (65535 == TemperatureFix)
                tv_waterTemText.setText(getResources().getString(R.string.text_null));
            else {
                llay_waterTem.setOnClickListener(this);
                if (TemperatureFix > 0 && TemperatureFix <= CupRecord.Temperature_Low_Value) {
                    tv_waterTemText.setText(getResources().getString(R.string.good_temperature));
                    iv_waterTemp.setImageResource(R.drawable.temp_low);
                    tv_waterTempTip.setText(getResources().getString(R.string.cupDetail_waterDrinkNo));
                } else if (TemperatureFix > CupRecord.Temperature_Low_Value && TemperatureFix <= CupRecord.Temperature_High_Value) {
                    tv_waterTemText.setText(getResources().getString(R.string.normal_temperature));
                    iv_waterTemp.setImageResource(R.drawable.temp_mid);
                    tv_waterTempTip.setText(getResources().getString(R.string.cupDetail_waterDrinkOk));
                } else if (TemperatureFix > CupRecord.Temperature_High_Value && TemperatureFix <= 100) {
                    tv_waterTemText.setText(getResources().getString(R.string.bad_temperature));
                    iv_waterTemp.setImageResource(R.drawable.temp_high);
                    tv_waterTempTip.setText(getResources().getString(R.string.cupDetail_waterDrinkNo));
                } else {
                    tv_waterTemText.setText(getResources().getString(R.string.text_null));
                    tv_waterTempTip.setText(getResources().getString(R.string.temp_null));
                }
            }
        } catch (Exception ex) {
            tv_waterTemText.setText(getString(R.string.text_null));
            tv_waterTempTip.setText(getResources().getString(R.string.temp_null));
        }
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
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        bundle.putString("MAC", getArguments().getString("MAC"));
        try {
            if (cupRecord.Volume != 0) {
                bundle.putInt("volum", cupRecord.Volume);
            }
            if (ranking != 0) {
                bundle.putInt("tdsRank", ranking);
            }
        } catch (Exception e) {
            bundle.putInt("volum", 0);
        }
        if (!isCupGuideFirst) {
            switch (v.getId()) {
                case R.id.llay_waterVolum://饮水量详情
                    //  getFragmentManager().beginTransaction().replace(R.id.framen_main_con, new WaterQuantityFragment()).commit();
                    waterQuantityFragment = new WaterQuantityFragment();
                    waterQuantityFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.framen_main_con, waterQuantityFragment).addToBackStack("Two").commitAllowingStateLoss();
                    break;
                case R.id.llay_waterTem://饮水量详情
                    waterTemperatureFragment = new WaterTemperatureFragment();
                    waterTemperatureFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.framen_main_con, waterTemperatureFragment).addToBackStack("Two").commitAllowingStateLoss();
                    break;
                case R.id.rlay_tdsdetail://tds详情
                    tdsFragment = new TDSFragment();
                    tdsFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.framen_main_con, tdsFragment).addToBackStack("Two").commitAllowingStateLoss();
                    break;
                case R.id.iv_cupSet://水杯设置
//                    beginGuide();
                    Intent setting = new Intent(getContext(), SetupGlassActivity.class);
                    setting.putExtra("MAC", Mac);
                    startActivityForResult(setting, 0);
                    break;
                case R.id.rlay_menu://侧边栏菜单
                    ((BaseMainActivity) getActivity()).myOverlayDrawer.toggleMenu();
                    break;
                default:
                    break;
            }
        } else {
            isCupGuideFirst = false;
            editor.putBoolean("isCupGuideFirst", false);
            editor.commit();
//            beginGuide();
        }
    }

    private void beginGuide() {
        Intent intent = new Intent(getContext(), TeachGuideForCupActivity.class);
        startActivity(intent);
    }

    public void ShowContent(int i, String mac) {
    }

    public void ChangeRawRecord() {
    }

    private class UiUpdateAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
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
            if (WaterCupFragment.this != null && WaterCupFragment.this.isAdded()) {
                try {
                    //
                    int power = (int) (POWER * 100 + 0.5);
                    if (power == 100) {
                        tv_batteryText.setText(power + "%");
                        iv_battery.setImageResource(R.drawable.battery100);
                    } else if (power < 100 && power >= 50) {
                        tv_batteryText.setText(power + "%");
                        iv_battery.setImageResource(R.drawable.battery70);
                    } else if (power < 50 && power > 0) {
                        tv_batteryText.setText(power + "%");
                        iv_battery.setImageResource(R.drawable.battery30);
                    } else if (power == 0) {
                        tv_batteryText.setText(power + "%");
                        iv_battery.setImageResource(R.drawable.battery0);
                    } else {
                        tv_batteryText.setText(getString(R.string.text_null));
                        iv_battery.setImageResource(R.drawable.battery0);
                    }
                } catch (Exception ex) {
                    tv_batteryText.setText(getString(R.string.text_null));
                    iv_battery.setImageResource(R.drawable.battery0);
                }
                try {
                    if (TDSFix > 5000) {
                        setTDSTextFace();
                        tv_tdsValue.setText(getResources().getString(R.string.null_text));
                        tv_tdsLevelText.setText(getResources().getString(R.string.text_null));
                        lay_tdsShort.setVisibility(View.GONE);
                        iv_tdsLevelImg.setVisibility(View.GONE);
                        rlay_tdsdetail.setEnabled(false);
                    } else {
                        RefreshBindDataView();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    RefreshBindVolume();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                changeState();
            }
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {
        }
    }

    //开启线程获取固件地址
    private void uploadFirmwarUrl(final String cuptype, final String firmwarNum) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetJsonObject netJsonObject = OznerCommand.GetFirmwareUrl(getContext(), cuptype, firmwarNum);
                Log.e("ffState1", "error====" + netJsonObject.value);
                Message message = new Message();
                if (netJsonObject.state > 0) {
                    try {
                        String path = netJsonObject.getJSONObject().getString("url");
                        Log.e("ffState1", "error====" + path);
                        message.obj = path;
                        message.what = 2;
                        message.arg2 = netJsonObject.state;
                        tdshandler.sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
//                    Log.e("ffState","state");
//                    message.obj="http://app.ozner.net:888/Download/CupMay202015094626.bin";
//                    message.what=2;
//                    message.arg2=1;
//                    tdshandler.sendMessage(message);
                }
            }
        }).start();
    }

    private void downloadFile(final String httpurl) {
        String sdpath = Environment.getExternalStorageDirectory() + File.separator;
        mSavePath = sdpath + "Ozner" + File.separator + "cup.bin";
        Log.e("path", mSavePath);
        Log.e("path", httpurl);
        final HttpUtils httpUtils = new HttpUtils();
        httpUtils.download(httpurl, mSavePath, true, true, new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                Log.e("path", "onSuccess====" + responseInfo.result.getPath());
                Intent intent = new Intent(getContext(), FirmwareUpgrade.class);
                intent.putExtra("MAC", Mac);
                if (responseInfo.result.getPath() != null) {
                    intent.putExtra("path", responseInfo.result.getPath());
                } else {
                    intent.putExtra("path", "null");
                }
                getActivity().startActivity(intent);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Log.e("path", "onFailure=====" + e);
                File file = new File(mSavePath);
                if (file.isFile()) {
                    file.delete();
                }
                file.exists();
            }
        });

    }

    public void CupSensorChange(String address) {
        if (Mac.equals(address)) {
//            ll
            new UiUpdateAsyncTask().execute();
        }
    }

    public void DeviceDataChange() {
    }

    @Override
    public void ContentChange(String mac, String state) {
        if (this.Mac.equals(mac) && WaterCupFragment.this.isAdded() && !WaterCupFragment.this.isDetached() && !WaterCupFragment.this.isRemoving()) {
            switch (state) {
                //正在链接中
                case BaseDeviceIO.ACTION_DEVICE_CONNECTING:
                    iv_data_loading.setImageResource(R.drawable.air_loding);
                    tv_data_loading.setText(getResources().getString(R.string.loding_now));
                    if (iv_data_loading.getAnimation() != null)
                        iv_data_loading.getAnimation().start();
                    rlay_top1.setVisibility(View.VISIBLE);
                    int powerConn = Math.round(POWER);
                    if (powerConn == 100) {
                        tv_batteryText.setText(getString(R.string.text_null));
                        iv_battery.setImageResource(R.drawable.battery0);
                    } else {
                        tv_batteryText.setText(getString(R.string.text_null));
                        iv_battery.setImageResource(R.drawable.battery0);
                    }
                case BaseDeviceIO.ACTION_DEVICE_CONNECTED:
                    if (iv_data_loading.getAnimation() != null)
                        iv_data_loading.getAnimation().cancel();
                    rlay_top1.setVisibility(View.GONE);
                    break;
                //已经断开连接
                case BaseDeviceIO.ACTION_DEVICE_DISCONNECTED:
                    iv_data_loading.setImageResource(R.drawable.air_loding_fair);
                    if (iv_data_loading.getAnimation() != null)
                        iv_data_loading.getAnimation().cancel();
                    tv_data_loading.setText(getResources().getString(R.string.loding_fair));
                    int powerDis = Math.round(POWER);
                    if (powerDis == 100) {
                        tv_batteryText.setText(getString(R.string.text_null));
                        iv_battery.setImageResource(R.drawable.battery0);
                    } else {
                        tv_batteryText.setText(getString(R.string.text_null));
                        iv_battery.setImageResource(R.drawable.battery0);
                    }
                    break;
            }
            changeState();


        }
    }

    @Override
    public void RecvChatData(String data) {

    }

    public void changeState() {
        BaseDeviceIO.ConnectStatus stateIo = cup.connectStatus();
        if (stateIo == BaseDeviceIO.ConnectStatus.Connecting) {
            iv_data_loading.setImageResource(R.drawable.air_loding);
            tv_data_loading.setText(getResources().getString(R.string.loding_now));
            if (iv_data_loading.getAnimation() != null)
                iv_data_loading.getAnimation().start();
            rlay_top1.setVisibility(View.VISIBLE);
            int powerConn = Math.round(POWER);
            if (powerConn == 100) {
                tv_batteryText.setText(getString(R.string.text_null));
                iv_battery.setImageResource(R.drawable.battery0);
            } else {
                tv_batteryText.setText(getString(R.string.text_null));
                iv_battery.setImageResource(R.drawable.battery0);
            }
        }
        if (stateIo == BaseDeviceIO.ConnectStatus.Connected) {
            if (iv_data_loading.getAnimation() != null)
                iv_data_loading.getAnimation().cancel();
            rlay_top1.setVisibility(View.GONE);
        }
        if (stateIo == BaseDeviceIO.ConnectStatus.Disconnect) {
            iv_data_loading.setImageResource(R.drawable.air_loding_fair);
            if (iv_data_loading.getAnimation() != null)
                iv_data_loading.getAnimation().cancel();
            tv_data_loading.setText(getResources().getString(R.string.loding_fair));

            int powerDis = Math.round(POWER);
            if (powerDis == 100) {
                tv_batteryText.setText(getString(R.string.text_null));
                iv_battery.setImageResource(R.drawable.battery0);
            } else {
                tv_batteryText.setText(getString(R.string.text_null));
                iv_battery.setImageResource(R.drawable.battery0);
            }
        }

    }
}

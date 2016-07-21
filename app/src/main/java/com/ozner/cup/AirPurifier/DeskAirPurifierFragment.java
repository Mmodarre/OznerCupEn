package com.ozner.cup.AirPurifier;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.AirPurifier.AirPurifier_Bluetooth;
import com.ozner.cup.Command.FootFragmentListener;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.Device.SetupAirPurifierActivity;
import com.ozner.cup.HttpHelper.NetWeather;
import com.ozner.cup.MainActivity;
import com.ozner.cup.MainEnActivity;
import com.ozner.cup.R;
import com.ozner.cup.control.CProessbarView;
import com.ozner.cup.control.OnCProessbarValueChangeListener;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by taoran on 2015/12/23.
 * modify by mengdongya
 */
public class DeskAirPurifierFragment extends Fragment implements View.OnClickListener, FootFragmentListener, OnCProessbarValueChangeListener {
    private Toolbar toolBar;
    private ImageView iv_purifierSetBtn, iv_data_loading;
    private String Mac = null, name;
    AirPurifier_Bluetooth airPurifier;
    OznerDevice device;
    private String temDanwei;
    private RotateAnimation animation;
    private NetWeather airWeather;
    private TextView airName, tv_air_address, tv_air_pmvalue, tv_air_quality;
    private int fengsu = 0, filterTime;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                lay_air_pm.setEnabled(true);
                switch (msg.what) {
                    case 1:
                        NetWeather weather = (NetWeather) msg.obj;
                        if (weather != null) {
                            airWeather = weather;
                            OznerApplication.setControlNumFace(tv_air_pmvalue);
                            if (weather.pm25 != null) {
                                tv_air_pmvalue.setText(weather.pm25);
                            }
                            if (weather.city != null) {
                                tv_air_address.setText(weather.city);
                            }
                            if (weather.qlty != null) {
                                tv_air_quality.setText(weather.qlty);
                            }
                        } else {
                            lay_air_pm.setEnabled(false);
                            tv_air_pmvalue.setText("0");
                            tv_air_address.setText(getResources().getString(R.string.text_null));
                            tv_air_quality.setText(getResources().getString(R.string.text_null));
                        }
                        break;
                }
            } catch (Exception e) {
                lay_air_pm.setEnabled(false);
            }
        }

    };

    private TextView tv_airOutside_pm, tv_airOutside_aqi, tv_airOutside_temp, tv_airOutside_humidity, tv_airOutside_data, tv_airOutside_city;


    //旋转动画
    private ImageView iv_xuanzhuan_x3;// iv_xuanzhuan_x1, iv_xuanzhuan_x2;
    private RelativeLayout rote_RelativeLayout, rlay_filter, rlay_top1, rlay_filterStatus;

    private LinearLayout lay_air_pm, linearLayout_bg, air_center_layout;

    private CProessbarView cProessbarView;
    private TextView tv_tdsValue, tv_air_temValue, tv_air_shidu_Value, tv_filterStatus, tv_tds, tv_data_loading, tv_flz;
    private int pm25, temp, shidu, lvXin;
    ValueAnimator animator;
    private boolean isOpenOn = false, isFirst = false, isConnected = false;
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private boolean isHandSlide = false;


    public DeskAirPurifierFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            Mac = getArguments().getString("MAC");
            device = OznerDeviceManager.Instance().getDevice(Mac);
            if (AirPurifierManager.IsBluetoothAirPurifier(device.Type())) {
                airPurifier = (AirPurifier_Bluetooth) device;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        View view = inflater.inflate(R.layout.airdesk_purifier_home_page, container, false);
        OznerApplication.changeTextFont((ViewGroup) view);
        initView(view);//初始化布局
        isFirst = true;
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            getData();
        }
        if (!((OznerApplication)getActivity().getApplication()).isLanguageCN()){
            view.findViewById(R.id.chin_stand).setVisibility(View.GONE);
        }
        return view;
    }

    private void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                NetWeather weather = OznerCommand.GetWeather(getActivity());
                if (weather != null) {
                    message.obj = weather;
                    message.what = 1;
                }
                handler.sendMessage(message);
            }
        }).start();
    }


    private void initView(View view) {
        iv_purifierSetBtn = (ImageView) view.findViewById(R.id.iv_purifierSetBtn);
        iv_purifierSetBtn.setOnClickListener(this);

        linearLayout_bg = (LinearLayout) view.findViewById(R.id.air_desk_bg);

        toolBar = (Toolbar) view.findViewById(R.id.air_main_toolbar);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((OznerApplication)getActivity().getApplication()).isLanguageCN()) {
                    ((MainActivity) getActivity()).myOverlayDrawer.toggleMenu();
                }else {
                    ((MainEnActivity) getActivity()).myOverlayDrawer.toggleMenu();
                }
            }
        });
        airName = (TextView) view.findViewById(R.id.main_toolbar_text);
        tv_tdsValue = (TextView) view.findViewById(R.id.tv_tdsValue);
        tv_air_temValue = (TextView) view.findViewById(R.id.tv_air_temValue);
        tv_air_shidu_Value = (TextView) view.findViewById(R.id.tv_air_shidu_Value);
        tv_filterStatus = (TextView) view.findViewById(R.id.tv_filterStatus);
        tv_tds = (TextView) view.findViewById(R.id.tv_tds);
        tv_data_loading = (TextView) view.findViewById(R.id.tv_data_loading);
        rote_RelativeLayout = (RelativeLayout) view.findViewById(R.id.rote_RelativeLayout);
        rlay_filter = (RelativeLayout) view.findViewById(R.id.rlay_filter);
        rlay_filterStatus = (RelativeLayout) view.findViewById(R.id.rlay_filterStatus);
        rlay_filterStatus.setOnClickListener(this);
        air_center_layout = (LinearLayout) view.findViewById(R.id.air_center_layout);
        air_center_layout.setOnClickListener(this);
        lay_air_pm = (LinearLayout) view.findViewById(R.id.lay_air_pm);
        lay_air_pm.setOnClickListener(this);
        iv_xuanzhuan_x3 = (ImageView) view.findViewById(R.id.iv_xuanzhuan_x3);

        InitAnimation();
        cProessbarView = (CProessbarView) view.findViewById(R.id.my_cproessbarview);
        cProessbarView.updateValue(0);
        tv_air_address = (TextView) view.findViewById(R.id.tv_air_address);
        tv_air_pmvalue = (TextView) view.findViewById(R.id.tv_air_pmvalue);
        tv_air_quality = (TextView) view.findViewById(R.id.tv_air_quality);

        rlay_top1 = (RelativeLayout) view.findViewById(R.id.rlay_top1);
        iv_data_loading = (ImageView) view.findViewById(R.id.iv_data_loading);
        tv_data_loading = (TextView) view.findViewById(R.id.tv_data_loading);
        animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatCount(-1);
        LinearInterpolator li = new LinearInterpolator();
        animation.setInterpolator(li);
        animation.setFillAfter(false);
        animation.setDuration(1000);
        iv_data_loading.setAnimation(animation);

        cProessbarView.setOnCProessbarValueChangeListener(this);
        setSize();
        tv_flz = (TextView) view.findViewById(R.id.tv_flz);
        tv_flz.setTextColor(getResources().getColor(R.color.white));
        tv_flz.setVisibility(View.INVISIBLE);
//        if(airPurifier.status().Power()){
//            tv_flz.setVisibility(View.VISIBLE);
//        }else{
////            tv_flz.setVisibility(View.INVISIBLE);
//        }

    }


    //滑动条
    @Override
    public void ValueChange(final int persent) {
        //修改数据
        if (persent <= 1) {
            airPurifier.status().setPower(false, new OperateCallback<Void>() {
                @Override
                public void onSuccess(Void var1) {
//                    tv_flz.setVisibility(View.INVISIBLE);
                    cProessbarView.updateValue(0);
                    isFirst = true;
                }

                @Override
                public void onFailure(Throwable var1) {

                    airPurifier.status().setPower(false, new OperateCallback<Void>() {
                        @Override
                        public void onSuccess(Void var1) {
                        }

                        @Override
                        public void onFailure(Throwable var1) {
                        }
                    });
                }
            });
        } else {
//            if(!airPurifier.status().Power()){

            airPurifier.status().setPower(true, new OperateCallback<Void>() {
                @Override
                public void onSuccess(Void var1) {
//                    tv_flz.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(Throwable var1) {
                    airPurifier.status().setPower(true, new OperateCallback<Void>() {
                        @Override
                        public void onSuccess(Void var1) {
//                            tv_flz.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onFailure(Throwable var1) {

                        }
                    });
                }
            });
//            }

        }

        if (airPurifier.status().Power()) {


            final Message message = new Message();
            airPurifier.status().setRPM((byte) persent, new OperateCallback<Void>() {
                @Override
                public void onSuccess(Void var1) {
                }

                @Override
                public void onFailure(Throwable var1) {
                }
            });
        } else {
//            tv_flz.setVisibility(View.INVISIBLE);

        }

    }

    private void setSize() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = (int) (dm.densityDpi * 4);
        int height = (int) (dm.densityDpi * 2);
        iv_xuanzhuan_x3.setAdjustViewBounds(true);
        iv_xuanzhuan_x3.setMaxWidth(width);
        iv_xuanzhuan_x3.setMinimumWidth(width);
        iv_xuanzhuan_x3.setMaxHeight(height);
        iv_xuanzhuan_x3.setMinimumHeight(height);
    }

    /*
   * 初始化动画
   * */
    public void InitAnimation() {
        RotateAnimation animation = new RotateAnimation(-360f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
        animation.setRepeatCount(-1);
        LinearInterpolator li = new LinearInterpolator();
        animation.setInterpolator(li);
        animation.setFillAfter(false);
        animation.setDuration(1000);
        iv_xuanzhuan_x3.setAnimation(animation);
    }


    /*
     * 初始化数据
     */
    public void initData() {
        isOpenOn = airPurifier.status().Power();
        name = airPurifier.getName();
        pm25 = airPurifier.sensor().PM25();
        temp = airPurifier.sensor().Temperature();
        shidu = airPurifier.sensor().Humidity();
        temDanwei = UserDataPreference.GetUserData(getContext(), UserDataPreference.TempUnit, "0");
        fengsu = airPurifier.status().RPM();//获得机器传过来的风速
        Log.e("taoran_fengsu", "initData风速:" + airPurifier.status().RPM() + isOpenOn);
        filterTime = airPurifier.sensor().FilterStatus().workTime;
        if(filterTime>60000){
            filterTime=60000;
        }
        Log.e("trFil", airPurifier.sensor().FilterStatus().workTime + "==========");
        Log.e("trFil", GetFilterTime.getFilter(airPurifier.sensor().FilterStatus().workTime) + "==========");
    }

    private void setDate() {
        Log.e("taoran_fengsu", "setData风速:" + fengsu);
        Log.e("lingchen", "setData_isFirst:" + isFirst);
        Log.e("lingchen", "setData_isFirst:" + isFirst);

        if (isAdded()) {
            if (airPurifier != null && airPurifier.status().Power()) {
                //更新滑动按钮
                if (isFirst) {
                    cProessbarView.updateValue(fengsu);
                }
            } else {
                isFirst = true;
                cProessbarView.updateValue(0);
            }


            if (!isOpenOn) {
                showClosed();
            }
            airName.setText(name);
            if (isOpenOn) {
                if (pm25 == 65535) {
                    tv_tdsValue.setText(getResources().getString(R.string.null_text));
                    tv_tds.setText("-");
                } else {
//            tv_tdsValue.setText("" + pm25);
                    if (pm25 < 75) {
                        tv_tds.setText(getString(R.string.excellent));
                        linearLayout_bg.setBackgroundResource(R.color.air_background);
                        toolBar.setBackgroundResource(R.color.air_background);
                        iv_xuanzhuan_x3.setImageResource(R.drawable.mengban1);
                    } else if (pm25 >= 75 && pm25 < 150) {
                        tv_tds.setText(getString(R.string.good));
                        linearLayout_bg.setBackgroundResource(R.color.air_good);
                        toolBar.setBackgroundResource(R.color.air_good);
                        iv_xuanzhuan_x3.setImageResource(R.drawable.mengban2);
                    } else if (pm25 >= 150) {
                        tv_tds.setText(getString(R.string.bads));
                        linearLayout_bg.setBackgroundResource(R.color.air_bad);
                        toolBar.setBackgroundResource(R.color.air_bad);
                        iv_xuanzhuan_x3.setImageResource(R.drawable.mengban3);
                    }
                    OznerApplication.setControlNumFace(tv_tdsValue);
                    OznerApplication.setControlNumFace(tv_air_temValue);
                    OznerApplication.setControlNumFace(tv_air_shidu_Value);
                    if (isFirst) {
                        isFirst = false;
                        animator = ValueAnimator.ofInt(0, pm25);
                        animator.setDuration(500);
                        animator.setInterpolator(new LinearInterpolator());//线性效果变化
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                Integer integer = (Integer) animator.getAnimatedValue();
                                tv_tdsValue.setText("" + integer);
                            }
                        });
                        animator.start();
                    } else {
//                    isFirst=true;
                        tv_tdsValue.setText("" + pm25);
                    }

                }
                if (temp == 65535) {
                    tv_air_temValue.setText("-");
                } else {
                    switch (temDanwei) {
                        case "0":
                            temDanwei = "℃";
                            tv_air_temValue.setText(temp + temDanwei);
                            break;
                        case "1":
                            temDanwei = "℉";
                            temp = temp * 9 / 5 + 32;
                            tv_air_temValue.setText(temp + temDanwei);
                            break;
                    }
                }

                if (shidu == 65535) {
                    tv_air_shidu_Value.setText("-");
                } else {
                    tv_air_shidu_Value.setText(shidu + "%");
                }
//            tv_flz.setVisibility(View.VISIBLE);
            }
//        else{
//            tv_flz.setVisibility(View.GONE);
//        }

            tv_tdsValue.setAlpha(1.0f);

            if (-1 == filterTime) {
                OznerApplication.setControlNumFace(tv_filterStatus);
                tv_filterStatus.setText(getString(R.string.text_null));
            } else {
                OznerApplication.setControlNumFace(tv_filterStatus);
                tv_filterStatus.setText(GetFilterTime.getFilter(filterTime) + "%");
            }


//            try {
//                Date date = new Date();
//                Date lastTime = airPurifier.sensor().FilterStatus().lastTime;
//                sdf.format(date);
//                sdf.format(lastTime);
//                calendar.setTime(lastTime);
//                long a = calendar.getTimeInMillis();
//                calendar.setTime(date);
//                long b = calendar.getTimeInMillis();
//                long data = (b - a) / (1000 * 24 * 3600);
//                lvXin = (int) (91 - data) * 100 / 91;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

//            if (lvXin < 0 | lvXin > 100) {
//                OznerApplication.setControlNumFace(tv_filterStatus);
//                tv_filterStatus.setText("0%");
//            } else {
//                tv_filterStatus.setText(lvXin + "%");
//                OznerApplication.setControlNumFace(tv_filterStatus);
//            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.air_center_layout:
            case R.id.rlay_filterStatus:
                if (isOpenOn) {
                    Intent intent = new Intent(getContext(), AirFilterActivity.class);
                    intent.putExtra("MAC", Mac);
                    startActivityForResult(intent, 0x1244);
                }
                break;
            case R.id.iv_purifierSetBtn:
                Intent setting = new Intent(getContext(), SetupAirPurifierActivity.class);
                setting.putExtra("MAC", Mac);
                startActivityForResult(setting, 0x1111);
                break;
            case R.id.lay_air_pm:
                final Dialog airDialog = new Dialog(getContext(), R.style.dialog_style);
                airDialog.setContentView(R.layout.air_outside_details);
                airDialog.getWindow().setGravity(Gravity.BOTTOM);
                WindowManager windowManager = getActivity().getWindowManager();
                Display display = windowManager.getDefaultDisplay();
                WindowManager.LayoutParams lp = airDialog.getWindow().getAttributes();
                lp.width = display.getWidth(); // 设置宽度
                Window window = airDialog.getWindow();
                window.setAttributes(lp);
                window.setWindowAnimations(R.style.dialogstyle);
                tv_airOutside_pm = (TextView) airDialog.findViewById(R.id.tv_airOutside_pm);
                if (airWeather != null) {
                    if (airWeather.pm25 != null) {
                        tv_airOutside_pm.setText(airWeather.pm25 + "μg/m3");
                    } else {
                        tv_airOutside_pm.setText(0 + "μg/m3");
                    }

                    tv_airOutside_aqi = (TextView) airDialog.findViewById(R.id.tv_airOutside_aqi);
                    if (airWeather.aqi != null) {
                        tv_airOutside_aqi.setText(airWeather.aqi);
                    } else {
                        tv_airOutside_aqi.setText("0");
                    }

                    tv_airOutside_temp = (TextView) airDialog.findViewById(R.id.tv_airOutside_temp);
                    if (airWeather.tmp != null) {
                        tv_airOutside_temp.setText(airWeather.tmp + "℃");
                    } else {
                        tv_airOutside_temp.setText(0 + "℃");
                    }


                    tv_airOutside_humidity = (TextView) airDialog.findViewById(R.id.tv_airOutside_humidity);
                    if (airWeather.hum != null) {
                        tv_airOutside_humidity.setText(airWeather.hum + "%");
                    } else {
                        tv_airOutside_humidity.setText(0 + "%");
                    }

                    tv_airOutside_data = (TextView) airDialog.findViewById(R.id.tv_airOutside_data);
                    if (airWeather.weatherform != null) {
                        tv_airOutside_data.setText(airWeather.weatherform);
                    } else {
                        tv_airOutside_data.setText(0);
                    }

                    tv_airOutside_data = (TextView) airDialog.findViewById(R.id.tv_airOutside_data);
                    if (airWeather.weatherform != null) {
                        tv_airOutside_data.setText(airWeather.weatherform);
                    } else {
                        tv_airOutside_data.setText(getResources().getString(R.string.text_null));
                    }

                    tv_airOutside_city = (TextView) airDialog.findViewById(R.id.tv_airOutside_city);
                    if (airWeather.city != null) {
                        tv_airOutside_city.setText(airWeather.city);
                    } else {
                        tv_airOutside_city.setText(getResources().getString(R.string.text_null));
                    }

                    airDialog.findViewById(R.id.tv_air_know).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            airDialog.dismiss();
                        }
                    });
                    airDialog.show();
                } else {
                    lay_air_pm.setEnabled(false);
//                    tv_airOutside_pm.setText(0 + "μg/m3");
//                    tv_airOutside_aqi.setText("0");
//                    tv_airOutside_temp.setText(0 + "℃");
//                    tv_airOutside_humidity.setText(0 + "%");
//                    tv_airOutside_data.setText(getResources().getString(R.string.text_null));
//                    tv_airOutside_city.setText(getResources().getString(R.string.text_null));
                }
                break;
        }

    }

    //关机界面显示
    private void showClosed() {
        if (!airPurifier.status().Power() && DeskAirPurifierFragment.this.isAdded()) {

            tv_tdsValue.setText(getString(R.string.air_closed));
            tv_tdsValue.setAlpha(0.6f);
            tv_air_temValue.setText("-");
            tv_air_shidu_Value.setText("-");
            tv_tds.setText("");
            tv_flz.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UiUpdateAsyncTask uiUpdateAsyncTask = new UiUpdateAsyncTask();
        uiUpdateAsyncTask.execute("airtai");
        changeState();
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
            if (isOpenOn) {
                tv_flz.setVisibility(View.VISIBLE);
            } else {
                tv_flz.setVisibility(View.INVISIBLE);
            }
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {
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
    public void ShowContent(int i, String mac) {

    }

    @Override
    public void ChangeRawRecord() {
    }

    /**
     * 设备数据发生变化
     */
    @Override
    public void CupSensorChange(String address) {
        if (this.Mac.equals(address)) {
            Log.e("lingchen", "CupSensorChange:" + address);
            //此处应该执行更新数据异步操作
            new UiUpdateAsyncTask().execute();
//            initData();
//            setDate();
        }
    }

    @Override
    public void DeviceDataChange() {
    }

    /**
     * 设备连接状态发生变化
     */
    @Override
    public void ContentChange(String mac, String state) {
        if (this.Mac.equals(mac) && DeskAirPurifierFragment.this.isAdded() && !DeskAirPurifierFragment.this.isDetached() && !DeskAirPurifierFragment.this.isRemoving()) {
            switch (state) {
                //正在链接中
                case BaseDeviceIO.ACTION_DEVICE_CONNECTING:
                    iv_data_loading.setImageResource(R.drawable.air_loding);
                    tv_data_loading.setText(getResources().getString(R.string.loding_now));
                    if (iv_data_loading.getAnimation() != null)
                        iv_data_loading.getAnimation().start();
                    rlay_top1.setVisibility(View.VISIBLE);
                case BaseDeviceIO.ACTION_DEVICE_CONNECTED:
                    isConnected = true;
                    if (iv_data_loading.getAnimation() != null)
                        iv_data_loading.getAnimation().cancel();
                    rlay_top1.setVisibility(View.GONE);
                    break;
                //已经断开连接
                case BaseDeviceIO.ACTION_DEVICE_DISCONNECTED:
                    rlay_top1.setVisibility(View.VISIBLE);
                    isConnected = false;
                    iv_data_loading.setImageResource(R.drawable.air_loding_fair);
                    tv_data_loading.setText(getResources().getString(R.string.loding_fair));
                    if (iv_data_loading.getAnimation() != null) {
                        iv_data_loading.getAnimation().cancel();
                    }
                    break;
            }
            changeState();
        }
    }

    public void changeState() {
        BaseDeviceIO.ConnectStatus stateIo = airPurifier.connectStatus();
        if (stateIo == BaseDeviceIO.ConnectStatus.Connecting) {
            iv_data_loading.setImageResource(R.drawable.air_loding);
            tv_data_loading.setText(getResources().getString(R.string.loding_now));
            if (iv_data_loading.getAnimation() != null)
                iv_data_loading.getAnimation().start();
            rlay_top1.setVisibility(View.VISIBLE);
        }
        if (stateIo == BaseDeviceIO.ConnectStatus.Connected) {
            if (iv_data_loading.getAnimation() != null)
                iv_data_loading.getAnimation().cancel();
            rlay_top1.setVisibility(View.GONE);
        }
        if (stateIo == BaseDeviceIO.ConnectStatus.Disconnect) {
            rlay_top1.setVisibility(View.VISIBLE);
            showClosed();
            iv_data_loading.setImageResource(R.drawable.air_loding_fair);
            tv_data_loading.setText(getResources().getString(R.string.loding_fair));
            if (iv_data_loading.getAnimation() != null) {
                iv_data_loading.getAnimation().cancel();
            }
        }
    }

    @Override
    public void RecvChatData(String data) {
    }

}




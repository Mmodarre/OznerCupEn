package com.ozner.yiquan.AirPurifier;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.yiquan.Command.FootFragmentListener;
import com.ozner.yiquan.Command.ImageHelper;
import com.ozner.yiquan.Command.OznerCommand;
import com.ozner.yiquan.Command.PageState;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.Device.SetupAirPurifierActivity;
import com.ozner.yiquan.HttpHelper.NetWeather;
import com.ozner.yiquan.MainActivity;
import com.ozner.yiquan.MainEnActivity;
import com.ozner.yiquan.R;
import com.ozner.yiquan.UIView.NetHelper;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by taoran on 2015/12/23.\
 * modify by mengdongya
 */
public class VerticalAirPurifierFragment extends Fragment implements View.OnClickListener, FootFragmentListener {

    private String Mac = null, temDanwei, name;
    //    private OznerDevice device;
    private AirPurifier_MXChip airPurifier;
    private int pm25 = 0, temp, shidu, voc, lvXin;
    private ImageView iv_purifierSetBtn;
    private ProgressDialog dialog;
    private Toolbar toolBar;
    private TextView airName, tv_air_address, tv_air_pmvalue, tv_air_quality, tv_tds, tv_phone_nonet, tv_device_nonet;
    private LinearLayout lay_air_outside, linearLayout_bg, air_center_layout;
    ValueAnimator animator;
    MyHandler myHandler;

    private NetWeather airWeather;
    private TextView tv_airOutside_pm, tv_airOutside_aqi, tv_airOutside_temp, tv_airOutside_humidity, tv_airOutside_data, tv_airOutside_city;

    private RelativeLayout rlay_open, rlay_mode, rlay_lock;
    private ImageView iv_open, iv_mode, iv_lock;
    private TextView tv_open, tv_mode, tv_lock, tv_tdsValue, tv_air_vocValue, tv_air_temValue, tv_air_shidu_Value, tv_filterStatus;
    private boolean isPowerOn = false;
    private boolean isModeOn = false;
    private boolean isOffLine = false;
    //    private boolean isTimingOn = false;
    private boolean isLockOn = false;
    private boolean modeDayOn = false;
    private boolean modeNightOn = false;
    private boolean modeAutoOn = false;

    private RelativeLayout rlay_top2, rlay_btn_mode3, rlay_btn_mode2, rlay_btn_mode1;
    //旋转动画
    private ImageView iv_xuanzhuan_x3;
    private RelativeLayout rlay_filter;
    private int workTime, maxWorktime;
    private int isNet;
    boolean isFirst = false;

    //add by xinde
    RelativeLayout rlay_hideContainer, rlay_btn_mode;
    ImageView iv_btn_mode, iv_btn_mode1, iv_btn_mode2, iv_btn_mode3;

    public VerticalAirPurifierFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroyView() {
        releaseObject();
        super.onDestroyView();
    }

    private void releaseObject() {
        airPurifier = null;
        dialog = null;
        animator = null;
        myHandler = null;
        airWeather = null;
        System.gc();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            Mac = getArguments().getString("MAC");
            OznerDevice device = OznerDeviceManager.Instance().getDevice(Mac);
            if (device instanceof AirPurifier_MXChip) {
                airPurifier = (AirPurifier_MXChip) device;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isFirst = true;
        View view = inflater.inflate(R.layout.air_purifier_home_page, container, false);

        OznerApplication.changeTextFont((ViewGroup) view);
        view.findViewById(R.id.chin_stand).setVisibility(View.GONE);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(getView());//初始化布局
        myHandler = new MyHandler();
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            getData();
        }
    }

    private void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                NetWeather weather = null;
                try {
                    weather = OznerCommand.GetWeather(getActivity());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    weather = null;
                }
                {
                    if (weather != null) {
                        message.obj = weather;
                        message.what = 1;
                        try {
                            Thread.sleep(2000);
                            if (myHandler != null) {
                                myHandler.sendMessage(message);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    //关机界面显示
    private void showClosed() {
        if (!isPowerOn && VerticalAirPurifierFragment.this.isAdded()) {
            tv_air_vocValue.setText(" - ");
            tv_tdsValue.setText(getString(R.string.air_closed));
            tv_tdsValue.setAlpha(0.6f);
            tv_air_vocValue.setText(" - ");
            tv_air_temValue.setText(" - ");
            tv_air_shidu_Value.setText(" - ");
            tv_tds.setVisibility(View.GONE);
        }
    }

    private void showOffLine() {
        tv_air_vocValue.setText(" - ");
        tv_tdsValue.setText(getResources().getString(R.string.detail_nonet));
        tv_tdsValue.setAlpha(0.6f);
        tv_air_vocValue.setText(" - ");
        tv_air_temValue.setText(" - ");
        tv_air_shidu_Value.setText(" - ");
        tv_tds.setVisibility(View.GONE);
    }

    //界面控件的初始化
    private void initView(View view) {
        //pm2.5、温度、湿度
        rlay_btn_mode3 = (RelativeLayout) view.findViewById(R.id.rlay_btn_mode3);
        rlay_btn_mode2 = (RelativeLayout) view.findViewById(R.id.rlay_btn_mode2);
        rlay_btn_mode1 = (RelativeLayout) view.findViewById(R.id.rlay_btn_mode1);
        iv_btn_mode = (ImageView) view.findViewById(R.id.iv_btn_mode);
        iv_btn_mode1 = (ImageView) view.findViewById(R.id.iv_btn_mode1);
        iv_btn_mode2 = (ImageView) view.findViewById(R.id.iv_btn_mode2);
        iv_btn_mode3 = (ImageView) view.findViewById(R.id.iv_btn_mode3);

        tv_tdsValue = (TextView) view.findViewById(R.id.tv_tdsValue);//pm2.5
        tv_air_vocValue = (TextView) view.findViewById(R.id.tv_air_vocValue);//空气质量
        tv_air_temValue = (TextView) view.findViewById(R.id.tv_air_temValue);//温度
        tv_air_shidu_Value = (TextView) view.findViewById(R.id.tv_air_shidu_Value);//温度

        tv_filterStatus = (TextView) view.findViewById(R.id.tv_filterStatus);//滤芯状态
        tv_tds = (TextView) view.findViewById(R.id.tv_tds);//空气质量状态 优 良 差

        iv_purifierSetBtn = (ImageView) view.findViewById(R.id.iv_purifierSetBtn);
        iv_purifierSetBtn.setOnClickListener(this);
        toolBar = (Toolbar) view.findViewById(R.id.air_main_toolbar);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((OznerApplication) getActivity().getApplication()).isLoginPhone()) {
                    ((MainActivity) getActivity()).myOverlayDrawer.toggleMenu();
                } else {
                    ((MainEnActivity) getActivity()).myOverlayDrawer.toggleMenu();
                }
            }
        });
        airName = (TextView) view.findViewById(R.id.main_toolbar_text);
        linearLayout_bg = (LinearLayout) view.findViewById(R.id.air_ver_set_background);
        //----------------------------3-------------------------------------
        rlay_filter = (RelativeLayout) view.findViewById(R.id.rlay_filter);
        rlay_filter.setOnClickListener(this);

        iv_xuanzhuan_x3 = (ImageView) view.findViewById(R.id.iv_xuanzhuan_x3);
        rlay_hideContainer = (RelativeLayout) view.findViewById(R.id.rlay_hidecontainer);
        rlay_hideContainer.setOnClickListener(this);

        rlay_btn_mode = (RelativeLayout) view.findViewById(R.id.rlay_btn_mode);
        rlay_btn_mode.setOnClickListener(this);
        rlay_btn_mode1.setOnClickListener(this);
        rlay_btn_mode2.setOnClickListener(this);
        rlay_btn_mode3.setOnClickListener(this);

        InitAnimation();

        air_center_layout = (LinearLayout) view.findViewById(R.id.air_center_layout);
        air_center_layout.setOnClickListener(this);
        lay_air_outside = (LinearLayout) view.findViewById(R.id.lay_air_outside);
        lay_air_outside.setOnClickListener(this);
        view.findViewById(R.id.rlay_filterStatus).setOnClickListener(this);
        tv_air_address = (TextView) view.findViewById(R.id.tv_air_address);
        tv_air_pmvalue = (TextView) view.findViewById(R.id.tv_air_pmvalue);
        tv_air_quality = (TextView) view.findViewById(R.id.tv_air_quality);
        tv_phone_nonet = (TextView) view.findViewById(R.id.tv_phone_nonet);
//        offline_notice = (TextView) view.findViewById(R.id.offline_notice);
        tv_device_nonet = (TextView) view.findViewById(R.id.tv_data_loading_fair);

        rlay_top2 = (RelativeLayout) view.findViewById(R.id.rlay_top2);

        //滑动布局1的控件
        rlay_open = (RelativeLayout) view.findViewById(R.id.rlay_openswitch);
        rlay_mode = (RelativeLayout) view.findViewById(R.id.rlay_modeswitch);
//        rlay_timing = (RelativeLayout) view.findViewById(R.id.rlay_timingswitch);
        rlay_lock = (RelativeLayout) view.findViewById(R.id.rlay_lockswitch);
        rlay_open.setOnClickListener(this);
        rlay_mode.setOnClickListener(this);
//        rlay_timing.setOnClickListener(this);
        rlay_lock.setOnClickListener(this);
        iv_open = (ImageView) view.findViewById(R.id.iv_openswitch);
        iv_mode = (ImageView) view.findViewById(R.id.iv_modeswitch);
//        iv_timing = (ImageView) view.findViewById(R.id.iv_timingswitch);
        iv_lock = (ImageView) view.findViewById(R.id.iv_lockswitch);
        tv_open = (TextView) view.findViewById(R.id.tv_openswitch);
        tv_mode = (TextView) view.findViewById(R.id.tv_modeswitch);
//        tv_timing = (TextView) view.findViewById(R.id.tv_timingswitch);
        tv_lock = (TextView) view.findViewById(R.id.tv_lockswitch);
        setSize();

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
        * */
    public void initData() {
        if (airPurifier != null) {
            name = airPurifier.getName();
            isOffLine = airPurifier.isOffline();
            isPowerOn = airPurifier.airStatus().Power();
            isLockOn = airPurifier.airStatus().Lock();

            pm25 = airPurifier.sensor().PM25();
            temp = airPurifier.sensor().Temperature();
            shidu = airPurifier.sensor().Humidity();
            voc = airPurifier.sensor().VOC();
            switch (airPurifier.airStatus().speed()) {
                case AirPurifier_MXChip.FAN_SPEED_AUTO://自动
//                switchModeBtn(R.id.rlay_btn_mode2);
                    modeDayOn = false;
                    modeAutoOn = true;
                    modeNightOn = false;
                    isModeOn = true;
                    break;
                case AirPurifier_MXChip.FAN_SPEED_SILENT://夜间
//                switchModeBtn(R.id.rlay_btn_mode3);
                    modeDayOn = false;
                    modeAutoOn = false;
                    modeNightOn = true;
                    isModeOn = true;
                    break;
                case AirPurifier_MXChip.FAN_SPEED_POWER://强力
//                switchModeBtn(R.id.rlay_btn_mode1);
                    modeDayOn = true;
                    modeAutoOn = false;
                    modeNightOn = false;
                    isModeOn = true;
                    break;
            }
        }
    }

    private void setDate() {
        airName.setText(name);
        temDanwei = UserDataPreference.GetUserData(getContext(), UserDataPreference.TempUnit, "0");
        tv_tdsValue.setAlpha(1.0f);
        switchMode(isModeOn);
        tv_tds.setVisibility(View.VISIBLE);
        iv_btn_mode1.setSelected(modeDayOn);
        iv_btn_mode2.setSelected(modeAutoOn);
        iv_btn_mode3.setSelected(modeNightOn);
        if (modeAutoOn) {
            iv_mode.setImageResource(R.drawable.air_mode_on);
            switchMode(true);
        } else if (modeDayOn) {
            iv_mode.setImageResource(R.drawable.air_modeday_on);
            switchMode(true);
        } else if (modeNightOn) {
            iv_mode.setImageResource(R.drawable.air_modenight_on);
            switchMode(true);
        } else if (!isModeOn) {
            iv_mode.setImageResource(R.drawable.air_mode_off);
            isModeOn = false;
            switchMode(false);
        }

        switchLock(isLockOn);

        if (isNet == 0 || isOffLine) {
            tv_tdsValue.setText(getString(R.string.phone_nonet));
        } else if(isOffLine){
            tv_tdsValue.setText(getString(R.string.detail_nonet));
        } else if (65535 == pm25 || pm25 <= 0) {
            tv_tdsValue.setText(getString(R.string.null_text));
            OznerApplication.setControlTextFace(tv_tdsValue);
            tv_tds.setText(" - ");
        } else if (pm25 > 1000) {
            tv_tdsValue.setText(getString(R.string.null_text));
        } else {
            if (pm25 < 75 && pm25 > 0) {
                tv_tds.setText(getString(R.string.excellent));
                linearLayout_bg.setBackgroundResource(R.color.air_background);
                toolBar.setBackgroundResource(R.color.air_background);
                iv_xuanzhuan_x3.setImageResource(R.drawable.mengban1);
            } else if (pm25 >= 75 && pm25 < 150) {
                tv_tds.setText(R.string.good);
                linearLayout_bg.setBackgroundResource(R.color.air_good);
                toolBar.setBackgroundResource(R.color.air_good);
                iv_xuanzhuan_x3.setImageResource(R.drawable.mengban2);
            } else if (pm25 >= 150) {
                tv_tds.setText(getString(R.string.bads));
                linearLayout_bg.setBackgroundResource(R.color.air_bad);
                toolBar.setBackgroundResource(R.color.air_bad);
                iv_xuanzhuan_x3.setImageResource(R.drawable.mengban3);
            }
            if (airPurifier != null) {
                if (isFirst) {
                    isFirst = false;
                    animator = ValueAnimator.ofInt(0, airPurifier.sensor().PM25());
                    animator.setDuration(500);
                    animator.setInterpolator(new LinearInterpolator());//线性效果变化
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            if (animator != null) {
                                Integer integer = (Integer) animator.getAnimatedValue();
                                tv_tdsValue.setText("" + integer);
                                OznerApplication.setControlNumFace(tv_tdsValue);
                            }
                        }
                    });
                    animator.start();
                } else {
                    tv_tdsValue.setText(airPurifier.sensor().PM25() + "");
                }
            }
        }

        if (65535 == voc) {
            tv_air_vocValue.setText(" - ");
        } else {
            OznerApplication.setControlTextFace(tv_air_vocValue);
            switch (voc) {
                case -1:
                    tv_air_vocValue.setText(getString(R.string.query_checking));
                    break;
                case 0:
                    tv_air_vocValue.setText(getString(R.string.excellent));
                    break;
                case 1:
                    tv_air_vocValue.setText(getString(R.string.good));
                    break;
                case 2:
                    tv_air_vocValue.setText(getString(R.string.ordinary));
                    break;
                case 3:
                    tv_air_vocValue.setText(getString(R.string.bads));
                    break;
            }
        }

        if (temp == 65535) {
            tv_air_temValue.setText(" - ");
        } else {
            OznerApplication.setControlNumFace(tv_air_temValue);
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

        if (65535 == shidu || !isPowerOn) {
            tv_air_shidu_Value.setText(" - ");
        } else {
            OznerApplication.setControlNumFace(tv_air_shidu_Value);
            tv_air_shidu_Value.setText(shidu + "%");
        }

        if (!isPowerOn) {
            showClosed();
        }
        switchOpen(isPowerOn);

        workTime = airPurifier.sensor().FilterStatus().workTime;
        maxWorktime = airPurifier.sensor().FilterStatus().maxWorkTime;
        if (maxWorktime == 0) {
            maxWorktime = 129600;
        }
        int lvxin = Math.round(100 - workTime * 100 / maxWorktime);
        tv_filterStatus.setText(lvxin + "%");
        OznerApplication.setControlNumFace(tv_filterStatus);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.air_center_layout:
            case R.id.rlay_filterStatus:
                if (isPowerOn && isNet != 0 && !isOffLine) {
                    Intent intent = new Intent(getContext(), VerAirFilterActivity.class);
                    intent.putExtra("MAC", Mac);
                    startActivityForResult(intent, 0x2323);
                } else {
                    if (isNet == 0) {
                        showDialog();
                    } else if (isOffLine) {
                        showDialog();
                    }
                }
                break;
            case R.id.iv_purifierSetBtn:
                Intent setting = new Intent(getContext(), SetupAirPurifierActivity.class);
                setting.putExtra(PageState.MAC, Mac);
                startActivityForResult(setting, 0x1111);
                break;
            case R.id.rlay_openswitch:
                if (!isOffLine) {
                    //矫正设备状态
                    dialog = ProgressDialog.show(getActivity(), getString(R.string.my_setting), getString(R.string.sending_order));
                    dialog.setCanceledOnTouchOutside(true);
                    if (airPurifier != null) {
                        airPurifier.airStatus().setPower(airPurifier.airStatus().Power(), new OperateCallback<Void>() {
                            @Override
                            public void onSuccess(Void var1) {
                                try {
                                    Thread.sleep(500);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                if (airPurifier != null)
                                    airPurifier.airStatus().setPower(!isPowerOn, new OperateCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void var1) {
                                            if (dialog != null) {
                                                try {
                                                    Thread.sleep(500);
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                } finally {
                                                    dialog.dismiss();
                                                }
                                                Message msg = new Message();
                                                msg.what = 3;
                                                if (myHandler != null) {
                                                    myHandler.sendMessage(msg);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable var1) {
                                            if (dialog != null) {
                                                try {
                                                    Thread.sleep(500);
                                                    switchOpen(isPowerOn);
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                } finally {
                                                    dialog.dismiss();
                                                }
                                            }
                                            Message msg = new Message();
                                            msg.what = 8;
                                            if (myHandler != null) {
                                                myHandler.sendMessage(msg);
                                            }
                                        }
                                    });
                            }

                            @Override
                            public void onFailure(Throwable var1) {
                                if (dialog != null) {
                                    try {
                                        Thread.sleep(500);
                                        if (airPurifier != null)
                                            switchOpen(airPurifier.airStatus().Power());
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        dialog.dismiss();
                                    }
                                }
                            }
                        });
                    }
                }
                break;
            case R.id.rlay_modeswitch:
                if (isPowerOn) {
                    initPopMenu();
                }
                break;
            case R.id.rlay_lockswitch:
                if (isPowerOn && !isOffLine) {
                    dialog = ProgressDialog.show(getActivity(), getString(R.string.my_setting), getString(R.string.sending_order));
                    dialog.setCanceledOnTouchOutside(true);
                    if (airPurifier != null) {
                        airPurifier.airStatus().setLock(isLockOn, new OperateCallback<Void>() {
                            @Override
                            public void onSuccess(Void var1) {
                                try {
                                    Thread.sleep(500);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                if (airPurifier != null) {
                                    airPurifier.airStatus().setLock(!isLockOn, new OperateCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void var1) {
                                            if (dialog != null) {
                                                try {
                                                    Thread.sleep(500);
                                                    Message msgL = new Message();
                                                    msgL.what = 4;
                                                    if (myHandler != null) {
                                                        myHandler.sendMessage(msgL);
                                                    }
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                } finally {
                                                    dialog.dismiss();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable var1) {
                                            Message msg = new Message();
                                            msg.what = 8;
                                            if (myHandler != null) {
                                                myHandler.sendMessage(msg);
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Throwable var1) {
                                if (dialog != null) {
                                    try {
                                        Thread.sleep(500);
                                        switchLock(isLockOn);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        dialog.dismiss();
                                    }
                                }
                            }
                        });
                    }
                }
                break;
            case R.id.rlay_btn_mode:
                rlay_hideContainer.setVisibility(View.GONE);
                break;
            case R.id.rlay_btn_mode1:
                if (isPowerOn && !isOffLine) {
                    dialog = ProgressDialog.show(getActivity(), getString(R.string.my_setting), getString(R.string.sending_order));
                    dialog.setCanceledOnTouchOutside(true);
                    if (airPurifier != null) {
                        airPurifier.airStatus().setSpeed(airPurifier.airStatus().speed(), new OperateCallback<Void>() {
                            @Override
                            public void onSuccess(Void var1) {
                                if (dialog != null) {
                                    try {
                                        Thread.sleep(500);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        dialog.dismiss();
                                    }
                                }
                                if (airPurifier != null) {
                                    airPurifier.airStatus().setSpeed(AirPurifier_MXChip.FAN_SPEED_POWER, new OperateCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void var1) {
                                            if (dialog != null) {
                                                try {
                                                    Thread.sleep(500);
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                } finally {
                                                    dialog.dismiss();
                                                }
                                                Message msg = new Message();
                                                msg.what = 5;
                                                if (myHandler != null) {
                                                    myHandler.sendMessage(msg);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable var1) {
                                            if (dialog != null) {
                                                try {
                                                    Thread.sleep(500);
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                } finally {
                                                    dialog.dismiss();
                                                }
                                            }
                                            Message msg = new Message();
                                            msg.what = 8;
                                            if (myHandler != null) {
                                                myHandler.sendMessage(msg);
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Throwable var1) {
                                if (dialog != null) {
                                    try {
                                        Thread.sleep(500);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        dialog.dismiss();
                                    }
                                }
                            }
                        });
                    }
                }
                break;
            case R.id.rlay_btn_mode2:
                if (isPowerOn && !isOffLine) {
                    dialog = ProgressDialog.show(getActivity(), getString(R.string.my_setting), getString(R.string.sending_order));
                    dialog.setCanceledOnTouchOutside(true);
                    if (airPurifier != null) {
                        airPurifier.airStatus().setSpeed(airPurifier.airStatus().speed(), new OperateCallback<Void>() {
                            @Override
                            public void onSuccess(Void var1) {
                                if (dialog != null) {
                                    try {
                                        Thread.sleep(500);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        dialog.dismiss();
                                    }
                                }
                                if (airPurifier != null) {
                                    airPurifier.airStatus().setSpeed(AirPurifier_MXChip.FAN_SPEED_AUTO, new OperateCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void var1) {
                                            if (dialog != null) {
                                                if (dialog != null) {
                                                    try {
                                                        Thread.sleep(500);
                                                    } catch (Exception ex) {
                                                        ex.printStackTrace();
                                                    } finally {
                                                        dialog.dismiss();
                                                    }
                                                }
                                                Message msg = new Message();
                                                msg.what = 6;
                                                if (myHandler != null) {
                                                    myHandler.sendMessage(msg);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable var1) {
                                            if (dialog != null) {
                                                try {
                                                    Thread.sleep(500);
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                } finally {
                                                    dialog.dismiss();
                                                }
                                            }
                                            Message msg = new Message();
                                            msg.what = 8;
                                            if (myHandler != null) {
                                                myHandler.sendMessage(msg);
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Throwable var1) {
                                if (dialog != null) {
                                    try {
                                        Thread.sleep(500);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        dialog.dismiss();
                                    }
                                }
                            }
                        });
                    }
                }
                break;
            case R.id.rlay_btn_mode3:
                if (isPowerOn && !isOffLine) {
                    dialog = ProgressDialog.show(getActivity(), getString(R.string.my_setting), getString(R.string.sending_order));
                    dialog.setCanceledOnTouchOutside(true);
                    if (airPurifier != null) {
                        airPurifier.airStatus().setSpeed(airPurifier.airStatus().speed(), new OperateCallback<Void>() {
                            @Override
                            public void onSuccess(Void var1) {
                                if (dialog != null) {
                                    try {
                                        Thread.sleep(500);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        dialog.dismiss();
                                    }
                                }
                                if (airPurifier != null) {
                                    airPurifier.airStatus().setSpeed(AirPurifier_MXChip.FAN_SPEED_SILENT, new OperateCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void var1) {
                                            if (dialog != null) {
                                                try {
                                                    Thread.sleep(500);
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                } finally {
                                                    dialog.dismiss();
                                                }
                                                Message msg = new Message();
                                                msg.what = 7;
                                                if (myHandler != null) {
                                                    myHandler.sendMessage(msg);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable var1) {
                                            if (dialog != null) {
                                                try {
                                                    Thread.sleep(500);
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                } finally {
                                                    dialog.dismiss();
                                                }
                                            }
                                            Message msg = new Message();
                                            msg.what = 8;
                                            if (myHandler != null) {
                                                myHandler.sendMessage(msg);
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Throwable var1) {
                                if (dialog != null) {
                                    try {
                                        Thread.sleep(500);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        dialog.dismiss();
                                    }
                                }
                            }
                        });
                    }
                }
                break;
            case R.id.rlay_hidecontainer:
                rlay_hideContainer.setVisibility(View.GONE);
                break;
            case R.id.lay_air_outside:
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
                tv_airOutside_aqi = (TextView) airDialog.findViewById(R.id.tv_airOutside_aqi);
                tv_airOutside_temp = (TextView) airDialog.findViewById(R.id.tv_airOutside_temp);
                tv_airOutside_humidity = (TextView) airDialog.findViewById(R.id.tv_airOutside_humidity);
                tv_airOutside_data = (TextView) airDialog.findViewById(R.id.tv_airOutside_data);
                tv_airOutside_city = (TextView) airDialog.findViewById(R.id.tv_airOutside_city);
                if (airWeather != null) {
                    if (airWeather.pm25 != null) {
                        tv_airOutside_pm.setText(airWeather.pm25 + "μg/m3");
                    } else {
                        tv_airOutside_pm.setText(0 + "μg/m3");
                    }
                    if (airWeather.aqi != null) {
                        tv_airOutside_aqi.setText(airWeather.aqi);
                    } else {
                        tv_airOutside_aqi.setText("0");
                    }
                    if (airWeather.tmp != null) {
                        tv_airOutside_temp.setText(airWeather.tmp + "℃");
                    } else {
                        tv_airOutside_temp.setText(0 + "℃");
                    }
                    if (airWeather.hum != null) {
                        tv_airOutside_humidity.setText(airWeather.hum + "%");
                    } else {
                        tv_airOutside_humidity.setText(0 + "%");
                    }
                    if (airWeather.weatherform != null) {
                        tv_airOutside_data.setText(airWeather.weatherform);
                    } else {
                        tv_airOutside_data.setText(getString(R.string.text_null));
                    }
                    if (airWeather.city != null) {
//                        if (!((OznerApplication) getActivity().getApplication()).isLanguageCN()) {
//                            tv_airOutside_city.setText(ChinaCities.getCityEnString(airWeather.city));
//                        } else {
                        tv_airOutside_city.setText(airWeather.city);
//                        }
//                        tv_airOutside_city.setText(airWeather.city);
                    }
                } else {
                    tv_airOutside_pm.setText(0 + "μg/m3");
                    tv_airOutside_aqi.setText("0");
                    tv_airOutside_temp.setText(0 + "℃");
                    tv_airOutside_humidity.setText(0 + "%");
                    tv_airOutside_data.setText(getString(R.string.text_null));
                    tv_airOutside_city.setText(getString(R.string.text_null));
                }
                airDialog.findViewById(R.id.tv_air_know).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        airDialog.dismiss();
                    }
                });
                airDialog.show();
                break;
        }
    }

    private void showDialog() {
        final Dialog airDialog = new Dialog(getContext(), R.style.dialog_style);
        airDialog.setContentView(R.layout.device_nonet_notice);
        TextView purifier_tip = (TextView) airDialog.findViewById(R.id.purifier_tip);
        purifier_tip.setText("4" + getString(R.string.device_nonet_notice2));
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


    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (VerticalAirPurifierFragment.this.isAdded()) {
                switch (msg.what) {
                    case 1:
                        NetWeather weather = (NetWeather) msg.obj;

                        if (weather != null) {

                            airWeather = weather;
                            if (weather.pm25 != null) {
                                tv_air_pmvalue.setText(weather.pm25);
                                OznerApplication.setControlNumFace(tv_air_pmvalue);
                            }
                            if (weather.city != null) {
                                if (VerticalAirPurifierFragment.this.isAdded()
                                        && !VerticalAirPurifierFragment.this.isDetached()
                                        && !VerticalAirPurifierFragment.this.isRemoving()) {

//                                    if (!((OznerApplication) getActivity().getApplication()).isLanguageCN()) {
//                                        tv_air_address.setText(ChinaCities.getCityEnString(weather.city));
//                                    } else {
                                    tv_air_address.setText(weather.city);
//                                    }
                                }
                            }
                            if (weather.qlty != null) {
                                if ("优".equals(weather.qlty)) {
                                    tv_air_quality.setText(getResources().getString(R.string.excellent));
                                } else if ("良".equals(weather.qlty)) {
                                    tv_air_quality.setText(getResources().getString(R.string.good));
                                } else if ("差".equals(weather.qlty)) {
                                    tv_air_quality.setText(getResources().getString(R.string.bads));
                                } else {
                                    tv_air_quality.setText(weather.qlty);
                                }
                            }
                        } else {
                            tv_air_pmvalue.setText("0");
                            OznerApplication.setControlNumFace(tv_air_pmvalue);
                            tv_air_address.setText(getString(R.string.air_dataLoding));
                            tv_air_quality.setText(getString(R.string.air_dataLoding));
                        }
                        break;
                    case 3:
                        switchOpen(isPowerOn);
                        break;
                    case 4:
                        switchLock(isLockOn);
                        break;
                    case 5:
                        switchModeBtn(R.id.rlay_btn_mode1);
                        rlay_btn_mode2.setSelected(false);
                        rlay_btn_mode3.setSelected(false);
                        break;
                    case 6:
                        switchModeBtn(R.id.rlay_btn_mode2);
                        rlay_btn_mode1.setSelected(false);
                        rlay_btn_mode3.setSelected(false);
                        break;
                    case 7:
                        rlay_btn_mode1.setSelected(false);
                        rlay_btn_mode2.setSelected(false);
                        switchModeBtn(R.id.rlay_btn_mode3);
                        break;
                    case 8:
                        Toast.makeText(getContext(), getString(R.string.sending_failed), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    private void switchModeBtn(int id) {
        switch (id) {
            case R.id.rlay_btn_mode1:
                modeDayOn = true;
                modeNightOn = false;
                modeAutoOn = false;
                if (rlay_hideContainer.getVisibility() == View.VISIBLE) {
                    rlay_hideContainer.setVisibility(View.GONE);
                }
                if (modeDayOn) {
                    iv_btn_mode1.setSelected(true);
                    iv_mode.setImageResource(R.drawable.air_modeday_on);
                    switchMode(true);
                    isModeOn = true;
                }
                break;
            case R.id.rlay_btn_mode2:
                modeAutoOn = true;
                modeNightOn = false;
                modeDayOn = false;
                if (rlay_hideContainer.getVisibility() == View.VISIBLE) {
                    rlay_hideContainer.setVisibility(View.GONE);
                }
                if (modeAutoOn) {
                    iv_btn_mode2.setSelected(true);
                    iv_mode.setSelected(true);
                    switchMode(true);
                    isModeOn = true;

                }
                break;
            case R.id.rlay_btn_mode3:
                modeNightOn = true;
                modeAutoOn = false;
                modeDayOn = false;
                if (rlay_hideContainer.getVisibility() == View.VISIBLE) {
                    rlay_hideContainer.setVisibility(View.GONE);
                }
                if (modeNightOn) {
                    iv_btn_mode3.setSelected(true);
                    iv_mode.setImageResource(R.drawable.air_modenight_on);
                    switchMode(true);
                    isModeOn = true;
                }
                break;
        }

        if (!modeDayOn && !modeAutoOn && !modeNightOn) {
            switchMode(false);
            iv_mode.setSelected(false);
            iv_mode.setImageResource(R.drawable.air_mode_off);
            isModeOn = false;
        }
        rlay_btn_mode1.setSelected(modeDayOn);
        rlay_btn_mode2.setSelected(modeAutoOn);
        rlay_btn_mode3.setSelected(modeNightOn);
    }

    private void switchMode(boolean isOn) {
        rlay_mode.setSelected(isOn);
//        iv_mode.setSelected(isOn);
        tv_mode.setSelected(isOn);
        isModeOn = isOn;
    }

    private void switchOpen(boolean isOn) {
        iv_open.setSelected(isOn);
        rlay_open.setSelected(isOn);
        tv_open.setSelected(isOn);
        if (!isOn) {
            showClosed();
        }
        if (!isOn && isModeOn) {
            switchMode(isOn);
            if (!isOn) {
                iv_mode.setImageResource(R.drawable.air_mode_off);
            } else {
                switchModeBtn(R.id.rlay_btn_mode2);
                switchMode(true);
            }
        }

        if (!isOn && isLockOn) {
            switchLock(isOn);
        }

        isPowerOn = isOn;
    }


    private void switchLock(boolean isOn) {
        rlay_lock.setSelected(isOn);
        iv_lock.setSelected(isOn);
        tv_lock.setSelected(isOn);
        isLockOn = isOn;
    }

    private void initPopMenu() {
        int[] pos = new int[2];
        rlay_mode.getLocationOnScreen(pos);
        setLayout(rlay_btn_mode, rlay_btn_mode1, rlay_btn_mode3, rlay_btn_mode2, pos[0], pos[1]);
        rlay_hideContainer.setVisibility(View.VISIBLE);
        setAnimator(rlay_btn_mode, rlay_btn_mode1, rlay_btn_mode3, rlay_btn_mode2);
    }

    public void setLayout(View view, View viewleft, View viewright, View viewup, int x, int y) {
        int px = OznerCommand.dip2px(getContext(), 25);
        int px2 = OznerCommand.dip2px(getContext(), 85);
        int px3 = OznerCommand.dip2px(getContext(), 55);
        view.setX(x);
        view.setY(y - px);
        viewleft.setX(x - px3);
        viewleft.setY(y - px2);
        viewright.setX(x + px3);
        viewright.setY(y - px2);
        viewup.setX(x);
        viewup.setY(y - px2 - px3);
    }

    public void setAnimator(View viw, View viewleft, View viewright, View viewup) {
        Animation animation1 = AnimationUtils.loadAnimation(getContext(), R.anim.abc_fade_in);
        animation1.setInterpolator(new DecelerateInterpolator(1));
        Animation animation2 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        animation2.setInterpolator(new DecelerateInterpolator(1));
        viw.startAnimation(animation1);
        viewleft.startAnimation(animation2);
        viewright.startAnimation(animation2);
        viewup.startAnimation(animation2);
    }

    private class UiUpdateAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            isNet = NetHelper.checkNetwork(getContext());
            initData();
            //开启线程获取网络数据
//        handler
            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {

        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            if (VerticalAirPurifierFragment.this != null && VerticalAirPurifierFragment.this.isAdded()) {
                setDate();
                if (isNet != 0 && !isOffLine) {
                    rlay_top2.setVisibility(View.INVISIBLE);
//                    offline_notice.setVisibility(View.INVISIBLE);
                } else if (isNet == 0) {
                    tv_phone_nonet.setVisibility(View.VISIBLE);
                    tv_device_nonet.setVisibility(View.INVISIBLE);
                    rlay_top2.setVisibility(View.VISIBLE);
//                    offline_notice.setVisibility(View.INVISIBLE);
                    showOffLine();
                } else if (isOffLine) {
                    rlay_top2.setVisibility(View.INVISIBLE);
                    showOffLine();
                }
            }
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {
        }
    }

    /**
     * 刷新网络状态
     */
    private void refreshNetStatus() {
        isNet = NetHelper.checkNetwork(getContext());
        if (isNet != 0 && !isOffLine) {
            rlay_top2.setVisibility(View.INVISIBLE);
//                    offline_notice.setVisibility(View.INVISIBLE);
        } else if (isNet == 0) {
            tv_phone_nonet.setVisibility(View.VISIBLE);
            tv_device_nonet.setVisibility(View.INVISIBLE);
            rlay_top2.setVisibility(View.VISIBLE);
//                    offline_notice.setVisibility(View.INVISIBLE);
            showOffLine();
        } else if (isOffLine) {
            rlay_top2.setVisibility(View.INVISIBLE);
            showOffLine();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UiUpdateAsyncTask uiUpdateAsyncTask = new UiUpdateAsyncTask();
        uiUpdateAsyncTask.execute("airver");

//        refreshUIData();
//        ((MainActivity)getActivity()).isShouldResume=false;
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
        if (this.Mac.equals(address)) {
            //此处应该执行更新数据异步操作
            new UiUpdateAsyncTask().execute();
//            refreshUIData();
        }
    }

    @Override
    public void DeviceDataChange() {

    }

    @Override
    public void ContentChange(String mac, String state) {
    }

    @Override
    public void RecvChatData(String data) {

    }
}




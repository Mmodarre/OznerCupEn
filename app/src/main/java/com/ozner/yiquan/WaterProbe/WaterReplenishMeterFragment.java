package com.ozner.yiquan.WaterProbe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.yiquan.Command.FootFragmentListener;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.PageState;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.Device.SetupWaterReplenMeterActivity;
import com.ozner.yiquan.HttpHelper.NetJsonObject;
import com.ozner.yiquan.HttpHelper.OznerDataHttp;
import com.ozner.yiquan.Main.BaseMainActivity;
import com.ozner.yiquan.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDeviceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by mengdongya on 2016/3/3.
 * modify by taoran
 */
public class WaterReplenishMeterFragment extends Fragment implements View.OnClickListener, FootFragmentListener {

    private String Mac, name;
    private WaterReplenishmentMeter waterReplenishmentMeter;
    ImageView ivSetup, iv_battery, iv_water_replenish_face, iv_water_replenish_type, firmwar_pb;
    private TextView tv_name, tv_batteryText, tv_data_loading, tv_water_replenish, tv_query_notice, tv_water_replenish_skin,
            tv_fuzhi_class, tv_fuzhi_bili, tv_eyesSkin_notice, lastValue, varValue, tv_battery_notice;
    private RelativeLayout rlay_menu, rely_round, query_result, rely_water_replenish_skin;
    private boolean isOpen = false, isTesting = false, TAG = false;
    private RotateAnimation animation;
    private int sex = 0, state;   //state标志眼、颈、脸、手四个位置的被选中状态
    private float oilValue = 0, waterValue = 0, lastestValue = 0;
    private LinearLayout laly_water_replenish, laly_water_replenish_skin;
    private int progress = 0;
    int queryTimes = 0, queryHands, queryFace, queryEyes, queryNeck, times;
    float varHandsValue, varFaceValue, varEyesValue, varNeckValue;
    SharedPreferences sh;
    SharedPreferences.Editor editor;
    BaseDeviceIO.ConnectStatus connectStatus;
    float battery;
    private float faceTotalValue, handTotalValue, neckTotalValue, eyesTotalValue, skinVarValue;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            Mac = getArguments().getString("MAC");
            waterReplenishmentMeter = (WaterReplenishmentMeter) OznerDeviceManager.Instance().getDevice(Mac);
            sh = getActivity().getSharedPreferences("SkinValues", Context.MODE_PRIVATE);
            editor = sh.edit();
        } catch (NullPointerException e) {
        }
        Log.e("mdy",Mac);
        View view = inflater.inflate(R.layout.water_replenish_meter_detail, container, false);
        initView(view);
        init();
        initDeviceState();
        OznerApplication.changeTextFont((ViewGroup) view);
        return view;
    }

    private void init() {
        if (waterReplenishmentMeter != null) {
            try {
                sex = (int) (waterReplenishmentMeter.getAppValue(PageState.Sex));
            } catch (Exception e) {
            }
            if (sex == 1) {
                iv_water_replenish_face.setImageResource(R.drawable.nan1);
            } else if (sex == 0) {
                iv_water_replenish_face.setImageResource(R.drawable.nv1);
            }
        }

    }

    private void initData() {
        name = waterReplenishmentMeter.Setting().name();
        isOpen = waterReplenishmentMeter.status().power();
        isTesting = waterReplenishmentMeter.status().isTesting();
        oilValue = waterReplenishmentMeter.status().testValue().oil;
        waterValue = waterReplenishmentMeter.status().testValue().moisture;
        battery = waterReplenishmentMeter.status().battery();
    }

    private void setDate() {
        tv_name.setText(name);

        if (queryFace != 0) {
            varFaceValue = faceTotalValue / queryFace;
            varFaceValue = new BigDecimal(varFaceValue).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        } else {
            varFaceValue = 0;
        }
        if (queryHands != 0) {
            varHandsValue = handTotalValue / queryHands;
            varHandsValue = new BigDecimal(varHandsValue).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        } else {
            varHandsValue = 0;
        }
        if (queryEyes != 0) {
            varEyesValue = eyesTotalValue / queryEyes;
            varEyesValue = new BigDecimal(varEyesValue).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        } else {
            varEyesValue = 0;
        }
        if (queryNeck != 0) {
            varNeckValue = neckTotalValue / queryNeck;
            varNeckValue = new BigDecimal(varNeckValue).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        } else {
            varNeckValue = 0;
        }

        switch (state) {
            case 1: //脸
                switchFace();
                break;
            case 2: //手
                switchHands();
                break;
            case 3: //眼
                switchEyes();
                break;
            case 4: //脖子
                switchNeck();
                break;
        }
        if (isOpen) {
            try {
                int power = Math.round(battery * 100);
                if (power == 100) {
                    iv_battery.setImageResource(R.drawable.battery100);
                    tv_batteryText.setText(String.valueOf(power) + "%");
                } else if (power < 100 && power >= 50) {
                    iv_battery.setImageResource(R.drawable.battery70);
                    tv_batteryText.setText(String.valueOf(power) + "%");
                } else if (power < 50 && power > 0) {
                    iv_battery.setImageResource(R.drawable.battery30);
                    tv_batteryText.setText(String.valueOf(power) + "%");
                    if (power < 15) {
                        tv_battery_notice.setVisibility(View.VISIBLE);
                    }
                } else if (power == 0) {
                    iv_battery.setImageResource(R.drawable.battery0);
                    tv_batteryText.setText(getResources().getString(R.string.text_null));
                }
                tv_batteryText.setTextColor(getResources().getColor(R.color.white));
            } catch (Exception ex) {
            }
        }
    }

    private void initDeviceState() { //设备连接状态
        connectStatus = waterReplenishmentMeter.connectStatus();
        switch (connectStatus) {
            case Connecting:
                tv_data_loading.setText(getString(R.string.loding_now));
                break;
            case Connected:
                tv_data_loading.setVisibility(View.GONE);
                break;
            case Disconnect:
                tv_data_loading.setText(getString(R.string.loding_fair));
                tv_data_loading.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void initQuery() {
        tv_query_notice.setText(R.string.query_checking);
        firmwar_pb.setVisibility(View.VISIBLE);
        firmwar_pb.setAnimation(animation);
        animation.startNow();
        query_result.setVisibility(View.GONE);
        tv_query_notice.setVisibility(View.VISIBLE);
        tv_eyesSkin_notice.setVisibility(View.INVISIBLE);
        rely_round.setBackgroundResource(R.drawable.water_replenish_meter_blue);
        TAG = true;
    }

    private void switchNeck() {
        if (isTesting) {
            initQuery();
        } else {
            firmwar_pb.setAnimation(null);
            firmwar_pb.setVisibility(View.GONE);
            if (TAG) {
                TAG = false;
                if (waterValue > 0) {
                    if (waterValue <= 35 && waterValue > 0) {
                        tv_fuzhi_class.setText(getString(R.string.dry));
                        rely_round.setBackgroundResource(R.drawable.water_replenish_meter_origen);
                        tv_eyesSkin_notice.setText(getString(R.string.neck_skin_notice1));
                    } else if (waterValue > 35 && waterValue <= 45) {
                        tv_fuzhi_class.setText(getString(R.string.normal));
                        tv_eyesSkin_notice.setText(getString(R.string.neck_skin_notice2));
                    } else if (waterValue > 45) {
                        tv_fuzhi_class.setText(getString(R.string.wetness));
                        tv_eyesSkin_notice.setText(getString(R.string.neck_skin_notice3));
                    }

                    tv_query_notice.setVisibility(View.GONE);
                    query_result.setVisibility(View.VISIBLE);
                    tv_eyesSkin_notice.setVisibility(View.VISIBLE);
                    tv_fuzhi_bili.setText((int) (waterValue + 0.5) + "");
                    rely_water_replenish_skin.setVisibility(View.VISIBLE);

                    if (waterValue > 0) {
                        new UpdateAsyncTask().execute(PageState.NeckSkinValue);
                        neckTotalValue += waterValue;
                        queryNeck ++;
                        lastestValue = waterValue;
                        waterValue = (int) (waterValue + 0.5);
                        editor.putString(Mac + "neck", waterValue + "");
                        editor.commit();
                    }
                } else if (waterValue == 0) {
                    tv_query_notice.setText(getResources().getString(R.string.query_error));
                    tv_query_notice.setVisibility(View.VISIBLE);
                } else {
                    tv_query_notice.setText(getResources().getString(R.string.query_dry));
                    tv_query_notice.setVisibility(View.VISIBLE);
                }
            }

        }

    }

    private void switchEyes() {
        if (isTesting) {
            initQuery();
        } else {
            firmwar_pb.setAnimation(null);
            firmwar_pb.setVisibility(View.GONE);
            if (TAG) {
                TAG = false;
                if (waterValue > 0) {
                    tv_query_notice.setVisibility(View.GONE);
                    if (waterValue <= 35 && waterValue > 0) {
                        tv_fuzhi_class.setText(getString(R.string.dry));
                        rely_round.setBackgroundResource(R.drawable.water_replenish_meter_origen);
                        tv_eyesSkin_notice.setText(getString(R.string.eyes_skin_notice1));
                    } else if (waterValue > 35 && waterValue <= 45) {
                        tv_fuzhi_class.setText(getString(R.string.normal));
                        tv_eyesSkin_notice.setText(getString(R.string.eyes_skin_notice2));
                    } else if (waterValue > 45) {
                        tv_fuzhi_class.setText(getString(R.string.wetness));
                        tv_eyesSkin_notice.setText(getString(R.string.eyes_skin_notice3));
                    }
                    tv_query_notice.setVisibility(View.GONE);
                    query_result.setVisibility(View.VISIBLE);
                    tv_eyesSkin_notice.setVisibility(View.VISIBLE);
                    tv_fuzhi_bili.setText((int) (waterValue + 0.5) + "");
                    rely_water_replenish_skin.setVisibility(View.VISIBLE);
                    if (waterValue > 0) {
                        new UpdateAsyncTask().execute(PageState.EyesSkinValue);
                        lastestValue = waterValue;
                        eyesTotalValue += waterValue;
                        queryEyes ++;
                        waterValue = (int) (waterValue + 0.5);
                        editor.putString(Mac + "eyes", waterValue + "");
                        editor.commit();

                    }
                } else if (waterValue == 0) {
                    tv_query_notice.setText(getResources().getString(R.string.query_error));
                    tv_query_notice.setVisibility(View.VISIBLE);
                } else {
                    tv_query_notice.setText(getResources().getString(R.string.query_dry));
                    tv_query_notice.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private void switchHands() {
        if (isTesting) {
            initQuery();
        } else {
            firmwar_pb.setAnimation(null);
            firmwar_pb.setVisibility(View.GONE);
            if (TAG) {
                TAG = false;
                if (waterValue > 0) {
                    tv_query_notice.setVisibility(View.GONE);
                    if (waterValue <= 30 && waterValue > 0) {
                        tv_fuzhi_class.setText(getString(R.string.dry));
                        tv_eyesSkin_notice.setText(getString(R.string.hands_skin_notice1));
                        rely_round.setBackgroundResource(R.drawable.water_replenish_meter_origen);
                    } else if (waterValue > 30 && waterValue <= 38) {
                        tv_fuzhi_class.setText(getString(R.string.normal));
                        tv_eyesSkin_notice.setText(getString(R.string.hands_skin_notice2));
                    } else if (waterValue > 38) {
                        tv_fuzhi_class.setText(getString(R.string.wetness));
                        tv_eyesSkin_notice.setText(getString(R.string.hands_skin_notice3));
                    }
                    tv_query_notice.setVisibility(View.GONE);
                    query_result.setVisibility(View.VISIBLE);
                    tv_eyesSkin_notice.setVisibility(View.VISIBLE);
                    tv_fuzhi_bili.setText((int) (waterValue + 0.5) + "");
                    rely_water_replenish_skin.setVisibility(View.VISIBLE);
                    if (waterValue > 0) {
                        new UpdateAsyncTask().execute(PageState.HandSkinValue);
                        lastestValue = waterValue;
                        handTotalValue += waterValue;
                        queryHands ++;
                        waterValue = (int) (waterValue + 0.5);
                        editor.putString(Mac + "hands", waterValue + "");
                        editor.commit();
                    }
                } else if (waterValue == 0) {
                    tv_query_notice.setText(getResources().getString(R.string.query_error));
                    tv_query_notice.setVisibility(View.VISIBLE);
                } else {
                    tv_query_notice.setText(getResources().getString(R.string.query_dry));
                    tv_query_notice.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private void switchFace() {
        if (isTesting) {
            initQuery();
        } else {
            firmwar_pb.setAnimation(null);
            firmwar_pb.setVisibility(View.GONE);
            if (TAG) {
                TAG = false;
                if (waterValue > 0) {
                    tv_query_notice.setVisibility(View.GONE);
                    if (waterValue <= 32 && waterValue > 0) {
                        tv_fuzhi_class.setText(getString(R.string.dry));
                        tv_eyesSkin_notice.setText(getString(R.string.face_skin_notice1));
                        rely_round.setBackgroundResource(R.drawable.water_replenish_meter_origen);
                    } else if (waterValue > 32 && waterValue <= 42) {
                        tv_fuzhi_class.setText(getString(R.string.normal));
                        tv_eyesSkin_notice.setText(getString(R.string.face_skin_notice2));
                    } else if (waterValue > 42) {
                        tv_fuzhi_class.setText(getString(R.string.wetness));
                        tv_eyesSkin_notice.setText(getString(R.string.face_skin_notice3));
                    }
                    tv_query_notice.setVisibility(View.GONE);
                    query_result.setVisibility(View.VISIBLE);
                    tv_eyesSkin_notice.setVisibility(View.VISIBLE);
                    tv_fuzhi_bili.setText((int) (waterValue + 0.5) + "");
                    rely_water_replenish_skin.setVisibility(View.VISIBLE);
                    if (waterValue > 0) {
                        new UpdateAsyncTask().execute(PageState.FaceSkinValue);
                        lastestValue = waterValue;
                        faceTotalValue += waterValue;
                        queryFace++;
                        waterValue = (int) (waterValue + 0.5);
                        editor.putString(Mac + "face", waterValue + "");
                        editor.commit();
                    }
                } else if (waterValue == 0) {
                    tv_query_notice.setText(getResources().getString(R.string.query_error));
                    tv_query_notice.setVisibility(View.VISIBLE);
                } else {
                    tv_query_notice.setText(getResources().getString(R.string.query_dry));
                    tv_query_notice.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private void initView(View view) {
        if (((OznerApplication) getActivity().getApplication()).isLoginPhone()) {
            view.findViewById(R.id.llay_cupHolder).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.llay_cupHolder).setVisibility(View.GONE);
        }
        ivSetup = (ImageView) view.findViewById(R.id.iv_water_replenish_setup);
        ivSetup.setOnClickListener(this);
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_query_notice = (TextView) view.findViewById(R.id.tv_query_notice);
        tv_water_replenish_skin = (TextView) view.findViewById(R.id.tv_water_replenish_skin);
        lastValue = (TextView) view.findViewById(R.id.tv_water_replenish_skinAge);
        varValue = (TextView) view.findViewById(R.id.tv_water_replenish_skinLast);
        tv_fuzhi_class = (TextView) view.findViewById(R.id.tv_fuzhi_class);
        tv_fuzhi_bili = (TextView) view.findViewById(R.id.tv_fuzhi_bili);
        tv_eyesSkin_notice = (TextView) view.findViewById(R.id.tv_eyesSkin_notice);
        tv_data_loading = (TextView) view.findViewById(R.id.tv_data_loading);
        rlay_menu = (RelativeLayout) view.findViewById(R.id.rlay_menu);
        rely_round = (RelativeLayout) view.findViewById(R.id.rely_round);//中间深蓝色的圆
        query_result = (RelativeLayout) view.findViewById(R.id.query_result);//中间深蓝色的圆
        rely_water_replenish_skin = (RelativeLayout) view.findViewById(R.id.rely_water_replenish_skin);//跳详情页面的部分
        rely_water_replenish_skin.setVisibility(View.GONE);
        rely_water_replenish_skin.setOnClickListener(this);
        rlay_menu.setOnClickListener(this);
        tv_batteryText = (TextView) view.findViewById(R.id.tv_batteryText);
        tv_battery_notice = (TextView) view.findViewById(R.id.tv_battery_notice);
        iv_battery = (ImageView) view.findViewById(R.id.iv_battery);
        tv_water_replenish = (TextView) view.findViewById(R.id.tv_water_replenish);
//        firmwar_pb = (WaterReplenishProgressBar) view.findViewById(R.id.firmwar_pb);
//        firmwar_pb.setMax(100);
        firmwar_pb = (ImageView) view.findViewById(R.id.firmwar_pb_pic);
        animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);
        animation.setRepeatCount(10);
        LinearInterpolator li = new LinearInterpolator();
        animation.setInterpolator(li);
        animation.setFillAfter(false);
        laly_water_replenish_skin = (LinearLayout) view.findViewById(R.id.laly_water_replenish_skin);
        laly_water_replenish_skin.setOnClickListener(this);
        laly_water_replenish = (LinearLayout) view.findViewById(R.id.laly_water_replenish);
        laly_water_replenish.setOnClickListener(this);
        iv_water_replenish_face = (ImageView) view.findViewById(R.id.iv_water_replenish_face);
        iv_water_replenish_face.setOnClickListener(this);
        iv_water_replenish_type = (ImageView) view.findViewById(R.id.iv_water_replenish_type);
        iv_water_replenish_type.setOnClickListener(this);
        iv_water_replenish_face.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Log.e("tag", v.getHeight() + "=======" + v.getWidth() + "=======");
//                Log.e("tagx", "x=====" + event.getX());
//                Log.e("tagy", "y=====" + event.getY());
                if (event.getX() < (v.getWidth() / 2) && event.getX() > (v.getWidth() / 3 - 100)) {
                    if (event.getY() < ((v.getHeight() / 2) + 150) && event.getY() > (v.getHeight() / 2 - 100)) {
                        changeFace();
                    } else if (event.getY() > ((v.getHeight() / 2) + 150) && event.getY() < ((v.getHeight() / 2) + 400)) {
                        changeHands();
                    }
                } else if (event.getX() > (v.getWidth() / 2) && event.getX() < (2 * v.getWidth() / 3)) {
                    if (event.getY() < ((v.getHeight() / 2) + 150) && event.getY() > (v.getHeight() / 3)) {
                        changeEye();
                    } else if (event.getY() > ((v.getHeight() / 2) + 150) && event.getY() < ((v.getHeight() / 2) + 300)) {
                        changeNeck();
                    }

                }
//                tv_data_loading.setVisibility(View.GONE);
//                new UiUpdateAsyncTask().execute();
                initData();
                setDate();
                return false;
            }
        });
    }

    private void change() {
        tv_battery_notice.setVisibility(View.INVISIBLE);
        iv_water_replenish_type.setVisibility(View.VISIBLE);
        iv_water_replenish_face.setVisibility(View.GONE);
        tv_water_replenish.setVisibility(View.GONE);
        rely_round.setVisibility(View.VISIBLE);
//        rely_round.setBackgroundResource(R.drawable.water_replenish_meter_blue);
        rely_water_replenish_skin.setVisibility(View.VISIBLE);
        laly_water_replenish_skin.setVisibility(View.GONE);
//        query_result.setVisibility(View.GONE);
    }

    private void changeFace() {
        change();
        if (sex == 0) {
            iv_water_replenish_type.setImageResource(R.drawable.nv3_02);
        } else {
            iv_water_replenish_type.setImageResource(R.drawable.nan3_02);
        }
        tv_query_notice.setText(getResources().getString(R.string.query_notice_face));
        state = 1;
        varValue.setText(String.format(getString(R.string.avg_times), varFaceValue, queryFace));
        String faceLastValue = sh.getString(Mac + "face", null);
        if (faceLastValue != null) {
            lastValue.setText(faceLastValue + "%");
        }else {
            lastValue.setText(getString(R.string.text_null));
        }


    }

    private void changeHands() {
        change();
        if (sex == 0) {
            iv_water_replenish_type.setImageResource(R.drawable.nv5_02);
        } else {
            iv_water_replenish_type.setImageResource(R.drawable.nan5_02);
        }
        tv_query_notice.setText(getResources().getString(R.string.query_notice_hand));
        state = 2;
        varValue.setText(String.format(getString(R.string.avg_times), varHandsValue, queryHands));
        String handsLastValue = sh.getString(Mac + "hands", null);
        if (null != handsLastValue) {
            lastValue.setText(handsLastValue + "%");
        }else {
            lastValue.setText(getString(R.string.text_null));
        }
    }

    private void changeEye() {
        change();
        if (sex == 0) {
            iv_water_replenish_type.setImageResource(R.drawable.nv2_02);
        } else {
            iv_water_replenish_type.setImageResource(R.drawable.nan2_02);
        }
        tv_query_notice.setText(getResources().getString(R.string.query_notice_eyes));
        state = 3;
        varValue.setText(String.format(getString(R.string.avg_times), varEyesValue, queryEyes));
        String eyesLastValue = sh.getString(Mac + "eyes", null);
        if (eyesLastValue != null) {
            lastValue.setText(eyesLastValue + "%");
        }else {
            lastValue.setText(getString(R.string.text_null));
        }
    }

    private void changeNeck() {
        change();
        if (sex == 0) {
            iv_water_replenish_type.setImageResource(R.drawable.nv4_02);
        } else {
            iv_water_replenish_type.setImageResource(R.drawable.nan4_02);
        }
        tv_query_notice.setText(getResources().getString(R.string.query_notice_bozi));
        state = 4;
        varValue.setText(String.format(getString(R.string.avg_times), varNeckValue, queryNeck));
        String neckLastValue = sh.getString(Mac + "neck", null);
        if (neckLastValue != null) {
            lastValue.setText(neckLastValue + "%");
        }else {
            lastValue.setText(getString(R.string.text_null));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        new GetTimesAsyncTask().execute();
        new GetWaterRMAsyncTask().execute();
        new UiUpdateAsyncTask().execute();
        rely_water_replenish_skin.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        Bundle params = new Bundle();
        params.putString("MAC", Mac);
        switch (v.getId()) {
            case R.id.iv_water_replenish_setup:
                intent.setClass(getContext(), SetupWaterReplenMeterActivity.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 0x1111);
                break;
            case R.id.rlay_menu:
                ((BaseMainActivity) getActivity()).myOverlayDrawer.toggleMenu();
                break;
            case R.id.laly_water_replenish_skin:
                SkinQueryNullFragment skinQueryNullFragment = new SkinQueryNullFragment();
                skinQueryNullFragment.setArguments(params);
                getFragmentManager().beginTransaction()
                        .replace(R.id.framen_main_con, skinQueryNullFragment).addToBackStack("replen")
                        .commitAllowingStateLoss();
                state = 0;
                break;
            case R.id.rely_water_replenish_skin:
                params.putString("state", state + "");
                SkinDetailFragment skinDetailFragment = new SkinDetailFragment();
                skinDetailFragment.setArguments(params);
                getFragmentManager().beginTransaction()
                        .replace(R.id.framen_main_con, skinDetailFragment).addToBackStack("replen")
                        .commitAllowingStateLoss();
                rely_water_replenish_skin.setVisibility(View.GONE);
                state = 0;
                break;
            case R.id.iv_water_replenish_type:
            case R.id.laly_water_replenish:
                laly_water_replenish.setBackgroundResource(R.color.water_replen_face);
                iv_water_replenish_type.setVisibility(View.GONE);
                iv_water_replenish_face.setVisibility(View.VISIBLE);
                tv_water_replenish.setVisibility(View.VISIBLE);
                rely_round.setVisibility(View.GONE);
                rely_round.setBackgroundResource(R.drawable.water_replenish_meter_blue);
                laly_water_replenish_skin.setVisibility(View.VISIBLE);
                rely_water_replenish_skin.setVisibility(View.GONE);
                tv_eyesSkin_notice.setVisibility(View.INVISIBLE);
                query_result.setVisibility(View.GONE);
                tv_query_notice.setVisibility(View.VISIBLE);
                firmwar_pb.setAnimation(null);
                firmwar_pb.setVisibility(View.GONE);
                TAG = false;
                state = 0;
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
            new UiUpdateAsyncTask().execute();
        }
    }

    @Override
    public void DeviceDataChange() {
    }

    @Override
    public void ContentChange(String mac, String state) {
        if (WaterReplenishMeterFragment.this.isAdded() && this.Mac.equals(mac)) {
            initDeviceState();
        }
    }

    @Override
    public void RecvChatData(String data) {
    }

    class UpdateAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params != null) {
                List<NameValuePair> pars = new ArrayList<>();
                pars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(getContext())));
                pars.add(new BasicNameValuePair("mac", Mac));
                pars.add(new BasicNameValuePair("ynumber", oilValue + ""));
                pars.add(new BasicNameValuePair("snumber", waterValue + ""));
                pars.add(new BasicNameValuePair("action", params[0]));
                String filterUrl = OznerPreference.ServerAddress(getContext()) + "OznerDevice/UpdateBuShuiYiNumber";
//                Log.e("123456", "doInBackground: url+" + filterUrl);
//                Log.e("123456", "updateValue");
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(getContext(), filterUrl, pars);
                return netJsonObject;
            }
            return null;
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
        }
    }

    class GetWaterRMAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params != null) {
                List<NameValuePair> pars = new ArrayList<>();
                pars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(getContext())));
                pars.add(new BasicNameValuePair("mac", Mac));
                pars.add(new BasicNameValuePair("myaction", PageState.EyesSkinValue));
                String filterUrl = OznerPreference.ServerAddress(getContext()) + "/OznerServer/GetBuShuiFenBu";
                Log.e("123456", filterUrl);
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(getContext(), filterUrl, pars);
                Log.e("123456", "GetBuShuiFenBu===" + netJsonObject.value);
                return netJsonObject;
            }
            return null;

        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            faceTotalValue = 0;
            handTotalValue = 0;
            eyesTotalValue = 0;
            neckTotalValue = 0;
            if (netJsonObject != null && netJsonObject.state > 0) {
                try {
                    JSONObject jsonObject1 = netJsonObject.getJSONObject().getJSONObject("data");
                    JSONObject object = jsonObject1.getJSONObject(PageState.FaceSkinValue);
                    JSONArray month = object.getJSONArray("monty");
                    int b = month.length();
                    for (int i = 0; i < b; i++) {
                        JSONObject object1 = (JSONObject) month.get(i);
                        float water = Float.parseFloat(object1.getString("snumber"));
                        float oil = Float.parseFloat(object1.getString("ynumber"));
                        int time = Integer.parseInt(object1.getString("times"));
                        faceTotalValue += time * water;
                        skinVarValue += time * oil;
                        times += time;
                    }
                    float oil = skinVarValue / times;
                    if (oil <= 12) {
                        tv_water_replenish_skin.setText(getString(R.string.skin_dry));
                    } else if (oil > 12 && oil <= 20) {
                        tv_water_replenish_skin.setText(getString(R.string.skin_mid));
                    } else if (oil > 20) {
                        tv_water_replenish_skin.setText(getString(R.string.skin_oily));
                    }

                } catch (Exception e) {
                }
                try {
                    JSONObject jsonObject1 = netJsonObject.getJSONObject().getJSONObject("data");
                    JSONObject object = jsonObject1.getJSONObject(PageState.HandSkinValue);
                    JSONArray month = object.getJSONArray("monty");

                    int b = month.length();
                    for (int i = 0; i < b; i++) {
                        JSONObject object1 = (JSONObject) month.get(i);
                        float water = Float.parseFloat(object1.getString("snumber"));
                        int time = Integer.parseInt(object1.getString("times"));
                        handTotalValue += time * water;
                    }

                } catch (Exception e) {
                }
                try {
                    JSONObject jsonObject1 = netJsonObject.getJSONObject().getJSONObject("data");
                    JSONObject object = jsonObject1.getJSONObject(PageState.EyesSkinValue);
                    JSONArray month = object.getJSONArray("monty");
                    int b = month.length();
                    for (int i = 0; i < b; i++) {
                        JSONObject object1 = (JSONObject) month.get(i);
                        float water = Float.parseFloat(object1.getString("snumber"));
                        int time = Integer.parseInt(object1.getString("times"));
                        eyesTotalValue += time * water;
                    }
                } catch (Exception e) {
                }
                try {
                    JSONObject jsonObject1 = netJsonObject.getJSONObject().getJSONObject("data");
                    JSONObject object = jsonObject1.getJSONObject(PageState.NeckSkinValue);
                    JSONArray month = object.getJSONArray("monty");
                    int b = month.length();
                    for (int i = 0; i < b; i++) {
                        JSONObject object1 = (JSONObject) month.get(i);
                        float water = Float.parseFloat(object1.getString("snumber"));
                        int time = Integer.parseInt(object1.getString("times"));
                        neckTotalValue += time * water;
                    }

                } catch (Exception e) {
                }

            }
        }
    }

    class GetTimesAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params != null) {
                List<NameValuePair> pars = new ArrayList<>();
                pars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(getContext())));
                pars.add(new BasicNameValuePair("mac", Mac));
                String filterUrl = OznerPreference.ServerAddress(getContext()) + "OznerDevice/GetTimesCountBuShui";
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(getContext(), filterUrl, pars);
//                Log.e("123456", netJsonObject.state + "GetTimesAsyncTask" + netJsonObject.value);
                return netJsonObject;
            }
            return null;

        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            queryFace = 0;
            queryHands = 0;
            queryEyes = 0;
            queryNeck = 0;
            if (netJsonObject != null && netJsonObject.state > 0) {
                try {
                    JSONArray jsonArray = netJsonObject.getJSONObject().getJSONArray("data");
                    for (int i = 0; i < 4; i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        String action = object.getString("action");
                        switch (action) {
                            case PageState.FaceSkinValue:
                                queryFace = Integer.parseInt(object.getString("times"));
                                break;
                            case PageState.HandSkinValue:
                                queryHands = Integer.parseInt(object.getString("times"));
                                break;
                            case PageState.EyesSkinValue:
                                queryEyes = Integer.parseInt(object.getString("times"));
                                break;
                            case PageState.NeckSkinValue:
                                queryNeck = Integer.parseInt(object.getString("times"));
                                break;
                        }
                    }
                } catch (Exception e) {
                }
            }
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
            if (WaterReplenishMeterFragment.this != null && WaterReplenishMeterFragment.this.isAdded()) {
                setDate();
            }
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {
        }
    }

}

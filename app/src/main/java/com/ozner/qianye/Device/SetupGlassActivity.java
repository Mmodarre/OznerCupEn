package com.ozner.qianye.Device;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ozner.qianye.Command.PageState;
import com.ozner.qianye.Command.UserDataPreference;
import com.ozner.cup.Cup;
import com.ozner.cup.CupSetting;
import com.ozner.qianye.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDeviceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mengdongya on 2015/12/1.
 */
public class SetupGlassActivity extends AppCompatActivity implements View.OnClickListener {
    Cup mCup = null;
    CupSetting cupSetting;
    private String Mac;
    private ColorPickerView cup_setting_colorpickerview;
    TextView tv_glass_name, tv_weight, takewater_ml, takewater_time, toolbar_save;
    TextView takewater_timeInterval, unit_time;
    SlidButton cupRemind, mobileRemind;
    int state1, state2, state3, state4 = 0;  //选中状态是1，未选中为0；mobileRemind也是
    //    int currentTextColor;
    Date StartTime, EndTime;
    RadioButton btn_takewater_temperature, btn_takewater_tds;
    ScrollView scrollview_setup_glass;
    private String weight, name, ml, addr;
    private boolean mr = false, cr = false, temp = false, tds = false;
    private int cupColor;
    private int remindInt, currentSel;
    private Toolbar toolbar;
    private boolean flag;
    List<String> dropList = new ArrayList<String>();
    SpinnerPopWindow mSpinner;
    LinearLayout ll_about_smart_glass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mac = getIntent().getStringExtra("MAC");
        mCup = (Cup) OznerDeviceManager.Instance().getDevice(Mac);
        cupSetting = mCup.Setting();
        setContentView(R.layout.activity_setup_glass);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.main_bgcolor));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.main_bgcolor));
        }
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        if (Mac != null) {
            UiUpdateAsyncTask uiUpdate = new UiUpdateAsyncTask();
            uiUpdate.execute("setcup");
        }
        reloadDropData();
        mSpinner = new SpinnerPopWindow(this);
        mSpinner.refreshData(dropList);
        mSpinner.setItemListener(new SpinnerPopWindow.IOnItemSelectListener() {
            @Override
            public void onItemClick(int pos) {
                if (pos < dropList.size()) {
                    takewater_timeInterval.setText(dropList.get(pos));
                    currentSel = pos;
                }
            }
        });
        initview();
        initClickListener();
    }

    public void showSpinWindow() {
        if (null != mSpinner && !mSpinner.isShowing()) {
            mSpinner.setWidth(takewater_timeInterval.getWidth());
            mSpinner.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            mSpinner.setBackgroundDrawable(getResources().getDrawable(R.color.colorLine));
            mSpinner.showAsDropDown(takewater_timeInterval);
        }
    }

    public void hideSpinWindow() {
        if (null != mSpinner && mSpinner.isShowing()) {
            mSpinner.dismiss();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideSpinWindow();
        return super.onTouchEvent(event);
    }

    private void reloadDropData() {
        for (int i = 0; i < 5; i++) {
            SpinnerPopWindow.DropDownItem item = new SpinnerPopWindow.DropDownItem();
            if (i == 4) {
                dropList.add("120");
//                item.text = "2小时";
            } else if (i == 3) {
                dropList.add("60");
//                item.text = "1小时";
            } else {
                dropList.add(i * 15 + 15 + "");
//                item.text = i*15+15+"分钟";
            }
//            dropList.add(item);
        }
    }

    private void initview() {
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tv_weight = (TextView) findViewById(R.id.activity_et_weight);
        takewater_ml = (TextView) findViewById(R.id.activity_et_water_ml);
        takewater_time = (TextView) findViewById(R.id.tv_takewater_time);
        toolbar_save = (TextView) findViewById(R.id.toolbar_save);
        toolbar_save.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.toolbar_text)).setText(getResources().getString(R.string.my_smart_glass));
        takewater_timeInterval = (TextView) findViewById(R.id.tv_settime_span);
        unit_time = (TextView) findViewById(R.id.unit_time);
        takewater_timeInterval.setOnClickListener(this);
        btn_takewater_temperature = (RadioButton) findViewById(R.id.btn_takewater_temperature);
        btn_takewater_tds = (RadioButton) findViewById(R.id.btn_takewater_tds);
        cup_setting_colorpickerview = (ColorPickerView) findViewById(R.id.cup_setting_colorpickerview);
        cup_setting_colorpickerview.setOnColorChangedListener(new ColorPickerBaseView.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                if (mCup != null && mCup.connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                    mCup.changeHaloColor(color);
                    cupColor = color;
                }
            }
        });
        scrollview_setup_glass = (ScrollView) findViewById(R.id.scrollview_setup_glass);
        cupRemind = (SlidButton) findViewById(R.id.switch0);
        cupRemind.SetSlidButtonState(mCup.Setting().RemindEnable());
        cupRemind.SetOnChangedListener(new SlidButton.OnChangedListener() {
            @Override
            public void OnChanged(boolean checkState) {
                    cupSetting.RemindEnable(checkState);
                    mCup.updateSettings();

            }
        });
        mobileRemind = (SlidButton) findViewById(R.id.switch1);
        mobileRemind.SetOnChangedListener(new SlidButton.OnChangedListener() {
            @Override
            public void OnChanged(boolean checkState) {
            }
        });
        tv_glass_name = (TextView) findViewById(R.id.tv_glass_name);
        cup_setting_colorpickerview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    scrollview_setup_glass.requestDisallowInterceptTouchEvent(false);
                } else {
                    scrollview_setup_glass.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });
        //设置体重，饮水量
        if (mCup.getAppValue(PageState.DEVICE_WEIGHT) != null) {
            weight = mCup.getAppValue(PageState.DEVICE_WEIGHT).toString();
        } else {
            weight = "55";
        }
        tv_weight.setHint(weight);
        if (mCup.getAppValue(PageState.DRINK_GOAL) != null) {
            ml = mCup.getAppValue(PageState.DRINK_GOAL).toString();
        } else {
            ml = Math.rint(55 * 27.428) + "";
        }
        takewater_ml.setHint(ml);
        remindInt = cupSetting.remindInterval();
        takewater_timeInterval.setHint(String.valueOf(remindInt));
        temp = cupSetting.haloMode() == CupSetting.Halo_Temperature ? true : false;
        tds = cupSetting.haloMode() == CupSetting.Halo_TDS ? true : false;
        btn_takewater_temperature.setChecked(temp);
        btn_takewater_tds.setChecked(tds);
        if (temp) {
            findViewById(R.id.show_as_temp).setVisibility(View.VISIBLE);
            findViewById(R.id.show_as_tds).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.show_as_temp).setVisibility(View.INVISIBLE);
            findViewById(R.id.show_as_tds).setVisibility(View.VISIBLE);
        }
    }


    private void initData() {
        name = cupSetting.name();
//        addr = mCup.getAppValue(PageState.DEVICE_ADDRES).toString();
        //设置今日状态
        state1 = Integer.valueOf(UserDataPreference.GetUserData(this, UserDataPreference.GanMao, "0"));
        state2 = Integer.valueOf(UserDataPreference.GetUserData(this, UserDataPreference.SportSweat, "0"));
        state3 = Integer.valueOf(UserDataPreference.GetUserData(this, UserDataPreference.HotWeather, "0"));
        state4 = Integer.valueOf(UserDataPreference.GetUserData(this, UserDataPreference.MenstrualComing, "0"));

        //饮水提醒时间段和时间间隔

        StartTime = new Date(0, 0, 0, cupSetting.remindStart() / 3600, cupSetting.remindStart() % 3600 / 60);
        EndTime = new Date(0, 0, 0, cupSetting.remindEnd() / 3600, cupSetting.remindEnd() % 3600 / 60);
        mr = UserDataPreference.GetUserData(this, UserDataPreference.MobileRemind, "0").equals("1");
        cr = cupSetting.RemindEnable();
        cupColor = cupSetting.haloColor();
    }

    private void setDate() {
//        tv_glass_name.setText(name+"("+addr+")");
        tv_glass_name.setText(name);
        tv_weight.setHint(weight);
        int wei = 0;
        try {
            wei = Integer.parseInt(weight);
        } catch (Exception e) {
        }
        if ("0".equals(ml)) {
            takewater_ml.setHint("" + Math.round((wei * 27.428)));
        } else {
            takewater_ml.setHint(ml);
        }
        if (state1 % 2 == 1) {
            findViewById(R.id.ll_ganmao_image).setSelected(true);
            ((TextView) findViewById(R.id.ll_ganmao_text)).setTextColor(getResources().getColor(R.color.theme_blue));
        }
        if (state2 % 2 == 1) {
            findViewById(R.id.ll_sport_image).setSelected(true);
            ((TextView) findViewById(R.id.ll_sport_text)).setTextColor(getResources().getColor(R.color.theme_blue));
        }
        if (state3 % 2 == 1) {
            findViewById(R.id.ll_hotday_image).setSelected(true);
            ((TextView) findViewById(R.id.ll_hotday_text)).setTextColor(getResources().getColor(R.color.theme_blue));
        }
        if (state4 % 2 == 1) {
            findViewById(R.id.ll_dayima_image).setSelected(true);
            ((TextView) findViewById(R.id.ll_dayima_text)).setTextColor(getResources().getColor(R.color.theme_blue));
        }
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        takewater_time.setText(fmt.format(StartTime) + "-" + fmt.format(EndTime));

//        cupRemind.SetSlidButtonState(cr);
//        mobileRemind.SetSlidButtonState(mr);
        //设置灯带的颜色
        cup_setting_colorpickerview.SetCenterColor(cupColor);
        //提醒模式   温度/TDS
    }

    private void initClickListener() {
        tv_glass_name.setOnClickListener(this);
        findViewById(R.id.ll_ganmao).setOnClickListener(this);
        findViewById(R.id.ll_dayima).setOnClickListener(this);
        findViewById(R.id.ll_sport).setOnClickListener(this);
        findViewById(R.id.ll_hotday).setOnClickListener(this);
        btn_takewater_temperature.setOnClickListener(this);
        btn_takewater_tds.setOnClickListener(this);
        findViewById(R.id.tv_delDeviceBtn).setOnClickListener(this);
        ll_about_smart_glass = (LinearLayout) findViewById(R.id.ll_about_smart_glass);
        ll_about_smart_glass.setOnClickListener(this);
        takewater_time.setOnClickListener(this);
        takewater_timeInterval.setOnClickListener(this);
        toolbar_save.setOnClickListener(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SetupGlassActivity.this).setMessage(getString(R.string.weather_save_device)).setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SaveSetting();
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).show();
            }
        });

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        final EditText editText = new EditText(this);
        switch (v.getId()) {
            case R.id.tv_settime_span:
                if (mSpinner.isShowing()) {
                    hideSpinWindow();
                } else {
                    showSpinWindow();
                }
                break;
            case R.id.tv_glass_name:
                intent.setClass(this, SetDeviceName.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 0);
                break;
            case R.id.ll_ganmao:
                state1++;
                state1 = state1 % 2;
                if (state1 % 2 == 1) {
                    findViewById(R.id.ll_ganmao_image).setSelected(true);
                    ((TextView) findViewById(R.id.ll_ganmao_text)).setTextColor(getResources().getColor(R.color.theme_blue));
                    ml = Integer.parseInt(ml) + 50 + "";
                    takewater_ml.setHint(ml);
                } else {
                    findViewById(R.id.ll_ganmao_image).setSelected(false);
                    ((TextView) findViewById(R.id.ll_ganmao_text)).setTextColor(getResources().getColor(R.color.toolbar_text_color));
                    ml = Integer.parseInt(ml) - 50 + "";
                    takewater_ml.setHint(ml);
                }
                break;
            case R.id.ll_sport:
                state2++;
                state2 = state2 % 2;
                if (state2 % 2 == 1) {
                    findViewById(R.id.ll_sport_image).setSelected(true);
                    ((TextView) findViewById(R.id.ll_sport_text)).setTextColor(getResources().getColor(R.color.theme_blue));
                    ml = Integer.parseInt(ml) + 50 + "";
                    takewater_ml.setHint(ml);
                } else {
                    findViewById(R.id.ll_sport_image).setSelected(false);
                    ((TextView) findViewById(R.id.ll_sport_text)).setTextColor(getResources().getColor(R.color.toolbar_text_color));
                    ml = Integer.parseInt(ml) - 50 + "";
                    takewater_ml.setHint(ml);
                }
                break;
            case R.id.ll_hotday:
                state3++;
                state3 = state3 % 2;
                if (state3 % 2 == 1) {
                    findViewById(R.id.ll_hotday_image).setSelected(true);
                    ((TextView) findViewById(R.id.ll_hotday_text)).setTextColor(getResources().getColor(R.color.theme_blue));
                    ml = Integer.parseInt(ml) + 50 + "";
                    takewater_ml.setHint(ml);
                } else {
                    findViewById(R.id.ll_hotday_image).setSelected(false);
                    ((TextView) findViewById(R.id.ll_hotday_text)).setTextColor(getResources().getColor(R.color.toolbar_text_color));
                    ml = Integer.parseInt(ml) - 50 + "";
                    takewater_ml.setHint(ml);
                }
                break;
            case R.id.ll_dayima:
                state4++;
                state4 = state4 % 2;
                if (state4 % 2 == 1) {
                    findViewById(R.id.ll_dayima_image).setSelected(true);
                    ((TextView) findViewById(R.id.ll_dayima_text)).setTextColor(getResources().getColor(R.color.theme_blue));
                    ml = Integer.parseInt(ml) + 50 + "";
                    takewater_ml.setHint(ml);
                } else {
                    findViewById(R.id.ll_dayima_image).setSelected(false);
                    ((TextView) findViewById(R.id.ll_dayima_text)).setTextColor(getResources().getColor(R.color.toolbar_text_color));
                    ml = Integer.parseInt(ml) - 50 + "";
                    takewater_ml.setHint(ml);
                }
                break;
            case R.id.tv_takewater_time:
                intent.setClass(this, SetRemindTime.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 1);
                break;
            case R.id.btn_takewater_temperature:
                btn_takewater_temperature.setChecked(true);
                btn_takewater_tds.setChecked(false);
                findViewById(R.id.show_as_temp).setVisibility(View.VISIBLE);
                findViewById(R.id.show_as_tds).setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_takewater_tds:
                btn_takewater_tds.setChecked(true);
                btn_takewater_temperature.setChecked(false);
                findViewById(R.id.show_as_temp).setVisibility(View.INVISIBLE);
                findViewById(R.id.show_as_tds).setVisibility(View.VISIBLE);
                break;
            case R.id.ll_about_smart_glass:
                intent.setClass(this, AboutDeviceActivity.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 1);
                break;
            case R.id.tv_delDeviceBtn:
                new AlertDialog.Builder(this).setTitle("").setMessage(getString(R.string.weather_delete_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                OznerDeviceManager.Instance().remove(mCup);
                                Intent data = new Intent();
                                data.putExtra("MAC", Mac);
                                setResult(PageState.DeleteDevice, data);
                                SetupGlassActivity.this.finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                break;
            case R.id.toolbar_save:
                new AlertDialog.Builder(this).setTitle("").setMessage(getString(R.string.weather_save_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SaveSetting();

                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                break;
            default:
                if (mSpinner.isShowing()) {
                    hideSpinWindow();
                }
                break;
        }
    }


    /*
    * 保存设置
    * */
    public void SaveSetting() {
        cupSetting.haloColor(cupColor);
        cupSetting.haloMode(btn_takewater_temperature.isChecked() ? CupSetting.Halo_Temperature : CupSetting.Halo_TDS);
        String jiange = takewater_timeInterval.getText().toString();
        try {
            remindInt = Integer.parseInt(jiange);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        UserDataPreference.SetUserData(this, UserDataPreference.GanMao, state1 + "");
        UserDataPreference.SetUserData(this, UserDataPreference.SportSweat, state2 + "");
        UserDataPreference.SetUserData(this, UserDataPreference.HotWeather, state3 + "");
        UserDataPreference.SetUserData(this, UserDataPreference.MenstrualComing, state4 + "");
        UserDataPreference.SetUserData(this, UserDataPreference.MobileRemind, mobileRemind.getSlidButtonState() ? "1" : "0");

//        Intent intent = new Intent(SetupGlassActivity.this, RemindService.class);
//        intent.putExtra("MAC", Mac);
//        if (mobileRemind.getSlidButtonState()) {
//            startService(intent);
////            bindService(intent,scnn, Context.BIND_AUTO_CREATE);
//        } else {
//            if (flag) {
//                stopService(intent);
////                unbindService(scnn);
//            }
//        }

        cupSetting.remindInterval(remindInt);
        OznerDeviceManager.Instance().save(mCup);
        if (tv_weight.getText().toString().isEmpty()) {
            mCup.setAppdata(PageState.DEVICE_WEIGHT, tv_weight.getHint().toString());
        } else {
            mCup.setAppdata(PageState.DEVICE_WEIGHT, tv_weight.getText().toString());
        }

        if (takewater_ml.getText().toString().isEmpty()) {
            try{
                int a = Integer.parseInt(tv_weight.getText().toString());
                mCup.setAppdata(PageState.DRINK_GOAL,(int)(a*27.428)+"");
            }catch (Exception e){
                int a = Integer.parseInt(tv_weight.getHint().toString());
                mCup.setAppdata(PageState.DRINK_GOAL,(int)(a*27.428)+"");
            }
            mCup.setAppdata(PageState.DRINK_GOAL, takewater_ml.getHint().toString());
        } else {
            mCup.setAppdata(PageState.DRINK_GOAL, takewater_ml.getText().toString());
        }
        mCup.updateSettings();
        SetupGlassActivity.this.finish();
    }

    ServiceConnection scnn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            flag = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            flag = false;
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        new UiUpdateAsyncTask().execute();
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
            //  initData();
            setDate();
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {
        }
    }

}

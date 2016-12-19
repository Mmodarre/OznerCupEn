package com.ozner.qianye.Device;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.Tap;
import com.ozner.tap.TapSetting;

import com.ozner.qianye.Command.PageState;
import com.ozner.qianye.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mengdongya on 2015/12/10.
 */
public class SetupWaterProbeActivity extends AppCompatActivity implements View.OnClickListener {

    Tap mTap = null;
    TapSetting mTapSetting = null;
    String Mac;
    TextView tv_probe_name, tv_time1_display, tv_time2_display, toolbar_save;
    Date time1, time2;
    RadioButton first, second;
    LinearLayout ll_about_water_probe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Mac = getIntent().getStringExtra(PageState.MAC);
            mTap = (Tap) OznerDeviceManager.Instance().getDevice(Mac);
            mTapSetting = mTap.Setting();
        } catch (NullPointerException e) {
        }
        setContentView(R.layout.activity_setup_waterprobe);

        ((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SetupWaterProbeActivity.this).setMessage(getString(R.string.weather_save_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                comit();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();
                            }
                        }).show();
            }
        });

        tv_probe_name = (TextView) findViewById(R.id.tv_probe_name);
        tv_time1_display = (TextView) findViewById(R.id.tv_time1_display);
        tv_time2_display = (TextView) findViewById(R.id.tv_time2_display);
        toolbar_save = (TextView) findViewById(R.id.toolbar_save);
        first = (RadioButton) findViewById(R.id.tv_time1);
        second = (RadioButton) findViewById(R.id.tv_time2);

        toolbar_save.setVisibility(View.VISIBLE);

        toolbar_save.setOnClickListener(this);
        findViewById(R.id.ll_detection_time1).setOnClickListener(this);
        findViewById(R.id.ll_detection_time2).setOnClickListener(this);
        ll_about_water_probe= (LinearLayout) findViewById(R.id.ll_about_water_probe);
        ll_about_water_probe.setOnClickListener(this);
        findViewById(R.id.tv_delDeviceBtn).setOnClickListener(this);
        tv_probe_name.setOnClickListener(this);

        initView();
    }

    private void initView() {
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        ((TextView) findViewById(R.id.toolbar_text)).setText(getString(R.string.my_water_probe));
        first.setText(getString(R.string.detection_time) + "1");
        second.setText(getString(R.string.detection_time) + "2");
        if (mTap != null) {
            tv_probe_name.setText(mTapSetting.name());
            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");

            time1 = new Date(0, 0, 0, mTap.Setting().DetectTime1() / 3600,
                    mTap.Setting().DetectTime1() % 3600 / 60);
            if (time1.getHours() == 0 && time1.getMinutes() == 0) {
                time1.setHours(10);
                time1.setMinutes(0);
            }
            tv_time1_display.setText(fmt.format(time1));

            time2 = new Date(0, 0, 0, mTap.Setting().DetectTime2() / 3600, mTap
                    .Setting().DetectTime2() % 3600 / 60);
            if (time2.getHours() == 0 && time2.getMinutes() == 0) {
                time2.setHours(18);
                time2.setMinutes(0);
            }
            tv_time2_display.setText(fmt.format(time2));
        }
    }

    private void comit() {
        if (tv_probe_name.getText().toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.set_cup_name_notice3), Toast.LENGTH_SHORT).show();
        } else {
            mTapSetting.name(tv_probe_name.getText().toString());
            mTapSetting.isDetectTime1(true);
            mTapSetting.DetectTime1(time1.getHours() * 3600 + time1.getMinutes() * 60);
            mTapSetting.isDetectTime2(true);
            mTapSetting.DetectTime2(time2.getHours() * 3600 + time2.getMinutes() * 60);
            mTap.updateSettings();
            this.finish();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.toolbar_save:
                new AlertDialog.Builder(this).setTitle("").setMessage(getString(R.string.weather_save_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                comit();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                break;
            case R.id.tv_probe_name:
                intent.setClass(this, SetDeviceName.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 0);
                break;
            case R.id.ll_detection_time1: {
                first.setTextColor(getResources().getColor(R.color.checked));
                findViewById(R.id.start_icon).setVisibility(View.VISIBLE);

                final TimePicker picker = new TimePicker(this);
                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                picker.setIs24HourView(true);
                picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay,
                                              int minute) {
                    }
                });

                picker.setCurrentHour(time1.getHours());
                picker.setCurrentMinute(time1.getMinutes());

                new AlertDialog.Builder(this).setView(picker)
                        .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                                time1 = new Date(0, 0, 0, picker
                                        .getCurrentHour(), picker
                                        .getCurrentMinute());
                                tv_time1_display.setText(fmt.format(time1));
                                first.setTextColor(getResources().getColor(R.color.toolbar_text_color));
                                findViewById(R.id.start_icon).setVisibility(View.INVISIBLE);
                            }
                        }).show();
            }
            break;
            case R.id.ll_detection_time2: {
                second.setTextColor(getResources().getColor(R.color.checked));
                findViewById(R.id.start_icon).setVisibility(View.VISIBLE);

                final TimePicker picker = new TimePicker(this);
                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                picker.setIs24HourView(true);
                picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay,
                                              int minute) {
                    }
                });

                picker.setCurrentHour(time2.getHours());
                picker.setCurrentMinute(time2.getMinutes());

                new AlertDialog.Builder(this).setView(picker)
                        .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                                time2 = new Date(0, 0, 0, picker
                                        .getCurrentHour(), picker
                                        .getCurrentMinute());
                                tv_time2_display.setText(fmt.format(time2));
                                second.setTextColor(getResources().getColor(R.color.toolbar_text_color));
                                findViewById(R.id.start_icon).setVisibility(View.INVISIBLE);
                            }
                        }).show();

            }
            break;
            case R.id.ll_about_water_probe:
                intent.setClass(this, AboutDeviceActivity.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 3);
                break;
            case R.id.tv_delDeviceBtn:
                new AlertDialog.Builder(this).setTitle("").setMessage(getString(R.string.weather_delete_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent data = new Intent();
                                data.putExtra(PageState.MAC, Mac);
                                setResult(PageState.DeleteDevice, data);
                                SetupWaterProbeActivity.this.finish();
                            }
                        }).setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
        }
    }

    @Override
    protected void onPostResume() {
        initView();
        super.onPostResume();
    }
}

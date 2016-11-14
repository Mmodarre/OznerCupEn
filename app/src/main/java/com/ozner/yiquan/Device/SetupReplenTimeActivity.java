package com.ozner.yiquan.Device;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.yiquan.Alarm.Alarm;
import com.ozner.yiquan.Alarm.AlarmReceiver;
//import com.ozner.cup.Alarm.AlarmService;
//import com.ozner.cup.Alarm.NoticeActivity;
import com.ozner.yiquan.Alarm.Alarms;
import com.ozner.yiquan.Command.PageState;
import com.ozner.yiquan.R;
import com.ozner.device.OznerDeviceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by mengdongya on 2016/3/1.
 */
public class SetupReplenTimeActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private TextView toolbar_save, tv_time1_display, tv_time2_display, tv_time3_display, tv_time1, tv_time2, tv_time3;
    private String Mac;
    Date firstTime, secondTime, thirdTime;
    SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
    WaterReplenishmentMeter waterReplenishmentMeter;
    private ImageView checkBox1, checkBox2, checkBox3;
    long time1, time2, time3;
    SharedPreferences sh;
    SharedPreferences.Editor editor;
    Alarm alarm1 = new Alarm();
    Alarm alarm2 = new Alarm();
    Alarm alarm3 = new Alarm();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Mac = getIntent().getStringExtra("MAC");
            waterReplenishmentMeter = (WaterReplenishmentMeter) OznerDeviceManager.Instance().getDevice(Mac);
        } catch (Exception e) {
        }
        setContentView(R.layout.activity_setup_replen_time);
        sh = this.getSharedPreferences("SkinValues", Context.MODE_PRIVATE);
        editor = sh.edit();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SetupReplenTimeActivity.this).setMessage(getString(R.string.weather_save_device)).setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        submit();
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
        toolbar_save = (TextView) findViewById(R.id.toolbar_save);
        toolbar_save.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.toolbar_text)).setText(getString(R.string.water_replen_meter));
        toolbar_save.setOnClickListener(this);

        tv_time1_display = (TextView) findViewById(R.id.tv_time1_display);
        tv_time2_display = (TextView) findViewById(R.id.tv_time2_display);
        tv_time3_display = (TextView) findViewById(R.id.tv_time3_display);
        tv_time1 = (TextView) findViewById(R.id.tv_time1);
        tv_time2 = (TextView) findViewById(R.id.tv_time2);
        tv_time3 = (TextView) findViewById(R.id.tv_time3);
        checkBox1 = (ImageView) findViewById(R.id.cb_set_time1);
        checkBox2 = (ImageView) findViewById(R.id.cb_set_time2);
        checkBox3 = (ImageView) findViewById(R.id.cb_set_time3);
        findViewById(R.id.rl_detection_time1).setOnClickListener(this);
        findViewById(R.id.rl_detection_time2).setOnClickListener(this);
        findViewById(R.id.rl_detection_time3).setOnClickListener(this);
        initData();

        //        alarm1.id = 0;
        alarm1.daysOfWeek = new Alarm.DaysOfWeek(0x7f);
        alarm1.vibrate = true;
        alarm1.label = "补水时间到了，亲，该补水了！！！";
        alarm1.alert = RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_RINGTONE);
//        alarm2.id = 1;
        alarm2.daysOfWeek = new Alarm.DaysOfWeek(0x7f);
        alarm2.vibrate = true;
        alarm2.label = "补水时间到了，亲，该补水了！！！";
        alarm2.alert = RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_RINGTONE);
//        alarm3.id = 2;
        alarm3.daysOfWeek = new Alarm.DaysOfWeek(0x7f);
        alarm3.vibrate = true;
        alarm3.label = "补水时间到了，亲，该补水了！！！";
        alarm3.alert = RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_RINGTONE);
    }

    private void initData() {

        try {
            time1 = (long) waterReplenishmentMeter.getAppValue(PageState.Time1);
        } catch (Exception e) {
//            Toast.makeText(this, "补水仪时间1获取异常", Toast.LENGTH_SHORT).show();
        }
        try {
            time2 = (long) waterReplenishmentMeter.getAppValue(PageState.Time2);
        } catch (Exception e) {
//            Toast.makeText(this, "补水仪时间2获取异常", Toast.LENGTH_SHORT).show();
        }
        try {
            time3 = (long) waterReplenishmentMeter.getAppValue(PageState.Time3);
        } catch (Exception e) {
//            Toast.makeText(this, "补水仪时间3获取异常", Toast.LENGTH_SHORT).show();
        }

        if (time1 != 0) {
            firstTime = new Date(time1);
            tv_time1_display.setText(fmt.format(firstTime));
            tv_time1_display.setSelected(true);
            tv_time1.setSelected(true);
            checkBox1.setSelected(true);
        } else {
            firstTime = new Date();
            firstTime.setHours(8);
            firstTime.setMinutes(30);
            tv_time1_display.setText(fmt.format(firstTime));
        }
        if (time2 != 0) {
            secondTime = new Date(time2);
            tv_time2_display.setText(fmt.format(secondTime));
            tv_time2_display.setSelected(true);
            tv_time2.setSelected(true);
            checkBox2.setSelected(true);
        } else {
            secondTime = new Date();
            secondTime.setHours(14);
            secondTime.setMinutes(30);
            tv_time2_display.setText(fmt.format(secondTime));
        }
        if (time3 != 0) {
            thirdTime = new Date(time3);
            tv_time3_display.setText(fmt.format(thirdTime));
            tv_time3_display.setSelected(true);
            tv_time3.setSelected(true);
            checkBox3.setSelected(true);
        } else {
            thirdTime = new Date();
            thirdTime.setHours(20);
            thirdTime.setMinutes(0);
            tv_time3_display.setText(fmt.format(thirdTime));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_save:
                submit();
                break;
            case R.id.rl_detection_time1:
                if (checkBox1.isSelected()) {
                    tv_time1_display.setSelected(false);
                    tv_time1.setSelected(false);
                    checkBox1.setSelected(false);
                } else {
                    final TimePicker picker1 = new TimePicker(this);
                    picker1.setIs24HourView(true);
                    picker1.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                        @Override
                        public void onTimeChanged(TimePicker view, int hourOfDay,
                                                  int minute) {
                            alarm1.hour = hourOfDay;
                            alarm1.minutes = minute;
                        }
                    });

                    picker1.setCurrentHour(firstTime.getHours());
                    picker1.setCurrentMinute(firstTime.getMinutes());

                    new AlertDialog.Builder(this).setView(picker1)
                            .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    firstTime = new Date(0, 0, 0, picker1
                                            .getCurrentHour(), picker1
                                            .getCurrentMinute());
                                    tv_time1_display.setText(fmt.format(firstTime));
                                    tv_time1_display.setSelected(true);
                                    tv_time1.setSelected(true);
                                    checkBox1.setSelected(true);
                                }
                            }).show();
                }
                break;
            case R.id.rl_detection_time2:
                if (checkBox2.isSelected()) {
                    tv_time2_display.setSelected(false);
                    tv_time2.setSelected(false);
                    checkBox2.setSelected(false);
                } else {
                    final TimePicker picker2 = new TimePicker(this);
                    picker2.setIs24HourView(true);
                    picker2.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                        @Override
                        public void onTimeChanged(TimePicker view, int hourOfDay,
                                                  int minute) {
                            alarm2.hour = hourOfDay;
                            alarm2.minutes = minute;
                        }
                    });

                    picker2.setCurrentHour(secondTime.getHours());
                    picker2.setCurrentMinute(secondTime.getMinutes());

                    new AlertDialog.Builder(this).setView(picker2)
                            .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                                    secondTime = new Date(0, 0, 0, picker2
                                            .getCurrentHour(), picker2
                                            .getCurrentMinute());
                                    tv_time2_display.setText(fmt.format(secondTime));
                                    tv_time2_display.setSelected(true);
                                    tv_time2.setSelected(true);
                                    checkBox2.setSelected(true);
                                }
                            }).show();
                }
                break;
            case R.id.rl_detection_time3:
                if (checkBox3.isSelected()) {
                    tv_time3_display.setSelected(false);
                    tv_time3.setSelected(false);
                    checkBox3.setSelected(false);
                } else {
                    final TimePicker picker3 = new TimePicker(this);
                    picker3.setIs24HourView(true);
                    picker3.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                        @Override
                        public void onTimeChanged(TimePicker view, int hourOfDay,
                                                  int minute) {
                            alarm3.hour = hourOfDay;
                            alarm3.minutes = minute;
                        }
                    });

                    picker3.setCurrentHour(thirdTime.getHours());
                    picker3.setCurrentMinute(thirdTime.getMinutes());

                    new AlertDialog.Builder(this).setView(picker3)
                            .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                                    thirdTime = new Date(0, 0, 0, picker3
                                            .getCurrentHour(), picker3
                                            .getCurrentMinute());
                                    tv_time3_display.setText(fmt.format(thirdTime));
                                    tv_time3_display.setSelected(true);
                                    tv_time3.setSelected(true);
                                    checkBox3.setSelected(true);
                                }
                            }).show();
                }
                break;
        }
    }

    private void submit() {
        if (checkBox1.isSelected()) {
            waterReplenishmentMeter.setAppdata(PageState.Time1, firstTime.getTime());
            editor.putLong(PageState.Time1, firstTime.getTime());
        } else {
            waterReplenishmentMeter.setAppdata(PageState.Time1, 0);
            editor.putLong(PageState.Time1, 0);
        }
        if (checkBox2.isSelected()) {
            waterReplenishmentMeter.setAppdata(PageState.Time2, secondTime.getTime());
            editor.putLong(PageState.Time2, secondTime.getTime());
        } else {
            waterReplenishmentMeter.setAppdata(PageState.Time2, 0);
            editor.putLong(PageState.Time2, 0);
        }
        if (checkBox3.isSelected()) {
            waterReplenishmentMeter.setAppdata(PageState.Time3, thirdTime.getTime());
            editor.putLong(PageState.Time3, thirdTime.getTime());
        } else {
            waterReplenishmentMeter.setAppdata(PageState.Time3, 0);
            editor.putLong(PageState.Time3, 0);
        }
        editor.commit();
        alarm1.enabled = checkBox1.isSelected();
        alarm2.enabled = checkBox2.isSelected();
        alarm3.enabled = checkBox3.isSelected();
        int a = 0;
        try {
            a = Alarms.getAlarmsCursor(getContentResolver()).getCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (a == 0) {
            Alarms.addAlarm(this, alarm1);
            Alarms.addAlarm(this, alarm2);
            Alarms.addAlarm(this, alarm3);
        } else if (a >= 3) {
            Alarms.setAlarm(this, alarm1);
            Alarms.setAlarm(this, alarm2);
            Alarms.setAlarm(this, alarm3);
        }
        waterReplenishmentMeter.updateSettings();
        SetupReplenTimeActivity.this.finish();
    }

}

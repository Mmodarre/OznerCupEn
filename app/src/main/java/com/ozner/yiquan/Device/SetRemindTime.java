package com.ozner.yiquan.Device;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ozner.yiquan.Cup;
import com.ozner.yiquan.CupSetting;
import com.ozner.device.OznerDeviceManager;

import com.ozner.yiquan.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mengdongya on 2015/12/2.
 */
public class SetRemindTime extends AppCompatActivity implements View.OnClickListener {

    Date StartTime;
    Date EndTime;
    TextView remindStartTime, remindEndTime, toolbar_save;
    RadioButton start, end;
    String Mac;
    Cup mCup = null;
    CupSetting mCupSetting = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mac = getIntent().getStringExtra("MAC");
        mCup = (Cup) OznerDeviceManager.Instance().getDevice(Mac);
        mCupSetting = mCup.Setting();
        setContentView(R.layout.set_remind_time);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.fz_blue));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.fz_blue));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetRemindTime.this.finish();
            }
        });
        toolbar_save = (TextView) findViewById(R.id.toolbar_save);
        toolbar_save.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.toolbar_text)).setText(getString(R.string.take_water_time));
        remindStartTime = (TextView) findViewById(R.id.tv_starttime_display);
        remindEndTime = (TextView) findViewById(R.id.tv_endtime_display);
        start = (RadioButton) findViewById(R.id.tv_start_time);
        end = (RadioButton) findViewById(R.id.tv_end_time);

        init();
    }

    private void init() {

        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        StartTime = new Date(0, 0, 0, mCup.Setting().remindStart() / 3600,
                mCup.Setting().remindStart() % 3600 / 60);
        EndTime = new Date(0, 0, 0, mCup.Setting().remindEnd() / 3600, mCup
                .Setting().remindEnd() % 3600 / 60);

        remindStartTime.setText(fmt.format(StartTime));
        remindEndTime.setText(fmt.format(EndTime));

        toolbar_save.setOnClickListener(this);
        findViewById(R.id.ll_starttime).setOnClickListener(this);
        findViewById(R.id.ll_endtime).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_save:
                if (EndTime.getHours() < StartTime.getHours()) {
                    Toast.makeText(this, getString(R.string.set_remind_time_notice1), Toast.LENGTH_LONG).show();
                } else if (EndTime.equals(StartTime)) {
                    Toast.makeText(this, getString(R.string.set_remind_time_notice2), Toast.LENGTH_LONG).show();
                } else {
                    mCupSetting.remindStart(StartTime.getHours() * 3600 + StartTime.getMinutes() * 60);
                    mCupSetting.remindEnd(EndTime.getHours() * 3600 + EndTime.getMinutes() * 60);
//                    OznerDeviceManager.Instance().save(mCup);
                    mCup.updateSettings();
                    SetRemindTime.this.finish();
                }
                break;
            case R.id.ll_starttime: {
                start.setTextColor(getResources().getColor(R.color.checked));
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

                picker.setCurrentHour(StartTime.getHours());
                picker.setCurrentMinute(StartTime.getMinutes());

                new AlertDialog.Builder(this).setView(picker)
                        .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                                StartTime = new Date(0, 0, 0, picker
                                        .getCurrentHour(), picker
                                        .getCurrentMinute());
                                remindStartTime.setText(fmt.format(StartTime));
                                start.setTextColor(getResources().getColor(R.color.font_black));
                                findViewById(R.id.start_icon).setVisibility(View.INVISIBLE);
                            }
                        }).show();
            }
            break;
            case R.id.ll_endtime: {
                end.setTextColor(getResources().getColor(R.color.checked));
                findViewById(R.id.end_icon).setVisibility(View.VISIBLE);
                final TimePicker picker = new TimePicker(this);
                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                picker.setIs24HourView(true);
                picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay,
                                              int minute) {
                    }
                });

                picker.setCurrentHour(EndTime.getHours());
                picker.setCurrentMinute(EndTime.getMinutes());

                new AlertDialog.Builder(this).setView(picker)
                        .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                                EndTime = new Date(0, 0, 0,
                                        picker.getCurrentHour(), picker
                                        .getCurrentMinute());
                                remindEndTime.setText(fmt.format(EndTime));
                                end.setTextColor(getResources().getColor(R.color.font_black));
                                findViewById(R.id.end_icon).setVisibility(View.INVISIBLE);
                            }
                        }).show();
            }
            break;
        }
    }
}

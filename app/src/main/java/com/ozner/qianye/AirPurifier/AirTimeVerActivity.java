package com.ozner.qianye.AirPurifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.AirPurifier.PowerTimer;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import com.ozner.qianye.Command.OznerCommand;
import com.ozner.qianye.R;

/**
 * Created by mengdongya on 2015/12/28.
 */
public class AirTimeVerActivity extends AppCompatActivity implements View.OnClickListener {
    public final static int Monday = 0x01;
    public final static int Tuesday = 0x02;
    public final static int Wednesday = 0x04;
    public final static int Trusday = 0x08;
    public final static int Firday = 0x10;
    public final static int Sturday = 0x20;
    public final static int Sunday = 0x40;
    String Mac = null;
    AirPurifier_MXChip airPurifier;
    OznerDevice device;
    CheckBox cb_one, cb_two, cb_three, cb_four, cb_five, cb_six, cb_seven;
    TextView toolbar_text, toolbar_save, startHour, startMin, endHour, endMin;
    int startHourTime = 0, startMiniTime = 0, endHourTime = 0, endMiniTime = 0, monday, tuesday, wednesday, trusday, firday, sturday, sunday;
    ImageButton ib_start_hour_add, ib_start_hour_minus, ib_start_min_add, ib_start_min_minus, ib_end_hour_add, ib_end_hour_minus, ib_end_min_add, ib_end_min_minus;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            Mac = getIntent().getStringExtra("MAC");
            device = OznerDeviceManager.Instance().getDevice(Mac);
            if (AirPurifierManager.IsWifiAirPurifier(device.Type())) {
                airPurifier = (AirPurifier_MXChip) device;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        setContentView(R.layout.air_ver_time_frament);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getApplicationContext()).setMessage(getString(R.string.weather_save_device)).setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        save();
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
        cb_one = (CheckBox) findViewById(R.id.cb_one);
        cb_two = (CheckBox) findViewById(R.id.cb_two);
        cb_three = (CheckBox) findViewById(R.id.cb_three);
        cb_four = (CheckBox) findViewById(R.id.cb_four);
        cb_five = (CheckBox) findViewById(R.id.cb_five);
        cb_six = (CheckBox) findViewById(R.id.cb_six);
        cb_seven = (CheckBox) findViewById(R.id.cb_seven);

        ib_start_hour_add = (ImageButton) findViewById(R.id.ib_start_hour_add);
        ib_start_hour_minus = (ImageButton) findViewById(R.id.ib_start_hour_minus);
        ib_start_min_add = (ImageButton) findViewById(R.id.ib_start_min_add);
        ib_start_min_minus = (ImageButton) findViewById(R.id.ib_start_min_minus);
        ib_end_hour_add = (ImageButton) findViewById(R.id.ib_end_hour_add);
        ib_end_hour_minus = (ImageButton) findViewById(R.id.ib_end_hour_minus);
        ib_end_min_add = (ImageButton) findViewById(R.id.ib_end_min_add);
        ib_end_min_minus = (ImageButton) findViewById(R.id.ib_end_min_minus);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWide = dm.widthPixels;
        int margin = OznerCommand.dip2px(getApplicationContext(),20);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)((screenWide-margin)/7+0.5),(int)((screenWide-margin)/7+0.5));
        cb_one.setLayoutParams(layoutParams);
        cb_two.setLayoutParams(layoutParams);
        cb_three.setLayoutParams(layoutParams);
        cb_four.setLayoutParams(layoutParams);
        cb_five.setLayoutParams(layoutParams);
        cb_six.setLayoutParams(layoutParams);
        cb_seven.setLayoutParams(layoutParams);

        initview();

    }

    private void initview() {

        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_text.setText(getString(R.string.timing));
        toolbar_save = (TextView) findViewById(R.id.toolbar_save);
        toolbar_save.setVisibility(View.VISIBLE);
        toolbar_save.setOnClickListener(this);
        startHour = (TextView) findViewById(R.id.tv_start_hour);
        startMin = (TextView) findViewById(R.id.tv_start_min);
        endHour = (TextView) findViewById(R.id.tv_end_hour);
        endMin = (TextView) findViewById(R.id.tv_end_min);

        ib_start_hour_add.setOnClickListener(this);
        ib_start_hour_minus.setOnClickListener(this);
        ib_start_min_add.setOnClickListener(this);
        ib_start_min_minus.setOnClickListener(this);
        ib_end_hour_add.setOnClickListener(this);
        ib_end_hour_minus.setOnClickListener(this);
        ib_end_min_add.setOnClickListener(this);
        ib_end_min_minus.setOnClickListener(this);

        PowerTimer powerTimer = airPurifier.PowerTimer();
        startHourTime = powerTimer.PowerOnTime / 60;
        startMiniTime = powerTimer.PowerOnTime % 60;
        endHourTime = powerTimer.PowerOffTime / 60;
        endMiniTime = powerTimer.PowerOffTime % 60;

        if (startHourTime > 9) {
            startHour.setText(startHourTime + "");
        } else {
            startHour.setText("0" + startHourTime);
        }

        if (startMiniTime > 9) {
            startMin.setText(startMiniTime + "");
        } else {
            startMin.setText("0" + startMiniTime);
        }

        if (endHourTime > 9) {
            endHour.setText(endHourTime + "");
        } else {
            endHour.setText("0" + endHourTime);
        }

        if (endMiniTime > 9) {
            endMin.setText(endMiniTime + "");
        } else {
            endMin.setText("0" + endMiniTime);
        }

        int[] currentWeek = new int[7];
        for (int i = 0; i < 7; i++) {
            currentWeek[i] = powerTimer.Week >> i & 0x01;
//            Log.e("122464",i+":"+currentWeek[i]);
        }
        cb_one.setChecked(1 == currentWeek[0]);
        cb_two.setChecked(1 == currentWeek[1]);
        cb_three.setChecked(1 == currentWeek[2]);
        cb_four.setChecked(1 == currentWeek[3]);
        cb_five.setChecked(1 == currentWeek[4]);
        cb_six.setChecked(1 == currentWeek[5]);
        cb_seven.setChecked(1 == currentWeek[6]);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_save:
                new AlertDialog.Builder(this).setMessage(getString(R.string.weather_save_device)).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        save();
                    }
                }).show();
                break;
            case R.id.ib_start_hour_add:
                startHourTime++;
                if (startHourTime >= 12 && startHourTime < 24) {
                    startHour.setText(startHourTime + "");
                } else if (startHourTime < 10) {
                    startHour.setText("0" + startHourTime);
                } else if (startHourTime > 23) {
                    startHourTime = 23;
                    startHour.setText("23");
                }
                break;
            case R.id.ib_start_hour_minus:
                startHourTime--;
                if (startHourTime <= 0) {
                    startHourTime = 0;
                    startHour.setText("0" + startHourTime);
                } else if (startHourTime < 10) {
                    startHour.setText("0" + startHourTime);
                } else {
                    startHour.setText(startHourTime + "");
                }
                break;
            case R.id.ib_start_min_add:
                startMiniTime++;
                if (startMiniTime < 60 && startMiniTime > 9) {
                    startMin.setText(startMiniTime + "");
                } else if (startMiniTime < 0) {
                    startMiniTime = 0;
                    startMin.setText("00");
                } else {
                    startMin.setText("0" + startMiniTime);
                }
                break;
            case R.id.ib_start_min_minus:
                startMiniTime--;
                if (startMiniTime <= 0) {
                    startMiniTime = 0;
                    startMin.setText("0" + startMiniTime);
                } else if (startMiniTime < 10) {
                    startMin.setText("0" + startMiniTime);
                } else {
                    startMin.setText(startMiniTime + "");
                }
                break;
            case R.id.ib_end_hour_add:
                endHourTime++;
                if (endHourTime >= 12 && endHourTime < 24) {
                    endHour.setText(endHourTime + "");
                } else if (endHourTime < 10) {
                    endHour.setText("0" + endHourTime);
                } else if (endHourTime > 23) {
                    endHourTime = 23;
                    endHour.setText("23");
                }
                break;
            case R.id.ib_end_hour_minus:
                endHourTime--;
                if (endHourTime <= 0) {
                    endHourTime = 0;
                    endHour.setText("0" + endHourTime);
                } else if (endHourTime < 10) {
                    endHour.setText("0" + endHourTime);
                } else {
                    endHour.setText(endHourTime + "");
                }
                break;
            case R.id.ib_end_min_add:
                endMiniTime++;
                if (endMiniTime < 60) {
                    if (endMiniTime < 10 && endMiniTime > 0) {
                        endMin.setText("0" + endMiniTime);
                    } else {
                        endMin.setText(endMiniTime + "");
                    }
                } else if (endMiniTime <= 0) {
                    endMiniTime = 0;
                    endMin.setText("00");
                }
                break;
            case R.id.ib_end_min_minus:
                endMiniTime--;
                if (endMiniTime <= 0) {
                    endMiniTime = 0;
                    endMin.setText("0" + endMiniTime);
                } else if (endMiniTime < 10) {
                    endMin.setText("0" + endMiniTime);
                } else {
                    endMin.setText(endMiniTime + "");
                }
                break;
            case R.id.cb_one:
                cb_one.setChecked(!cb_one.isChecked());
                break;
            case R.id.cb_two:
                cb_two.setChecked(!cb_two.isChecked());
                break;
            case R.id.cb_three:
                cb_three.setChecked(!cb_three.isChecked());
                break;
            case R.id.cb_four:
                cb_four.setChecked(!cb_four.isChecked());
                break;
            case R.id.cb_five:
                cb_five.setChecked(!cb_five.isChecked());
                break;
            case R.id.cb_six:
                cb_six.setChecked(!cb_six.isChecked());
                break;
            case R.id.cb_seven:
                cb_seven.setChecked(!cb_seven.isChecked());
                if (cb_seven.isChecked()) {
                }
                break;
        }

    }

    private void save() {
        PowerTimer timer = airPurifier.PowerTimer();
        timer.Enable = true;
        timer.PowerOnTime = (short) (startHourTime * 60 + startMiniTime);
        timer.PowerOffTime = (short) (endHourTime * 60 + endMiniTime);
        monday = cb_one.isChecked() ? Monday : 0;
        tuesday = cb_two.isChecked() ? Tuesday : 0;
        wednesday = cb_three.isChecked() ? Wednesday : 0;
        trusday = cb_four.isChecked() ? Trusday : 0;
        firday = cb_five.isChecked() ? Firday : 0;
        sturday = cb_six.isChecked() ? Sturday : 0;
        sunday = cb_seven.isChecked() ? Sunday : 0;
        timer.Week = (byte) (monday | tuesday | wednesday | trusday | firday | sturday | sunday);
        airPurifier.updateSettings();
        AirTimeVerActivity.this.finish();
    }

}

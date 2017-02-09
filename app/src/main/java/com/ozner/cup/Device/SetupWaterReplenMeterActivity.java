package com.ozner.cup.Device;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.cup.ACSqlLite.CCacheWorking;
import com.ozner.cup.ACSqlLite.CSqlCommand;
import com.ozner.cup.Command.NetCacheWork;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.R;
import com.ozner.device.OznerDeviceManager;

import org.json.JSONObject;

/**
 * Created by mengdongya on 2016/3/1.
 */
public class SetupWaterReplenMeterActivity extends AppCompatActivity implements View.OnClickListener {
    private String Mac;
    private Toolbar toolbar;
    private TextView toolbar_save, tv_sex;
    private EditText et_device_name;
    private WaterReplenishmentMeter waterReplenishmentMeter;
    private RelativeLayout rl_replenmeter_instru;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Mac = getIntent().getStringExtra("MAC");
            waterReplenishmentMeter = (WaterReplenishmentMeter) OznerDeviceManager.Instance().getDevice(Mac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_setup_water_replen_meter);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar_save = (TextView) findViewById(R.id.toolbar_save);
        toolbar_save.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.toolbar_text)).setText(getResources().getString(R.string.water_replen_meter));
        et_device_name = (EditText) findViewById(R.id.et_device_name);
        tv_sex = (TextView) findViewById(R.id.tv_sex);
        initClick();
        initdata();
    }

    private void initdata() {
        try {
            et_device_name.setText(waterReplenishmentMeter.getName());
            int value = (int) waterReplenishmentMeter.getAppValue(PageState.Sex);
            String sex = value == 0 ? getString(R.string.women) : getString(R.string.men);
            tv_sex.setText(sex);
        } catch (Exception e) {
        }

    }

    private void initClick() {
//        findViewById(R.id.rl_set_name).setOnClickListener(this);
        findViewById(R.id.rl_set_sex).setOnClickListener(this);
        findViewById(R.id.rl_set_remind_time).setOnClickListener(this);
        findViewById(R.id.rl_goumai).setOnClickListener(this);
        rl_replenmeter_instru = (RelativeLayout) findViewById(R.id.rl_replenmeter_instru);
        rl_replenmeter_instru.setOnClickListener(this);

//        if (((OznerApplication)getApplication()).isLanguageCN()){
//            rl_replenmeter_instru.setVisibility(View.VISIBLE);
//        }else{
//            rl_replenmeter_instru.setVisibility(View.GONE);
//        }

//        findViewById(R.id.rl_play_show).setOnClickListener(this);
        findViewById(R.id.tv_delDeviceBtn).setOnClickListener(this);
        toolbar_save.setOnClickListener(this);
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
                                waterReplenishmentMeter.Setting().name(et_device_name.getText().toString());
                                waterReplenishmentMeter.updateSettings();
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                break;
//            case R.id.rl_set_name:
//                intent.setClass(this, SetDeviceName.class);
//                intent.putExtra("MAC", Mac);
//                startActivityForResult(intent, 0x01);
//                break;
            case R.id.rl_set_sex:
                intent.setClass(this, SetSexActivity.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 0x02);
                break;
            case R.id.rl_set_remind_time:
                intent.setClass(this, SetupReplenTimeActivity.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 0x03);
                break;
            case R.id.rl_goumai:
                break;
            case R.id.rl_replenmeter_instru:
                intent.setClass(this, AboutDeviceActivity.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 0x05);
                break;
//            case R.id.rl_play_show:
//                break;
            case R.id.tv_delDeviceBtn:
                new AlertDialog.Builder(this).setTitle("").setMessage(getString(R.string.weather_delete_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                OznerDeviceManager.Instance().remove(mCup);
                                Intent data = new Intent();
                                data.putExtra("MAC", Mac);
                                setResult(PageState.DeleteDevice, data);
                                try {
                                    //执行缓存任务标识ACTION
                                    NetCacheWork netCacheWork = new NetCacheWork();
                                    netCacheWork.action = CCacheWorking.WorkAction.DeleteDevice;
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("Mac", Mac);
                                    //执行任务所需要的约定数据
                                    netCacheWork.data = jsonObject.toString();
                                    //添加到缓存网络任务池
                                    CSqlCommand.getInstance().AddNetCacheWorks(SetupWaterReplenMeterActivity.this, netCacheWork);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                SetupWaterReplenMeterActivity.this.finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
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
        super.onPostResume();
        initdata();
    }
}

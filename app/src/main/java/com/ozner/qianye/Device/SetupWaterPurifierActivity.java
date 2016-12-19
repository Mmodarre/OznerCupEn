package com.ozner.qianye.Device;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.device.OznerDeviceManager;

import com.ozner.qianye.Command.PageState;
import com.ozner.qianye.R;

/**
 * Created by mengdongya on 2015/12/11.
 */
public class SetupWaterPurifierActivity extends AppCompatActivity implements View.OnClickListener {

    String Mac = null;
    WaterPurifier mWaterPurifier = null;
    String url = null;
    Toolbar toolbar;
    TextView toolbar_save, tv_purifier_name,water_purifier_mac;
    RelativeLayout ll_about_water_purifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mac = getIntent().getStringExtra("MAC");
        mWaterPurifier = (WaterPurifier) OznerDeviceManager.Instance().getDevice(Mac);
//        url = getIntent().getStringExtra("smlinkurl");
        setContentView(R.layout.activity_setup_waterpurifier);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SetupWaterPurifierActivity.this).setTitle("").setMessage(getString(R.string.weather_save_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mWaterPurifier.updateSettings();
                                SetupWaterPurifierActivity.this.finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();
                            }
                        }).show();

            }
        });
        toolbar_save = (TextView) findViewById(R.id.toolbar_save);
        toolbar_save.setVisibility(View.VISIBLE);
        toolbar_save.setOnClickListener(this);
        tv_purifier_name = (TextView) findViewById(R.id.tv_purifier_name);
        tv_purifier_name.setOnClickListener(this);
        water_purifier_mac = (TextView) findViewById(R.id.water_purifier_mac);
        water_purifier_mac.setText(Mac);
        ll_about_water_purifier = (RelativeLayout) findViewById(R.id.ll_about_water_purifier);
        ll_about_water_purifier.setOnClickListener(this);
        findViewById(R.id.ll_about_water_purifier_ver).setOnClickListener(this);
        findViewById(R.id.tv_delDeviceBtn).setOnClickListener(this);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        initview();
    }

    private void initview() {
        ((TextView) findViewById(R.id.toolbar_text)).setText(getResources().getString(R.string.my_water_purifier));
        tv_purifier_name.setText(mWaterPurifier.getName());
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
                                mWaterPurifier.updateSettings();
                                SetupWaterPurifierActivity.this.finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                break;
            case R.id.ll_about_water_purifier:
                intent.setClass(this, AboutDeviceActivity.class);
                intent.putExtra("MAC", Mac);
                intent.putExtra("Desk",true);
                startActivityForResult(intent, 2);
                break;
            case R.id.ll_about_water_purifier_ver:
                intent.setClass(this, AboutDeviceActivity.class);
                intent.putExtra("MAC", Mac);
                intent.putExtra("Desk",false);
                startActivityForResult(intent, 2);
                break;
            case R.id.tv_delDeviceBtn:
                new AlertDialog.Builder(this).setTitle("").setMessage(getString(R.string.weather_delete_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                OznerDeviceManager.Instance().remove(mWaterPurifier);
                                Intent data = new Intent();
                                data.putExtra("MAC", Mac);
                                setResult(PageState.DeleteDevice, data);
                                SetupWaterPurifierActivity.this.finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                break;
            case R.id.tv_purifier_name:
                intent.setClass(this, SetDeviceName.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 0x1236);
                break;
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        initview();
    }
}

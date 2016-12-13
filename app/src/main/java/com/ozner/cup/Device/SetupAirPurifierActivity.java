package com.ozner.cup.Device;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifier;
import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.device.OznerDeviceManager;

import com.ozner.cup.Command.PageState;
import com.ozner.cup.R;

/**
 * Created by mengdongya on 2015/12/22.
 */
public class SetupAirPurifierActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    TextView toolbar_save, toolbar_text, tv_airpurifier_name, tv_airpurifier_type;
    String Mac = null;
    AirPurifier airPurifier;
    RelativeLayout ll_airpurifier_instru;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mac = getIntent().getStringExtra("MAC");
        try {
            airPurifier = (AirPurifier) OznerDeviceManager.Instance().getDevice(Mac);
        }catch (Exception e){e.printStackTrace();}
        setContentView(R.layout.activity_setup_air_purifier);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SetupAirPurifierActivity.this).setMessage(getString(R.string.weather_save_device)).setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        airPurifier.updateSettings();
                        SetupAirPurifierActivity.this.finish();
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
        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_save.setVisibility(View.VISIBLE);
        toolbar_save.setOnClickListener(this);
        initView();
    }

    private void initView() {
        tv_airpurifier_name = (TextView) findViewById(R.id.tv_airpurifier_name);
        tv_airpurifier_name.setText(airPurifier.getName());
        tv_airpurifier_type = (TextView) findViewById(R.id.tv_airpurifier_type);
        if (AirPurifierManager.IsBluetoothAirPurifier(airPurifier.Type())) {//台式
            tv_airpurifier_type.setText(getString(R.string.my_air_purifier_tai));
            toolbar_text.setText(getString(R.string.my_air_purifier_tai));
        } else if (AirPurifierManager.IsWifiAirPurifier(airPurifier.Type())) {//立式
            tv_airpurifier_type.setText(getString(R.string.my_air_purifier_ver));
            toolbar_text.setText(getString(R.string.my_air_purifier_ver));
        } else {
            SetupAirPurifierActivity.this.finish();
        }

        findViewById(R.id.tv_airpurifier_name).setOnClickListener(this);
        ll_airpurifier_instru= (RelativeLayout) findViewById(R.id.ll_airpurifier_instru);
        ll_airpurifier_instru.setOnClickListener(this);
        findViewById(R.id.ll_common_problem).setOnClickListener(this);
        findViewById(R.id.tv_delDeviceBtn).setOnClickListener(this);

        if (((OznerApplication)getApplication()).isLanguageCN()){
            ll_airpurifier_instru.setVisibility(View.VISIBLE);
        }else{
            ll_airpurifier_instru.setVisibility(View.GONE);
        }
        if(OznerPreference.isLoginPhone(SetupAirPurifierActivity.this)){
            findViewById(R.id.ll_common_problem).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.ll_common_problem).setVisibility(View.GONE);
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
                                airPurifier.updateSettings();
                                SetupAirPurifierActivity.this.finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                break;
            case R.id.ll_airpurifier_instru:
                intent.setClass(getBaseContext(), AboutDeviceActivity.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 0x11);
                break;
            case R.id.ll_common_problem:
                intent = new Intent(this, CommonProblemActivity.class);
                startActivityForResult(intent, 0x2132);
                break;
            case R.id.tv_delDeviceBtn:
                new AlertDialog.Builder(this).setTitle("").setMessage(getString(R.string.weather_delete_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent data = new Intent();
                                data.putExtra("MAC", Mac);
                                setResult(PageState.DeleteDevice, data);
                                SetupAirPurifierActivity.this.finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                break;
            case R.id.tv_airpurifier_name:
                intent.setClass(this, SetDeviceName.class);
                intent.putExtra("MAC", Mac);
                startActivityForResult(intent, 3);
                break;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        initView();
    }
}

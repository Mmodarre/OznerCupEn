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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.R;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.Tap;
import com.ozner.tap.TapSetting;

/**
 * Created by taoran on 2015/12/10.
 */
public class SetupWaterTDSPenActivity extends AppCompatActivity implements View.OnClickListener {

    Tap mTap = null;
    TapSetting mTapSetting = null;
    String Mac;
    TextView tv_time1_display, tv_time2_display, toolbar_save;
    RadioButton first, second;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mac = getIntent().getStringExtra(PageState.MAC);
        mTap = (Tap) OznerDeviceManager.Instance().getDevice(Mac);
        mTapSetting = mTap.Setting();
        setContentView(R.layout.activity_setup_watertdspen);

        ((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SetupWaterTDSPenActivity.this).setMessage(getString(R.string.weather_save_device))
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
        editText = (EditText) findViewById(R.id.edit_tds_pen_name);
        toolbar_save = (TextView) findViewById(R.id.toolbar_save);
        toolbar_save.setVisibility(View.VISIBLE);
        toolbar_save.setOnClickListener(this);
        findViewById(R.id.ll_about_water_probe).setOnClickListener(this);

        if(OznerPreference.isLoginPhone(SetupWaterTDSPenActivity.this)){
            findViewById(R.id.ll_about_water_probe).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.ll_about_water_probe).setVisibility(View.GONE);
        }
        findViewById(R.id.tv_delDeviceBtn).setOnClickListener(this);
        initView();
    }

    private void initView() {
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        ((TextView) findViewById(R.id.toolbar_text)).setText(getString(R.string.my_water_pen));
        if (mTap != null) {
            editText.setText(mTapSetting.name());

        }
    }
    private void comit() {
        if (editText.getText().toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.set_cup_name_notice3), Toast.LENGTH_SHORT).show();
        } else {
            mTapSetting.name(editText.getText().toString());
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
//            case R.id.edit_tds_pen_name:
//
//                break;
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
                                SetupWaterTDSPenActivity.this.finish();
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

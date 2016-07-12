package com.ozner.cup.mycenter;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.R;
import com.ozner.cup.Command.MeasurementUnit;

public class SetUnitActivity extends AppCompatActivity implements View.OnClickListener {
    RelativeLayout rlay_centigrade, rlay_fahrenheit;
    RelativeLayout rlay_Unit_ML, rlay_Unit_DL, rlay_Unit_OZ;
    ImageView iv_tempSelect1, iv_tempSelect2;
    ImageView iv_MLSelect, iv_DLSelect, iv_OZSelect;
    private int tempUnit, volUnit;
    private TextView toolbar_text,toolbar_save;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_unit);
        OznerApplication.changeTextFont((ViewGroup)getWindow().getDecorView());
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
        }
        UserDataPreference.Init(SetUnitActivity.this);

        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_text.setText(getString(R.string.Center_Unit));
        toolbar_save = (TextView)findViewById(R.id.toolbar_save);
        toolbar_save.setVisibility(View.VISIBLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.MyCenter_ToolBar, null));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.MyCenter_ToolBar));
        }
        toolbar.setBackgroundColor(0xfff9f9f9);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setResult(12);
                SetUnitActivity.this.finish();
            }
        });
        toolbar_save.setOnClickListener(this);

//        rlay_back = (RelativeLayout) findViewById(R.id.rlay_back);
//        rlay_save = (RelativeLayout) findViewById(R.id.rlay_save);
        rlay_centigrade = (RelativeLayout) findViewById(R.id.rlay_centigrade);
        rlay_fahrenheit = (RelativeLayout) findViewById(R.id.rlay_fahrenheit);
        rlay_Unit_ML = (RelativeLayout) findViewById(R.id.rlay_Unit_ML);
        rlay_Unit_DL = (RelativeLayout) findViewById(R.id.rlay_Unit_DL);
        rlay_Unit_OZ = (RelativeLayout) findViewById(R.id.rlay_Unit_OZ);

        iv_tempSelect1 = (ImageView) findViewById(R.id.iv_tempSelect1);
        iv_tempSelect2 = (ImageView) findViewById(R.id.iv_tempSelect2);
        iv_MLSelect = (ImageView) findViewById(R.id.iv_MLSelect);
        iv_DLSelect = (ImageView) findViewById(R.id.iv_DLSelect);
        iv_OZSelect = (ImageView) findViewById(R.id.iv_OZSelect);

//        rlay_save.setOnClickListener(this);
//        rlay_back.setOnClickListener(this);
        rlay_centigrade.setOnClickListener(this);
        rlay_fahrenheit.setOnClickListener(this);
        rlay_Unit_ML.setOnClickListener(this);
        rlay_Unit_DL.setOnClickListener(this);
        rlay_Unit_OZ.setOnClickListener(this);
        init();
    }

    private void init() {
//        tempUnit = sharedPre.getInt("tempUnit", MeasurementUnit.TempUnit.CENTIGRADE);
//        volUnit = sharedPre.getInt("volUnit", MeasurementUnit.VolumUnit.ML);
        tempUnit = Integer.parseInt(UserDataPreference.GetUserData(SetUnitActivity.this, UserDataPreference.TempUnit, "0"));
        volUnit = Integer.parseInt(UserDataPreference.GetUserData(SetUnitActivity.this, UserDataPreference.VolUnit, "0"));
        selectTempUnit(tempUnit);
        selectVolumnUnit(volUnit);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlay_centigrade:
                selectTempUnit(MeasurementUnit.TempUnit.CENTIGRADE);
                break;
            case R.id.rlay_fahrenheit:
                selectTempUnit(MeasurementUnit.TempUnit.FAHRENHEIT);
                break;
            case R.id.rlay_Unit_ML:
                selectVolumnUnit(MeasurementUnit.VolumUnit.ML);
                break;
            case R.id.rlay_Unit_DL:
                selectVolumnUnit(MeasurementUnit.VolumUnit.DL);
                break;
            case R.id.rlay_Unit_OZ:
                selectVolumnUnit(MeasurementUnit.VolumUnit.OZ);
                break;
//            case R.id.rlay_save:
////                editor.putInt("tempUnit", tempUnit);
////                editor.putInt("volUnit", volUnit);
////                editor.commit();
//                UserDataPreference.SetUserData(SetUnitActivity.this, UserDataPreference.TempUnit, String.valueOf(tempUnit));
//                UserDataPreference.SetUserData(SetUnitActivity.this, UserDataPreference.VolUnit, String.valueOf(volUnit));
//                Toast toast = Toast.makeText(this, getString(R.string.Center_SaveSuccess), Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
//                break;
//            case R.id.rlay_back:
//                onBackPressed();
//                break;
            case R.id.toolbar_save:
                UserDataPreference.SetUserData(SetUnitActivity.this, UserDataPreference.TempUnit, String.valueOf(tempUnit));
                UserDataPreference.SetUserData(SetUnitActivity.this, UserDataPreference.VolUnit, String.valueOf(volUnit));
                Toast save_toast = Toast.makeText(this, getString(R.string.Center_SaveSuccess), Toast.LENGTH_SHORT);
                save_toast.setGravity(Gravity.CENTER, 0, 0);
                save_toast.show();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void selectTempUnit(int unit) {
        tempUnit = unit;
        switch (unit) {
            case MeasurementUnit.TempUnit.CENTIGRADE://摄氏度
                iv_tempSelect1.setSelected(true);
                iv_tempSelect2.setSelected(false);
                break;
            case MeasurementUnit.TempUnit.FAHRENHEIT://华氏度
                iv_tempSelect1.setSelected(false);
                iv_tempSelect2.setSelected(true);
                break;
        }
    }

    private void selectVolumnUnit(int unit) {
        volUnit = unit;
        switch (unit) {
            case MeasurementUnit.VolumUnit.ML:
                iv_MLSelect.setSelected(true);
                iv_DLSelect.setSelected(false);
                iv_OZSelect.setSelected(false);
                break;
            case MeasurementUnit.VolumUnit.DL:
                iv_MLSelect.setSelected(false);
                iv_DLSelect.setSelected(true);
                iv_OZSelect.setSelected(false);
                break;
            case MeasurementUnit.VolumUnit.OZ:
                iv_MLSelect.setSelected(false);
                iv_DLSelect.setSelected(false);
                iv_OZSelect.setSelected(true);
                break;
        }
    }


}

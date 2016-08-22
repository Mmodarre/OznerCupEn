package com.ozner.yiquan.AirPurifier;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.R;

/**
 * Created by taoran on 2015/12/3.
 */
public class AirPmIntroduceAcitivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView introduce_toolbar_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.air_pm_introduce);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        toolbar=(Toolbar)findViewById(R.id.introduce_toolbar);
        introduce_toolbar_text=(TextView)findViewById(R.id.introduce_toolbar_text);
        introduce_toolbar_text.setText(getResources().getString(R.string.airRoom_pm_what));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

}
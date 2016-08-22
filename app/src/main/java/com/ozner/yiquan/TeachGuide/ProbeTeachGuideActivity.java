package com.ozner.yiquan.TeachGuide;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.R;

/*
*created by xinde on 2016/01/02
 */
public class ProbeTeachGuideActivity extends Activity implements View.OnClickListener {
    RelativeLayout rlay_step1, rlay_step2;
    ImageView iv_close1, iv_close2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_probe_teach_guide);
        OznerApplication.changeTextFont((ViewGroup)getWindow().getDecorView());
        rlay_step1 = (RelativeLayout) findViewById(R.id.rlay_stet1);
        rlay_step2 = (RelativeLayout) findViewById(R.id.rlay_step2);
        iv_close1 = (ImageView) findViewById(R.id.iv_close1);
        iv_close2 = (ImageView) findViewById(R.id.iv_close2);

        rlay_step1.setOnClickListener(this);
        rlay_step2.setOnClickListener(this);
        iv_close1.setOnClickListener(this);
        iv_close2.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close1:
            case R.id.iv_close2:
            case R.id.rlay_step2:
                this.finish();
                break;
            case R.id.rlay_stet1:
                rlay_step1.setVisibility(View.GONE);
                rlay_step2.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, 0);
    }
}

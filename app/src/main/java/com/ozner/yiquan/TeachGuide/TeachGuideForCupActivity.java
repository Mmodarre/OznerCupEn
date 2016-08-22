package com.ozner.yiquan.TeachGuide;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.R;

public class TeachGuideForCupActivity extends Activity implements View.OnClickListener {

    LinearLayout rlay_over1, rlay_over2, rlay_over3;
    ImageView iv_close1, iv_close2, iv_close3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View rootview = getLayoutInflater().inflate(R.layout.activity_teach_guide_for_cup, null);
        setContentView(rootview);
//        setContentView(R.layout.activity_teach_guide_for_cup);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        rlay_over1 = (LinearLayout) rootview.findViewById(R.id.rlay_over1);
        rlay_over2 = (LinearLayout) rootview.findViewById(R.id.rlay_over2);
        rlay_over3 = (LinearLayout) rootview.findViewById(R.id.rlay_over3);

        iv_close1 = (ImageView) rootview.findViewById(R.id.iv_close1);
        iv_close2 = (ImageView) rootview.findViewById(R.id.iv_close2);
        iv_close3 = (ImageView) rootview.findViewById(R.id.iv_close3);

        rlay_over1.setOnClickListener(this);
        rlay_over2.setOnClickListener(this);
        rlay_over3.setOnClickListener(this);
        iv_close1.setOnClickListener(this);
        iv_close2.setOnClickListener(this);
        iv_close3.setOnClickListener(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlay_over1:

                rlay_over1.setVisibility(View.GONE);
                rlay_over2.setVisibility(View.VISIBLE);
                rlay_over3.setVisibility(View.GONE);
                break;
            case R.id.rlay_over2:
                rlay_over1.setVisibility(View.GONE);
                rlay_over2.setVisibility(View.GONE);
                rlay_over3.setVisibility(View.VISIBLE);
                break;
            case R.id.rlay_over3:
            case R.id.iv_close1:
            case R.id.iv_close2:
            case R.id.iv_close3:
                TeachGuideForCupActivity.this.finish();
                break;
        }
    }
}

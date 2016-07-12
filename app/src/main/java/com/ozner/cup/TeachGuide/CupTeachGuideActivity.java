package com.ozner.cup.TeachGuide;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.R;


/**
 * Created by xinde @2015-12-1 水杯引导
 */

public class CupTeachGuideActivity extends Activity implements View.OnClickListener {

    LinearLayout llay_bubble1, llay_tipsText1, llay_over1;
    ImageView iv_close1;
    Button btn_text1, btn_tipsText1;

    LinearLayout llay_bubble2, llay_tipsText2, llay_over2;
    ImageView iv_close2;
    Button btn_text2, btn_tipsText2;

    LinearLayout llay_bubble3, llay_tipsText3, llay_over3;
    ImageView iv_close3;
    Button btn_text3, btn_tipsText3;

    LinearLayout flay_over1, flay_over2, flay_over3;

    int step = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_cup_teach_guide);
        flay_over1 = (LinearLayout) findViewById(R.id.flay_over1);
        flay_over2 = (LinearLayout) findViewById(R.id.flay_over2);
        flay_over3 = (LinearLayout) findViewById(R.id.flay_over3);
        llay_over1 = (LinearLayout) findViewById(R.id.llay_over1);
        llay_bubble1 = (LinearLayout) findViewById(R.id.llay_bubble1);
        llay_tipsText1 = (LinearLayout) findViewById(R.id.llay_tipsText1);
        iv_close1 = (ImageView) findViewById(R.id.iv_close1);
        btn_text1 = (Button) findViewById(R.id.btn_text1);
        btn_tipsText1 = (Button) findViewById(R.id.btn_tipstext1);

        btn_text1.setOnClickListener(this);
        btn_tipsText1.setOnClickListener(this);
        llay_over1.setOnClickListener(this);
        llay_tipsText1.setOnClickListener(this);
        llay_bubble1.setOnClickListener(this);

        iv_close1.setOnClickListener(this);


        llay_over2 = (LinearLayout) findViewById(R.id.llay_over2);
        llay_bubble2 = (LinearLayout) findViewById(R.id.llay_bubble2);
        llay_tipsText2 = (LinearLayout) findViewById(R.id.llay_tipsText2);
        iv_close2 = (ImageView) findViewById(R.id.iv_close2);
        btn_text2 = (Button) findViewById(R.id.btn_text2);
        btn_tipsText2 = (Button) findViewById(R.id.btn_tipstext2);

        btn_text2.setOnClickListener(this);
        btn_tipsText2.setOnClickListener(this);
        llay_over2.setOnClickListener(this);
        llay_tipsText2.setOnClickListener(this);
        llay_bubble2.setOnClickListener(this);

        iv_close2.setOnClickListener(this);


        llay_over3 = (LinearLayout) findViewById(R.id.llay_over3);
        llay_bubble3 = (LinearLayout) findViewById(R.id.llay_bubble3);
        llay_tipsText3 = (LinearLayout) findViewById(R.id.llay_tipsText3);
        iv_close3 = (ImageView) findViewById(R.id.iv_close3);
        btn_text3 = (Button) findViewById(R.id.btn_text3);
        btn_tipsText3 = (Button) findViewById(R.id.btn_tipstext3);

        btn_text3.setOnClickListener(this);
        btn_tipsText3.setOnClickListener(this);
        llay_over3.setOnClickListener(this);
        llay_tipsText3.setOnClickListener(this);
        llay_bubble3.setOnClickListener(this);

        iv_close3.setOnClickListener(this);
        OznerApplication.changeTextFont((ViewGroup)getWindow().getDecorView());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_text1:
            case R.id.btn_tipstext1:
            case R.id.llay_tipsText1:
            case R.id.llay_bubble1:
            case R.id.llay_over1:
                flay_over1.setVisibility(View.GONE);
                flay_over3.setVisibility(View.GONE);
                if (View.GONE == flay_over1.getVisibility() && View.GONE == flay_over3.getVisibility())
                    flay_over2.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_text2:
            case R.id.btn_tipstext2:
            case R.id.llay_tipsText2:
            case R.id.llay_bubble2:
            case R.id.llay_over2:
                flay_over1.setVisibility(View.GONE);
                flay_over2.setVisibility(View.GONE);

                if (View.GONE == flay_over2.getVisibility() && View.GONE == flay_over1.getVisibility())
                    flay_over3.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_text3:
            case R.id.btn_tipstext3:
            case R.id.llay_tipsText3:
            case R.id.llay_bubble3:
            case R.id.llay_over3:
                this.finish();
                break;

            case R.id.iv_close3:
            case R.id.iv_close2:
            case R.id.iv_close1:
                this.finish();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, 0);
    }
}

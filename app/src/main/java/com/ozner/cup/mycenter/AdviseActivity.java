package com.ozner.cup.mycenter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.OznerDataHttp;
import com.ozner.cup.R;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/*
* Created by xinde on 2015/12/10
 */

public class AdviseActivity extends AppCompatActivity implements View.OnClickListener {
//    RelativeLayout rlay_back;
    EditText et_adviseText;
    TextView tv_length;
    Button btn_Submit;
    ProgressBar pb_progress;
    AdviseHandle adviseHandle = new AdviseHandle();
    String userid = "";
    String usertoken = "";
    private TextView toolbar_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advise);
        OznerApplication.changeTextFont((ViewGroup)getWindow().getDecorView());
//        if (android.os.Build.VERSION.SDK_INT >= 21) {
//            Window window = getWindow();
//            //更改状态栏颜色
//            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
//            //更改底部导航栏颜色(限有底部的手机)
//            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
//        }
        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_text.setText(getString(R.string.Center_Adsive));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdviseActivity.this.finish();
            }
        });
        userid = UserDataPreference.GetUserData(AdviseActivity.this, UserDataPreference.UserId, null);
        usertoken = OznerPreference.UserToken(AdviseActivity.this);
        Log.e("tag", "advise_usertoken:" + usertoken);
//        rlay_back = (RelativeLayout) findViewById(R.id.rlay_back);
        et_adviseText = (EditText) findViewById(R.id.et_adviseText);
        tv_length = (TextView) findViewById(R.id.tv_length);
        btn_Submit = (Button) findViewById(R.id.btn_Submit);
        pb_progress = (ProgressBar) findViewById(R.id.pb_progress);

//        rlay_back.setOnClickListener(this);
        btn_Submit.setOnClickListener(this);

        et_adviseText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //tv_length.setText(String.valueOf(count));
            }

            @Override
            public void afterTextChanged(Editable s) {
                tv_length.setText(String.valueOf(s.length()));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.rlay_back:
//                onBackPressed();
//                break;
            case R.id.btn_Submit:
                if (userid != null && userid != "" && usertoken != null && usertoken != "") {
                    if (!TextUtils.isEmpty(et_adviseText.getText())) {
                        String msg = et_adviseText.getText().toString();
                        submitOpinion(usertoken, msg);
                    } else {
                        Toast toast = Toast.makeText(AdviseActivity.this, getResources().getString(R.string.Advise_InputAdvise), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                } else {
                    Toast toast = Toast.makeText(AdviseActivity.this, getResources().getString(R.string.ShouldLogin), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        hideProgress();
        super.onBackPressed();
    }

    public void submitOpinion(final String token, final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                adviseRequest(token, msg);
            }
        }).start();
        showProgress();
    }

    public void adviseRequest(String token, String msg) {
        if (msg != null && msg != "") {
            List<NameValuePair> pars = new ArrayList<NameValuePair>();
//            pars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(AdviseActivity.this)));
            pars.add(new BasicNameValuePair("usertoken", token));
            pars.add(new BasicNameValuePair("message", msg));
            NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(AdviseActivity.this,
                    OznerPreference.ServerAddress(AdviseActivity.this) + "/OznerServer/SubmitOpinion", pars);
            if (netJsonObject.state > 0) {
                adviseHandle.sendEmptyMessage(0x141);
            } else {
                adviseHandle.sendEmptyMessage(0x142);
            }
        }
    }

    private void showProgress() {
        btn_Submit.setClickable(false);
        pb_progress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        btn_Submit.setClickable(true);
        pb_progress.setVisibility(View.GONE);
    }

    class AdviseHandle extends Handler {

        @Override
        public void handleMessage(Message msg) {
            hideProgress();
            switch (msg.what) {
                case 0x141:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case 0x142:
                    Toast toast = Toast.makeText(AdviseActivity.this, getResources().getString(R.string.submit_fail), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
            }
            super.handleMessage(msg);
        }
    }
}

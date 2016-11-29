package com.ozner.cup.mycenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.NetUserVfMessage;
import com.ozner.cup.R;

import java.io.Serializable;

public class SendVerifyActivity extends AppCompatActivity implements View.OnClickListener {
    private final int VERIFY_RESULT = 0x01;
    RelativeLayout rlay_cancelBtn, rlay_sendBtn, rlay_cleanUpBtn;
    EditText et_sendMsg;
    String mobile = "";
    int clickPos = -1;
    SendVerifyHandler sendVerifyHandler = new SendVerifyHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_verify);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        mobile = getIntent().getStringExtra("mobile");
        clickPos = getIntent().getIntExtra("clickPos", -1);

//        if (android.os.Build.VERSION.SDK_INT >= 21) {
//            Window window = getWindow();
//            //更改状态栏颜色
//            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
//            //更改底部导航栏颜色(限有底部的手机)
//            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
//        }

        rlay_cancelBtn = (RelativeLayout) findViewById(R.id.rlay_cancelBtn);
        rlay_sendBtn = (RelativeLayout) findViewById(R.id.rlay_sendBtn);
        rlay_cleanUpBtn = (RelativeLayout) findViewById(R.id.rlay_cleanUpBtn);
        et_sendMsg = (EditText) findViewById(R.id.et_sendMsg);

        rlay_cancelBtn.setOnClickListener(this);
        rlay_sendBtn.setOnClickListener(this);
        rlay_cleanUpBtn.setOnClickListener(this);
        et_sendMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    rlay_cleanUpBtn.setVisibility(View.VISIBLE);
                } else {
                    rlay_cleanUpBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlay_sendBtn:
                NetUserVfMessage netUserVfMessage = new NetUserVfMessage();
                netUserVfMessage.FriendMobile = mobile;
                netUserVfMessage.RequestContent = String.valueOf(et_sendMsg.getText());
                sendVerifyMsg(SendVerifyActivity.this, netUserVfMessage);
                et_sendMsg.setText("");
                break;
            case R.id.rlay_cancelBtn:
                this.finish();
                break;
            case R.id.rlay_cleanUpBtn:
                et_sendMsg.setText("");
                break;
        }
    }

    private void sendVerifyMsg(final Activity activity, final NetUserVfMessage netUserVfMessage) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetJsonObject result = OznerCommand.AddFriend(activity, netUserVfMessage);
                Message msg = new Message();
                msg.what = VERIFY_RESULT;
                msg.obj = (Serializable) result;
                sendVerifyHandler.sendMessage(msg);
            }
        }).start();
    }

    class SendVerifyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VERIFY_RESULT:
                    NetJsonObject result = (NetJsonObject) msg.obj;
                    if (result != null && result.state > 0) {
                        Intent resIntent = new Intent();
                        resIntent.putExtra("clickPos", clickPos);
                        setResult(RESULT_OK, resIntent);
                        finish();
                    } else {
                        Toast.makeText(SendVerifyActivity.this, getString(R.string.Center_send_err), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
}

package com.ozner.cup.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.cup.BuildConfig;
import com.ozner.cup.Command.NetErrDecode;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.R;

import java.util.Timer;
import java.util.TimerTask;

public class ResetPwdActivity extends AppCompatActivity {

    private static final int GET_VERIFY_CODE = 2;
    private final int RESET_PWD = 3;
    EditText et_email, et_verCode, et_new_password, et_confirm_password;
    TextView tv_verifyCode;
    private ProgressDialog dialog;
    Timer timer = new Timer();
    private int timeIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd);
        et_email = (EditText) findViewById(R.id.et_email);
        et_verCode = (EditText) findViewById(R.id.et_verCode);
        et_new_password = (EditText) findViewById(R.id.et_new_password);
        et_confirm_password = (EditText) findViewById(R.id.et_confirm_password);
        tv_verifyCode = (TextView) findViewById(R.id.tv_verifyCode);
        tv_verifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_email.getText().toString().trim().length() != 0 && et_email.getText().toString() != null) {
                    Toast.makeText(ResetPwdActivity.this, getString(R.string.input_username), Toast.LENGTH_SHORT).show();
                } else {
                    getEmailCode(et_email.getText().toString().trim());
                }
            }
        });
    }

    public void backUp(View view) {
        startActivity(new Intent(ResetPwdActivity.this, LoginEnActivity.class));
        finish();
    }

    private void getEmailCode(String email) {
        dialog = ProgressDialog.show(this, null, getString(R.string.sendEmailCode));
        dialog.setCanceledOnTouchOutside(false);
        OznerCommand.getEmailCode(this, email, new OznerCommand.HttpCallback() {
            @Override
            public void HandleResult(NetJsonObject result) {
                Message message = mhandler.obtainMessage();
                message.what = GET_VERIFY_CODE;
                message.obj = result;
                mhandler.sendMessage(message);
            }
        });
    }

    public void modifyPwd(View view) {
        if (et_email.getText().toString().trim().equals(null)) {
            Toast.makeText(ResetPwdActivity.this, getString(R.string.input_username), Toast.LENGTH_SHORT).show();
            return;
        } else if (!et_new_password.getText().toString().equals(et_confirm_password.getText().toString())) {
            Toast.makeText(ResetPwdActivity.this, getString(R.string.different_pwd), Toast.LENGTH_SHORT).show();
            return;
        } else {
            dialog = ProgressDialog.show(ResetPwdActivity.this, null, getString(R.string.submiting));
            dialog.setCanceledOnTouchOutside(false);
            OznerCommand.resetPwd(ResetPwdActivity.this, et_email.getText().toString(), et_new_password.getText().toString(), et_verCode.getText().toString(), new OznerCommand.HttpCallback() {
                @Override
                public void HandleResult(NetJsonObject result) {
                    Message message = mhandler.obtainMessage();
                    message.what = RESET_PWD;
                    message.obj = result;
                    mhandler.sendMessage(message);
                }
            });
        }
    }

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (dialog != null) {
                dialog.cancel();
            }

            switch (msg.what) {
                case RESET_PWD:

                    NetJsonObject regRes = (NetJsonObject) msg.obj;
                    if (regRes != null) {
                        Log.e("123456", ((NetJsonObject) msg.obj).value.toString());
                        if (regRes.state > 0) {
                            startActivity(new Intent(ResetPwdActivity.this, LoginEnActivity.class));
                            finish();
                        } else {
                            NetErrDecode.ShowErrMsgDialog(ResetPwdActivity.this, regRes.state, getString(R.string.regFail));
                        }
                    } else {
                        Toast.makeText(ResetPwdActivity.this, getString(R.string.innet_wrong), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case GET_VERIFY_CODE:
                    NetJsonObject result = (NetJsonObject) msg.obj;
                    if (result != null) {
                        if (result.state > 0) {
                            startWaitVerifyCode();
                        } else {
                            Toast.makeText(ResetPwdActivity.this, getString(R.string.login_code_sendwrong), Toast.LENGTH_SHORT).show();
                            if (BuildConfig.DEBUG)
                                Log.e("123456", "getEmailCode: " + result.value);
                        }
                    } else {
                        Toast.makeText(ResetPwdActivity.this, getString(R.string.innet_wrong), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1:
                    if (timeIndex < 59) {
                        timeIndex++;
                        String waitTime = String.valueOf(60 - timeIndex);
                        tv_verifyCode.setText(waitTime + getString(R.string.second));
                    } else {
                        stopWaitVerifyCode();
                    }
                    break;
            }

        }
    };

    private void stopWaitVerifyCode() {
        timeIndex = 0;
        task.cancel();
        timer = null;
        tv_verifyCode.setText(getString(R.string.resend));
        tv_verifyCode.setEnabled(true);
    }

    private void startWaitVerifyCode() {
        tv_verifyCode.setEnabled(false);
        if (timer == null)
            timer = new Timer();
        else {
            timer = null;
            timer = new Timer();
        }
        if (task != null) {
            task.cancel();
            task = new TimerTask() {

                @Override
                public void run() {
                    // 需要做的事:发送消息
                    Message message = new Message();
                    message.what = 1;
                    mhandler.sendMessage(message);
                }
            };
        } else {
            task = new TimerTask() {

                @Override
                public void run() {
                    // 需要做的事:发送消息
                    Message message = new Message();
                    message.what = 1;
                    mhandler.sendMessage(message);
                }
            };
        }
        timer.schedule(task, 0, 1000);
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            mhandler.sendMessage(message);
        }
    };
}

package com.ozner.cup.Login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.cup.BuildConfig;
import com.ozner.cup.Command.NetErrDecode;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private final static String EMAIL_REG = "^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";
    private final int TimerTicker = 0x01;
    private final int GET_VERIFY_CODE = 2;
    private final int REGISTER = 3;
    Button btn_sign_up;
    TextView tv_verifyCode;
    ImageView iv_cb_xieyi;
    EditText et_email, et_verCode, et_password, et_confirm_password;
    private boolean isTyXieyi = true;
    private int timeIndex = 0;
    Timer timer = new Timer();
    private ProgressDialog dialog;
    MyClickListener clickListener = new MyClickListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initViews();
        setListener();
    }

    private void initViews() {
        btn_sign_up = (Button) findViewById(R.id.btn_sign_up);
        tv_verifyCode = (TextView) findViewById(R.id.tv_verifyCode);
        et_email = (EditText) findViewById(R.id.et_email);
        et_verCode = (EditText) findViewById(R.id.et_verCode);
        et_password = (EditText) findViewById(R.id.et_password);
        et_confirm_password = (EditText) findViewById(R.id.et_confirm_password);
        iv_cb_xieyi = (ImageView) findViewById(R.id.iv_cb_xieyi);
    }

    private void setListener() {
        btn_sign_up.setOnClickListener(clickListener);
        tv_verifyCode.setOnClickListener(clickListener);
        iv_cb_xieyi.setOnClickListener(clickListener);
    }

    class MyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_sign_up:
                    if (et_email.getText().length() > 0 && isEmail(et_email.getText().toString())) {
                        if (et_verCode.getText().length() > 0) {
                            if (et_password.getText().length() > 0) {
                                if (et_confirm_password.getText().toString().equals(et_password.getText().toString())) {
                                    if (isTyXieyi) {
                                        registerAccount(et_email.getText().toString(), et_password.getText().toString(), et_verCode.getText().toString().trim());
                                    } else {
                                        Toast.makeText(SignUpActivity.this, getString(R.string.loging_ty_xiexi), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.passDiff), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(SignUpActivity.this, getString(R.string.inputPassword), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, getString(R.string.inputVerCode), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, getString(R.string.inputValidEmail), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.tv_verifyCode:
                    if (et_email.getText().length() > 0 && isEmail(et_email.getText().toString())) {
                        getEmailCode(et_email.getText().toString().trim());
                    } else {
                        Toast.makeText(SignUpActivity.this, getString(R.string.inputValidEmail), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.iv_cb_xieyi://同意协议
                    if (isTyXieyi) {
                        isTyXieyi = false;
                        iv_cb_xieyi.setImageResource(R.drawable.cb_off);
                    } else {
                        isTyXieyi = true;
                        iv_cb_xieyi.setImageResource(R.drawable.cb_on);
                    }
                    break;
            }
        }
    }


    /**
     * 校验输入是否是邮箱
     *
     * @param email
     *
     * @return
     */
    private boolean isEmail(String email) {
        Pattern p = Pattern.compile(EMAIL_REG);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 获取邮箱验证码
     *
     * @param email
     */
    private void getEmailCode(String email) {
        dialog = ProgressDialog.show(this, null, getString(R.string.sendEmailCode));
        dialog.setCanceledOnTouchOutside(false);
        OznerCommand.getEmailCode(this, email, new OznerCommand.HttpCallback() {
            @Override
            public void HandleResult(NetJsonObject result) {
                Message message = mHandler.obtainMessage();
                message.what = GET_VERIFY_CODE;
                message.obj = result;
                mHandler.sendMessage(message);
            }
        });
    }

    /**
     * 注册账号
     *
     * @param email
     * @param password
     * @param code
     */
    private void registerAccount(String email, String password, String code) {
        dialog = ProgressDialog.show(this, null, getString(R.string.submitting));
        dialog.setCanceledOnTouchOutside(false);
        OznerCommand.mailRegister(this, email, password, code, new OznerCommand.HttpCallback() {
            @Override
            public void HandleResult(NetJsonObject result) {
                Message message = mHandler.obtainMessage();
                message.what = REGISTER;
                message.obj = result;
                mHandler.sendMessage(message);
            }
        });
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (dialog != null) {
                dialog.cancel();
            }
            switch (msg.what) {
                case REGISTER://注册账号
                    NetJsonObject regRes = (NetJsonObject) msg.obj;
                    if (regRes != null) {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "register_result:" + regRes.value);
                        }
                        if (regRes.state > 0) {
                            Toast.makeText(SignUpActivity.this, getString(R.string.regSucc), Toast.LENGTH_SHORT).show();
                            SignUpActivity.this.finish();
                        } else {
                            NetErrDecode.ShowErrMsgDialog(SignUpActivity.this, regRes.state, getString(R.string.regFail));
//                            Toast.makeText(SignUpActivity.this, getString(R.string.regFail), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, getString(R.string.innet_wrong), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case GET_VERIFY_CODE:
                    NetJsonObject result = (NetJsonObject) msg.obj;
                    if (result != null) {
                        if (result.state > 0) {
                            startWaitVerifyCode();
                        } else {
                            Toast.makeText(SignUpActivity.this, getString(R.string.login_code_sendwrong), Toast.LENGTH_SHORT).show();
                            if (BuildConfig.DEBUG)
                                Log.e(TAG, "getEmailCode: " + result.value);
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, getString(R.string.innet_wrong), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case TimerTicker:
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

    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            mHandler.sendMessage(message);
        }
    };

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
                    mHandler.sendMessage(message);
                }
            };
        } else {
            task = new TimerTask() {

                @Override
                public void run() {
                    // 需要做的事:发送消息
                    Message message = new Message();
                    message.what = 1;
                    mHandler.sendMessage(message);
                }
            };
        }
        timer.schedule(task, 0, 1000);
    }


    private void stopWaitVerifyCode() {
        timeIndex = 0;
        task.cancel();
        timer = null;
        tv_verifyCode.setText(getString(R.string.resend));
        tv_verifyCode.setEnabled(true);
    }
}

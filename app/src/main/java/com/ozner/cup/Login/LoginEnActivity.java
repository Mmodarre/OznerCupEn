package com.ozner.cup.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.cup.ACSqlLite.CSqlCommand;
import com.ozner.cup.BaiduPush.OznerBroadcastAction;
import com.ozner.cup.BuildConfig;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.NetUserInfo;
import com.ozner.cup.HttpHelper.OznerDataHttp;
import com.ozner.cup.MainEnActivity;
import com.ozner.cup.R;
import com.ozner.device.OznerDeviceManager;

import org.json.JSONObject;

public class LoginEnActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginEnActivity";
    EditText et_username, et_pass;
    TextView tv_register, tv_wrong, tv_verifyLogin;
    Button btn_login;
    ImageButton ibtn_facebook, ibtn_googleplus, ibtn_linkedin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_en);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.guideColor));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.guideColor));
        }
        initViews();
    }

    private void initViews() {
        tv_register = (TextView) findViewById(R.id.tv_register);
        btn_login = (Button) findViewById(R.id.btn_login);
        et_username = (EditText) findViewById(R.id.et_username);
        et_pass = (EditText) findViewById(R.id.et_pass);
        ibtn_facebook = (ImageButton) findViewById(R.id.ibtn_facebook);
        ibtn_googleplus = (ImageButton) findViewById(R.id.ibtn_googleplus);
        ibtn_linkedin = (ImageButton) findViewById(R.id.ibtn_linkedin);
        tv_wrong = (TextView) findViewById(R.id.tv_wrong);
        tv_verifyLogin = (TextView) findViewById(R.id.tv_verifyLogin);
        setListener();
    }

    private void setListener() {
        tv_register.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        ibtn_linkedin.setOnClickListener(this);
        ibtn_facebook.setOnClickListener(this);
        ibtn_googleplus.setOnClickListener(this);
        tv_verifyLogin.setOnClickListener(this);
        et_username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                tv_wrong.setText("");
            }
        });
        et_pass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                tv_wrong.setText("");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (et_username.getText().length() > 0) {
                    if (et_pass.getText().length() > 0) {
                        mailLogin(et_username.getText().toString().trim(), et_pass.getText().toString().trim());
                    } else {
                        Toast.makeText(LoginEnActivity.this, getString(R.string.input_password), Toast.LENGTH_SHORT).show();
                        et_pass.requestFocus();
                    }
                } else {
                    Toast.makeText(LoginEnActivity.this, getString(R.string.input_username), Toast.LENGTH_SHORT).show();
                    et_username.requestFocus();
                }
                break;
            case R.id.tv_verifyLogin:
                startActivity(new Intent(LoginEnActivity.this, LoginActivity.class));
                this.finish();
                break;
            case R.id.tv_register:
                startActivity(new Intent(LoginEnActivity.this, SignUpActivity.class));
                break;
//            case R.id.ibtn_facebook:
//                ssoLogin(Facebook.NAME);
//                break;
//            case R.id.ibtn_googleplus:
//                ssoLogin(GooglePlus.NAME);
//                break;
//            case R.id.ibtn_linkedin:
//                ssoLogin(LinkedIn.NAME);
//                break;
        }
    }

    /**
     * 邮箱登录
     *
     * @param email
     * @param passowrd
     */
    private void mailLogin(String email, String passowrd) {
        new LoginResultAyncTask().execute(email, passowrd);
    }


    private class LoginResultAyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(LoginEnActivity.this, null, getString(R.string.login_aync_ing));
            dialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            NetJsonObject netJsonObject = OznerCommand.mailLogin(LoginEnActivity.this, params[0], params[1]);
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Login_result:" + netJsonObject.value);
            }
            if (netJsonObject.state > 0) {
                try {
                    OznerPreference.setIsLogin(getBaseContext(), true);
                    OznerPreference.setUserToken(getBaseContext(), netJsonObject.getJSONObject().getString("usertoken"));
                    OznerPreference.SetValue(getBaseContext(), UserDataPreference.UserId, netJsonObject.getJSONObject().getString("userid"));
                    try {
                        final String finalToken = OznerPreference.UserToken(getBaseContext());
                        Handler handler = new Handler(Looper.getMainLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                OznerDeviceManager.Instance().setOwner(msg.obj.toString(), finalToken);
                                try {
                                    CSqlCommand.getInstance().SetTableName(getBaseContext(), "N" + msg.obj.toString().replace("-", ""));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                super.handleMessage(msg);
                            }
                        };
                        Message m = new Message();
                        m.obj = netJsonObject.getJSONObject().getString("userid");
                        handler.sendMessage(m);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    OznerCommand.CheckTokenAndInitUserData(LoginEnActivity.this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                NetUserInfo netUserInfo = OznerDataHttp.RefreshUserInfo(getBaseContext());
//                                           NetDeviceList netDeviceList=new NetDeviceList();
                //网络操作成功
                if (netUserInfo.state > 0) {
                    try {
                        JSONObject userinfo = netUserInfo.userinfo.getJSONObject("userinfo");
                        String userid = userinfo.getString(UserDataPreference.UserId);
                        OznerPreference.SetValue(getBaseContext(), UserDataPreference.UserId, userid);
                        UserDataPreference.SaveUserData(getBaseContext(), userinfo);
                        if (UserDataPreference.GetUserData(LoginEnActivity.this, UserDataPreference.hasUpdateUserInfo, "false").equals("false")) {
                            sendBroadcast(new Intent(OznerBroadcastAction.UpdateUserInfo));
                        }
                    } catch (Exception ex) {
                        ex.getMessage();
                    }
                } else {
                }
                //初始化用户头像
                OznerCommand.InitUserHeadImg(LoginEnActivity.this);
            }
            return netJsonObject;
        }

        @Override
        protected void onPostExecute(NetJsonObject result) {
            if (dialog != null)
                dialog.dismiss();
            if (result != null && result.state > 0) {
                ShowMainPage();
            } else {
                OznerPreference.setIsLogin(getBaseContext(), false);
                switch (result.state) {
                    case 0:
                        tv_wrong.setText(getResources().getString(R.string.innet_wrong));
                        break;
                    case -10002:
                        tv_wrong.setText(getResources().getString(R.string.login_wrong_Code_Outtime));
                        break;
                    case -10003:
                        tv_wrong.setText(getResources().getString(R.string.login_wrong_Code_error));
                        break;
                    case -10004:
                        tv_wrong.setText(getResources().getString(R.string.server_Exception));
                        break;
                    case -10023:
                        tv_wrong.setText(getResources().getString(R.string.loginFail));
                        break;
                    default:
                        tv_wrong.setText(getResources().getString(R.string.loginFail));
                        break;
                }
            }
        }
    }

    private void ShowMainPage() {
        Intent mainIntent = new Intent(LoginEnActivity.this, MainEnActivity.class);
        startActivity(mainIntent);
        this.finish();
    }

//    /*
//    *第三方登录
//     */
//    private void ssoLogin(final String platformName) {
//        //初始化SDK
//        ShareSDK.initSDK(this);
//        ShareLoginApi ssoApi = new ShareLoginApi();
//        ssoApi.setPlatform(platformName);
//        ssoApi.setOnLoginListener(new OnLoginListener() {
//            @Override
//            public boolean onLogin(String platform, HashMap<String, Object> res) {
//                // TODO: 2016/7/5 判断登录返回信息是否在用户数据库存在，存在就登录成功，否则登录失败
//
//                Platform loginPlat = ShareSDK.getPlatform(platformName);
//                logSSOLoginInfo(platform, res);
//                if (loginPlat.getDb().getUserId() != null && !loginPlat.getDb().getUserId().isEmpty()) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//
//            @Override
//            public boolean onRegister(UserInfo info) {
//                return true;
//            }
//        });
//        ssoApi.login(this);
//    }
//
//
//    private void logSSOLoginInfo(String paltname, HashMap<String, Object> res) {
//        Platform platform = ShareSDK.getPlatform(paltname);
//
//        Log.e(TAG, "onComplete: UserName:" + platform.getDb().getUserName());
//        Log.e(TAG, "onComplete: UserID:" + platform.getDb().getUserId());
//        Log.e(TAG, "onComplete: UserIcon:" + platform.getDb().getUserIcon());
//        Log.e(TAG, "onComplete: UserGender:" + platform.getDb().getUserGender());
//        if (res != null) {
//            for (String key : res.keySet()) {
//                Log.e(TAG, String.format("handleMessage_请求结果集: %s---->%s", key, res.get(key)));
//            }
//        }
//    }
}

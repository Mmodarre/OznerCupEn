package com.ozner.yiquan.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.yiquan.ACSqlLite.CSqlCommand;
import com.ozner.yiquan.BaiduPush.OznerBroadcastAction;
import com.ozner.yiquan.Command.Contants;
import com.ozner.yiquan.Command.OznerCommand;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.PageState;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.HttpHelper.NetDeviceList;
import com.ozner.yiquan.HttpHelper.NetJsonObject;
import com.ozner.yiquan.HttpHelper.NetUserInfo;
import com.ozner.yiquan.HttpHelper.OznerDataHttp;
import com.ozner.yiquan.MainActivity;
import com.ozner.yiquan.MainEnActivity;
import com.ozner.yiquan.R;
import com.ozner.yiquan.mycenter.WebActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class LoginActivity extends BaseLoginActivity {

    private TextView tv_skip, tv_codeget, tv_login, tv_xiexi, tv_voice, tv_wrong;
    private EditText et_phone, et_code;
    private ImageView cb_xieyi;
    private boolean isTyXieyi = true;
    private int timeindex = 0;
    private boolean iscanclick = true;
    private String tishiformat;
    Timer timer = new Timer();

    //停止改变获取短信状态
    public void StopChangeVoiceButton() {
        iscanclick = true;
        timeindex = 0;
        task.cancel();
        timer = null;
        tv_codeget.setText(getString(R.string.login_btn_getphone));
        tv_codeget.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.fz_blue));
        tv_codeget.setBackgroundResource(R.drawable.selector_btn_voice);
    }

    //改变获取短信状态
    public void ChangeVoiceButton() {
        tv_codeget.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.font_gray));
        tv_codeget.setBackgroundResource(R.drawable.btn_bg_radio_on);
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
                    handler.sendMessage(message);
                }
            };
        } else {
            task = new TimerTask() {

                @Override
                public void run() {
                    // 需要做的事:发送消息
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            };
        }
        timer.schedule(task, 0, 1000);
        iscanclick = false;
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (timeindex < 60) {
                    timeindex++;
                    String title = String.format(tishiformat, 60 - timeindex);
                    tv_codeget.setText(title);

                } else {
                    StopChangeVoiceButton();
                }
                //   tvShow.setText(Integer.toString(i++));
            }
            super.handleMessage(msg);
        }

        ;
    };

    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    private void Init() {
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        tv_skip = (TextView) findViewById(R.id.login_btn_skip);
        tv_codeget = (TextView) findViewById(R.id.login_btn_codeget);
        tv_login = (TextView) findViewById(R.id.login_btn_login);
        tv_xiexi = (TextView) findViewById(R.id.login_btn_xieyi);
        tv_voice = (TextView) findViewById(R.id.login_btn_voice);
        et_phone = (EditText) findViewById(R.id.login_et_phone);
        et_code = (EditText) findViewById(R.id.login_et_code);
        cb_xieyi = (ImageView) findViewById(R.id.login_cb_xieyi);
        tv_wrong = (TextView) findViewById(R.id.login_tv_wrong);
        LoginClickListenr mylistener = new LoginClickListenr();
        tv_skip.setOnClickListener(mylistener);
        tv_codeget.setOnClickListener(mylistener);
        tv_login.setOnClickListener(mylistener);
        tv_xiexi.setOnClickListener(mylistener);
        tv_voice.setOnClickListener(mylistener);
        cb_xieyi.setOnClickListener(mylistener);

        et_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                tv_wrong.setText("");
            }
        });
        et_code.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                tv_wrong.setText("");
            }
        });
        tishiformat = getResources().getString(R.string.login_code_timeout);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_v2);

        Init();
        checkOtherLogin();
    }


    //                RequestParams params = new RequestParams();
//                params.put("phone", "13526885317");
//                //
//                AsyncHttpClient client = new AsyncHttpClient();
//                client.post("http://ozner.bynear.cn/OznerServer/GetPhoneCode", params, new JsonHttpResponseHandler() {
//                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                        Toast.makeText(getBaseContext(), "访问网络失败", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, org.json.JSONObject response) {
//                        Toast.makeText(getBaseContext(), response.toString(), Toast.LENGTH_SHORT).show();
//                        Toast.makeText(getBaseContext(), response.toString(), Toast.LENGTH_SHORT).show();
//                    }
//
//
//
//
//                  });
    class LoginClickListenr implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //获取验证码
                case R.id.login_btn_codeget:
                    if (iscanclick)
                        GetPhoneCode();
                    break;
                //登陆
                case R.id.login_btn_login:
                    if (isTyXieyi) {
                        Login();
                    } else {
                        tv_wrong.setText(getString(R.string.loging_ty_xiexi));
                    }

                    break;
                //跳过
                case R.id.login_btn_skip: {
                    OznerPreference.setIsLogin(getBaseContext(), false);
                    Intent mainactivity = new Intent(getApplicationContext(), MainEnActivity.class);
                    startActivity(mainactivity);
                    LoginActivity.this.finish();
                }
                break;
                //获取语音验证码
                case R.id.login_btn_voice:
                    GetVoic();
                    break;
                //协议
                case R.id.login_btn_xieyi:
                    Intent webIntent = new Intent();
                    webIntent.setClass(LoginActivity.this, WebActivity.class);
                    webIntent.putExtra("URL", Contants.URL_BASE_EXCEPTIONS);
                    webIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(webIntent);
                    break;
                //是否同意协议
                case R.id.login_cb_xieyi:
                    tv_wrong.setText("");
                    if (isTyXieyi) {
                        isTyXieyi = false;
                        cb_xieyi.setImageResource(R.drawable.cb_off);
                    } else {
                        isTyXieyi = true;
                        cb_xieyi.setImageResource(R.drawable.cb_on);
                    }
                    break;

            }
        }

        public void GetVoic() {
            String phone = et_phone.getText().toString();
            if (phone != null && phone.length() == 11) {
                new GetVoiceCodeAsyncTask().execute(phone);
            } else {
                tv_wrong.setText(getResources().getString(R.string.login_wrong_phone));
            }
//            tv_wrong.setText(getResources().getString(R.string.login_wrong_getvoice));
        }

        public void GetPhoneCode() {

            String phone = et_phone.getText().toString();
            if (phone != null && phone.length() == 11) {
                ChangeVoiceButton();
                new GetPhoneCodeAsyncTask().execute(phone);
//                RequestParams params = new RequestParams();
//                params.put("phone", phone);
//                AsyncHttpClient client = new AsyncHttpClient();
//                client.post("http://app.ozner.net:888//OznerServer/GetPhoneCode", params, new JsonHttpResponseHandler() {
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                        tv_wrong.setText(getResources().getString(R.string.innet_wrong));
//                        StopChangeVoiceButton();
//                    }
//
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                        tv_wrong.setText(getResources().getString(R.string.login_code_sendwrong));
//                        StopChangeVoiceButton();
//                    }
//
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, org.json.JSONObject response) {
//                        int result = 0;
//                        try {
//                            result = Integer.parseInt(response.getString("state"));
//                        } catch (Exception ex) {
//                            result = 0;
//                        }
//                        if (result > 0) {
//                        } else {
//                            tv_wrong.setText(getResources().getString(R.string.login_code_sendwrong));
//                            StopChangeVoiceButton();
//                        }
//                    }
//                });
            } else {
                tv_wrong.setText(getResources().getString(R.string.login_wrong_phone));
            }
        }

        //登陆
        private void Login() {
            String phone = et_phone.getText().toString();
            if (phone != null && phone.length() == 11) {
                String code = et_code.getText().toString();
                if (code != null && code.length() >= 4) {
                    new UiUpdateAsyncTask().execute(phone, code);
//                    new GetDeviceListAsyncTask().execute();
                } else {
                    tv_wrong.setText(getResources().getString(R.string.login_wrong_emptycode));
                }
            } else {
                tv_wrong.setText(getResources().getString(R.string.login_wrong_phone));
            }
        }

        private void LoginAsync() {
            UiUpdateAsyncTask task = new UiUpdateAsyncTask();
            task.execute("", "");
        }

        private class UiUpdateAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
            private ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(LoginActivity.this, getString(R.string.login_aync), getString(R.string.login_aync_ing));
            }

            //doInBackground方法内部执行后台任务,不可在此方法内修改UI
            @Override
            protected NetJsonObject doInBackground(String... params) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                NetJsonObject netJsonObject = OznerCommand.Login(LoginActivity.this, params[0], params[1]);
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
                        OznerCommand.CheckTokenAndInitUserData(LoginActivity.this);
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
                            if (UserDataPreference.GetUserData(LoginActivity.this, UserDataPreference.hasUpdateUserInfo, "false").equals("false")) {
                                sendBroadcast(new Intent(OznerBroadcastAction.UpdateUserInfo));
                            }
                        } catch (Exception ex) {
                            ex.getMessage();
                        }
                    } else {
                    }
                    //初始化用户头像
                    OznerCommand.InitUserHeadImg(LoginActivity.this);
                }
                return netJsonObject;
            }

            //onProgressUpdate方法用于更新进度信息
            @Override
            protected void onProgressUpdate(Integer... progresses) {

            }

            //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
            @Override
            protected void onPostExecute(NetJsonObject result) {
                if (dialog != null)
                    dialog.dismiss();
                if (result != null && result.state > 0) {
                    ShowMainPage(null);
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
                        default:
                            tv_wrong.setText(getResources().getString(R.string.login_wrong_login_error));
                            break;
                    }


                }
            }

            //onCancelled方法用于在取消执行中的任务时更改UI
            @Override
            protected void onCancelled() {

            }
        }

        private class GetPhoneCodeAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
            private ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(LoginActivity.this, null, "正在获取验证码");
            }

            //doInBackground方法内部执行后台任务,不可在此方法内修改UI
            @Override
            protected NetJsonObject doInBackground(String... params) {
                NetJsonObject netJsonObject = OznerCommand.GetPhoneCode(LoginActivity.this, params[0]);
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return netJsonObject;
            }

            //onProgressUpdate方法用于更新进度信息
            @Override
            protected void onProgressUpdate(Integer... progresses) {

            }

            //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
            @Override
            protected void onPostExecute(NetJsonObject result) {
                try {
                    if (dialog != null)
                        dialog.dismiss();
                    if (result != null && result.state > 0) {
                        tv_wrong.setText("");
                    } else {
                        tv_wrong.setText(getResources().getString(R.string.login_code_sendwrong));
                        StopChangeVoiceButton();
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
                            default:
                                tv_wrong.setText(getResources().getString(R.string.login_wrong_login_error));
                                break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            //onCancelled方法用于在取消执行中的任务时更改UI
            @Override
            protected void onCancelled() {

            }
        }
    }

    class GetDeviceListAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params != null) {
                List<NameValuePair> pars = new ArrayList<>();
                pars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(LoginActivity.this)));
                String filterUrl = OznerPreference.ServerAddress(LoginActivity.this) + "OznerServer/GetDeviceList";
                Log.e("123456", "doInBackground: url+" + filterUrl);
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(LoginActivity.this, filterUrl, pars);
                Log.e("123456", "doInBackground: +" + netJsonObject.value);
                return netJsonObject;
            }
            return null;
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (netJsonObject != null && netJsonObject.state > 0) {
                try {
                    JSONArray jsonArray = netJsonObject.getJSONObject().getJSONArray("data");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        String Mac = object.getString("Mac");
                        String type = object.getString("DeviceType");
                        String setting = object.getString("Settings");
                        OznerDevice device = OznerDeviceManager.Instance().getDevice(Mac, type, setting);
                        if (device != null) {
                            OznerDeviceManager.Instance().save(device);
                        }
                        try {
                            String appData = object.getString("AppData");
                            if (appData != null || !appData.equals(null)) {
                                String key = appData.split(":")[0];
                                String value = appData.split(":")[1];
                                key = key.substring(1, key.length());
                                value = value.substring(0, value.length() - 1);
                                device.setAppdata(key, value);
                                device.updateSettings();
                            }
                        } catch (Exception e) {
                        }
                        device.setAppdata(PageState.sortPosi, i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class GetVoiceCodeAsyncTask extends AsyncTask<String, Void, NetJsonObject> {
        @Override
        protected void onPreExecute() {
            tv_voice.setClickable(false);
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params != null) {
                String phone = params[0];
                String voiceCodeUrl = OznerPreference.ServerAddress(LoginActivity.this) + "/OznerServer/GetVoicePhoneCode";
                List<NameValuePair> pars = new ArrayList<>();
                pars.add(new BasicNameValuePair("phone", phone));
                NetJsonObject result = OznerDataHttp.OznerWebServer(LoginActivity.this, voiceCodeUrl, pars);
                return result;
            }
            return null;
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (netJsonObject != null && netJsonObject.state > 0) {
                tv_wrong.setText(getResources().getString(R.string.login_wrong_getvoice));
            } else {
                tv_wrong.setText(getResources().getString(R.string.login_wrong_getvoice));
            }
        }
    }

    private void ShowMainPage(NetDeviceList devicejson) {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
        System.exit(0);
//       finishAndRemoveTask();
    }
}

package com.ozner.cup.Device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.Command.PageState;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.wifi.WifiPair;

import com.ozner.cup.R;

/**
 * Created by mengdongya on 2015/12/22.
 * 立式空净
 */
public class MatchAirPuriVerActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    LinearLayout connection_to_wifi, ll_setpurifier_name, ll_searched_device, device_place2, ll_conn_notice;
    RelativeLayout rl_restart_matching, device_place1, rlay_air_matching;
    TextView toolbar_text, match_airpurifier_notice, match_airpurifier_wifi, tv_selectedWifi, et_password, tv_matchingTips, tv_state;
    ImageView image = null, iv_air_purifier_ver, iv_remember_password, image1, image2, image3, image4, image5, indeximage;
    EditText et_air_name, et_air_position;
    Button finish_add_device;
    TimerCount timerCount;
    WifiPair wifiPair;
    AnimationDrawable anim1, anim2, anim3, anim4, anim5;
    SharedPreferences wifiPreferences;
    WifiManager wifiManager;
    Monitor monitor;
    ListAdapter adapter;
    RecyclerView deviceList;
    final WifiPairImp wifiPairImp = new WifiPairImp();
    private final int SENDDING_CONFIG = 0x01;
    private final int WAITTING_RESTART = 0x02;
    private final int WAITTING_REGIST = 0x03;
    private final int QUERYING_AYLA = 0x04;
    private final int QUERYING_MXPAIR = 0x05;
    MyHandler myHandler = new MyHandler();
    BaseDeviceIO bindIO;
    int state1 = 1;
    private String Mac;
    Animation animinput, animfadeout, animfadein;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.add_device));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.add_device));
        }
        wifiPreferences = this.getSharedPreferences("WifiPassword", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_match_airli);

        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        monitor = new Monitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.registerReceiver(monitor, filter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.add_device);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar_text = (TextView) findViewById(R.id.toolbar_text);

        animinput = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        animfadeout = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        animfadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        animinput.setInterpolator(new DecelerateInterpolator(2.0f));
        animfadeout.setInterpolator(new DecelerateInterpolator(2.0f));
        animfadein.setInterpolator(new DecelerateInterpolator(2.0f));
        initView();
        try {
            wifiPair = new WifiPair(this, wifiPairImp);
        } catch (WifiPair.NullSSIDException e) {
            e.printStackTrace();
        }
        startAnim();
    }

    private void initView() {
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        deviceList = (RecyclerView) findViewById(R.id.my_recycler_view);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        mLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        deviceList.setLayoutManager(mLayoutManager);

        connection_to_wifi = (LinearLayout) findViewById(R.id.connection_to_wifi);//输入wifi密码
        ll_setpurifier_name = (LinearLayout) findViewById(R.id.ll_setpurifier_name);//设置净水器名字和使用地点
        ll_searched_device = (LinearLayout) findViewById(R.id.ll_searched_device);//搜索到设备
        rl_restart_matching = (RelativeLayout) findViewById(R.id.rl_restart_matching);//重新配对
        rlay_air_matching = (RelativeLayout) findViewById(R.id.rlay_air_matching);//路由器发送到云
        image = (ImageView) findViewById(R.id.iv_matching_airpurifier);
        iv_air_purifier_ver = (ImageView) findViewById(R.id.iv_air_purifier_ver);
        iv_remember_password = (ImageView) findViewById(R.id.iv_remember_password);
        iv_remember_password.setSelected(true);
        match_airpurifier_notice = (TextView) findViewById(R.id.match_airpurifier_notice);
        match_airpurifier_wifi = (TextView) findViewById(R.id.match_airpurifier_wifi);
        tv_selectedWifi = (TextView) findViewById(R.id.purifier_tv_wifi_name);
        et_password = (TextView) findViewById(R.id.purifier_tv_wifi_password);
        et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        et_air_name = (EditText) findViewById(R.id.et_device_name);
        et_air_name.setHint(getString(R.string.edit_airpurifier_name));
        et_air_position = (EditText) findViewById(R.id.et_device_position);
        tv_matchingTips = (TextView) findViewById(R.id.tv_matchingTips);
        finish_add_device = (Button) findViewById(R.id.finish_add_device);
        tv_state = (TextView) findViewById(R.id.tv_state);
        image1 = (ImageView) findViewById(R.id.image1);
        anim1 = (AnimationDrawable) image1.getDrawable();
        image2 = (ImageView) findViewById(R.id.image2);
        anim2 = (AnimationDrawable) image2.getDrawable();
        image3 = (ImageView) findViewById(R.id.image3);
        anim3 = (AnimationDrawable) image3.getDrawable();
        image4 = (ImageView) findViewById(R.id.image4);
        anim4 = (AnimationDrawable) image4.getDrawable();
        image5 = (ImageView) findViewById(R.id.image5);
        anim5 = (AnimationDrawable) image5.getDrawable();

        device_place1 = (RelativeLayout) findViewById(R.id.device_place1);
        device_place2 = (LinearLayout) findViewById(R.id.device_place2);
        ll_conn_notice = (LinearLayout) findViewById(R.id.ll_conn_notice);
        ll_conn_notice.setVisibility(View.VISIBLE);

        iv_remember_password.setOnClickListener(this);
        findViewById(R.id.iv_passwordImg).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);
        findViewById(R.id.btn_restart_match).setOnClickListener(this);
        findViewById(R.id.finish_add_device).setOnClickListener(this);
        device_place1.setOnClickListener(this);
        device_place2.setOnClickListener(this);

        toolbar_text.setText(getString(R.string.match_device));
        connection_to_wifi.setVisibility(View.GONE);
        ll_setpurifier_name.setVisibility(View.GONE);
        ll_searched_device.setVisibility(View.GONE);
        rlay_air_matching.setVisibility(View.GONE);
        rl_restart_matching.setVisibility(View.INVISIBLE);

        finish_add_device.setOnClickListener(new View.OnClickListener() {
            //完成
            @Override
            public void onClick(View v) {
                if (Mac != null && Mac.length() > 0) {
                    SaveDevice(Mac);
                }
//                Toast.makeText(getBaseContext(), "选中设备已经断开,请重新匹配", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void startAnim() {
        timerCount = new TimerCount(2000, 1000);
        timerCount.start();
        RotateAnimation animation = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);
        animation.setRepeatCount(1);
        LinearInterpolator li = new LinearInterpolator();
        animation.setInterpolator(li);
        animation.setFillAfter(false);
        image.startAnimation(animation);
    }

    //开始匹配
    private void Match() {
        if (iv_remember_password.isSelected()) {
            weatherRemPassword();
        }

        String ssid = "";
        try {
            ssid = tv_selectedWifi.getText().toString().trim();
        } catch (Exception ex) {
            ssid = "";
        }

        String password = "";
        try {
            password = et_password.getText().toString().trim();
        } catch (Exception ex) {
            ex.printStackTrace();
            password = "";
        }

        if (ssid.length() > 0) {
            try {
                if (password.length() > 0) {
                    wifiPair.pair(ssid, password);
                    showMatching();
                } else {
                    Toast toast = Toast.makeText(this,R.string.input_password,Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showMatchFail();
            }
        } else {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.notice_match_wifi), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_remember_password:
                iv_remember_password.setSelected(!iv_remember_password.isSelected());
                break;
            case R.id.next:
                Match();
                break;
            case R.id.btn_restart_match:
                restartSearchDevice();
                break;
            case R.id.iv_passwordImg:
                state1++;
                state1 = state1 % 2;
                if (state1 % 2 == 1) {
                    et_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                break;
            case R.id.device_place1:
                if (device_place2.getVisibility() == View.VISIBLE) {
                    device_place2.setVisibility(View.INVISIBLE);
                    findViewById(R.id.iv_show_device_place2).setSelected(false);

                } else {
                    device_place2.setVisibility(View.VISIBLE);
                    findViewById(R.id.iv_show_device_place2).setSelected(true);
                    et_air_position.setText(getString(R.string.living_room));
                }
                break;
            case R.id.device_place2:
                device_place2.setVisibility(View.INVISIBLE);
                findViewById(R.id.iv_show_device_place2).setSelected(false);
                et_air_position.setText(getString(R.string.bedroom));
                break;
        }
    }

    //重新搜索设备
    private void restartSearchDevice() {
        toolbar_text.setText(getString(R.string.match_device));
        connection_to_wifi.startAnimation(animfadein);
        ll_setpurifier_name.startAnimation(animfadeout);
        ll_searched_device.startAnimation(animfadeout);
        rl_restart_matching.startAnimation(animfadeout);
        rlay_air_matching.startAnimation(animfadeout);
        connection_to_wifi.setVisibility(View.VISIBLE);
        ll_setpurifier_name.setVisibility(View.GONE);
        ll_searched_device.setVisibility(View.GONE);
        rlay_air_matching.setVisibility(View.GONE);
        rl_restart_matching.setVisibility(View.GONE);
        image.setImageDrawable(getResources().getDrawable(R.drawable.match_purifier_wifi));
        match_airpurifier_notice.setText(getString(R.string.connection_wlan));
        match_airpurifier_notice.setVisibility(View.VISIBLE);
        ll_conn_notice.setVisibility(View.INVISIBLE);
        match_airpurifier_wifi.setText(getString(R.string.selected_wlan));
        match_airpurifier_wifi.setVisibility(View.VISIBLE);
    }


    class WifiPairImp implements WifiPair.WifiPairCallback {
        //
        @Override
        public void onStartPairAyla() {
            Message msg = new Message();
            msg.what = 2;
            msg.obj = QUERYING_AYLA;
            myHandler.sendMessage(msg);
        }

        @Override
        public void onStartPariMxChip() {
            Message msg = new Message();
            msg.what = 2;
            msg.obj = QUERYING_MXPAIR;
            myHandler.sendMessage(msg);
        }

        //正在发送配置信息
        @Override
        public void onSendConfiguration() {
            Message msg = new Message();
            msg.what = 2;
            msg.obj = SENDDING_CONFIG;
            myHandler.sendMessage(msg);
        }

        //等待设备重启
        @Override
        public void onWaitConnectWifi() {
            Message msg = new Message();
            msg.what = 2;
            msg.obj = WAITTING_RESTART;
            myHandler.sendMessage(msg);

        }

        //等待设备激活
        @Override
        public void onActivateDevice() {
            Message msg = new Message();
            msg.what = 2;
            msg.obj = WAITTING_REGIST;
            myHandler.sendMessage(msg);

        }

        //配对完成
        @Override
        public void onPairComplete(BaseDeviceIO io) {
            bindIO = io;
            Message msg = new Message();
            msg.what = 3;
            myHandler.sendMessage(msg);

        }

        //配网失败
        @Override
        public void onPairFailure(Exception e) {
            Message msg = new Message();
            msg.what = 1;
            myHandler.sendMessage(msg);

        }
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //配网失败
                case 1:
                    //showSelectWifi();
                    showMatchFail();
                    break;
                //配网成功
                case 2:
                    switch ((int) msg.obj) {
                        case QUERYING_AYLA:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_SendConfig));
                            break;
                        case QUERYING_MXPAIR:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_SendConfig));
                            break;
                        case SENDDING_CONFIG:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_SendConfig));
                            break;
                        case WAITTING_RESTART:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_WaitRestart));
                            break;
                        case WAITTING_REGIST:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_WaitRegist));
                            break;
                        default:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_RouterConnecting));
                            break;
                    }
                    break;
                case 3:
//                    MatchSuccessShow();
                    showChangeName();
                    break;
            }
        }
    }

    //设置空净名称和使用地点的页面
    private void showChangeName() {
        toolbar_text.setText(getResources().getString(R.string.match_successed));
        match_airpurifier_notice.startAnimation(animfadeout);
        match_airpurifier_wifi.startAnimation(animfadeout);
        connection_to_wifi.startAnimation(animfadeout);
        rl_restart_matching.startAnimation(animfadeout);
        rlay_air_matching.startAnimation(animfadeout);
        ll_searched_device.startAnimation(animfadein);
        match_airpurifier_notice.setVisibility(View.GONE);
        match_airpurifier_wifi.setVisibility(View.GONE);
        connection_to_wifi.setVisibility(View.GONE);
        ll_conn_notice.setVisibility(View.GONE);

        findViewById(R.id.ll_iv_tv).setVisibility(View.GONE);
        findViewById(R.id.btn_restart_match).setVisibility(View.GONE);
        findViewById(R.id.tv_match_purifier_notice2).setVisibility(View.GONE);
        rl_restart_matching.setVisibility(View.GONE);
        iv_air_purifier_ver.setVisibility(View.GONE);
        rlay_air_matching.setVisibility(View.GONE);
        image.setImageResource(R.drawable.air_purifier_ver_searched);
        ll_searched_device.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2f));
        ll_searched_device.setVisibility(View.VISIBLE);
        //Success
        try {
            ChangeWidth(1);
            adapter = new ListAdapter();
            deviceList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void ChangeWidth(int size) {
        if (size >= 3)
            size = 3;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(OznerCommand.dip2px(getBaseContext(), size * 120), ViewGroup.LayoutParams.MATCH_PARENT);
        deviceList.setLayoutParams(layoutParams);
    }

    //路由器连接云，
    private void showMatching() {
        ll_searched_device.setVisibility(View.GONE);
        connection_to_wifi.setVisibility(View.GONE);
        rl_restart_matching.setVisibility(View.GONE);
        ll_conn_notice.setVisibility(View.VISIBLE);
        rlay_air_matching.setVisibility(View.VISIBLE);
        anim1.start();
        anim2.start();
        anim3.start();
        anim4.start();
        anim5.start();
        match_airpurifier_wifi.setVisibility(View.VISIBLE);
        match_airpurifier_wifi.setText(getString(R.string.matching_wifi));
        match_airpurifier_notice.setVisibility(View.INVISIBLE);
        toolbar_text.setText(getResources().getString(R.string.match_device));
        image.setImageResource(R.drawable.match_purifier_wifi);

//        adapter = new ListAdapter(getBaseContext());
//        deviceList.setAdapter(adapter);adasd defgdfw
//        adapter.notifyDataSetChanged();
    }

    //搜索失败
    private void showMatchFail() {
        ll_searched_device.startAnimation(animfadeout);
        connection_to_wifi.startAnimation(animfadeout);
        rlay_air_matching.startAnimation(animfadeout);
        rl_restart_matching.startAnimation(animfadein);
        image.startAnimation(animfadein);
        ll_searched_device.setVisibility(View.GONE);
        connection_to_wifi.setVisibility(View.GONE);
        rl_restart_matching.setVisibility(View.VISIBLE);
        rlay_air_matching.setVisibility(View.GONE);
        image.setImageResource(R.drawable.match_device_failed);
        ll_conn_notice.setVisibility(View.INVISIBLE);
        match_airpurifier_notice.setText(getResources().getString(R.string.failed_searching));
        match_airpurifier_wifi.setText(getResources().getString(R.string.restart_match));
        match_airpurifier_notice.setVisibility(View.VISIBLE);
        match_airpurifier_wifi.setVisibility(View.VISIBLE);
        toolbar_text.setText(getResources().getString(R.string.match_failed));

    }

    //是否记住密码
    private void weatherRemPassword() {
        String ssid = tv_selectedWifi.getText().toString().trim();
        SharedPreferences.Editor editor = wifiPreferences.edit();
        try {
            editor.putString("password." + ssid, et_password.getText().toString().trim());
        } finally {
            editor.commit();
        }
    }


    class ListAdapter extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener {

        public ListAdapter() {

        }

        private void Reload() {
        }

        @Override
        public int getItemCount() {
            // TODO Auto-generated method stub
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public void onBindViewHolder(ViewHolder convertView, final int position) {
            // TODO Auto-generated method stub
            BaseDeviceIO device = (BaseDeviceIO) bindIO;

            if (device != null) {
                if (Mac != null && device.getAddress().equals(Mac)) {
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.air_purifier_ver_searched);
                    convertView.item_selected.setChecked(true);
                } else {
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.air_purifier_ver_small);
                    convertView.item_selected.setChecked(false);
                }
                convertView.Cup_tv_device_item_name.setText(getString(R.string.air_puri_ver));
            } else {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.device_null), Toast.LENGTH_SHORT).show();
            }

            convertView.item_selected.setClickable(false);
            convertView.RootView.setOnClickListener(new MyOnClickListener(device.getAddress()));
            //IS SUCCESS SHOW 防止函数重复调用
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
            // TODO Auto-generated method stub
            View itemLayout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.device_list_item, null);
            return new ViewHolder(itemLayout);
        }

        @Override
        public void onClick(View v) {
        }
    }

    //GridView Item 点击事件
    class MyOnClickListener implements View.OnClickListener {
        private String device;

        public MyOnClickListener(String address) {
            device = address;
        }

        @Override
        public void onClick(View v) {
            if (device != Mac) {
                Mac = device;
                adapter.notifyDataSetChanged();
                MatchSuccessShow();
            }

        }
    }

    //配对成功显示界面
    private void MatchSuccessShow() {
        ll_setpurifier_name.startAnimation(animinput);
        toolbar_text.setText(getString(R.string.match_successed));
        iv_air_purifier_ver.setVisibility(View.GONE);
        et_air_position.setText(getString(R.string.living_room));
        ll_setpurifier_name.setVisibility(View.VISIBLE);
        ll_conn_notice.setVisibility(View.INVISIBLE);
        tv_state.setVisibility(View.GONE);
        findViewById(R.id.tv_control1).setVisibility(View.GONE);
        findViewById(R.id.tv_control2).setVisibility(View.VISIBLE);
        image.setImageResource(R.drawable.match_device_successed);
        image.startAnimation(animfadein);
    }

    //把图片加给选择的item
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView Cup_iv_device_item_image; //图片
        RadioButton item_selected; //被选择设备下标图片
        TextView Cup_tv_device_item_name;   //名字
        View RootView;

        public ViewHolder(View v) {
            super(v);
            this.RootView = v;
            this.Cup_iv_device_item_image = (ImageView) v.findViewById(R.id.iv_device_item_image);
            this.Cup_tv_device_item_name = (TextView) v.findViewById(R.id.tv_device_item_name);
            this.item_selected = (RadioButton) v.findViewById(R.id.item_selected);
        }
    }

    //  倒计时，计时器
    class TimerCount extends CountDownTimer {
        public TimerCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            //结束时干什么
            iv_air_purifier_ver.setVisibility(View.GONE);
            image.setImageDrawable(getResources().getDrawable(R.drawable.match_purifier_wifi));
            match_airpurifier_notice.setText(getString(R.string.connection_wlan));
            ll_conn_notice.setVisibility(View.INVISIBLE);
            match_airpurifier_notice.setVisibility(View.VISIBLE);
            match_airpurifier_wifi.setText(getString(R.string.selected_wlan));
            match_airpurifier_wifi.setVisibility(View.VISIBLE);
            rl_restart_matching.setVisibility(View.GONE);
            connection_to_wifi.setVisibility(View.VISIBLE);

//            if (deviceNum == 0) {
//
//            } else {
//                //搜索设备成功的界面显示
//
//            }
        }
    }

    private void loadWifi() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        int wifiState = wifiManager.getWifiState();
        if (wifiInfo != null) {
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                String ssid = wifiInfo.getSSID().replace("\"", "");
//                if (AylaIOManager.isAylaSSID(ssid)) return;
                tv_selectedWifi.setText(ssid);
                String pwd = wifiPreferences.getString("password." + ssid, "");
                et_password.setText(pwd);
            } else
                tv_selectedWifi.setText("");
        } else {
            tv_selectedWifi.setText("");
        }
    }

    class Monitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                loadWifi();
            }
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                loadWifi();
            }
        }
    }

    private void SaveDevice(String mac) {
        if (bindIO != null) {
            String name = et_air_name.getText().toString();
            String addr = et_air_position.getText().toString();
            try {
                //通过找到的蓝牙对象控制对象获取设备对象
                OznerDevice device = OznerDeviceManager.Instance().getDevice(bindIO);
                if (device != null && AirPurifierManager.IsWifiAirPurifier(device.Type())) {
                    //保存设备
                    if (name.isEmpty()) {
                        device.Setting().name(getString(R.string.my_air_purifier_ver));
                    } else {
                        device.Setting().name(name);
                    }
                    OznerDeviceManager.Instance().save(device);
                    device.setAppdata(PageState.DEVICE_ADDRES, addr);
                    //添加网络缓存任务
                    OznerCommand.CNetCacheBindDeviceTask(getBaseContext(), device);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.finish();
            return;
        }

    }

    @Override
    protected void onDestroy() {
        try {
            this.unregisterReceiver(monitor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            super.onDestroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

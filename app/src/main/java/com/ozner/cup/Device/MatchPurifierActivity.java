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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.wifi.WifiPair;
import com.ozner.wifi.mxchip.MXChipIO;

import java.util.ArrayList;
import java.util.List;

//import com.ozner.wifi.ayla.AylaIOManager;

/**
 * Created by mengdongya on 2015/11/23.
 */
public class MatchPurifierActivity extends AppCompatActivity implements SpinnerPopWindow.IOnItemSelectListener, View.OnClickListener {
    TextView toolbar_text, tv_matchStatus, tv_matchTips, tv_selectedDevice, tv_selectedWifi, tv_matchingTips;
    ImageView iv_matchStatus, iv_passwordImg, iv_remainPass, image1, image2, image3, image4, image5;
    WifiManager wifiManager;
    LinearLayout llay_WifiSelect, llay_selectWifi, llay_matchTips, llay_matchTipsText, llay_setPurifierName, llay_matchSuccess;
    RelativeLayout rlay_matchWifiFail, rlay_purifier_matching, rlay_wifiSelected;
    SharedPreferences wifiPreferences;
    SharedPreferences.Editor editor;
    Button btn_ReMatch, btn_next;
    Button btn_done;
    Monitor monitor;
    Toolbar toolbar;
    MyHandler myHandler = new MyHandler();
    ListAdapter adapter;
    RecyclerView deviceList;
    AnimationDrawable anim1, anim2, anim3, anim4, anim5;
    EditText et_purifierName, et_purifierPos, et_password;
    List<MXChipIO> list;
    List<String> poslist = new ArrayList<String>();
    SpinnerPopWindow posSpinner;
    final WifiPairImp wifiPairImp = new WifiPairImp();
    BaseDeviceIO bindIO;
    private final int SENDDING_CONFIG = 0x01;
    private final int WAITTING_RESTART = 0x02;
    private final int WAITTING_REGIST = 0x03;
    private final int QUERYING_AYLA = 0x04;
    private final int QUERYING_MXPAIR = 0x05;
    int state1 = 1;
    private String Mac;
    Animation animinput, animfadeout, animfadein;
    private WifiPair wifiPair;

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
        editor = wifiPreferences.edit();
        setContentView(R.layout.activity_match_purifier2);
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        monitor = new Monitor();
        animinput = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        animfadeout = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        animfadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        animinput.setInterpolator(new DecelerateInterpolator(2.0f));
        animfadeout.setInterpolator(new DecelerateInterpolator(2.0f));
        animfadein.setInterpolator(new DecelerateInterpolator(2.0f));
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.registerReceiver(monitor, filter);
        try {
            wifiPair = new WifiPair(this, wifiPairImp);
        } catch (WifiPair.NullSSIDException e) {
            e.printStackTrace();
        }
        initView();
    }

//    //  倒计时，计时器
//    class TimerCount extends CountDownTimer {
//        public TimerCount(long millisInFuture, long countDownInterval) {
//            super(millisInFuture, countDownInterval);
//        }
//
//        @Override
//        public void onTick(long millisUntilFinished) {
//        }
//
//        @Override
//        public void onFinish() {
//            //结束时干什么
//            showSelectWifi();
//            loadWifi();
//            llay_matchTips.setVisibility(View.VISIBLE);
//            llay_WifiSelect.setVisibility(View.VISIBLE);
//        }
//    }

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

    private void Match() {
        if (iv_remainPass.isSelected()) {
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
            password = et_password.getText().toString();
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
            } catch (WifiPair.PairRunningException e) {
                e.printStackTrace();
                showMatchFail();
            }

        } else {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.notice_match_wifi), Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.add_device);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar_text = (TextView) findViewById(R.id.toolbar_text);//标题
        deviceList = (RecyclerView) findViewById(R.id.my_recycler_view);//
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        mLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        deviceList.setLayoutManager(mLayoutManager);

        rlay_wifiSelected = (RelativeLayout) findViewById(R.id.rlay_wifiSelected);//wifi输入框
        iv_matchStatus = (ImageView) findViewById(R.id.iv_matchStatus);//配对状态图片
        tv_matchStatus = (TextView) findViewById(R.id.tv_matchStatus);//配对状态信息
        tv_matchTips = (TextView) findViewById(R.id.tv_matchTips);//配对状态提示
        llay_matchTips = (LinearLayout) findViewById(R.id.llay_matchTips);
        llay_matchTipsText = (LinearLayout) findViewById(R.id.llay_matchTipsText);//配对提示布局
        llay_WifiSelect = (LinearLayout) findViewById(R.id.llay_WifiSelect);//选择wifi时下方布局，配网失败 父布局
        llay_selectWifi = (LinearLayout) findViewById(R.id.llay_selectWifi);//选择wifi布局
        rlay_matchWifiFail = (RelativeLayout) findViewById(R.id.rlay_matchWifiFail);//配网失败布局
        rlay_purifier_matching = (RelativeLayout) findViewById(R.id.rlay_purifier_matching);//配网中布局
        tv_matchingTips = (TextView) findViewById(R.id.tv_matchingTips);
        llay_matchSuccess = (LinearLayout) findViewById(R.id.rlay_matchSuccess);//配网成功
        llay_setPurifierName = (LinearLayout) findViewById(R.id.llay_setPurifierName);//设置设备名字
        tv_selectedDevice = (TextView) findViewById(R.id.tv_selectedDevice);
        tv_selectedWifi = (TextView) findViewById(R.id.tv_selectedWifi);
        iv_remainPass = (ImageView) findViewById(R.id.iv_remainPass);
        iv_remainPass.setSelected(true);
        iv_passwordImg = (ImageView) findViewById(R.id.iv_passwordImg);
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
        et_purifierPos = (EditText) findViewById(R.id.et_purifierPos);
        btn_ReMatch = (Button) findViewById(R.id.btn_ReMatch);//重新配对按钮
        btn_next = (Button) findViewById(R.id.btn_next);//下一步
        et_password = (EditText) findViewById(R.id.et_password);
        et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        btn_done = (Button) findViewById(R.id.btn_done);
        et_purifierName = (EditText) findViewById(R.id.et_purifierName);
        et_purifierPos = (EditText) findViewById(R.id.et_purifierPos);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Match();
            }
        });
        btn_ReMatch.setOnClickListener(this);
        iv_passwordImg.setOnClickListener(this);
        iv_remainPass.setOnClickListener(this);
        btn_done.setOnClickListener(new View.OnClickListener() {
            //完成
            @Override
            public void onClick(View v) {
                if (Mac != null && Mac.length() > 0) {

                    SaveDevice(Mac);
                    return;
                }
//                Toast.makeText(getBaseContext(), getResources().getString(R.string.device_null), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SaveDevice(String mac) {
        if (bindIO != null) {
            Mac = bindIO.getAddress();
            String name = et_purifierName.getText().toString();
            String address = et_purifierPos.getText().toString();
            try {
                OznerDevice device = OznerDeviceManager.Instance().getDevice(bindIO);
                if (device instanceof WaterPurifier) {
                    if (name != null && name.length() > 0) {
                        device.Setting().name(name);
                    } else {
                        device.Setting().name(getString(R.string.water_purifier));
                    }
                    OznerDeviceManager.Instance().save(device);
                    device.updateSettings();
                    device.setAppdata(PageState.DEVICE_ADDRES, address);
                    //添加网络缓存任务
                    OznerCommand.CNetCacheBindDeviceTask(getBaseContext(), device);
                }
                finish();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    @Override
    public void onItemClick(int pos) {
        setDevicePos(pos);
    }

    /*
    * 设置净水器位置
     */
    private void setDevicePos(int pos) {
        if (pos >= 0 && pos < poslist.size()) {
            et_purifierPos.setText(poslist.get(pos));
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

    /*
    * 选择wifi界面
     */
    private void showSelectWifi() {
        llay_matchSuccess.startAnimation(animfadeout);
        llay_matchTipsText.startAnimation(animfadein);
        rlay_matchWifiFail.startAnimation(animfadeout);
        rlay_purifier_matching.startAnimation(animfadeout);
        llay_matchSuccess.setVisibility(View.GONE);
        llay_matchTipsText.setVisibility(View.VISIBLE);
        llay_WifiSelect.setVisibility(View.VISIBLE);
        llay_selectWifi.setVisibility(View.VISIBLE);
        rlay_matchWifiFail.setVisibility(View.GONE);
        rlay_purifier_matching.setVisibility(View.GONE);
        iv_matchStatus.setImageResource(R.drawable.match_purifier_wifi);
        tv_matchStatus.setText(getResources().getString(R.string.connection_wlan));
        tv_matchTips.setText(getResources().getString(R.string.Purifier_SelectWifi));
        toolbar_text.setText(getResources().getString(R.string.match_device));
        tv_matchStatus.setVisibility(View.VISIBLE);
        tv_matchTips.setVisibility(View.VISIBLE);
    }

    /*
    * wifi配网中界面
     */
    private void showMatching() {
        llay_matchSuccess.startAnimation(animfadeout);
        rlay_purifier_matching.startAnimation(animfadein);
        llay_WifiSelect.startAnimation(animfadeout);
        llay_matchSuccess.setVisibility(View.GONE);
        llay_selectWifi.setVisibility(View.GONE);
//        llay_matchTipsText.setVisibility(View.INVISIBLE);
        llay_WifiSelect.setVisibility(View.GONE);
        rlay_purifier_matching.setVisibility(View.VISIBLE);
        anim1.start();
        anim2.start();
        anim3.start();
        anim4.start();
        anim5.start();
        tv_matchTips.setVisibility(View.GONE);
        tv_matchStatus.setVisibility(View.GONE);
        toolbar_text.setText(getResources().getString(R.string.match_device));
        iv_matchStatus.setImageResource(R.drawable.match_purifier_wifi);
    }

    /*
    * 配网失败界面
     */
    private void showMatchFail() {
        llay_matchSuccess.startAnimation(animfadeout);
        llay_matchTipsText.startAnimation(animfadein);
        llay_WifiSelect.startAnimation(animfadein);
        llay_selectWifi.startAnimation(animfadeout);
        rlay_purifier_matching.startAnimation(animfadeout);
        llay_matchSuccess.setVisibility(View.GONE);
        llay_matchTipsText.setVisibility(View.VISIBLE);
        llay_WifiSelect.setVisibility(View.VISIBLE);
        llay_selectWifi.setVisibility(View.GONE);
        rlay_matchWifiFail.setVisibility(View.VISIBLE);
        rlay_purifier_matching.setVisibility(View.GONE);
        iv_matchStatus.setImageResource(R.drawable.match_device_failed);
        tv_matchStatus.setText(getResources().getString(R.string.failed_searching));
        tv_matchTips.setText(getResources().getString(R.string.restart_match));
        toolbar_text.setText(getResources().getString(R.string.match_failed));
        tv_matchStatus.setVisibility(View.VISIBLE);
        tv_matchTips.setVisibility(View.VISIBLE);
    }

    /*
    *修改设备名字界面
     */
    private void showChangeName() {
        llay_matchTipsText.startAnimation(animfadeout);
        llay_WifiSelect.startAnimation(animfadeout);
        rlay_matchWifiFail.startAnimation(animfadeout);
        rlay_purifier_matching.startAnimation(animfadeout);
        llay_matchTipsText.setVisibility(View.GONE);
        llay_WifiSelect.setVisibility(View.GONE);
        rlay_matchWifiFail.setVisibility(View.GONE);
        rlay_purifier_matching.setVisibility(View.GONE);
        iv_matchStatus.setImageResource(R.drawable.water_purifier_selected);
        llay_matchSuccess.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2f));
        llay_matchSuccess.setVisibility(View.VISIBLE);
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

    //配对成功显示界面
    private void MatchSuccessShow() {
        llay_setPurifierName.startAnimation(animinput);
        toolbar_text.setText(getString(R.string.match_successed));
        et_purifierPos.setText(getString(R.string.living_room));
        findViewById(R.id.tv_control1).setVisibility(View.GONE);
        findViewById(R.id.tv_control2).setVisibility(View.VISIBLE);
        llay_setPurifierName.setVisibility(View.VISIBLE);
        tv_selectedDevice.setVisibility(View.GONE);
        iv_matchStatus.setImageResource(R.drawable.match_device_successed);
        iv_matchStatus.startAnimation(animfadein);
        llay_setPurifierName.setVisibility(View.VISIBLE);
    }

    class WifiPairImp implements WifiPair.WifiPairCallback {

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
                        case SENDDING_CONFIG:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_SendConfig));
                            break;
                        case WAITTING_RESTART:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_WaitRestart));
                            break;
                        case WAITTING_REGIST:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_WaitRegist));
                            break;
                        case QUERYING_AYLA:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_SendConfig));
                            break;
                        case QUERYING_MXPAIR:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_SendConfig));
                            break;
                        default:
                            tv_matchingTips.setText(getResources().getString(R.string.Purifier_RouterConnecting));
                            break;
                    }
                    break;
                case 3:
                    //配对成功
//                    MatchSuccessShow();
                    showChangeName();
                    break;
            }
        }
    }

    class ListAdapter extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener {
        public ListAdapter() {
        }

        @Override
        public int getItemCount() {
            // TODO Auto-generated method stub
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(ViewHolder convertView, final int position) {
            // TODO Auto-generated method stub
            BaseDeviceIO device = (BaseDeviceIO) bindIO;
            if (device != null) {
                if (Mac != null && device.getAddress().equals(Mac)) {
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.water_purifier_selected);
                    convertView.item_selected.setChecked(true);
                } else {
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.water_purifier_small);
                    convertView.item_selected.setChecked(false);
                }
                convertView.Cup_tv_device_item_name.setText(getString(R.string.water_purifier));
                //  viewHolder.item_selected.setChecked(false);
            } else {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.device_null), Toast.LENGTH_SHORT).show();
            }

            convertView.item_selected.setClickable(false);
            convertView.RootView.setOnClickListener(new MyOnClickListener(device.getAddress()));
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

    public void ChangeWidth(int size) {
        if (size >= 3)
            size = 3;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(OznerCommand.dip2px(getBaseContext(), size * 120), ViewGroup.LayoutParams.MATCH_PARENT);
        deviceList.setLayoutParams(layoutParams);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_passwordImg:
                state1++;
                state1 = state1 % 2;
                if (state1 % 2 == 1) {
                    et_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                break;
            case R.id.iv_remainPass:
                boolean pwd = iv_remainPass.isSelected();
                iv_remainPass.setSelected(!pwd);
                break;
            case R.id.btn_ReMatch:
                showSelectWifi();
                break;
        }
    }


    private void weatherRemPassword() {
        String ssid = tv_selectedWifi.getText().toString().trim();
        try {
            editor.putString("password." + ssid, et_password.getText().toString().trim());
        } finally {
            editor.commit();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        initView();
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

package com.ozner.cup.Device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import com.ozner.WaterPurifier.WaterPurifierManager;
import com.ozner.WaterPurifier.WaterPurifier_RO_BLE;
import com.ozner.bluetooth.BluetoothIO;
import com.ozner.bluetooth.BluetoothScan;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.Cup;
import com.ozner.cup.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.NotSupportDeviceException;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.util.ArrayList;


//import com.ozner.bluetooth.BaseBluetoothDeviceManager;

/**
 * Created by taoran on 2016/11/11.
 *
 */
public class MatchROPurifierActivity extends AppCompatActivity {
    ArrayList<BaseDeviceIO> list = new ArrayList<BaseDeviceIO>();
    LinearLayout deviceListLayout, ll_glass_name;
    RelativeLayout ll_restart_matching;
    RecyclerView deviceList;
    ImageView image = null, indeximage, iv_smart_glass;
    TextView toolbarText, et_glass_address, tv_state, matchcup_tv_downside, matchcup_tv_bluetooth;
    Button restartSearch, finish_add_glass;
    EditText et_glass_name, et_weight;
    ListAdapter adapter;
    Monitor mMonitor = new Monitor();
    TimerCount timerCount;
    private String Mac;
    private boolean isSuccesShow = false;
    private int deviceNum = 0;
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
        setContentView(R.layout.activity_match_ro);

        //   deviceListLayout.setVisibility(View.VISIBLE);
        initView();     //初始化View
        searchingDevice();//旋转
        adapter = new ListAdapter();
        deviceList.setAdapter(adapter);
        new UiUpdateAsyncTask().execute();


        animinput = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        animfadeout = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        animfadein = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        animinput.setInterpolator(new DecelerateInterpolator(2.0f));
        animfadeout.setInterpolator(new DecelerateInterpolator(2.0f));
        animfadein.setInterpolator(new DecelerateInterpolator(2.0f));
    }

    private class UiUpdateAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(2000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {

        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {

            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothScan.ACTION_SCANNER_FOUND);
            adapter.Reload();
            getBaseContext().registerReceiver(mMonitor, filter);//启动蓝牙搜索
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {

        }
    }

    private void initView() {
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.add_device);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbarText = (TextView) findViewById(R.id.toolbar_text);
        toolbarText.setText(getString(R.string.match_device));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        image = (ImageView) findViewById(R.id.iv_matching_glass);
        restartSearch = (Button) findViewById(R.id.btn_restart_match);
        restartSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarText.setText(getString(R.string.match_device));
                image.setImageResource(R.drawable.device_add_waiting);
                findViewById(R.id.iv_smart_glass).setVisibility(View.VISIBLE);
                restartSearch.setVisibility(View.GONE);
                ((TextView) findViewById(R.id.tv_match_glass_notice)).setText(getString(R.string.kindly_reminder));
                ((TextView) findViewById(R.id.matchcup_tv_downside)).setText(getString(R.string.match_ro_notice));
                ((TextView) findViewById(R.id.matchcup_tv_bluetooth)).setText(getString(R.string.matching_bluetooth));
                ((TextView) findViewById(R.id.tv_match_glass_notice2)).setText(getString(R.string.open_ro));
                searchingDevice();
            }
        });
        ll_restart_matching = (RelativeLayout) findViewById(R.id.ll_restart_matching);
        deviceListLayout = (LinearLayout) findViewById(R.id.ll_searched_device);
        deviceListLayout.setVisibility(View.GONE);
        deviceList = (RecyclerView) findViewById(R.id.my_recycler_view);
        ll_glass_name = (LinearLayout) findViewById(R.id.ll_glass_name);
        ll_glass_name.setVisibility(View.GONE);
        finish_add_glass = (Button) findViewById(R.id.finish_add_glass);
        et_glass_name = (EditText) findViewById(R.id.et_glass_name);
        et_glass_address = (TextView) findViewById(R.id.et_glass_address);
        et_weight = (EditText) findViewById(R.id.et_weight);
        tv_state = (TextView) findViewById(R.id.tv_state);
        iv_smart_glass = (ImageView) findViewById(R.id.iv_smart_glass);
        matchcup_tv_downside = (TextView) findViewById(R.id.matchcup_tv_downside);
        matchcup_tv_bluetooth = (TextView) findViewById(R.id.matchcup_tv_bluetooth);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        mLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        deviceList.setLayoutManager(mLayoutManager);
        finish_add_glass.setOnClickListener(new View.OnClickListener() {
            //完成
            @Override
            public void onClick(View v) {
                if (list != null && list.size() > 0) {
                    for (BaseDeviceIO io : list) {
                        if (io.getAddress().equals(Mac)) {
                            SaveDevice(io);
                            return;
                        }
                    }
                }
//                Toast.makeText(getBaseContext(),getResources().getString(R.string.device_null),Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void searchingDevice() {
        isSuccesShow = false;
        if (mMonitor == null) {
            IntentFilter filter = new IntentFilter();
//            filter.addAction(OznerApplication.ACTION_ServiceInit);
            filter.addAction(BluetoothScan.ACTION_SCANNER_FOUND);
//            filter.addAction(BaseBluetoothDeviceManager.ACTION_OZNER_BLUETOOTH_BIND_MODE);
            mMonitor = new Monitor();
            this.registerReceiver(mMonitor, filter);
        }

        if (timerCount == null) {
            ll_restart_matching.setVisibility(View.VISIBLE);
            deviceListLayout.setVisibility(View.GONE);

            timerCount = new TimerCount(30000, 1000);
            timerCount.start();
            RotateAnimation animation = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(3000);
            animation.setRepeatCount(9);
            LinearInterpolator li = new LinearInterpolator();
            animation.setInterpolator(li);
            animation.setFillAfter(false);
            image.setAnimation(animation);
        }
    }

    private void stopRotate() {
        if (timerCount != null) {
            timerCount.cancel();
            timerCount = null;
        }
        if (image != null) {
            {
                Animation s = image.getAnimation();
                if (s != null) {
                    s.cancel();
                }
            }
        }
    }

    ///搜索成功显示界面
    private void ShowSerachSuccess() {
        try {
            isSuccesShow = true;
            image.getAnimation().cancel();
            toolbarText.setText(getString(R.string.searched_device));
            image.setImageResource(R.drawable.water_purifier_selected);
            iv_smart_glass.startAnimation(animfadeout);
            matchcup_tv_downside.startAnimation(animfadeout);
            matchcup_tv_bluetooth.startAnimation(animfadeout);
            ll_restart_matching.startAnimation(animfadeout);
            iv_smart_glass.setVisibility(View.GONE);
            matchcup_tv_downside.setVisibility(View.GONE);
            matchcup_tv_bluetooth.setVisibility(View.GONE);
            ll_restart_matching.setVisibility(View.GONE);
            deviceListLayout.setVisibility(View.VISIBLE);
            deviceListLayout.startAnimation(animfadein);
        } catch (Exception ex) {

        }
    }

    ///搜索失败显示界面
    private void ShowSearchFailed() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        anim.setInterpolator(new DecelerateInterpolator(2.0f));
        isSuccesShow = false;
        //搜索设备失败的界面显示
//                toolbarText.setText(getString(R.string.match_failed));
        image.setImageResource(R.drawable.match_device_failed);
        image.startAnimation(animfadeout);
        iv_smart_glass.startAnimation(animfadeout);
        iv_smart_glass.setVisibility(View.GONE);
        restartSearch.setVisibility(View.VISIBLE);
        restartSearch.startAnimation(animfadein);
        deviceListLayout.startAnimation(animfadeout);
        deviceListLayout.setVisibility(View.GONE);
        ((TextView) findViewById(R.id.tv_match_glass_notice)).setText(getString(R.string.common_problem));
        ((TextView) findViewById(R.id.matchcup_tv_downside)).setText(getString(R.string.failed_searching));
        ((TextView) findViewById(R.id.matchcup_tv_bluetooth)).setText(getString(R.string.restart_match));
        ((TextView) findViewById(R.id.tv_match_glass_notice2)).setText(getString(R.string.problem_notice_ro));
    }

    //配对成功显示界面
    private void MatchSuccessShow() {
        toolbarText.setText(getString(R.string.match_successed));
        findViewById(R.id.tv_control1).setVisibility(View.GONE);
        findViewById(R.id.tv_control2).setVisibility(View.VISIBLE);
        tv_state.setVisibility(View.GONE);
        ll_glass_name.setVisibility(View.VISIBLE);
        ll_glass_name.startAnimation(animinput);
        image.setImageResource(R.drawable.match_device_successed);
        image.startAnimation(animfadein);
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
            stopRotate();
            //结束时干什么
            if (deviceNum == 0) {
                ShowSearchFailed();
                try {
                    MatchROPurifierActivity.this.unregisterReceiver(mMonitor);
                    mMonitor = null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                //搜索设备成功的界面显示
                if (!isSuccesShow) {
                    ShowSerachSuccess();

                }
            }
        }
    }

    /*
    * 蓝牙广播数据接收器
    * */
    class Monitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.Reload();
        }
    }

    class ListAdapter extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener {

        public ListAdapter() {
        }

        @Override
        public int getItemCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        private void Reload() {
            //清空设备列表，重新搜索添加设备（智能杯）
            list.clear();
            if (OznerDeviceManager.Instance() != null) {
                BaseDeviceIO[] deviceIOs = null;
                try {
                    deviceIOs = OznerDeviceManager.Instance().getNotBindDevices();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    deviceIOs = null;
                }
                if (deviceIOs != null) {
                    for (BaseDeviceIO device : deviceIOs) {
                        //只添加 智能杯
                        if (WaterPurifierManager.IsWaterPurifier(device.getType())) {
                            if (device instanceof BluetoothIO) {
                                BluetoothIO bluetoothIO = (BluetoothIO) device;
                                bluetoothIO.getFirmware();
                                //检查杯子处于倒置模式
                                if (WaterPurifier_RO_BLE.isBindMode(bluetoothIO))
                                    list.add(device);
                            }
                        }
                    }
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.device_null), Toast.LENGTH_SHORT).show();
                }

            }
            if (deviceNum != list.size() && list.size() != 0) {
                deviceNum = list.size();
                GridLayoutManager mLayoutManager = new GridLayoutManager(getBaseContext(), list.size());
                mLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
                ChangeWidth(list.size());
                adapter.notifyDataSetChanged();
                if (deviceNum > 0) {
                    //IS SUCCESS SHOW 方式函数重复调用
                    if (!isSuccesShow)
                        ShowSerachSuccess();
                } else {
                    //IS SUCCESS SHOW 方式函数重复调用
                    if (isSuccesShow)
                        searchingDevice();
                }
            }


        }

        public void ChangeWidth(int size) {
            if (size >= 3)
                size = 3;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(OznerCommand.dip2px(getBaseContext(), size * 120), ViewGroup.LayoutParams.MATCH_PARENT);
            deviceList.setLayoutParams(layoutParams);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public void onBindViewHolder(ViewHolder convertView, final int position) {
            // TODO Auto-generated method stub
            BaseDeviceIO device = (BaseDeviceIO) list.get(position);

            if (device != null) {
                if (Mac != null && device.getAddress().equals(Mac)) {
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.water_purifier_selected);
                    convertView.item_selected.setChecked(true);
                } else {
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.water_purifier_small);
                    convertView.item_selected.setChecked(false);
                }
                convertView.Cup_tv_device_item_name.setText(getString(R.string.ro_purifier));
                //  viewHolder.item_selected.setChecked(false);
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

    @Override
    protected void onDestroy() {
        try {
            this.unregisterReceiver(mMonitor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            super.onDestroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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

    //GridView Item 点击事件
    class MyOnClickListener implements View.OnClickListener {
        private String device;

        public MyOnClickListener(String address) {
            device = address;
        }

        @Override
        public void onClick(View v) {
            if (device != Mac) {
//                for (int i=0;i<list.size();i++){
//                    if (device.equals(list.get(i).getAddress())){
//
//                    }
//                }
                Mac = device;
                adapter.notifyDataSetChanged();
                MatchSuccessShow();
            }

        }
    }

    private Cup saveCup;
    private BaseDeviceIO baseDeviceIO;

    private void SaveDevice(BaseDeviceIO deviceIO) {

        String glassName = et_glass_name.getText().toString();
        String addr = et_glass_address.getText().toString();

        try {
            //通过找到的蓝牙对象控制对象获取设备对象
            OznerDevice device = OznerDeviceManager.Instance().getDevice(deviceIO);
            if (device != null) {
                //保存设备
                if (glassName.isEmpty()) {
                    device.Setting().name(getString(R.string.ro_purifier));
                } else {
                    device.Setting().name(glassName);
                }
                OznerDeviceManager.Instance().save(device);
                device.setAppdata(PageState.DEVICE_ADDRES, addr);
                //添加网络缓存任务
                OznerCommand.CNetCacheBindDeviceTask(getBaseContext(), device);
                this.finish();
            }
        } catch (NotSupportDeviceException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return;

    }

}

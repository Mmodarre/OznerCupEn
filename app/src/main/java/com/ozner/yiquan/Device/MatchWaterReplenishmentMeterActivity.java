package com.ozner.yiquan.Device;

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

import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeterMgr;
import com.ozner.bluetooth.BluetoothIO;
import com.ozner.bluetooth.BluetoothScan;
import com.ozner.yiquan.Command.OznerCommand;
import com.ozner.yiquan.Command.PageState;
import com.ozner.yiquan.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.NotSupportDeviceException;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.util.ArrayList;

/**
 * Created by mengdongya on 2016/2/26.
 */
public class MatchWaterReplenishmentMeterActivity extends AppCompatActivity implements View.OnClickListener {
    ArrayList<BaseDeviceIO> list = new ArrayList<BaseDeviceIO>();
    LinearLayout deviceListLayout, ll_set_devicename;
    RelativeLayout ll_restart_matching;
    RecyclerView deviceList;
    ImageView image = null, iv_water_replen_meter;
    TextView toolbarText, tv_state, match_replen_tv_downside, match_replen_tv_bluetooth, tv_women, tv_men;
    Button restartSearch, finish_add_device;
    EditText et_device_name;
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
        setContentView(R.layout.activity_waterreplenishment_meter);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_women:
                tv_women.setSelected(true);
                tv_women.setTextColor(getResources().getColor(R.color.white));
                tv_men.setSelected(true);
                tv_men.setTextColor(getResources().getColor(R.color.colorTds));
                break;
            case R.id.tv_men:
                tv_men.setSelected(false);
                tv_men.setTextColor(getResources().getColor(R.color.white));
                tv_women.setSelected(false);
                tv_women.setTextColor(getResources().getColor(R.color.colorTds));
                break;
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

        image = (ImageView) findViewById(R.id.iv_matching_water_replenish_meter);
        restartSearch = (Button) findViewById(R.id.btn_restart_match);
        restartSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarText.setText(getString(R.string.match_device));
                image.setImageResource(R.drawable.device_add_waiting);
                findViewById(R.id.iv_smart_water_replenish_meter).setVisibility(View.VISIBLE);
                restartSearch.setVisibility(View.GONE);
                ((TextView) findViewById(R.id.match_replen_tv_downside)).setText(getString(R.string.match_replenish_meter_notice));
                ((TextView) findViewById(R.id.match_replen_tv_bluetooth)).setText(getString(R.string.matching_bluetooth));
                searchingDevice();
            }
        });
        ll_restart_matching = (RelativeLayout) findViewById(R.id.ll_restart_matching);
        deviceListLayout = (LinearLayout) findViewById(R.id.ll_searched_device);
        deviceListLayout.setVisibility(View.GONE);
        deviceList = (RecyclerView) findViewById(R.id.my_recycler_view);
        ll_set_devicename = (LinearLayout) findViewById(R.id.ll_water_replenish_meter_name);
        ll_set_devicename.setVisibility(View.GONE);
        finish_add_device = (Button) findViewById(R.id.finish_add_water_replenish_meter);
        et_device_name = (EditText) findViewById(R.id.et_water_replenish_meter_name);
        tv_state = (TextView) findViewById(R.id.tv_state);
        tv_women = (TextView) findViewById(R.id.tv_women);
        tv_men = (TextView) findViewById(R.id.tv_men);
        tv_women.setOnClickListener(this);
        tv_men.setOnClickListener(this);
        iv_water_replen_meter = (ImageView) findViewById(R.id.iv_smart_water_replenish_meter);
        match_replen_tv_downside = (TextView) findViewById(R.id.match_replen_tv_downside);
        match_replen_tv_bluetooth = (TextView) findViewById(R.id.match_replen_tv_bluetooth);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        mLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        deviceList.setLayoutManager(mLayoutManager);
        finish_add_device.setOnClickListener(new View.OnClickListener() {
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
            ll_restart_matching.setVisibility(View.INVISIBLE);
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
        isSuccesShow = true;
        if (!image.getAnimation().hasEnded()) {
            image.getAnimation().cancel();
        }
        toolbarText.setText(getString(R.string.match_device));
        image.setImageResource(R.drawable.searched_water_replenish_meter);
        iv_water_replen_meter.startAnimation(animfadeout);
        match_replen_tv_downside.startAnimation(animfadeout);
        match_replen_tv_bluetooth.startAnimation(animfadeout);
        ll_restart_matching.startAnimation(animfadeout);
        iv_water_replen_meter.setVisibility(View.GONE);
        match_replen_tv_downside.setVisibility(View.GONE);
        match_replen_tv_bluetooth.setVisibility(View.GONE);
        ll_restart_matching.setVisibility(View.GONE);
        deviceListLayout.setVisibility(View.VISIBLE);
        deviceListLayout.startAnimation(animfadein);

    }

    ///搜索失败显示界面
    private void ShowSearchFailed() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        anim.setInterpolator(new DecelerateInterpolator(2.0f));
        isSuccesShow = false;
        //搜索设备失败的界面显示
        toolbarText.setText(getString(R.string.match_failed));
        image.setImageResource(R.drawable.match_device_failed);
        image.startAnimation(animfadeout);
        iv_water_replen_meter.startAnimation(animfadeout);
        iv_water_replen_meter.setVisibility(View.GONE);
        restartSearch.setVisibility(View.VISIBLE);
        restartSearch.startAnimation(animfadein);
        deviceListLayout.startAnimation(animfadeout);
        deviceListLayout.setVisibility(View.GONE);
        ll_restart_matching.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.match_replen_tv_downside)).setText(getString(R.string.failed_searching));
        ((TextView) findViewById(R.id.match_replen_tv_bluetooth)).setText(getString(R.string.restart_match));
    }

    //配对成功显示界面
    private void MatchSuccessShow() {
        toolbarText.setText(getString(R.string.match_successed));
        findViewById(R.id.tv_control1).setVisibility(View.GONE);
        findViewById(R.id.tv_control2).setVisibility(View.VISIBLE);
        tv_state.setVisibility(View.GONE);
        ll_set_devicename.setVisibility(View.VISIBLE);
        ll_set_devicename.startAnimation(animinput);
        image.setImageResource(R.drawable.match_device_successed);
        image.startAnimation(animfadein);
        tv_women.setSelected(true);
        tv_men.setSelected(true);
        tv_women.setTextColor(getResources().getColor(R.color.white));
        tv_men.setTextColor(getResources().getColor(R.color.colorTds));
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
                    MatchWaterReplenishmentMeterActivity.this.unregisterReceiver(mMonitor);
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
            //清空设备列表，重新搜索添加设备
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
                        if (WaterReplenishmentMeterMgr.IsWaterReplenishmentMeter(device.getType())) {
                            if (device instanceof BluetoothIO) {
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
                    if (!isSuccesShow) {
                        ShowSerachSuccess();
                    }
                } else {
                    //IS SUCCESS SHOW 方式函数重复调用
                    if (isSuccesShow) {
                        searchingDevice();
                    }
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
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.searched_water_replenish_meter);
                    convertView.item_selected.setChecked(true);
                } else {
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.searched_replenish_meter);
                    convertView.item_selected.setChecked(false);
                }
                convertView.Cup_tv_device_item_name.setText(getString(R.string.water_replen_meter));
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
                Mac = device;
                adapter.notifyDataSetChanged();
                MatchSuccessShow();
            }

        }
    }

    private void SaveDevice(BaseDeviceIO deviceIO) {

        String deviceName = et_device_name.getText().toString();

        try {
            //通过找到的蓝牙对象控制对象获取设备对象
            OznerDevice device = OznerDeviceManager.Instance().getDevice(deviceIO);
            if (device != null) {
                //保存设备
                if (deviceName.isEmpty()) {
                    device.Setting().name(getString(R.string.water_replen_meter));
                } else {
                    device.Setting().name(deviceName);
                }
                OznerDeviceManager.Instance().save(device);
                if (tv_women.isSelected()) {
                    device.setAppdata(PageState.Sex, 0);
                } else {
                    device.setAppdata(PageState.Sex, 1);
                }
                device.updateSettings();
                //添加网络缓存任务
                OznerCommand.CNetCacheBindDeviceTask(getBaseContext(), device);
                this.finish();
            }else{
                Toast.makeText(getBaseContext(),getResources().getString(R.string.device_null), Toast.LENGTH_SHORT).show();
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

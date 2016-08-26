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

import com.ozner.bluetooth.BluetoothIO;
import com.ozner.bluetooth.BluetoothScan;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.NotSupportDeviceException;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.Tap;
import com.ozner.tap.TapManager;

import java.util.ArrayList;

/**
 * Created by taoran on 2016/3/10.
 */
public class MatchTdsPenActivity extends AppCompatActivity {
    Toolbar toolbar = null;
    ArrayList<BaseDeviceIO> list = new ArrayList<BaseDeviceIO>();
    Monitor mMonitor = new Monitor();
    LinearLayout ll_searched_device, ll_probe_info,device_place2;
    RelativeLayout ll_restart_matching,device_place1;
    TextView tv_state, matchprobe_notice, matchcup_tv_bluetooth, toolbarText,tv_match_glass_notice2;
    ListAdapter adapter;
    RecyclerView deviceList;
    Button btn_restart_match, finish_add_device;
    EditText et_glass_name, et_device_position;
    private boolean isSuccesShow = false;
    private int deviceNum = 0;
    ImageView image = null, iv_water_probe;
    TimerCount timerCount;
    Animation animinput, animfadeout, animfadein;
    private String Mac;

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
        setContentView(R.layout.activity_match_probe);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.add_device);
        toolbarText = (TextView) findViewById(R.id.toolbar_text);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        animinput= AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        animfadeout=AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        animfadein=AnimationUtils.loadAnimation(this,R.anim.abc_fade_in);
        animinput.setInterpolator(new DecelerateInterpolator(2.0f));
        animfadeout.setInterpolator(new DecelerateInterpolator(2.0f));
        animfadein.setInterpolator(new DecelerateInterpolator(2.0f));
        InitView();
        StartSearch();

        adapter = new ListAdapter(this);
        try {
            deviceList.setAdapter(adapter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        new UiUpdateAsyncTask().execute();

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
            adapter.Reload();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothScan.ACTION_SCANNER_FOUND);
            getBaseContext().registerReceiver(mMonitor, filter);
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {

        }
    }

    /*
    * 注册蓝牙监听
    * */
    private void RegisterBluetooth() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothScan.ACTION_SCANNER_FOUND);
        this.registerReceiver(mMonitor, filter);
    }

    private void UnRegisterBluetooth() {
        try {
            this.unregisterReceiver(mMonitor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void InitView() {
        OznerApplication.changeTextFont((ViewGroup)getWindow().getDecorView());
        image = (ImageView) findViewById(R.id.iv_matching_probe);
        ll_probe_info = (LinearLayout) findViewById(R.id.ll_probe_info);
        ll_probe_info.setVisibility(View.GONE);
        ll_restart_matching = (RelativeLayout) findViewById(R.id.ll_restart_matching);
        ll_restart_matching.setVisibility(View.INVISIBLE);
        tv_state = (TextView) findViewById(R.id.tv_state);
        tv_match_glass_notice2 = (TextView) findViewById(R.id.tv_match_glass_notice2);
        matchprobe_notice = (TextView) findViewById(R.id.matchprobe_notice);
        matchcup_tv_bluetooth = (TextView) findViewById(R.id.matchcup_tv_bluetooth);
        btn_restart_match = (Button) findViewById(R.id.btn_restart_match);
        finish_add_device = (Button) findViewById(R.id.finish_add_device);
        ll_searched_device = (LinearLayout) findViewById(R.id.ll_searched_device);
        ll_searched_device.setVisibility(View.GONE);
        iv_water_probe = (ImageView) findViewById(R.id.iv_water_probe);
        iv_water_probe.setImageResource(R.drawable.tdspen6);
        deviceList = (RecyclerView) findViewById(R.id.my_recycler_view);
        device_place1 = (RelativeLayout) findViewById(R.id.device_place1);
        device_place1.setVisibility(View.GONE);
        device_place2 = (LinearLayout) findViewById(R.id.device_place2);
        device_place2.setVisibility(View.GONE);
        et_glass_name = (EditText) findViewById(R.id.et_device_name);
        et_glass_name.setHint(R.string.water_tdspen_hit);
        et_device_position = (EditText) findViewById(R.id.et_device_position);
        btn_restart_match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartSearch();
            }
        });
        toolbarText.setText(getString(R.string.match_device));
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        mLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        deviceList.setLayoutManager(mLayoutManager);

        finish_add_device.setOnClickListener(new View.OnClickListener() {
            //完成
            @Override
            public void onClick(View v) {
                if (Mac != null && Mac.length() > 0) {
                    if (list != null && list.size() > 0) {
                        for (BaseDeviceIO io : list) {
                            SaveDevice(io);
                            return;

                        }
                    }
                }
//                Toast.makeText(getBaseContext(), "选中设备已经断开,请重新匹配", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /*
    *停止旋转
    * */
    private void startRotate() {
        StartTime();
        RotateAnimation animation = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(3000);
        animation.setRepeatCount(9);
        animation.setRepeatCount(-1);
        LinearInterpolator li = new LinearInterpolator();
        animation.setInterpolator(li);
        animation.setFillAfter(false);
        image.setAnimation(animation);
    }

    /*
    * 启动旋转
    * */
    private void stopRotate() {
        StopTime();
        if (image != null) {
            {
                Animation s = image.getAnimation();
                if (s != null) {
                    s.cancel();
                }
            }
        }
    }

    /*
    * 结束计时器
    * */
    public void StartTime() {
        if (timerCount == null) {
//            ll_restart_matching.setVisibility(View.VISIBLE);
            timerCount = new TimerCount(30000, 1000);
            timerCount.start();
        } else {
            StopTime();
            StartTime();
        }
    }

    /*
    * 启动计时器
    * */
    public void StopTime() {
        if (timerCount != null) {
            timerCount.cancel();
            timerCount = null;
        }
    }

    /*
    * 开始搜索
    * */
    public void StartSearch() {
        toolbarText.setText(getString(R.string.match_device));
        image.setImageResource(R.drawable.device_add_waiting);
        ll_searched_device.setVisibility(View.GONE);
        ll_restart_matching.setVisibility(View.INVISIBLE);
        iv_water_probe.setVisibility(View.VISIBLE);
        matchprobe_notice.setText(getString(R.string.match_probe_notice));
        matchcup_tv_bluetooth.setText(getString(R.string.matching_bluetooth));
        startRotate();
        RegisterBluetooth();
    }

    /*
    *搜索成功显示界面
    * */
    public void ShowSearchSuccess() {
        isSuccesShow = true;
        ll_restart_matching.startAnimation(animfadeout);
        ll_searched_device.startAnimation(animfadein);
        ll_restart_matching.setVisibility(View.GONE);
        iv_water_probe.setVisibility(View.GONE);
        matchprobe_notice.setVisibility(View.GONE);
        matchcup_tv_bluetooth.setVisibility(View.GONE);
        image.setImageDrawable(getResources().getDrawable(R.drawable.tdspen4));
        ViewGroup.LayoutParams s = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 2f);
        ll_searched_device.setLayoutParams(s);
        ll_searched_device.setVisibility(View.VISIBLE);
        stopRotate();
    }

    /*
    * 搜索失败显示界面
    * */
    public void ShowSearchFailed() {
        isSuccesShow = false;
        toolbarText.setText(getString(R.string.match_failed));
        ll_searched_device.startAnimation(animfadeout);
        ll_restart_matching.startAnimation(animfadein);
        ll_searched_device.setVisibility(View.GONE);
        ll_restart_matching.setVisibility(View.VISIBLE);
        matchprobe_notice.setText(getString(R.string.failed_searching));
        matchcup_tv_bluetooth.setText(getString(R.string.restart_match));
        image.setImageResource(R.drawable.match_device_failed);
        tv_match_glass_notice2.setText(getResources().getString(R.string.problem_notice_probe2));
        iv_water_probe.setVisibility(View.GONE);
        stopRotate();
    }

    /*
    * 显示输入信息
    * */

    private void MatchSuccessShow() {
        toolbarText.setText(getString(R.string.match_successed));
        tv_state.setVisibility(View.GONE);
        findViewById(R.id.tv_control1).setVisibility(View.GONE);
        findViewById(R.id.tv_control2).setVisibility(View.VISIBLE);
        ll_probe_info.startAnimation(animinput);
        image.startAnimation(animfadein);
        ll_probe_info.setVisibility(View.VISIBLE);
        image.setImageResource(R.drawable.match_device_successed);
    }

    class TimerCount extends CountDownTimer {
        public TimerCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            if (deviceNum > 0) {
                if (!isSuccesShow) {
                    ShowSearchSuccess();
                }

            } else {
                ShowSearchFailed();
                UnRegisterBluetooth();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UnRegisterBluetooth();
        finish();
    }

    class Monitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.Reload();
        }
    }

    class ListAdapter extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener {
        Context mContext;
        LayoutInflater mInflater;
        ViewHolder viewHolder;

        public ListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
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
                        //只添加 tdspen
                        if (TapManager.IsTap(device.getType())) {
                            if (device instanceof BluetoothIO) {
                                BluetoothIO bluetoothIO = (BluetoothIO) device;
                                //检查水探头处于start模式
                                if (Tap.isBindMode(bluetoothIO))
                                    list.add(device);
                            }
                        }
                    }
                }
            }
            if (deviceNum != list.size() && list.size() != 0) {
                deviceNum = list.size();
                GridLayoutManager mLayoutManager = new GridLayoutManager(getBaseContext(), list.size());
                mLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
                ChangeWidth(list.size());
                adapter.notifyDataSetChanged();
                if (list.size() > 0) {
                    //IS SUCCESS SHOW 方式函数重复调用
                    ShowSearchSuccess();
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
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public int getItemCount() {
            // TODO Auto-generated method stub
            return list.size();
        }
        @Override
        public void onBindViewHolder(ViewHolder convertView, final int position) {
            // TODO Auto-generated method stub
            BaseDeviceIO device = (BaseDeviceIO) list.get(position);
            if (device != null) {
                if(Mac!=null&&device.getAddress().equals(Mac)) {
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.tdspen4);
                    convertView.Cup_iv_device_item_image.setAlpha(1.0f);
                    convertView.item_selected.setChecked(true);
                }
                else {
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.tdspen4);
                    convertView.Cup_iv_device_item_image.setAlpha(0.6f);
                    convertView.item_selected.setChecked(false);
                }
                convertView.Cup_tv_device_item_name.setText(getString(R.string.water_tdspen));
            }
            convertView.RootView.setClickable(false);
            convertView.RootView.setOnClickListener(new MyOnClickListener(device.getAddress()));
            //IS SUCCESS SHOW 防止函数重复调用
            if (!isSuccesShow) {
                if (timerCount != null) {
                    timerCount.cancel();
                    timerCount = null;
                }
                ShowSearchSuccess();
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
            // TODO Auto-generated method stub
            View itemLayout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.device_list_item, null);
            return new ViewHolder(itemLayout);
        }

        @Override
        public void onClick(View v) {}
    }
    //把图片加给选择的item

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView Cup_iv_device_item_image; //图片
        TextView Cup_tv_device_item_name;   //名字
        RadioButton item_selected;//设备是否被选中
        View RootView;

        public ViewHolder(View v) {
            super(v);
            this.RootView = v;
            this.Cup_iv_device_item_image = (ImageView) v.findViewById(R.id.iv_device_item_image);
            this.Cup_tv_device_item_name = (TextView) v.findViewById(R.id.tv_device_item_name);
            this.item_selected = (RadioButton)v.findViewById(R.id.item_selected);
        }
    }

    //GridView Item 点击事件
    class MyOnClickListener implements View.OnClickListener {
        private String  device;
        public MyOnClickListener(String address) {
            device=address;
        }

        @Override
        public void onClick(View v) {
            if(device!=Mac)
            {
                Mac=device;
                adapter.notifyDataSetChanged();
                MatchSuccessShow();
            }

        }
    }

    private void SaveDevice(BaseDeviceIO deviceIO) {
        String name = et_glass_name.getText().toString();
//        String address = et_device_position.getText().toString();
        try {
            //通过找到的蓝牙对象控制对象获取设备对象
            OznerDevice device = OznerDeviceManager.Instance().getDevice(deviceIO);
            //device有可能为空
            if (device != null && TapManager.IsTap(device.Type())) {
                OznerDeviceManager.Instance().save(device);
                //保存设备
                if (name.isEmpty()){
                    device.Setting().name(getString(R.string.water_tdspen));
                }else {
                    device.Setting().name(name);
                }

                device.setAppdata(PageState.TapType,"pen");
//                device.setAppdata(PageState.DEVICE_ADDRES, address);
                device.updateSettings();
                //添加网络缓存任务
                OznerCommand.CNetCacheBindDeviceTask(getBaseContext(), device);
            }else {
                Toast.makeText(getBaseContext(),getResources().getString(R.string.device_null),Toast.LENGTH_SHORT).show();
            }
        } catch (NotSupportDeviceException e) {
            e.printStackTrace();
        }
        this.finish();
        return;
    }
}

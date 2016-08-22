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

import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.AirPurifier.AirPurifier_Bluetooth;
import com.ozner.bluetooth.BluetoothScan;
import com.ozner.yiquan.Command.OznerCommand;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.NotSupportDeviceException;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import com.ozner.yiquan.Command.PageState;
import com.ozner.yiquan.R;

import java.util.ArrayList;

/**
 * Created by mengdongya on 2015/12/22.
 * 台式空净
 */
public class MatchAirPuriTaiActivity extends AppCompatActivity implements View.OnClickListener {

    ArrayList<BaseDeviceIO> list = new ArrayList<BaseDeviceIO>();
    LinearLayout ll_edit_devicename_place, ll_searched_device,device_place2;
    RelativeLayout rl_restart_matching,device_place1;
    RecyclerView deviceList;
    ImageView image = null, indeximage,iv_show_device_place2,iv_air_tai;
    TextView toolbarText, tv_state, tv_matchair_notice, tv_matchair_bluetooth;
    Button restartSearch, finish_add_device;
    EditText et_device_name,et_device_position;
    ListAdapter adapter;
    Monitor mMonitor = new Monitor();
    TimerCount timerCount;
    private boolean isSuccesShow = false;
    private int deviceNum = 0;
    private String Mac;
    Animation animinput,animfadeout,animfadein;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(OznerApplication.ACTION_ServiceInit);
        filter.addAction(BluetoothScan.ACTION_SCANNER_FOUND);
        //      filter.addAction(BaseBluetoothDeviceManager.ACTION_OZNER_BLUETOOTH_BIND_MODE);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.add_device));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.add_device));
        }
        setContentView(R.layout.activity_match_airtai);

        //   ll_searched_device.setVisibility(View.VISIBLE);
        initView();     //初始化View
        searchingDevice();//旋转
        adapter = new ListAdapter(this);
        deviceList.setAdapter(adapter);
        new UiUpdateAsyncTask().execute();

        animinput= AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        animfadeout=AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        animfadein=AnimationUtils.loadAnimation(this,R.anim.abc_fade_in);
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
            try{
                Thread.sleep(2000);
            }catch (Exception ex){ex.printStackTrace();}
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
            filter.addAction(OznerApplication.ACTION_ServiceInit);
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
        OznerApplication.changeTextFont((ViewGroup)getWindow().getDecorView());
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

        image = (ImageView) findViewById(R.id.iv_matching_air_tai);
        iv_air_tai = (ImageView) findViewById(R.id.iv_air_tai);
        restartSearch = (Button) findViewById(R.id.btn_restart_match);

        rl_restart_matching = (RelativeLayout) findViewById(R.id.rl_restart_matching);//重新配对
        rl_restart_matching.setVisibility(View.INVISIBLE);
        ll_searched_device = (LinearLayout) findViewById(R.id.ll_searched_device);//搜索到设备
        ll_searched_device.setVisibility(View.GONE);
        deviceList = (RecyclerView) findViewById(R.id.my_recycler_view);
        ll_edit_devicename_place = (LinearLayout) findViewById(R.id.ll_edit_devicename_place);
        ll_edit_devicename_place.setVisibility(View.GONE);
        finish_add_device = (Button) findViewById(R.id.finish_add_device);
        et_device_name = (EditText) findViewById(R.id.et_device_name);
        et_device_name.setHint(getString(R.string.edit_airpurifier_name));
        et_device_position = (EditText) findViewById(R.id.et_device_position);
        et_device_position.setText(getString(R.string.living_room));
        tv_state = (TextView) findViewById(R.id.tv_state);
        tv_matchair_notice = (TextView) findViewById(R.id.tv_matchair_notice);
        tv_matchair_bluetooth = (TextView) findViewById(R.id.tv_matchair_bluetooth);
        device_place1 = (RelativeLayout) findViewById(R.id.device_place1);
        device_place2 = (LinearLayout)findViewById(R.id.device_place2);
        iv_show_device_place2 = (ImageView)findViewById(R.id.iv_show_device_place2);
        device_place1.setOnClickListener(this);
        device_place2.setOnClickListener(this);
        restartSearch.setOnClickListener(this);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this,1);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_restart_match:
                restartSearch.setVisibility(View.INVISIBLE);
                findViewById(R.id.ll_iv_tv).setVisibility(View.INVISIBLE);
                findViewById(R.id.tv_match_purifier_notice2).setVisibility(View.INVISIBLE);
                toolbarText.setText(getString(R.string.match_device));
                image.setImageResource(R.drawable.device_add_waiting);
                iv_air_tai.setVisibility(View.VISIBLE);
                tv_matchair_notice.setVisibility(View.GONE);
                tv_matchair_bluetooth.setText(getString(R.string.matching_bluetooth));
                searchingDevice();
                break;
            case R.id.device_place1:
                if (device_place2.getVisibility() == View.VISIBLE){
                    device_place2.setVisibility(View.INVISIBLE);
                    iv_show_device_place2.setSelected(false);
                }else {
                    device_place2.setVisibility(View.VISIBLE);
                    et_device_position.setText(getString(R.string.living_room));
                    iv_show_device_place2.setSelected(true);
                }
                break;
            case R.id.device_place2:
                device_place2.setVisibility(View.INVISIBLE);
                et_device_position.setText(getString(R.string.bedroom));
                iv_show_device_place2.setSelected(false);
                break;
        }
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
            rl_restart_matching.setVisibility(View.INVISIBLE);
            ll_searched_device.setVisibility(View.GONE);

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
        if (image.getAnimation()!=null&&!image.getAnimation().hasEnded()&&image !=null) {
            image.getAnimation().cancel();
        }
        image.setImageResource(R.drawable.air_purifier_tai);
        iv_air_tai.startAnimation(animfadeout);
        tv_matchair_notice.startAnimation(animfadeout);
        tv_matchair_bluetooth.startAnimation(animfadeout);
        rl_restart_matching.startAnimation(animfadeout);
        iv_air_tai.setVisibility(View.GONE);
        tv_matchair_notice.setVisibility(View.GONE);
        tv_matchair_bluetooth.setVisibility(View.GONE);
        rl_restart_matching.setVisibility(View.GONE);
        ll_searched_device.setVisibility(View.VISIBLE);
        ll_searched_device.startAnimation(animfadein);
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
        iv_air_tai.startAnimation(animfadeout);
        iv_air_tai.setVisibility(View.GONE);
        ll_searched_device.startAnimation(animfadeout);
        ll_searched_device.setVisibility(View.GONE);
        rl_restart_matching.setVisibility(View.VISIBLE);
        tv_matchair_notice.setText(getString(R.string.failed_searching));
        tv_matchair_notice.setVisibility(View.VISIBLE);
        tv_matchair_bluetooth.setText(getString(R.string.restart_match));
        restartSearch.setVisibility(View.VISIBLE);
        restartSearch.startAnimation(animfadein);
        findViewById(R.id.ll_iv_tv).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_match_purifier_notice2).setVisibility(View.VISIBLE);
    }

    //配对成功显示界面
    private void MatchSuccessShow() {
        toolbarText.setText(getString(R.string.match_successed));
        ll_edit_devicename_place.setVisibility(View.VISIBLE);
        ll_edit_devicename_place.startAnimation(animinput);
        tv_state.setVisibility(View.GONE);
        findViewById(R.id.tv_control1).setVisibility(View.GONE);
        findViewById(R.id.tv_control2).setVisibility(View.VISIBLE);
        et_device_position.setText(getString(R.string.living_room));
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
                    MatchAirPuriTaiActivity.this.unregisterReceiver(mMonitor);
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
        Context mContext;
        LayoutInflater mInflater;
        ViewHolder viewHolder;

        public ListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        private void Reload() {
            //清空设备列表，重新搜索添加设备台式空净
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
                        //只添加 台式空净   是不是配对模式
                        if (AirPurifierManager.IsBluetoothAirPurifier(device.getType())&& OznerDeviceManager.Instance().checkisBindMode(device)) {
                            list.add(device);
                        }
                    }
                }
            }
            if (deviceNum != list.size() && list.size() > 0) {
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
            if(size>=3)
                size=3;
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(OznerCommand.dip2px(getBaseContext(), size * 120), ViewGroup.LayoutParams.MATCH_PARENT);
            deviceList.setLayoutParams(layoutParams);
        }
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }
        @Override
        public void onBindViewHolder(ViewHolder convertView, final int position) {
            // TODO Auto-generated method stub
            BaseDeviceIO device = (BaseDeviceIO) list.get(position);

            if (device != null) {
                if(Mac!=null&&device.getAddress().equals(Mac)) {
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.air_purifier_selected);
                    convertView.item_selected.setChecked(true);
                }else {
                    convertView.Cup_iv_device_item_image.setImageResource(R.drawable.air_purifier_tai_small);
                    convertView.item_selected.setChecked(false);
                }
                convertView.Cup_tv_device_item_name.setText(getString(R.string.air_puri_taishi));
            }

            convertView.RootView.setClickable(false);
            convertView.RootView.setOnClickListener(new MyOnClickListener(device.getAddress()));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
            // TODO Auto-generated method stub
            View itemLayout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.device_list_item, null);
            return new ViewHolder(itemLayout);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public void onClick(View v) {
        }
    }

    @Override
    protected void onDestroy() {
//        if(image.getAnimation().hasStarted()){
//            image.getAnimation().cancel();
//        }
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
            this.RootView=v;
            this.Cup_iv_device_item_image= (ImageView) v.findViewById(R.id.iv_device_item_image);
            this.Cup_tv_device_item_name= (TextView) v.findViewById(R.id.tv_device_item_name);
            this.item_selected= (RadioButton) v.findViewById(R.id.item_selected);
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
        String name = et_device_name.getText().toString();
        String addr = et_device_position.getText().toString();
        try {
            //通过找到的蓝牙对象控制对象获取设备对象
            OznerDevice device = OznerDeviceManager.Instance().getDevice(deviceIO);

            if (device != null && device instanceof AirPurifier_Bluetooth) {
                //保存设备
                if (name.isEmpty()){
                    device.Setting().name(getString(R.string.my_air_purifier_tai));
                }else {
                    device.Setting().name(name);
                }

                OznerDeviceManager.Instance().save(device);
                device.setAppdata(PageState.DEVICE_ADDRES, addr);
                device.updateSettings();
                //添加网络缓存任务
                OznerCommand.CNetCacheBindDeviceTask(getBaseContext(),device);
            }else{
                Toast.makeText(getBaseContext(),getResources().getString(R.string.device_null),Toast.LENGTH_SHORT).show();
            }
        } catch (NotSupportDeviceException e) {
            e.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
            Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
        }
        this.finish();
        return;

    }

}

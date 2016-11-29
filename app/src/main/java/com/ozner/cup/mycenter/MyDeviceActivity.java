package com.ozner.cup.mycenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifier_Bluetooth;
import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Cup;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.Tap;

public class MyDeviceActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    GridView gv_myDevice;
    //    RelativeLayout rlay_back;
    OznerDevice[] devicelist = new OznerDevice[]{};
    DeviceAdapter myDeviceAdatper;
    private TextView toolbar_text;
    private LinearLayout llay_deviceGrid;
    private RelativeLayout rlay_deviceNone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_device);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
//        if (android.os.Build.VERSION.SDK_INT >= 21) {
//            Window window = getWindow();
//            //更改状态栏颜色
//            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
//            //更改底部导航栏颜色(限有底部的手机)
//            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
//        }

        myDeviceAdatper = new DeviceAdapter(MyDeviceActivity.this);
//        rlay_back = (RelativeLayout) findViewById(R.id.rlay_back);
        gv_myDevice = (GridView) findViewById(R.id.gv_myDevice);
        llay_deviceGrid = (LinearLayout) findViewById(R.id.llay_deviceGrid);
        rlay_deviceNone = (RelativeLayout) findViewById(R.id.rlay_deviceNone);
//        rlay_back.setOnClickListener(this);
        gv_myDevice.setAdapter(myDeviceAdatper);
        gv_myDevice.setOnItemClickListener(this);

        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_text.setText(getString(R.string.Center_HadDevice));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (android.os.Build.VERSION.SDK_INT >= 23) {
//            toolbar.setBackgroundColor(getResources().getColor(R.color.MyCenter_ToolBar, null));
//        } else {
//            toolbar.setBackgroundColor(getResources().getColor(R.color.MyCenter_ToolBar));
//        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setResult(12);
                MyDeviceActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        String userid = UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.UserId, null);
        if (userid != null && userid.length() > 0) {
            devicelist = OznerDeviceManager.Instance().getDevices();
            myDeviceAdatper.clear();
            myDeviceAdatper.addAll(devicelist);
        }
        if (devicelist.length > 0) {
            rlay_deviceNone.setVisibility(View.GONE);
            llay_deviceGrid.setVisibility(View.VISIBLE);
        } else {
            rlay_deviceNone.setVisibility(View.VISIBLE);
            llay_deviceGrid.setVisibility(View.GONE);
        }
        super.onResume();
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.rlay_back:
//                onBackPressed();
//                break;
//        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OznerDevice device = devicelist[position];
        Intent intent = new Intent();
        intent.putExtra(PageState.CENTER_DEVICE_ADDRESS, device.Address());
        setResult(PageState.CenterDeviceClick, intent);
        this.finish();
    }


    class DeviceAdapter extends ArrayAdapter<OznerDevice> {
        private LayoutInflater mInflater;
        private Context mContext;

        public DeviceAdapter(Context context) {
            super(context, R.layout.center_mydevice_grideview_item);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.center_mydevice_grideview_item, null);
                OznerApplication.changeTextFont((ViewGroup) convertView);
                viewHolder.iv_deviceImg = (ImageView) convertView.findViewById(R.id.iv_deviceImg);
                viewHolder.tv_tips = (TextView) convertView.findViewById(R.id.tv_tips);
                viewHolder.tv_deviceName = (TextView) convertView.findViewById(R.id.tv_deviceName);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            OznerDevice device = getItem(position);
            viewHolder.tv_deviceName.setText((device.getName() != null && device.getName().length() > 0) ? device.getName() : device.Address());
            if (device instanceof Cup) {//智能水杯
                Cup myCup = (Cup) device;
                if (myCup.connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.my_center_cup);
                } else {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.my_center_cup_gray);
                }
            } else if (device instanceof Tap) {//水探头
                Tap myTap = (Tap) device;
                if (myTap.connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.my_center_tap);
                } else {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.my_center_tap_gray);
                }
            } else if (device instanceof WaterPurifier) {//净水器
                WaterPurifier mywaterPur = (WaterPurifier) device;
                if (mywaterPur.connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.my_center_purifier);
                } else {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.my_center_purifier_gray);
                }
            } else if (device instanceof AirPurifier_Bluetooth) {//台式空气净化器
                AirPurifier_Bluetooth airPurifierBlue = (AirPurifier_Bluetooth) device;
                if (airPurifierBlue.connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.air_slide_tai_selected);
                } else {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.my_center_air_desk_gray);
                }
            } else if (device instanceof AirPurifier_MXChip) {//立式空气净化器
                AirPurifier_MXChip airPurifier_MX = (AirPurifier_MXChip) device;
                if (airPurifier_MX.connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.air_slide_ver_selected);
                } else {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.my_center_air_ver_gray);
                }
            }else if (device instanceof WaterReplenishmentMeter) {//智能补水仪
                WaterReplenishmentMeter waterReplenishmentMeter = (WaterReplenishmentMeter)device;
                if (waterReplenishmentMeter.connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.match_water_replen_meter);
                } else {
                    viewHolder.iv_deviceImg.setImageResource(R.drawable.my_center_wrm_gray);
                }
            }
            return convertView;
        }

        class ViewHolder {
            public TextView tv_tips;
            public ImageView iv_deviceImg;
            public TextView tv_deviceName;
        }
    }
}

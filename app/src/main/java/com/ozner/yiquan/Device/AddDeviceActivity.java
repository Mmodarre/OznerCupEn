package com.ozner.yiquan.Device;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ozner.yiquan.Command.CustomToast;
import com.ozner.yiquan.R;

/**
 * Created by mengdongya on 2015/11/20.
 * Modify by C-sir@hotmail.com
 */
public class AddDeviceActivity extends AppCompatActivity {
    //        private int deviceImages[] = {R.drawable.smart_glass, R.drawable.water_probe, R.drawable.water_purifier,R.drawable.air_purifier_vertical,R.drawable.air_purifier_taishi };
//    private int deviceImages[] = {R.drawable.smart_glass, R.drawable.water_probe, R.drawable.tdspen, R.drawable.water_purifier,R.drawable.air_purifier_vertical,R.drawable.air_purifier_taishi,R.drawable.water_replenishment_meter };
<<<<<<< HEAD
    private int deviceImages[] = {R.drawable.water_probe, R.drawable.tdspen, R.drawable.water_purifier, R.drawable.air_purifier_vertical, R.drawable.air_purifier_taishi};

    //        private int deviceNames[] = {R.string.smart_glass, R.string.water_probe, R.string.water_purifier,R.string.air_purifier_ver,R.string.air_purifier_taishi};
//    private int deviceNames[] = {R.string.smart_glass, R.string.water_probe, R.string.water_tdspen, R.string.water_purifier,R.string.air_purifier_ver,R.string.air_purifier_taishi,R.string.water_replen_meter};
    private int deviceNames[] = {R.string.water_probe, R.string.water_tdspen, R.string.water_purifier, R.string.air_purifier_ver, R.string.air_purifier_taishi};
=======

    //依泉需要的设备
    private int deviceImages[] = {R.drawable.water_probe, R.drawable.water_purifier, R.drawable.air_purifier_vertical, R.drawable.water_replenishment_meter};

    //        private int deviceNames[] = {R.string.smart_glass, R.string.water_probe, R.string.water_purifier,R.string.air_purifier_ver,R.string.air_purifier_taishi};
//    private int deviceNames[] = {R.string.smart_glass, R.string.water_probe, R.string.water_tdspen, R.string.water_purifier,R.string.air_purifier_ver,R.string.air_purifier_taishi,R.string.water_replen_meter};
    private int deviceNames[] = {R.string.water_probe, R.string.water_purifier, R.string.air_purifier_ver, R.string.water_replen_meter};
>>>>>>> ozner_uriage

    //        private int connectionIcon[] = {R.drawable.bluetooth, R.drawable.bluetooth, R.drawable.wifi, R.drawable.wifi, R.drawable.bluetooth};
//    private int connectionIcon[] = {R.drawable.bluetooth, R.drawable.bluetooth,R.drawable.bluetooth, R.drawable.wifi, R.drawable.wifi, R.drawable.bluetooth, R.drawable.bluetooth};
    private int connectionIcon[] = {R.drawable.bluetooth, R.drawable.bluetooth, R.drawable.wifi, R.drawable.wifi, R.drawable.bluetooth};

    //        private int connectionName[] = {R.string.bluetooth_connection, R.string.bluetooth_connection, R.string.wifi_connection, R.string.wifi_connection,R.string.bluetooth_connection};
//    private int connectionName[] = {R.string.bluetooth_connection, R.string.bluetooth_connection, R.string.bluetooth_connection, R.string.wifi_connection, R.string.wifi_connection,R.string.bluetooth_connection,R.string.bluetooth_connection};
    private int connectionName[] = {R.string.bluetooth_connection, R.string.bluetooth_connection, R.string.wifi_connection, R.string.wifi_connection, R.string.bluetooth_connection};

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
        setContentView(R.layout.activity_add_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.add_device);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((TextView) findViewById(R.id.toolbar_text)).setText(getString(R.string.select_device));
        findViewById(R.id.toolbar_save).setVisibility(View.INVISIBLE);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDeviceActivity.this.finish();
            }
        });

        ListView deviceList = (ListView) findViewById(R.id.adddevice_lv_device);
        deviceList.setAdapter(new DeviceAdapter());
        deviceList.setOnItemClickListener(new myListener());

        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
    }

    private class myListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent();
            switch (position) {

                //                case 0:
//                    intent.setClass(AddDeviceActivity.this, MatchCupActivity.class);
//                    break;
                case 0:
                    intent.setClass(AddDeviceActivity.this, MatchProbeActivity.class);
                    break;
//                case 1:
//                    intent.setClass(AddDeviceActivity.this, MatchTdsPenActivity.class);
//                    break;
                case 1:
                    intent.setClass(AddDeviceActivity.this, MatchPurifierActivity.class);
                    break;
                case 2:
                    intent.setClass(AddDeviceActivity.this, MatchAirPuriVerActivity.class);
                    break;
                case 3:
//                    intent.setClass(AddDeviceActivity.this, MatchAirPuriTaiActivity.class);
                    intent.setClass(AddDeviceActivity.this, MatchWaterReplenishmentMeterActivity.class);
                    break;
//                case 5:
//                    intent.setClass(AddDeviceActivity.this, MatchWaterReplenishmentMeterActivity.class);
//                    break;
                default:
                    break;












////                case 0:
////                    intent.setClass(AddDeviceActivity.this, MatchCupActivity.class);
////                    break;
//                case 0:
//                    intent.setClass(AddDeviceActivity.this, MatchProbeActivity.class);
//                    break;
//                case 1:
//                    intent.setClass(AddDeviceActivity.this, MatchTdsPenActivity.class);
//                    break;
//                case 2:
//                    intent.setClass(AddDeviceActivity.this, MatchPurifierActivity.class);
//                    break;
//                case 3:
//                    intent.setClass(AddDeviceActivity.this, MatchAirPuriVerActivity.class);
//                    break;
//                case 4:
//                    intent.setClass(AddDeviceActivity.this, MatchAirPuriTaiActivity.class);
//                    break;
//                case 5:
//                    intent.setClass(AddDeviceActivity.this, MatchWaterReplenishmentMeterActivity.class);
//                    break;
//                default:
//                    break;













            }
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                AddDeviceActivity.this.finish();
            } else {
                CustomToast.showToastCenter(AddDeviceActivity.this, "没有找到要启动的页面");
            }
        }
    }

    private class DeviceAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return deviceNames.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.add_device_item, null);
                OznerApplication.changeTextFont((ViewGroup) convertView);
            }
            holder.deviceImage = (ImageView) convertView.findViewById(R.id.lv_device);
            holder.deviceName = (TextView) convertView.findViewById(R.id.tv_device_name);
            holder.connnettionIcon = (ImageView) convertView.findViewById(R.id.iv_connection_icon);
            holder.connectionText = (TextView) convertView.findViewById(R.id.tv_connection_text);
            holder.deviceImage.setImageResource(deviceImages[position]);
            holder.deviceName.setText(deviceNames[position]);
            holder.connnettionIcon.setImageResource(connectionIcon[position]);
            holder.connectionText.setText(connectionName[position]);
            if (!((OznerApplication) getApplication()).isLanguageCN()) {
                holder.connectionText.setText("");
            }

            return convertView;
        }
    }

    public class ViewHolder {
        public ImageView deviceImage;
        public TextView deviceName;
        public ImageView connnettionIcon;
        public TextView connectionText;
    }
}

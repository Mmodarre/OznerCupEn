package com.ozner.yiquan.Device;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifier;
import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.cup.Cup;
import com.ozner.cup.CupSetting;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.Tap;
import com.ozner.tap.TapSetting;

import com.ozner.yiquan.Command.PageState;
import com.ozner.yiquan.R;

import java.util.ArrayList;

/**
 * Created by mengdongya on 2015/12/2.
 */
public class SetDeviceName extends AppCompatActivity implements View.OnClickListener {

    ArrayList<String> list = new ArrayList<String>();
    ListView listView;
    ListAdapter adapter;
    TextView cupNameDisplay, toolbar_text, tv_setName_show_addr;
    SharedPreferences sh;
    SharedPreferences.Editor editor;
    String Mac;
    Cup mCup = null;
    CupSetting mCupSetting = null;
    int index = -1;
    Tap mTap = null;
    TapSetting mTapSetting = null;
    WaterPurifier mWaterPurifier = null;
    AirPurifier airPurifier = null;
//    WaterReplenishmentMeter waterReplenishmentMeter = null;
    OznerDevice device = null;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mac = getIntent().getStringExtra("MAC");
        device = OznerDeviceManager.Instance().getDevice(Mac);
        setContentView(R.layout.set_device_name);
        OznerApplication.changeTextFont((ViewGroup)getWindow().getDecorView());
        if (device instanceof Cup) {
            mCup = (Cup) device;
            mCupSetting = mCup.Setting();
            sh = this.getSharedPreferences("cupNames", Context.MODE_PRIVATE);
            list.add(getString(R.string.my_cup));
            list.add(getString(R.string.family_cup));
            list.add(getString(R.string.friend_cup));
        } else if (device instanceof Tap) {
            mTap = (Tap) device;
            mTapSetting = mTap.Setting();
            sh = this.getSharedPreferences("tapNames", Context.MODE_PRIVATE);
            list.add(getString(R.string.home));
            list.add(getString(R.string.office));
        } else if (device instanceof WaterPurifier) {
            mWaterPurifier = (WaterPurifier) device;
            sh = this.getSharedPreferences("waterPurifierNames", Context.MODE_PRIVATE);
            list.add(getString(R.string.home));
            list.add(getString(R.string.office));
        } else if (device instanceof AirPurifier) {
            airPurifier = (AirPurifier) device;
            sh = this.getSharedPreferences("airPurifierNames", Context.MODE_PRIVATE);
            list.add(getString(R.string.living_room));
            list.add(getString(R.string.bedroom));
        }
//        else if (device instanceof WaterReplenishmentMeter){
//            waterReplenishmentMeter = (WaterReplenishmentMeter)device;
//            sh = this.getSharedPreferences("waterReplenishMeterNames", Context.MODE_PRIVATE);
//            list.add(getString(R.string.office));
//            list.add(getString(R.string.home));
//        }

        editor = sh.edit();
        cupNameDisplay = (TextView) findViewById(R.id.cup_name_display);
        toolbar_text = ((TextView) findViewById(R.id.toolbar_text));
        tv_setName_show_addr = ((TextView) findViewById(R.id.tv_setName_show_addr));

        //把Name列表加入list
//        Map mapList = sh.getAll();
//        for (Object value : mapList.values()) {
//            list.add((String) value);
//        }

        try {
            initView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.toolbar_save).setVisibility(View.VISIBLE);
        adapter = new ListAdapter(this);
        cupNameDisplay.setText(device.getName());
        if (device instanceof Cup) {
            toolbar_text.setText(getString(R.string.my_smart_glass));
            tv_setName_show_addr.setText(getString(R.string.tv_setName_cup));
        } else if (device instanceof Tap) {
            toolbar_text.setText(getString(R.string.my_water_probe));
            tv_setName_show_addr.setText(getString(R.string.tv_setName_p));
        } else if (device instanceof WaterPurifier) {
            toolbar_text.setText(getString(R.string.my_water_purifier));
            tv_setName_show_addr.setText(getString(R.string.tv_setName_p));
        } else if (device instanceof AirPurifier) {
            tv_setName_show_addr.setText(getString(R.string.tv_setName_cup));
            if (AirPurifierManager.IsWifiAirPurifier(airPurifier.Type())) {
                toolbar_text.setText(getString(R.string.my_air_purifier_ver));
            } else {
                toolbar_text.setText(getString(R.string.my_air_purifier_tai));
            }
        }

        listView = (ListView) findViewById(R.id.lv_set_cupname);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemsCanFocus(false);
        try {
            Object a = device.getAppValue(PageState.DEVICE_ADDRES);
            if (a != null) {
                address = a.toString();
                if (address != null) {
                    for (int i = 0; i < list.size(); i++) {
                        if (address.equals(list.get(i))) {
                            listView.setItemChecked(i, true);
                        } else {
                            listView.setItemChecked(i, false);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        findViewById(R.id.edit_cup_name).setOnClickListener(this);
//        findViewById(R.id.add_cup_name).setOnClickListener(this);
//        findViewById(R.id.add_name_list).setOnClickListener(this);
        findViewById(R.id.toolbar_save).setOnClickListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                cupNameDisplay.setText(device.getName()+"("+list.get(position)+")");
                listView.setItemChecked(position, true);
                for (int i = 0; i < list.size(); i++) {
                    if (i != position) {
                        listView.setItemChecked(i, false);
                    }
                }
            }
        });

//        deleteName();
    }

    //长按删除
    private void deleteName() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                index = position;
                new AlertDialog.Builder(SetDeviceName.this).setMessage(getString(R.string.set_cup_name_notice1))
                        .setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                list.remove(index);
                                editor.remove(position + 1 + "");
                                editor.commit();
                                initView();
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                return true;
            }
        });

    }

    private void submit() {
        int posi = listView.getCheckedItemPosition();
        if (posi >= 0) {
            String addr = list.get(posi);
            device.setAppdata(PageState.DEVICE_ADDRES, addr);
        }
        device.Setting().name(cupNameDisplay.getText().toString());
        device.updateSettings();
        SetDeviceName.this.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_save:
                new AlertDialog.Builder(this).setTitle("").setMessage(getString(R.string.weather_save_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                submit();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                break;
        }

    }

    private class ListAdapter extends ArrayAdapter<CheckedTextView> {

        CheckedTextView textView = new CheckedTextView(getContext());
        LayoutInflater layoutInflater;

        public ListAdapter(Context context) {
            super(context, R.layout.edit_cup_name_item);
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public CheckedTextView getItem(int position) {
            textView.setText(list.get(position));
            return textView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.edit_cup_name_item, null);
            }
            holder.cupName = (CheckedTextView) convertView.findViewById(R.id.selected_cup_name);
//            holder.cupName.setTextColor(currentTextColor);
            holder.cupName.setText(list.get(position));
            return convertView;
        }


        public class ViewHolder {
            public CheckedTextView cupName;
        }

    }


}

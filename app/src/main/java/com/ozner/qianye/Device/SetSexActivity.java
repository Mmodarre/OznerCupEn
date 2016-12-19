package com.ozner.qianye.Device;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.qianye.Command.PageState;
import com.ozner.qianye.R;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.util.ArrayList;

/**
 * Created by mengdongya on 2016/3/1.
 */
public class SetSexActivity extends AppCompatActivity {
    ArrayList<String> list = new ArrayList<String>();
    ListView listView;
    TextView toolbar_text, toolbar_save;
    String Mac;
    OznerDevice device;
    WaterReplenishmentMeter waterReplenishmentMeter;
    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mac = getIntent().getStringExtra("MAC");
        device = OznerDeviceManager.Instance().getDevice(Mac);
        if (device instanceof WaterReplenishmentMeter) {
            waterReplenishmentMeter = (WaterReplenishmentMeter) device;
        }
        setContentView(R.layout.activity_setup_sex);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SetSexActivity.this).setTitle("").setMessage(getString(R.string.weather_save_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveSex();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();
                            }
                        }).show();
            }
        });
        toolbar_save = (TextView) findViewById(R.id.toolbar_save);
        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_save.setVisibility(View.VISIBLE);
        toolbar_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSex();
            }
        });
        toolbar_text.setText(getString(R.string.water_replen_meter));
        list.add(getString(R.string.women));
        list.add(getString(R.string.men));
        adapter = new ListAdapter(this);
        listView = (ListView) findViewById(R.id.lv_set_sex);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemsCanFocus(false);
        int a =0;
        try {
            a = (int) device.getAppValue(PageState.Sex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        for (int i = 0; i < list.size(); i++) {
            if (a  == i) {
                listView.setItemChecked(i, true);
            } else {
                listView.setItemChecked(i, false);
            }
        }
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
    }

    private void saveSex() {
        int posi = listView.getCheckedItemPosition();
        if (posi >= 0) {
            waterReplenishmentMeter.setAppdata(PageState.Sex, posi);
        }
        waterReplenishmentMeter.updateSettings();
        SetSexActivity.this.finish();
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
            holder.cupName.setText(list.get(position));
            return convertView;
        }


        public class ViewHolder {
            public CheckedTextView cupName;
        }

    }
}

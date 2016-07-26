package com.ozner.cup.slideleft.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.WaterPurifier.WaterPurifierManager;
import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeterMgr;
import com.ozner.cup.Command.DeviceData;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.CupManager;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.MainActivity;
import com.ozner.cup.MainEnActivity;
import com.ozner.cup.R;
import com.ozner.tap.TapManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2015/11/26.
 */
public class SlideAdapter extends ArrayAdapter<DeviceData> {

    static SlideAdapter instance;
    private Context mContext;
    private List<DeviceData> slideBeans;
    public List<View> myViewGroup = new ArrayList<View>();
    public View lastview;
    public final String[] constate = new String[]{getContext().getString(R.string.loding_now), getContext().getString(R.string.loding_success), getContext().getString(R.string.loding_null)};


    public SlideAdapter(Context context, List<DeviceData> deviceDatas) {
        super(context, R.layout.list_item_handle_left, R.id.title_text, deviceDatas);
        this.slideBeans = deviceDatas;
        mContext = context;
        instance = this;
    }

    public static SlideAdapter instance() {
        return instance;
    }

    /**
     * 拖动之后的列表
     *
     * @return
     */
    public List<DeviceData> getSortList() {
        ArrayList<DeviceData> list = new ArrayList<DeviceData>();
        int n = getCount();
        for (int i = 0; i < n; i++) {
            list.add(getItem(i));
        }
        return list;
    }

    /**
     * 拖动之后的列表
     *
     * @return
     */
    public List<DeviceData> getObjects() {
        return slideBeans;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        String Mac,NewMac = null;
        if (((OznerApplication)((Activity)mContext).getApplication()).isLoginPhone()) {
             Mac = ((MainActivity) getContext()).MAC;
             NewMac = ((MainActivity) getContext()).NewAddMAC;
        }else{
             Mac = ((MainEnActivity) getContext()).MAC;
             NewMac = ((MainEnActivity) getContext()).NewAddMAC;
        }
        SilideViewHolder holder = new SilideViewHolder();
        holder.ll_bg = (LinearLayout) v.findViewById(R.id.sliding_item_bg);
        holder.desc_con = (TextView) v.findViewById(R.id.sliding_item_con_state);
        holder.icon = (ImageView) v.findViewById(R.id.icon_imageview);
        holder.desc_text = (TextView) v.findViewById(R.id.desc_text);
        holder.left_item_state = (ImageView) v.findViewById(R.id.left_item_state);
        holder.title_text = (TextView) v.findViewById(R.id.title_text);
        DeviceData slilde = getItem(position);
//        if(slilde.getMac().equals(NewMac)) {
//            CustomToast.makeText((MainActivity) getContext(), holder.icon, 3000);
//            //已经展示，删除
//            ((MainActivity)getContext()).NewAddMAC="";
//        }
        if (slilde.getMac().equals(Mac))
            v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        else
            v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.draglist_bkg));
        String type = slilde.getDeviceType();
        {
            //智能水杯
            if (CupManager.IsCup(type)) {
                if (slilde.getMac().equals(Mac))
                    holder.icon.setImageResource(R.drawable.cup_on);
                else
                    holder.icon.setImageResource(R.drawable.cup);
                holder.left_item_state.setImageResource(R.mipmap.icon_bluetooth);
                //智能水探头
            } else if (TapManager.IsTap(type)) {
                if (slilde.getMac().equals(Mac))
                    holder.icon.setImageResource(R.drawable.tan_tou_on);
                else
                    holder.icon.setImageResource(R.drawable.tan_tou);
                holder.left_item_state.setImageResource(R.mipmap.icon_bluetooth);
                //智能净水器
            } else if (WaterPurifierManager.IsWaterPurifier(type)) {
                if (slilde.getMac().equals(Mac))
                    holder.icon.setImageResource(R.drawable.shuiji_on);
                else
                    holder.icon.setImageResource(R.drawable.shuiji);
                holder.left_item_state.setImageResource(R.drawable.wifiblue);
                //智能空净
            } else if (AirPurifierManager.IsBluetoothAirPurifier(type)) {

                if (slilde.getMac().equals(Mac))
                    holder.icon.setImageResource(R.drawable.airpurifier_tai_on);
                else
                    holder.icon.setImageResource(R.drawable.airpurifier_tai);
                holder.left_item_state.setImageResource(R.mipmap.icon_bluetooth);
                //智能立空净
            } else if (AirPurifierManager.IsWifiAirPurifier(type)) {
                if (slilde.getMac().equals(Mac))
                    holder.icon.setImageResource(R.drawable.airpurifier_ver_on);
                else
                    holder.icon.setImageResource(R.drawable.airpurifier_ver);
                holder.left_item_state.setImageResource(R.drawable.wifiblue);
            } else if (WaterReplenishmentMeterMgr.IsWaterReplenishmentMeter(type)) {
                if (slilde.getMac().equals(Mac))
                    holder.icon.setImageResource(R.drawable.water_replenish_meter_on);
                else
                    holder.icon.setImageResource(R.drawable.water_replenish_meter);
                holder.left_item_state.setImageResource(R.mipmap.icon_bluetooth);
            }
        }
        if (slilde.getOznerDevice() != null) {
            try {
                if (slilde.getOznerDevice() instanceof AirPurifier_MXChip) {
                    holder.desc_con.setText(((AirPurifier_MXChip) slilde.getOznerDevice()).isOffline() ? getContext().getString(R.string.loding_null) : getContext().getString(R.string.loding_success));
                } else if(slilde.getOznerDevice() instanceof WaterPurifier){
                    holder.desc_con.setText(((WaterPurifier) slilde.getOznerDevice()).isOffline() ? getContext().getString(R.string.loding_null) : getContext().getString(R.string.loding_success));
                }else {
                    holder.desc_con.setText(constate[slilde.getOznerDevice().connectStatus().ordinal()]);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            holder.desc_con.setText(constate[0]);
        }

        holder.title_text.setText(slilde.getOznerDevice().getName());
        Object name = slilde.getOznerDevice().getAppValue(PageState.DEVICE_ADDRES);
        if (name != null) {
            if ("SCP001".equals(slilde.getOznerDevice().Type())) {
                holder.desc_text.setText("");
            } else {
                holder.desc_text.setText(name.toString());
            }
        }


        myViewGroup.add(v);
        return v;
    }

//    private class ViewHolder {
//        public TextView desc_text,desc_con;
//        public ImageView icon;
//    }

}

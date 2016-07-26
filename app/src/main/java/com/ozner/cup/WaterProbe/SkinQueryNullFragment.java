package com.ozner.cup.WaterProbe;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.OznerDataHttp;
import com.ozner.cup.R;
import com.ozner.device.OznerDeviceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by mengdongya on 2016/3/7.
 */
public class SkinQueryNullFragment extends Fragment implements View.OnClickListener {
    private Toolbar toolbar;
    private TextView skin_show, notice_skin, skin_show_remind, query_times,
            skin_dry, skin_oily, skin_middle, tv_skin_notice, toolbar_text;
    private String Mac;
    private WaterReplenishmentMeter waterReplenishmentMeter;
    int times = 0, sex, quertTimes = 0;
    ImageView iv_skin_class, iv_current_select_skin;
    private float skinVarValue;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            Mac = getArguments().getString("MAC");
            waterReplenishmentMeter = (WaterReplenishmentMeter) OznerDeviceManager.Instance().getDevice(Mac);
        } catch (Exception e) {
        }
        View view = inflater.inflate(R.layout.fragment_skin_query, container, false);
        initView(view);
//        sex = (int)waterReplenishmentMeter.getAppValue(PageState.Sex);//sex=0代表女  1代表男
        OznerApplication.changeTextFont((ViewGroup) view);
        return view;
    }

    private void initView(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        if (((OznerApplication)getActivity().getApplication()).isLoginPhone()) {
            view.findViewById(R.id.llay_cupHolder).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.llay_cupHolder).setVisibility(View.GONE);
        }
        toolbar_text = (TextView) view.findViewById(R.id.toolbar_text);
        toolbar_text.setText(getString(R.string.skin_query_null));
        toolbar.setBackgroundColor(getResources().getColor(R.color.water_replen_face));
        skin_show = (TextView) view.findViewById(R.id.skin_show);//皮肤肤质
        notice_skin = (TextView) view.findViewById(R.id.notice_skin);//提示
        query_times = (TextView) view.findViewById(R.id.query_times);//检测次数
//        try {
//             quertTimes = (int)waterReplenishmentMeter.getAppValue(PageState.WRMQueryTimes);
//            if (quertTimes != 0){
//                notice_skin.setText(getString(R.string.query_times_unenough));
//            }
//            query_times.setText(quertTimes+"");
//        }catch (Exception e){}

        skin_show_remind = (TextView) view.findViewById(R.id.skin_show_remind);//皮肤肤质保护说明
//        query_time_quantum = (TextView) view.findViewById(R.id.query_time_quantum);//统计时间段
        skin_dry = (TextView) view.findViewById(R.id.skin_dry);//肤质
        skin_oily = (TextView) view.findViewById(R.id.skin_oily);//肤质
        skin_middle = (TextView) view.findViewById(R.id.skin_middle);//肤质
//        skin_mix = (TextView) view.findViewById(R.id.skin_mix);//肤质
//        skin_sensibility = (TextView) view.findViewById(R.id.skin_sensibility);//肤质
        tv_skin_notice = (TextView) view.findViewById(R.id.tv_skin_notice);//肤质
        skin_dry.setSelected(true);
        iv_current_select_skin = (ImageView) view.findViewById(R.id.iv_current_select_skin);
        iv_skin_class = (ImageView) view.findViewById(R.id.iv_skin_class);
        iv_skin_class.setOnClickListener(this);
        skin_dry.setOnClickListener(this);
        skin_oily.setOnClickListener(this);
        skin_middle.setOnClickListener(this);
//        skin_mix.setOnClickListener(this);
//        skin_sensibility.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.skin_dry:
                skin_dry.setSelected(true);
                skin_oily.setSelected(false);
                skin_middle.setSelected(false);
//                skin_mix.setSelected(false);
//                skin_sensibility.setSelected(false);
                tv_skin_notice.setText(getString(R.string.skin_dry_notice));
                iv_current_select_skin.setImageResource(R.drawable.ganzao);
                break;
            case R.id.skin_oily:
                skin_dry.setSelected(false);
                skin_oily.setSelected(true);
                skin_middle.setSelected(false);
//                skin_mix.setSelected(false);
//                skin_sensibility.setSelected(false);
                tv_skin_notice.setText(getString(R.string.skin_oily_notice));
                iv_current_select_skin.setImageResource(R.drawable.youxing_03);
                break;
            case R.id.skin_middle:
                skin_dry.setSelected(false);
                skin_oily.setSelected(false);
                skin_middle.setSelected(true);
//                skin_mix.setSelected(false);
//                skin_sensibility.setSelected(false);
                tv_skin_notice.setText(getString(R.string.skin_mid_notice));
                iv_current_select_skin.setImageResource(R.drawable.zhongxing_03);
                break;
//            case R.id.skin_mix:
//                skin_dry.setSelected(false);
//                skin_oily.setSelected(false);
//                skin_middle.setSelected(false);
//                skin_mix.setSelected(true);
//                skin_sensibility.setSelected(false);
//                tv_skin_notice.setText(getString(R.string.skin_mix_notice));
//                iv_current_select_skin.setImageResource(R.drawable.hunhexing_03);
//                break;
//            case R.id.skin_sensibility:
//                skin_dry.setSelected(false);
//                skin_oily.setSelected(false);
//                skin_middle.setSelected(false);
//                skin_mix.setSelected(false);
//                skin_sensibility.setSelected(true);
//                tv_skin_notice.setText(getString(R.string.skin_sensibility_notice));
//                iv_current_select_skin.setImageResource(R.drawable.mingganxing_03);
//                break;
            case R.id.skin_buy_layout:
                break;
            case R.id.iv_skin_class:
                getFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetWaterRMAsyncTask().execute();
        new GetTimesAsyncTask().execute();
    }

    class GetTimesAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params != null) {
                List<NameValuePair> pars = new ArrayList<>();
                pars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(getContext())));
                pars.add(new BasicNameValuePair("mac", Mac));
//                pars.add(new BasicNameValuePair("action",PageState.NeckSkinValue));
//                pars.add(new BasicNameValuePair("action",PageState.HandSkinValue));
//                pars.add(new BasicNameValuePair("action",PageState.FaceSkinValue));
//                pars.add(new BasicNameValuePair("action",PageState.EyesSkinValue));
                String filterUrl = OznerPreference.ServerAddress(getContext()) + "OznerDevice/GetTimesCountBuShui";
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(getContext(), filterUrl, pars);
//                Log.e("123456",filterUrl +"==="+ netJsonObject.state+"==="+netJsonObject.value);
                return netJsonObject;
            }
            return null;

        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            quertTimes = 0;
            if (netJsonObject != null && netJsonObject.state > 0) {
                try {
                    JSONArray jsonArray = netJsonObject.getJSONObject().getJSONArray("data");
                    int length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        quertTimes += Integer.parseInt(jsonObject.getString("times"));
                    }
                    query_times.setText(quertTimes + "");
                } catch (Exception e) {
                }
            }
        }
    }

    class GetWaterRMAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params != null) {
                List<NameValuePair> pars = new ArrayList<>();
                pars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(getContext())));
                pars.add(new BasicNameValuePair("mac", Mac));
                pars.add(new BasicNameValuePair("myaction", PageState.FaceSkinValue));
                String filterUrl = OznerPreference.ServerAddress(getContext()) + "OznerServer/GetBuShuiFenBu";
//                Log.e("123456","queryNull+"+filterUrl);
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(getContext(), filterUrl, pars);
//                Log.e("123456","queryNull+"+netJsonObject.value);
                return netJsonObject;
            }
            return null;

        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (netJsonObject != null && netJsonObject.state > 0) {
                try {
                    JSONObject jsonObject1 = netJsonObject.getJSONObject().getJSONObject("data");
                    JSONObject object = jsonObject1.getJSONObject(PageState.FaceSkinValue);
                    JSONArray jsonArray = object.getJSONArray("monty");
                    int b = jsonArray.length();
                    for (int i = 0; i < b; i++) {
                        JSONObject object1 = (JSONObject) jsonArray.get(i);
                        float oil = Float.parseFloat(object1.getString("ynumber"));
                        int time = Integer.parseInt(object1.getString("times"));
                        skinVarValue += time * oil;
                        times += time;
                    }
                    float oil = skinVarValue / times;
                    if (oil <= 12) {
                        skin_show.setText(getString(R.string.skin_dry));
                        skin_dry.setVisibility(View.GONE);
                        skin_oily.setSelected(true);
                        tv_skin_notice.setText(getString(R.string.skin_oily_notice));
                        skin_show_remind.setText(getString(R.string.skin_dry_notice));
                        iv_skin_class.setImageResource(R.drawable.ganzao);
                    } else if (oil > 12 && oil <= 20) {
                        skin_show.setText(getString(R.string.skin_mid));
                        skin_middle.setVisibility(View.GONE);
                        skin_show_remind.setText(getString(R.string.skin_mid_notice));
                        iv_skin_class.setImageResource(R.drawable.zhongxing_03);
                    } else if (oil > 20) {
                        skin_show.setText(getString(R.string.skin_oily));
                        skin_oily.setVisibility(View.GONE);
                        skin_show_remind.setText(getString(R.string.skin_oily_notice));
                        iv_skin_class.setImageResource(R.drawable.youxing_03);
                    }
                    notice_skin.setText(getString(R.string.query_times_unenough));
                } catch (Exception e) {
                }
            }
        }
    }
}

package com.ozner.cup.WaterProbe;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.cup.CupRecord;
import com.ozner.cup.CChat.CChatFragment;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.Device.OznerMallFragment;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.OznerDataHttp;
import com.ozner.cup.MainActivity;
import com.ozner.cup.MainEnActivity;
import com.ozner.cup.R;
import com.ozner.cup.WaterCup.TDSIntroduceFragment;
import com.ozner.cup.WaterCup.WaterKnowActivity;
import com.ozner.cup.WaterProbe.PurifierExp.UIZPurifierExpView;
import com.ozner.cup.mycenter.CenterBean.RankType;
import com.ozner.cup.mycenter.CenterBean.TDSRankAsyncTask;
import com.ozner.device.OznerDeviceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class PurifierTDSFragment extends Fragment implements View.OnClickListener {
    View rootView = null;
    UIZPurifierExpView uizPurifierExpView = null;
    Button btn_week, btn_month;
    //    RelativeLayout rlay_share, rlay_back;
    boolean isWeek = true;
    int[] mon_predata, mont_afterdata, week_predata, week_afterdata;
    int[] net_mon_predata, net_mon_afterdata, net_week_predata, net_week_afterdata;
    JSONArray weekArray, monthArray;
    Calendar curCal;
    private Toolbar toolbar;
    //获取tds数值
    private String mac;
    private WaterPurifier waterPurifier;
    private TextView tv_preValue, tv_afterValue, tv_friend_shortValueText, tv_tdsTips, tv_spec;
    private LinearLayout laly_consult, wateryield_health_know_layout, wateryield_health_buy_layout;
    private ImageView iv_purifierTips, iv_tdsface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mac = getArguments().getString("MAC");
        waterPurifier = (WaterPurifier) OznerDeviceManager.Instance().getDevice(mac);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.purifier_data_tds, container, false);
//        mac = getArguments().getString("MAC");
//        waterPurifier = (WaterPurifier) OznerDeviceManager.Instance().getDevice(mac);

        initView(rootView);//界面初始化
        btnInit();//按钮切换

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(OznerApplication.isLanguageCN()){
            ((MainActivity) getActivity()).isShouldResume = false;
        }else {
            ((MainEnActivity) getActivity()).isShouldResume = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(OznerApplication.isLanguageCN()){
            ((MainActivity) getActivity()).isShouldResume = true;
        }else{
            ((MainEnActivity) getActivity()).isShouldResume = false;
        }

    }

    void setTextFace() {
        tv_preValue.setTextSize(23);
        tv_afterValue.setTextSize(23);
        OznerApplication.setControlTextFace(tv_preValue);
        OznerApplication.setControlTextFace(tv_afterValue);
    }

    void setNumFace() {
        tv_preValue.setTextSize(27);
        tv_afterValue.setTextSize(27);
        OznerApplication.setControlNumFace(tv_preValue);
        OznerApplication.setControlNumFace(tv_afterValue);
    }

    private void initView(View rootView) {
        OznerApplication.changeTextFont((ViewGroup) rootView);
        uizPurifierExpView = (UIZPurifierExpView) rootView.findViewById(R.id.uiz_purifierExp);
//        rlay_share = (RelativeLayout) rootView.findViewById(R.id.rlay_share);
//        rlay_back = (RelativeLayout) rootView.findViewById(R.id.rlay_back);
        btn_week = (Button) rootView.findViewById(R.id.btn_week);
        btn_month = (Button) rootView.findViewById(R.id.btn_month);
        tv_preValue = (TextView) rootView.findViewById(R.id.tv_preValue);
        tv_afterValue = (TextView) rootView.findViewById(R.id.tv_afterValue);
        tv_friend_shortValueText = (TextView) rootView.findViewById(R.id.tv_friend_shortValueText);
        tv_tdsTips = (TextView) rootView.findViewById(R.id.tv_tdsTips);
        iv_tdsface = (ImageView) rootView.findViewById(R.id.iv_tdsface);
        tv_spec = (TextView) rootView.findViewById(R.id.tv_spec);
        toolbar = (Toolbar) rootView.findViewById(R.id.cup_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        OznerApplication.setControlNumFace(tv_spec);
        OznerApplication.setControlNumFace(tv_friend_shortValueText);
        if (waterPurifier != null && waterPurifier.sensor().TDS1() != 65535 && waterPurifier.sensor().TDS2() != 65535) {
            if (waterPurifier.sensor().TDS1() != 0 || waterPurifier.sensor().TDS2() != 0) {
                if (waterPurifier.sensor().TDS1() > waterPurifier.sensor().TDS2()) {
                    tv_preValue.setText(waterPurifier.sensor().TDS1() + "");
                    tv_afterValue.setText((waterPurifier.sensor().TDS2() != 0 ? waterPurifier.sensor().TDS2() : 1) + "");
                    setTdsTips(waterPurifier.sensor().TDS2());
                } else {
                    tv_preValue.setText(waterPurifier.sensor().TDS2() + "");
                    tv_afterValue.setText((waterPurifier.sensor().TDS1() != 0 ? waterPurifier.sensor().TDS1() : 1) + "");
                    setTdsTips(waterPurifier.sensor().TDS1());
                }
                setNumFace();
            } else {
                setTextFace();
            }
        } else {
            tv_preValue.setText(getString(R.string.text_null));
            tv_afterValue.setText(getString(R.string.text_null));
//            OznerApplication.setControlTextFace(tv_preValue);
//            OznerApplication.setControlTextFace(tv_afterValue);
            setTextFace();
        }


        btn_week.setOnClickListener(this);
        btn_month.setOnClickListener(this);
//        rlay_share.setOnClickListener(this);
//        rlay_back.setOnClickListener(this);


        laly_consult = (LinearLayout) rootView.findViewById(R.id.laly_consult);
        wateryield_health_know_layout = (LinearLayout) rootView.findViewById(R.id.wateryield_health_know_layout);
        wateryield_health_buy_layout = (LinearLayout) rootView.findViewById(R.id.wateryield_health_buy_layout);
        laly_consult.setOnClickListener(this);
        wateryield_health_know_layout.setOnClickListener(this);
        wateryield_health_buy_layout.setOnClickListener(this);
        if (OznerApplication.isLanguageCN()) {
            rootView.findViewById(R.id.llay_cupHolder).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.ll_en_no).setVisibility(View.VISIBLE);
        } else {
            rootView.findViewById(R.id.llay_cupHolder).setVisibility(View.GONE);
            rootView.findViewById(R.id.ll_en_no).setVisibility(View.GONE);
            wateryield_health_buy_layout.setVisibility(View.GONE);
        }
        iv_purifierTips = (ImageView) rootView.findViewById(R.id.iv_purifierTips);
        iv_purifierTips.setOnClickListener(this);
    }

    private void btnInit() {
        btn_week.setSelected(isWeek);
        btn_month.setSelected(!isWeek);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        curCal = Calendar.getInstance();
        reloadWeekData();
        TDSRankAsyncTask tdsRankAsyncTask = new TDSRankAsyncTask(getContext(), RankType.WaterType, new TDSRankAsyncTask.OnTDSRankLoadListener() {
            @Override
            public void onTDSRankLoaded(int rank) {
                tv_friend_shortValueText.setText(rank + "");
            }
        });
        tdsRankAsyncTask.execute();

        new PurifierExpUpdateTask(getContext(), mac).execute();
    }

    //从网络获取净水器tds周月分布数据
    class PurifierExpUpdateTask extends AsyncTask<String, Void, NetJsonObject> {
        private Context mContext;
        private String mac;
        private String updateUrl;

        public PurifierExpUpdateTask(Context context, String mac) {
            mContext = context;
            this.mac = mac;
        }

        @Override
        protected void onPreExecute() {
            updateUrl = OznerPreference.ServerAddress(mContext) + "/OznerServer/GetDeviceTdsFenBu";
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            List<NameValuePair> parms = new ArrayList<NameValuePair>();
            parms.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(mContext)));
            parms.add(new BasicNameValuePair("mac", mac));
            NetJsonObject result = OznerDataHttp.OznerWebServer(mContext, updateUrl, parms);
            return result;
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            Log.e("tag", "净水器周月数据分布：" + netJsonObject.value);
            if (netJsonObject.state > 0) {
                //TODO:根据时间初始化数组
                try {
                    JSONObject resJo = netJsonObject.getJSONObject();
                    weekArray = resJo.getJSONArray("week");
                    monthArray = resJo.getJSONArray("month");
                    if (isWeek) {
                        if (weekArray != null && weekArray.length() > 0) {
                            loadNetWeekData(weekArray);
                        } else {
                            reloadWeekData();
                        }
                    } else {
                        if (monthArray != null && monthArray.length() > 0) {
                            loadNetMonthData(monthArray);
                        } else {
                            reloadMonthData();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("tag", "净水器周月数据异常：" + ex.getMessage());
                    if (isWeek) {
                        reloadWeekData();
                    } else {
                        reloadMonthData();
                    }
                }
            }
            super.onPostExecute(netJsonObject);
        }
    }

    private void setTdsTips(int tdsValue) {
        if (tdsValue >= 0 && tdsValue <= CupRecord.TDS_Good_Value) {
            tv_tdsTips.setText(getString(R.string.waterTds_faceLow));
            iv_tdsface.setImageResource(R.drawable.lianghao);
        } else if (tdsValue > CupRecord.TDS_Good_Value && tdsValue <= CupRecord.TDS_Bad_Value) {
            tv_tdsTips.setText(getString(R.string.waterTds_faceMid));
            iv_tdsface.setImageResource(R.drawable.yiban);
        } else {
            tv_tdsTips.setText(getString(R.string.waterTds_faceHight));
            iv_tdsface.setImageResource(R.drawable.jingbao);
        }
    }

    private void loadNetMonthData(JSONArray array) {
        try {
            net_mon_predata = new int[31];
            net_mon_afterdata = new int[31];
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                int pretds = item.getInt("beforetds");
                int aftertds = item.getInt("tds");
                String timeStr = item.getString("stime");
                long timetick = Long.parseLong(timeStr.replace("/Date(", "").replace(")/", ""));
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date(timetick));
                net_mon_predata[cal.get(Calendar.DAY_OF_MONTH) - 1] = pretds > 200 ? 200 : pretds;
                net_mon_afterdata[cal.get(Calendar.DAY_OF_MONTH) - 1] = aftertds > 200 ? 200 : aftertds;
            }
            uizPurifierExpView.setMonthData(net_mon_predata, net_mon_afterdata);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("tag", "loadnetMonthData:" + ex.getMessage());
        }
    }

    private void loadNetWeekData(JSONArray array) {
        try {
            net_week_predata = new int[7];
            net_week_afterdata = new int[7];
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                int pretds = item.getInt("beforetds");
                int aftertds = item.getInt("tds");
                String timeStr = item.getString("stime");
                long timetick = Long.parseLong(timeStr.replace("/Date(", "").replace(")/", ""));
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date(timetick));
                if (1 == cal.get(Calendar.DAY_OF_WEEK)) {
                    net_week_predata[6] = pretds;
                    net_week_afterdata[6] = aftertds;
                } else {
                    net_week_predata[cal.get(Calendar.DAY_OF_WEEK) - 2] = pretds;
                    net_week_afterdata[cal.get(Calendar.DAY_OF_WEEK) - 2] = aftertds;
                }
            }
            uizPurifierExpView.setWeekData(net_week_predata, net_week_afterdata);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("tag", "ladnetWeekData:" + ex.getMessage());
        }
    }

    private void reloadMonthData() {
        mon_predata = new int[31];
        mont_afterdata = new int[31];
        for (int i = 0; i < mont_afterdata.length; i++) {
            int curDayOfMonth = curCal.get(Calendar.DAY_OF_MONTH);
            if (i == curDayOfMonth - 1) {
                if (waterPurifier.sensor().TDS1() > waterPurifier.sensor().TDS2()) {
                    mon_predata[i] = waterPurifier.sensor().TDS1();
                    mont_afterdata[i] = waterPurifier.sensor().TDS2();
                } else {
                    mon_predata[i] = waterPurifier.sensor().TDS2();
                    mont_afterdata[i] = waterPurifier.sensor().TDS1();
                }
            }
        }
        uizPurifierExpView.setMonthData(mon_predata, mont_afterdata);
    }

    private void reloadWeekData() {
        week_predata = new int[7];
        week_afterdata = new int[7];
        for (int i = 0; i < week_predata.length; i++) {
            int curDayOfWeek = curCal.get(Calendar.DAY_OF_WEEK);
            if (curDayOfWeek == 1) {
                if (waterPurifier.sensor().TDS1() > waterPurifier.sensor().TDS2()) {
                    week_predata[6] = waterPurifier.sensor().TDS1();
                    week_afterdata[6] = waterPurifier.sensor().TDS2();
                } else {
                    week_predata[6] = waterPurifier.sensor().TDS2();
                    week_afterdata[6] = waterPurifier.sensor().TDS1();
                }
            } else if ((curDayOfWeek - 2) == i) {
                if (waterPurifier.sensor().TDS1() > waterPurifier.sensor().TDS2()) {
                    week_predata[i] = waterPurifier.sensor().TDS1();
                    week_afterdata[i] = waterPurifier.sensor().TDS2();
                } else {
                    week_predata[i] = waterPurifier.sensor().TDS2();
                    week_afterdata[i] = waterPurifier.sensor().TDS1();
                }
            }
        }
        uizPurifierExpView.setWeekData(week_predata, week_afterdata);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_week:
                if (!isWeek) {
                    isWeek = true;
                    setWeekBtnSelected(isWeek);
                }
                break;
            case R.id.btn_month:
                if (isWeek) {
                    isWeek = false;
                    setMonthBtnSelected(!isWeek);
                }
                break;
//            case R.id.rlay_back:
////                getFragmentManager().popBackStack();
//                break;
//            case R.id.rlay_share:
////                ShareView.showShareToDialog(getContext());
//                break;

            case R.id.wateryield_health_know_layout:
//                    getFragmentManager().beginTransaction().add(R.id.framen_main_con, new WaterKnowFragment()).addToBackStack(null).commit();
//                    getActivity().getSupportFragmentManager().executePendingTransactions();
                Intent intent = new Intent(getContext(), WaterKnowActivity.class);
                intent.putExtra(PageState.MAC, mac);
                getActivity().startActivity(intent);
                break;
            case R.id.laly_consult:
                getFragmentManager().beginTransaction().replace(R.id.framen_main_con, new CChatFragment()).commitAllowingStateLoss();
                ((MainActivity) (getActivity())).pagenow = PageState.ZIXUNYEMIAN;
                ((MainActivity) (getActivity())).footNavFragment.ShowContent(PageState.ZIXUNYEMIAN, "");
                break;
            case R.id.wateryield_health_buy_layout:
                getFragmentManager().popBackStack();
                getFragmentManager().beginTransaction().replace(R.id.framen_main_con, new OznerMallFragment()).commitAllowingStateLoss();
                ((MainActivity) (getActivity())).pagenow = PageState.SHANGCHEGYEMIAN;
                ((MainActivity) (getActivity())).footNavFragment.ShowContent(PageState.SHANGCHEGYEMIAN, "");

                break;
            case R.id.iv_purifierTips:
                getFragmentManager().beginTransaction().add(R.id.framen_main_con, new TDSIntroduceFragment()).addToBackStack(null).commit();
                break;
        }
    }


    private void setWeekBtnSelected(boolean sel) {
        btn_week.setSelected(sel);
        btn_month.setSelected(!sel);
        if (sel) {
            if (weekArray != null && weekArray.length() > 0) {
                loadNetWeekData(weekArray);
            } else {
                reloadWeekData();
            }

        }
//        uizPurifierExpView.setWeekData(week_predata, week_afterdata);
    }

    private void setMonthBtnSelected(boolean sel) {
        btn_week.setSelected(!sel);
        btn_month.setSelected(sel);
        if (sel) {
            if (monthArray != null && monthArray.length() > 0) {
                loadNetMonthData(monthArray);
            } else {
                reloadMonthData();
            }
        }
//        uizPurifierExpView.setMonthData(mon_predata, mont_afterdata);
    }
}

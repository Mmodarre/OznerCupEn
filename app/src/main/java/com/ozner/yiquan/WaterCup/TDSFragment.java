package com.ozner.yiquan.WaterCup;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.yiquan.CChat.CChatFragment;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.PageState;
import com.ozner.cup.Cup;
import com.ozner.cup.CupRecord;
import com.ozner.cup.CupRecordList;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.Device.OznerMallFragment;
import com.ozner.yiquan.Main.BaseMainActivity;
import com.ozner.yiquan.MainActivity;
import com.ozner.yiquan.R;
import com.ozner.yiquan.UIView.ChartAdapter;
import com.ozner.yiquan.UIView.TDSChartView;
import com.ozner.yiquan.UIView.UIXWaterDetailProgress;
import com.ozner.yiquan.UIView.ViewReturn;
import com.ozner.device.OznerDeviceManager;

import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class TDSFragment extends Fragment {

    private TextView time_day, time_week, time_month;
    private RelativeLayout tds_layout, chartView_layout,tds_friend_layout,tds_health_layout;
    private TDSChartView tdsChartView;
    private UIXWaterDetailProgress progressView;
    private ChartAdapter.ViewMode mode = ChartAdapter.ViewMode.Day;

    int[] dataDay = new int[24];
    int[] dataWeek = new int[7];
    int[] dataMonth = new int[31];
    ChartAdapter adapterDay, adapterWeek, adapterMonth;

    //    CupRecord[] dataVolume;
//    CupRecord monthVolume;
    Date time;
    int months, weeks, days = 1;

    int count = 0, countHot = 0, countCold = 0, countNor = 0;
    int w_count = 0, w_countHot = 0, w_countCold = 0, w_countNor = 0;
    int m_count = 0, m_countHot = 0, m_countCold = 0, m_countNor = 0;
    int w_hot = 0, w_bad = 0, w_nor = 0;
    int m_hot = 0, m_bad = 0, m_nor = 0;
    int hot = 0, bad = 0, nor = 0;

    private ImageView tdsIntroduce, iv_tds_left, iv_tds_right,iv_tds_line1,iv_height;
    private LinearLayout healthKnow, tds_health_buy_layout, tds_consult_layout, laly_tds_value;
    private Toolbar toolbar;
    //    private ImageButton tdsShare;
    private TextView tdsText, tdsValue, tv_tapHealthPre, tv_tapGenericPre, tv_tapBadPre, tv_tds_facetest, tv_tdstext, tv_tds_friendtext;
    private String mac;
    private Cup cup;
    private CupRecordList cupRecordList;
    private CupRecord cupRecordTemp;
    private CupRecord[] cupRecords;
    private ImageView iv_tds_face;
    private int flagRight = 0, flagLeft = 0;
    private String path;
    private int rank, rankTds;

    public TDSFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_cup_tds, container, false);
        OznerApplication.changeTextFont((ViewGroup) view);
        mac = getArguments().getString("MAC");
        cup = (Cup) OznerDeviceManager.Instance().getDevice(mac);
        rankTds = getArguments().getInt("tdsRank");
        initView(view);
        OznerApplication.setControlNumFace(tv_tds_friendtext);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BaseMainActivity) getActivity()).isShouldResume = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((BaseMainActivity) getActivity()).isShouldResume = true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UiUpdateAsyncTask asyncTask = new UiUpdateAsyncTask();
        asyncTask.execute("tds");
//        TDSRankAsyncTask tdsRankAsyncTask=new TDSRankAsyncTask(getContext(), RankType.CupType, new TDSRankAsyncTask.OnTDSRankLoadListener() {
//            @Override
//            public void onTDSRankLoaded(int rank) {
//                tv_tds_friendtext.setText(rank + "");
//            }
//        });
//        tdsRankAsyncTask.execute();
    }

    private void initData() {
        //折线图数据的切换  taoran
        //日
        time = new Date(new Date().getTime() / 86400000 * 86400000);
        cupRecordList = cup.Volume();
        if (cupRecordList != null) {
            cupRecordTemp = cupRecordList.getRecordByDate(time);
            if (cupRecordTemp != null) {
                countHot = cupRecordTemp.TDS_Bad;
                countCold = cupRecordTemp.TDS_Good;
                countNor = cupRecordTemp.TDS_Mid;
                count = cupRecordTemp.Count;
                hot = countHot * 100 / count;
                bad = countCold * 100 / count;
                nor = 100 - (hot + bad);
            }

            cupRecords = cupRecordList.getRecordByDate(time, CupRecordList.QueryInterval.Hour);
            if (cupRecords != null) {
                for (int i = 0; i < dataDay.length; i++) {
                    for (int j = 0; j < cupRecords.length; j++) {
                        dataDay[cupRecords[j].start.getHours()] = cupRecords[j].TDS_High;
                    }
                }
            }
        }
        adapterDay = new ChartAdapter() {
            @Override
            public int count() {
                return dataDay.length;
            }

            @Override
            public int getValue(int Index) {
                return dataDay[Index];
            }

            @Override
            public int getMax() {
                return 200;
            }

            @Override
            public ViewMode getViewMode() {
                return ViewMode.Day;
            }
        };

        //周
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date timeWeek = new Date(cal.getTime().getTime() / 86400000 * 86400000);
        cupRecordList = cup.Volume();
        if (cupRecordList != null) {
            cupRecords = cupRecordList.getRecordByDate(timeWeek, CupRecordList.QueryInterval.Day);
            if (cupRecords != null) {
                for (int i = 0; i < dataWeek.length; i++) {
                    for (int j = 0; j < cupRecords.length; j++) {
                        if (cupRecords[j].start.getDay() != 0) {
                            dataWeek[cupRecords[j].start.getDay() - 1] = cupRecords[j].TDS_High;
                            w_countHot = cupRecords[j].TDS_Bad + w_countHot;
                            w_countCold = cupRecords[j].TDS_Good + w_countCold;
                            w_countNor = cupRecords[j].TDS_Mid + w_countNor;
                            w_count = cupRecords[j].Count + w_count;
                            w_hot = w_countHot * 100 / w_count;
                            w_bad = w_countCold * 100 / w_count;
                            w_nor = 100 - (w_hot + w_bad);
                        } else {
                            Date today = new Date();
                            if (today.getDay() == 0) {
                                dataWeek[6] = cupRecords[j].TDS_High;
                                w_countHot = cupRecords[j].TDS_Bad + w_countHot;
                                w_countCold = cupRecords[j].TDS_Good + w_countCold;
                                w_countNor = cupRecords[j].TDS_Mid + w_countNor;
                                w_count = cupRecords[j].Count + w_count;
                                w_hot = w_countHot * 100 / w_count;
                                w_bad = w_countCold * 100 / w_count;
                                w_nor = 100 - (w_hot + w_bad);
                            } else {
                                dataWeek[today.getDay() - 1] = cupRecords[j].TDS_High;
                                w_countHot = cupRecords[j].TDS_Bad + w_countHot;
                                w_countCold = cupRecords[j].TDS_Good + w_countCold;
                                w_countNor = cupRecords[j].TDS_Mid + w_countNor;
                                w_count = cupRecords[j].Count + w_count;
                                w_hot = w_countHot * 100 / w_count;
                                w_bad = w_countCold * 100 / w_count;
                                w_nor = 100 - (w_hot + w_bad);
                            }
                        }
                    }
                }
            }
        }
        adapterWeek = new ChartAdapter() {
            @Override
            public int count() {
                return dataWeek.length;
            }

            @Override
            public int getValue(int Index) {
                return dataWeek[Index];
            }

            @Override
            public int getMax() {
                return 200;
            }

            @Override
            public ViewMode getViewMode() {
                return ViewMode.Week;
            }
        };

        //月
        Calendar calMonth = Calendar.getInstance();
        calMonth.set(Calendar.DAY_OF_MONTH, 1);
        Date timeMonth = new Date(calMonth.getTime().getTime() / 86400000 * 86400000);
        cupRecordList = cup.Volume();
        if (cupRecordList != null) {
            cupRecords = cupRecordList.getRecordByDate(timeMonth, CupRecordList.QueryInterval.Day);
            if (cupRecords != null) {
                for (int i = 0; i < dataMonth.length; i++) {
                    for (int j = 0; j < cupRecords.length; j++) {
                        if (cupRecords[j].start != null) {
                            dataMonth[cupRecords[j].start.getDate() - 1] = cupRecords[j].TDS_High;
                            m_countHot = cupRecords[j].TDS_Bad + m_countHot;
                            m_countCold = cupRecords[j].TDS_Good + m_countCold;
                            m_countNor = cupRecords[j].TDS_Mid + m_countNor;
                            m_count = cupRecords[j].Count + m_count;
                            m_hot = m_countHot * 100 / m_count;
                            m_bad = m_countCold * 100 / m_count;
                            m_nor = 100 - (m_hot + m_bad);
                        } else {
                            dataMonth[cupRecordList.time.getDate()] = cupRecords[j].TDS_High;
                            m_countHot = cupRecords[j].TDS_Bad + m_countHot;
                            m_countCold = cupRecords[j].TDS_Good + m_countCold;
                            m_countNor = cupRecords[j].TDS_Mid + m_countNor;
                            m_count = cupRecords[j].Count + m_count;
                            m_hot = m_countHot * 100 / m_count;
                            m_bad = m_countCold * 100 / m_count;
                            m_nor = 100 - (m_hot + m_bad);
                        }
                    }
                }
            }
            adapterMonth = new ChartAdapter() {
                @Override
                public int count() {
                    return dataMonth.length;
                }

                @Override
                public int getValue(int Index) {
                    return dataMonth[Index];
                }

                @Override
                public int getMax() {
                    return 200;
                }

                @Override
                public ViewMode getViewMode() {
                    return ViewMode.Month;
                }
            };

        }
    }

    private class UiUpdateAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            initData();
            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {

        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            progressView.set_bad_progress(hot);
            progressView.set_normal_progress(nor);
            progressView.set_good_progress(bad);
            tv_tapHealthPre.setText(bad + "%");
            tv_tapGenericPre.setText(nor + "%");
            tv_tapBadPre.setText(hot + "%");
            progressView.startAnimation();
            if (adapterDay != null) {
                tdsChartView.setAdapter(adapterDay);
            }
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {

        }
    }

    private void initView(View view) {
        //时间tab的选择  taoran
        time_day = (TextView) view.findViewById(R.id.tv_time_day);
        time_week = (TextView) view.findViewById(R.id.tv_time_week);
        time_month = (TextView) view.findViewById(R.id.tv_time_month);
        tv_tdstext = (TextView) view.findViewById(R.id.tv_tdstext);
        time_day.setOnClickListener(myListener);
        time_week.setOnClickListener(myListener);
        time_month.setOnClickListener(myListener);
        toolbar = (Toolbar) view.findViewById(R.id.cup_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
//        tdsShare = (ImageButton) view.findViewById(R.id.cup_toolbar_share);
//        tdsShare.setVisibility(View.VISIBLE);
//        tdsShare.setOnClickListener(myListener);
        tdsIntroduce = (ImageView) view.findViewById(R.id.iv_tds_introduce);
        tdsIntroduce.setOnClickListener(myListener);
        tdsText = (TextView) view.findViewById(R.id.cup_toolbar_text);
        iv_tds_face = (ImageView) view.findViewById(R.id.iv_tds_face);
        tv_tds_facetest = (TextView) view.findViewById(R.id.tv_tds_facetest);
        tdsText.setText(getResources().getString(R.string.tds_test));
        tdsValue = (TextView) view.findViewById(R.id.tv_tds_value);
        if (cup.Sensor().TDSFix != 65535) {
            OznerApplication.setControlNumFace(tdsValue);
            tdsValue.setText(cup.Sensor().TDSFix + "");
            if (cup.Sensor().TDSFix > 0 && cup.Sensor().TDSFix <= CupRecord.TDS_Good_Value) {
                tv_tds_facetest.setText(getResources().getString(R.string.waterTds_faceLow));
                iv_tds_face.setBackgroundResource(R.drawable.lianghao);
            } else if (cup.Sensor().TDSFix > CupRecord.TDS_Good_Value && cup.Sensor().TDSFix <= CupRecord.TDS_Bad_Value) {
                tv_tds_facetest.setText(getResources().getString(R.string.waterTds_faceMid));
                iv_tds_face.setBackgroundResource(R.drawable.yiban);
            } else {
                iv_tds_face.setBackgroundResource(R.drawable.jingbao);
                tv_tds_facetest.setText(getResources().getString(R.string.waterTds_faceHight));
            }

        } else {
            OznerApplication.setControlTextFace(tdsValue);
            tdsValue.setText(getResources().getString(R.string.text_null));
        }
        tv_tapHealthPre = (TextView) view.findViewById(R.id.tv_tapHealthPre);
        tv_tapGenericPre = (TextView) view.findViewById(R.id.tv_tapGenericPre);
        tv_tapBadPre = (TextView) view.findViewById(R.id.tv_tapBadPre);

        healthKnow = (LinearLayout) view.findViewById(R.id.tds_health_know_layout);
        healthKnow.setOnClickListener(myListener);
        progressView = (UIXWaterDetailProgress) view.findViewById(R.id.progressView);
        progressView.startAnimation();
        tds_layout = (RelativeLayout) view.findViewById(R.id.tds_waterdetail_layout);
        chartView_layout = (RelativeLayout) view.findViewById(R.id.tds_chartview_layout);
        chartView_layout.setOnClickListener(myListener);
        tdsChartView = (TDSChartView) view.findViewById(R.id.chartView);
        laly_tds_value = (LinearLayout) view.findViewById(R.id.laly_tds_value);
//        tv_tds_distribution=(TextView)view.findViewById(R.id.tv_tds_distribution);
        ViewReturn.setProgressRelativeLayout(laly_tds_value);
        ViewReturn.setChartRelativeLayout(chartView_layout);

        tds_health_buy_layout = (LinearLayout) view.findViewById(R.id.tds_health_buy_layout);
        tds_health_buy_layout.setOnClickListener(myListener);
        tds_consult_layout = (LinearLayout) view.findViewById(R.id.tds_consult_layout);
        tds_consult_layout.setOnClickListener(myListener);
        if(OznerPreference.isLoginPhone(getContext())){
            tds_consult_layout.setVisibility(View.VISIBLE);
        }else{
            tds_consult_layout.setVisibility(View.GONE);
        }



        iv_tds_left = (ImageView) view.findViewById(R.id.iv_tds_left);
        iv_tds_left.setOnClickListener(myListener);
        iv_tds_right = (ImageView) view.findViewById(R.id.iv_tds_right);
        iv_tds_right.setOnClickListener(myListener);

        tv_tds_friendtext = (TextView) view.findViewById(R.id.tv_tds_friendtext);

        tds_friend_layout=(RelativeLayout)view.findViewById(R.id.tds_friend_layout);
        if(OznerPreference.isLoginPhone(getContext())){
            tds_friend_layout.setVisibility(View.VISIBLE);
        }else{
            tds_friend_layout.setVisibility(View.GONE);
        }

        iv_tds_line1=(ImageView)view.findViewById(R.id.iv_tds_line1);
        if(OznerPreference.isLoginPhone(getContext())){
            iv_tds_line1.setVisibility(View.VISIBLE);
        }else{
            iv_tds_line1.setVisibility(View.GONE);
        }


        iv_height=(ImageView)view.findViewById(R.id.iv_height);
        if(OznerPreference.isLoginPhone(getContext())){
            iv_height.setVisibility(View.VISIBLE);
        }else{
            iv_height.setVisibility(View.GONE);
        }

        tds_health_layout=(RelativeLayout)view.findViewById(R.id.tds_health_layout);
        if(OznerPreference.isLoginPhone(getContext())){
            tds_health_layout.setVisibility(View.VISIBLE);
        }else{
            tds_health_layout.setVisibility(View.GONE);
        }
    }

    MyListener myListener = new MyListener();

    private class MyListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_time_day:
                    changeDay();
                    break;
                case R.id.tv_time_week:
                    changeWeek();
                    break;
                case R.id.tv_time_month:
                    changeMonth();
                    break;
                case R.id.iv_tds_left:
                    if (flagLeft == 2) {
                        changeDay();
//                        flagLeft=3;
                    }
                    if (flagLeft == 3) {
                        changeWeek();
                        flagLeft = 2;
                    }
                    break;
                case R.id.iv_tds_right:
                    if (flagRight == 0) {
                        changeWeek();
                        flagRight = 3;
                    }
                    if (flagRight == 1) {
                        changeWeek();
                        flagRight = 3;
                    }
                    if (flagRight == 2) {
                        changeMonth();
//                        flags=1;
                    }
                    if (flagRight == 3) {
                        flagRight = 2;
                    }
                    break;
                case R.id.tds_chartview_layout:
                    chartView_layout.setVisibility(View.GONE);
                    laly_tds_value.setVisibility(View.VISIBLE);
                    break;
                case R.id.cup_toolbar_share:
                    //view转化成图片
                    if (tv_tds_friendtext.getText().toString() != null) {
                        int rank = Integer.parseInt(tv_tds_friendtext.getText().toString());
                        if (rankTds != 0) {
//                        Bitmap bitm = ShareView.createViewBitmap(getActivity(), "TDS", cup.Sensor().TDSFix,rank, rankTds);
//                        path = FileUtils.saveBitmap(bitm);
//                        ShareView.showShareToDialog(getActivity(), path);
                        } else {
                        }
                    }
                    break;
                case R.id.iv_tds_introduce:
                    getFragmentManager().beginTransaction().add(R.id.framen_main_con, new TDSIntroduceFragment()).addToBackStack(null).commit();
                    break;
                case R.id.tds_health_know_layout:
                    Intent intent = new Intent(getActivity(), WaterKnowActivity.class);
                    intent.putExtra(PageState.MAC, mac);
                    getActivity().startActivity(intent);
                    break;
                case R.id.tds_consult_layout:
                    getFragmentManager().beginTransaction().replace(R.id.framen_main_con, new CChatFragment()).commitAllowingStateLoss();
                    ((MainActivity) (getActivity())).pagenow = PageState.ZIXUNYEMIAN;
                    ((MainActivity) (getActivity())).footNavFragment.ShowContent(PageState.ZIXUNYEMIAN, "");

                    break;
                case R.id.tds_health_buy_layout:
                    getFragmentManager().popBackStack();
                    getFragmentManager().beginTransaction().replace(R.id.framen_main_con, new OznerMallFragment()).commitAllowingStateLoss();
                    ((MainActivity) (getActivity())).pagenow = PageState.SHANGCHEGYEMIAN;
                    ((MainActivity) (getActivity())).footNavFragment.ShowContent(PageState.SHANGCHEGYEMIAN, "");
                    break;
            }
        }
    }

    private void changeDay() {
        time_day.setSelected(false);
        time_week.setSelected(false);
        time_month.setSelected(false);
        progressView.set_bad_progress(hot);
        progressView.set_normal_progress(nor);
        progressView.set_good_progress(bad);
        progressView.startAnimation();
        if (adapterDay != null) {
            tdsChartView.setAdapter(adapterDay);
        } else {
            for (int i = 0; i < dataDay.length; i++) {
                dataDay[i] = 0;
            }

        }

        tv_tapHealthPre.setText(bad + "%");
        tv_tapGenericPre.setText(nor + "%");
        tv_tapBadPre.setText(hot + "%");
//        ViewReturn.setCountHot(hot);
//        ViewReturn.setCountBad(bad);
//        ViewReturn.setCountNor(nor);
//        tv_tds_distribution.setText(getResources().getString(R.string.tdsValue_days));
        iv_tds_left.setVisibility(View.INVISIBLE);
        iv_tds_right.setVisibility(View.VISIBLE);

        flagRight = 1;
        flagLeft = 1;
        time_day.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        time_month.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
        time_week.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
    }

    private void changeWeek() {
        time_day.setSelected(true);
        time_week.setSelected(true);
        time_month.setSelected(false);
        progressView.set_bad_progress(w_hot);
        progressView.set_normal_progress(w_nor);
        progressView.set_good_progress(w_bad);
        tv_tapHealthPre.setText(w_bad + "%");
        tv_tapGenericPre.setText(w_nor + "%");
        tv_tapBadPre.setText(w_hot + "%");
        progressView.startAnimation();
        if (adapterWeek != null) {
            tdsChartView.setAdapter(adapterWeek);
        } else {
            for (int i = 0; i < dataWeek.length; i++) {
                dataWeek[i] = 0;
            }
        }

//        tv_tds_distribution.setText(getResources().getString(R.string.tdsValue_weeks));
        iv_tds_left.setVisibility(View.VISIBLE);
        iv_tds_right.setVisibility(View.VISIBLE);
        flagRight = 2;
        flagLeft = 2;
        time_week.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        time_month.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
        time_day.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
    }

    private void changeMonth() {
        time_day.setSelected(true);
        time_week.setSelected(false);
        time_month.setSelected(true);
        progressView.set_bad_progress(m_hot);
        progressView.set_normal_progress(m_nor);
        progressView.set_good_progress(m_bad);
        progressView.startAnimation();
        if (adapterMonth != null) {
            tdsChartView.setAdapter(adapterMonth);
        } else {
            for (int i = 0; i < dataMonth.length; i++) {
                dataMonth[i] = 0;
            }
        }

        tv_tapHealthPre.setText(m_bad + "%");
        tv_tapGenericPre.setText(m_nor + "%");
        tv_tapBadPre.setText(m_hot + "%");
//        ViewReturn.setCountHot(m_hot);
//        ViewReturn.setCountBad(m_bad);
//        ViewReturn.setCountNor(m_nor);
//        tv_tds_distribution.setText(getResources().getString(R.string.tdsValue_months));
        iv_tds_left.setVisibility(View.VISIBLE);
        iv_tds_right.setVisibility(View.INVISIBLE);
        flagRight = 3;
        flagLeft = 3;
        time_month.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        time_day.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
        time_week.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
    }
}

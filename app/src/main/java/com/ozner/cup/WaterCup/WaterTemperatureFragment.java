package com.ozner.cup.WaterCup;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Cup;
import com.ozner.cup.CupRecord;
import com.ozner.cup.CupRecordList;
import com.ozner.cup.Main.BaseMainActivity;
import com.ozner.device.OznerDeviceManager;

import com.ozner.cup.CChat.CChatFragment;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.Device.OznerMallFragment;
import com.ozner.cup.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.UIView.ChartAdapter;
import com.ozner.cup.UIView.UIXTempChartView;
import com.ozner.cup.UIView.UIXWaterTemperatureProgress;
import com.ozner.cup.UIView.ViewReturn;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by taoran on 2015/11/27.
 */
public class WaterTemperatureFragment extends Fragment {

    private TextView time_day, time_week, time_month;
    private RelativeLayout tds_layout, chartView_layout;
    private UIXTempChartView tdsChartView;
    private ChartAdapter.ViewMode mode = ChartAdapter.ViewMode.Day;

    int[] dataDay = new int[24];
    int[] dataWeek = new int[7];
    int[] dataMonth = new int[31];
    ChartAdapter adapterDay, adapterWeek, adapterMonth;
    UIXWaterTemperatureProgress temperatureProgress;
    Date time;
    private String mac;
    private Cup cup;
    private LinearLayout healthKnow, temperature_health_buy_layout, temperature_consult_layout, laly_temperature_value;
    private RelativeLayout temperatureBack,temperature_friend_layout,temperature_health_layout;
    private MyListener myListener = new MyListener();
    int w_count = 0, w_countHot = 0, w_countCold = 0, w_countNor = 0;
    int count = 0, countHot = 0, countCold = 0, countNor = 0;
    int m_count = 0, m_countHot = 0, m_countCold = 0, m_countNor = 0;
    int w_hot = 0, w_bad = 0, w_nor = 0;
    int m_hot = 0, m_bad = 0, m_nor = 0;
    int hot = 0, bad = 0, nor = 0;

    private Toolbar toolbar;
    private TextView tdsText, tv_temperature_tapHealthPre,
            tv_temperature_tapGenericPre, tv_temperature_tapBadPre,
            tv_temperature_value, tv_temperature_distribution;

    private TextView tv_temperature_facetest;
    private ImageView iv_temperature_face, iv_temperature_left, iv_temperature_right,iv_temperature_line1,iv_height;


    private CupRecordList cupRecordList;
    private CupRecord[] cupRecords;
    private CupRecord cupRecordTemp;
    private int flagRight = 0, flagLeft = 0;


    public WaterTemperatureFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_cup_temperature, container, false);
        OznerApplication.changeTextFont((ViewGroup)view);
        mac = getArguments().getString("MAC");
        cup = (Cup) OznerDeviceManager.Instance().getDevice(mac);
        if (cup != null) {
            initView(view);//界面初始化
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UiUpdateAsyncTask asyncTask = new UiUpdateAsyncTask();
        asyncTask.execute("watertemp");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ((BaseMainActivity)getActivity()).isShouldResume=false;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((BaseMainActivity)getActivity()).isShouldResume=true;
    }
    private void initData() {
        //折线图数据的切换  taoran
        time = new Date(new Date().getTime() / 86400000 * 86400000);
        cupRecordList = cup.Volume();
        if (cupRecordList != null) {
            cupRecordTemp=cupRecordList.getRecordByDate(time);
            if(cupRecordTemp!=null){
                Log.e("dayTemp",cupRecordTemp.toString());
                countHot =cupRecordTemp.Temperature_High;
                countCold = cupRecordTemp.Temperature_Low;
                countNor = cupRecordTemp.Temperature_Mid;
                count =cupRecordTemp.Count;
                hot = countHot * 100 / count;
                bad = countCold * 100 / count;
                nor = 100-(hot+bad);
            }
            cupRecords = cupRecordList.getRecordByDate(time, CupRecordList.QueryInterval.Hour);
            if (cupRecords != null) {
                for (int i = 0; i < dataDay.length; i++) {
                    for (int j = 0; j < cupRecords.length; j++) {
                        Log.e("tag",cupRecords[j].Temperature_MAX+"");
                        if(50<=cupRecords[j].Temperature_MAX&&80>=cupRecords[j].Temperature_MAX) {
                            dataDay[cupRecords[j].start.getHours()] = cupRecords[j].Temperature_MAX+20;
                        }else{
                            try{
                                dataDay[cupRecords[j].start.getHours() - 1] = cupRecords[j].Temperature_MAX;
                            }catch(Exception ex){
                                ex.printStackTrace();
                                dataDay[cupRecords[j].start.getHours()] = cupRecords[j].Temperature_MAX;
                            }

                        }
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
                return 100;
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
//                        Log.e("tagWeek", cupRecords[j]+"========"+j);
                        if (cupRecords[j].start.getDay()!=0) {
                            if(50<=cupRecords[j].Temperature_MAX&&80>=cupRecords[j].Temperature_MAX) {
                                dataWeek[cupRecords[j].start.getDay() - 1] = cupRecords[j].Temperature_MAX+20;
                            }else{
                                dataWeek[cupRecords[j].start.getDay() - 1] = cupRecords[j].Temperature_MAX;
                            }
                            w_countHot = cupRecords[j].Temperature_High+w_countHot;
                            w_countCold = cupRecords[j].Temperature_Low+w_countCold;
                            w_countNor = cupRecords[j].Temperature_Mid+w_countNor;
                            w_count = cupRecords[j].Count+w_count;
                            w_hot = w_countHot * 100 / w_count;
                            w_bad = w_countCold * 100 / w_count;
                            w_nor = 100-(w_hot+w_bad);
                        }else {
                            Date today = new Date();
                            if (today.getDay() == 0) {
                                if(50<=cupRecords[j].Temperature_MAX&&80>=cupRecords[j].Temperature_MAX){
                                dataWeek[6] = cupRecords[j].Temperature_MAX+20;
                                }else {
                                    dataWeek[6] = cupRecords[j].Temperature_MAX;
                                }
                                w_countHot = cupRecords[j].Temperature_High+w_countHot;
                                w_countCold = cupRecords[j].Temperature_Low+w_countCold;
                                w_countNor = cupRecords[j].Temperature_Mid+w_countNor;
                                w_count = cupRecords[j].Count+w_count;
                                w_hot = w_countHot * 100 / w_count;
                                w_bad = w_countCold * 100 / w_count;
                                w_nor = 100-(w_hot+w_bad);
                            } else {
                                if(50<=cupRecords[j].Temperature_MAX&&80>=cupRecords[j].Temperature_MAX){
                                    dataWeek[today.getDay()-1] = cupRecords[j].Temperature_MAX=20;
                                }else{
                                    dataWeek[today.getDay()-1] = cupRecords[j].Temperature_MAX;
                                }
                                w_countHot = cupRecords[j].Temperature_High+w_countHot;
                                w_countCold = cupRecords[j].Temperature_Low+w_countCold;
                                w_countNor = cupRecords[j].Temperature_Mid+w_countNor;
                                w_count = cupRecords[j].Count+w_count;
                            w_hot = w_countHot * 100 / w_count;
                            w_bad = w_countCold * 100 / w_count;
                                w_nor = 100-(w_hot+w_bad);
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
                return 100;
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
//                        Log.e("tagMonth", cupRecords[j]+"========"+j);
                        if (cupRecords[j].start.getDate()!=0) {
                            if(50<=cupRecords[j].Temperature_MAX&&80>=cupRecords[j].Temperature_MAX) {
                                dataMonth[cupRecords[j].start.getDate() - 1] = cupRecords[j].Temperature_MAX+20;
                            }else{
                                dataMonth[cupRecords[j].start.getDate() - 1] = cupRecords[j].Temperature_MAX;
                            }
                            m_countHot = cupRecords[j].Temperature_High+m_countHot;
                            m_countCold = cupRecords[j].Temperature_Low+m_countCold;
                            m_countNor = cupRecords[j].Temperature_Mid+m_countNor;
                            m_count =cupRecords[j].Count+m_count;
                            m_hot = m_countHot * 100 / m_count;
                            m_bad = m_countCold * 100 / m_count;
                            m_nor = 100-(m_hot+m_bad);
                        }else{
                            if(50<=cupRecords[j].Temperature_MAX&&80>=cupRecords[j].Temperature_MAX){
                                dataMonth[cupRecordList.time.getDate() - 1] = cupRecords[j].Temperature_MAX+20;
                            }else {
                                dataMonth[cupRecordList.time.getDate() - 1] = cupRecords[j].Temperature_MAX;
                            }
                            m_countHot = cupRecords[j].Temperature_High+m_countHot;
                            m_countCold = cupRecords[j].Temperature_Low+m_countCold;
                            m_countNor = cupRecords[j].Temperature_Mid+m_countNor;
                            m_count =cupRecords[j].Count+m_count;
                            m_hot = m_countHot * 100 / m_count;
                            m_bad = m_countCold * 100 / m_count;
                            m_nor = 100-(m_hot+m_bad);
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
                            return 100;
                        }

                        @Override
                        public ViewMode getViewMode() {
                            return ViewMode.Month;
                        }
                    };
                }
            }
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
            temperatureProgress.set_bad_progress(hot);
            temperatureProgress.set_normal_progress(nor);
            temperatureProgress.set_good_progress(bad);
            temperatureProgress.startAnimation();

            tv_temperature_tapHealthPre.setText(bad + "%");
            tv_temperature_tapGenericPre.setText(nor+ "%");
            tv_temperature_tapBadPre.setText(hot+ "%");

            if (adapterDay != null)
                tdsChartView.setAdapter(adapterDay);
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {

        }
    }

    private void initView(View view) {
        OznerApplication.changeTextFont((ViewGroup) view);
        //时间tab的选择  taoran
        time_day = (TextView) view.findViewById(R.id.tv_temperature_time_day);
        time_week = (TextView) view.findViewById(R.id.tv_temperature_time_week);
        time_month = (TextView) view.findViewById(R.id.tv_temperature_time_month);
        tv_temperature_facetest = (TextView) view.findViewById(R.id.tv_temperature_facetest);
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
        tdsText = (TextView) view.findViewById(R.id.cup_toolbar_text);
        tdsText.setText(getResources().getString(R.string.temperature_text));
        iv_temperature_face = (ImageView) view.findViewById(R.id.iv_temperature_face);
        tv_temperature_value = (TextView) view.findViewById(R.id.tv_temperature_value);
        int temp = cup.Sensor().TemperatureFix;
        if (temp > 0 && temp <= CupRecord.Temperature_Low_Value) {
            tv_temperature_value.setText(getResources().getString(R.string.good_temperature));
            tv_temperature_facetest.setText(getResources().getString(R.string.waterTemp_faceClod));
            iv_temperature_face.setBackgroundResource(R.drawable.yiban);
        } else if (temp >CupRecord.Temperature_Low_Value && temp <= CupRecord.Temperature_High_Value) {
            tv_temperature_value.setText(getResources().getString(R.string.normal_temperature));
            tv_temperature_facetest.setText(getResources().getString(R.string.waterTemp_faceMid));
            iv_temperature_face.setBackgroundResource(R.drawable.lianghao);
        } else if (temp >CupRecord.Temperature_High_Value && temp <= 100) {
            tv_temperature_value.setText(getResources().getString(R.string.bad_temperature));
            tv_temperature_facetest.setText(getResources().getString(R.string.waterTemp_faceHot));
            iv_temperature_face.setBackgroundResource(R.drawable.jingbao);
        }
        tv_temperature_tapHealthPre = (TextView) view.findViewById(R.id.tv_temperature_tapHealthPre);
//        tv_temperature_tapHealthPre.setText(ViewReturn.getCountBad1() + "%");
        tv_temperature_tapGenericPre = (TextView) view.findViewById(R.id.tv_temperature_tapGenericPre);
//        tv_temperature_tapGenericPre.setText(ViewReturn.getCountNor1() + "%");
        tv_temperature_tapBadPre = (TextView) view.findViewById(R.id.tv_temperature_tapBadPre);
//        tv_temperature_tapBadPre.setText(ViewReturn.getCountHot1() + "%");
        laly_temperature_value = (LinearLayout) view.findViewById(R.id.laly_temperature_value);
        healthKnow = (LinearLayout) view.findViewById(R.id.temperature_health_know_layout);
        healthKnow.setOnClickListener(myListener);
        tds_layout = (RelativeLayout) view.findViewById(R.id.temperature_waterdetail_layout);
        chartView_layout = (RelativeLayout) view.findViewById(R.id.temperature_chartview_layout);
        chartView_layout.setOnClickListener(myListener);
//        tv_temperature_distribution = (TextView) view.findViewById(R.id.tv_temperature_distribution);
        ViewReturn.setProgressRelativeLayout(laly_temperature_value);
        ViewReturn.setChartRelativeLayout(chartView_layout);
        temperatureProgress = (UIXWaterTemperatureProgress) view.findViewById(R.id.temperature_progressView);
        temperatureProgress.setOnClickListener(myListener);
        temperatureProgress.startAnimation();
        tdsChartView = (UIXTempChartView) view.findViewById(R.id.temperature_chartView);

        temperature_health_buy_layout = (LinearLayout) view.findViewById(R.id.temperature_health_buy_layout);
        temperature_health_buy_layout.setOnClickListener(myListener);
        temperature_consult_layout = (LinearLayout) view.findViewById(R.id.temperature_consult_layout);
        temperature_consult_layout.setOnClickListener(myListener);
        if(OznerPreference.isLoginPhone(getContext())){
            temperature_consult_layout.setVisibility(View.VISIBLE);
        }else{
            temperature_consult_layout.setVisibility(View.GONE);
        }

//        temperature_friend_layout=(RelativeLayout)view.findViewById(R.id.temperature_friend_layout);
//        if(OznerPreference.isLoginPhone(getContext())){
//            temperature_friend_layout.setVisibility(View.VISIBLE);
//        }else{
//            temperature_friend_layout.setVisibility(View.GONE);
//        }
        iv_temperature_left = (ImageView) view.findViewById(R.id.iv_temperature_left);
        iv_temperature_left.setOnClickListener(myListener);
        iv_temperature_right = (ImageView) view.findViewById(R.id.iv_temperature_right);
        iv_temperature_right.setOnClickListener(myListener);


        iv_temperature_line1=(ImageView)view.findViewById(R.id.iv_temperature_line1);
        if(OznerPreference.isLoginPhone(getContext())){
            iv_temperature_line1.setVisibility(View.VISIBLE);
        }else{
            iv_temperature_line1.setVisibility(View.GONE);
        }

        temperature_health_layout=(RelativeLayout)view.findViewById(R.id.temperature_health_layout);
        if(OznerPreference.isLoginPhone(getContext())){
            temperature_health_layout.setVisibility(View.VISIBLE);
        }else{
            temperature_health_layout.setVisibility(View.GONE);
        }

        iv_height=(ImageView)view.findViewById(R.id.iv_height);
        if(OznerPreference.isLoginPhone(getContext())){
            iv_height.setVisibility(View.VISIBLE);
        }else{
            iv_height.setVisibility(View.GONE);
        }

    }

    private class MyListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_temperature_time_day:
                    changeDay();

                    break;
                case R.id.tv_temperature_time_week:
                    changeWeek();

                    break;
                case R.id.tv_temperature_time_month:
                    changeMonth();

                    break;

                case R.id.iv_temperature_left:
                    if (flagLeft == 2) {
                        changeDay();
//                        flagLeft=3;
                    }
                    if (flagLeft == 3) {
                        changeWeek();
                        flagLeft = 2;
                    }
                    break;
                case R.id.iv_temperature_right:
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

                case R.id.temperature_chartview_layout:
                    chartView_layout.setVisibility(View.GONE);
                    laly_temperature_value.setVisibility(View.VISIBLE);
                    break;
                case R.id.temperature_health_know_layout:
                    Intent intent = new Intent(getActivity(), WaterKnowActivity.class);
                    intent.putExtra(PageState.MAC, mac);
                    getActivity().startActivity(intent);
                    break;
                case R.id.temperature_consult_layout:
                    getFragmentManager().beginTransaction().replace(R.id.framen_main_con, new CChatFragment()).commitAllowingStateLoss();
                    //改变底部导航栏指示灯
                    ((MainActivity) (getActivity())).pagenow = PageState.ZIXUNYEMIAN;
                    ((MainActivity) (getActivity())).footNavFragment.ShowContent(PageState.ZIXUNYEMIAN, "");
                    break;
                case R.id.temperature_health_buy_layout:
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

        temperatureProgress.set_bad_progress(hot);
        temperatureProgress.set_normal_progress(nor);
        temperatureProgress.set_good_progress(bad);

        temperatureProgress.startAnimation();
        if(adapterDay!=null){
            tdsChartView.setAdapter(adapterDay);
        }else{
            for(int i=0;i<dataDay.length;i++){
                dataDay[i]=0;
            }
        }

        tv_temperature_tapHealthPre.setText(bad + "%");
        tv_temperature_tapGenericPre.setText(nor + "%");
        tv_temperature_tapBadPre.setText(hot + "%");

        time_day.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        time_month.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
        time_week.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
        //日水温
//        ViewReturn.setCountHot1(hot);
//        ViewReturn.setCountBad1(bad);
//        ViewReturn.setCountNor1(nor);
//        tv_temperature_distribution.setText(getResources().getString(R.string.temperature_distri));
        iv_temperature_left.setVisibility(View.INVISIBLE);
        iv_temperature_right.setVisibility(View.VISIBLE);

        flagRight = 1;
        flagLeft = 1;
    }

    private void changeWeek() {
        time_day.setSelected(true);
        time_week.setSelected(true);
        time_month.setSelected(false);
        time_week.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        time_month.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
        time_day.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
        if(adapterWeek!=null) {
            tdsChartView.setAdapter(adapterWeek);
        }else{
            for(int i=0;i<dataWeek.length;i++){
                dataWeek[i]=0;
            }
        }
        temperatureProgress.set_bad_progress(w_hot);
        temperatureProgress.set_normal_progress(w_nor);
        temperatureProgress.set_good_progress(w_bad);
        temperatureProgress.startAnimation();
        tv_temperature_tapHealthPre.setText(w_bad + "%");
        tv_temperature_tapGenericPre.setText(w_nor + "%");
        tv_temperature_tapBadPre.setText(w_hot + "%");
        //周水温
//        ViewReturn.setCountHot1(w_hot);
//        ViewReturn.setCountBad1(w_bad);
//        ViewReturn.setCountNor1(w_nor);
//        tv_temperature_distribution.setText(getResources().getString(R.string.tempValue_weeks));
        iv_temperature_left.setVisibility(View.VISIBLE);
        iv_temperature_right.setVisibility(View.VISIBLE);
        flagRight = 2;
        flagLeft = 2;
    }

    private void changeMonth() {
        time_day.setSelected(true);
        time_week.setSelected(false);
        time_month.setSelected(true);
        if(adapterMonth!=null) {
            tdsChartView.setAdapter(adapterMonth);
        }else{
            for(int i=0;i<dataMonth.length;i++){
                dataMonth[i]=0;
            }
        }
        tdsChartView.setAdapter(adapterMonth);
        temperatureProgress.set_bad_progress(m_hot);
        temperatureProgress.set_normal_progress(m_nor);
        temperatureProgress.set_good_progress(m_bad);
        temperatureProgress.startAnimation();

        tv_temperature_tapHealthPre.setText(m_bad + "%");
        tv_temperature_tapGenericPre.setText(m_nor + "%");
        tv_temperature_tapBadPre.setText(m_hot + "%");

        //月水温
//        ViewReturn.setCountHot1(m_hot);
//        ViewReturn.setCountBad1(m_bad);
//        ViewReturn.setCountNor1(m_nor);
//        tv_temperature_distribution.setText(getResources().getString(R.string.tempValue_months));
        iv_temperature_left.setVisibility(View.VISIBLE);
        iv_temperature_right.setVisibility(View.INVISIBLE);
        flagRight = 3;
        flagLeft = 3;
        time_month.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        time_day.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
        time_week.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTds));
    }
}

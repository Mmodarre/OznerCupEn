package com.ozner.cup.WaterCup;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.cup.Cup;
import com.ozner.cup.CupRecord;
import com.ozner.cup.CupRecordList;
import com.ozner.cup.CChat.CChatFragment;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.Device.OznerMallFragment;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.MainActivity;
import com.ozner.cup.MainEnActivity;
import com.ozner.cup.R;
import com.ozner.cup.UIView.ChartAdapter;
import com.ozner.cup.UIView.UIXVolumeChartView;
import com.ozner.cup.UIView.UIXVolumeChartViewWeek;
import com.ozner.device.OznerDeviceManager;

import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaterQuantityFragment extends Fragment {

    private TextView tv1, tv2, tv3;
    private UIXVolumeChartView volumeChartView;
    private UIXVolumeChartViewWeek volumeChartViewWeek;
    ChartAdapter.ViewMode mode = ChartAdapter.ViewMode.Day;
    private LinearLayout healthKnow, wateryield_consult_layout, wateryield_health_buy_layout;
    int[] dataDay = new int[24];
    int[] dataWeek = new int[7];
    int[] dataMonth = new int[31];
    Cup cup;
    ChartAdapter adapterDay, adapterWeek, adapterMonth;
    private MyListener myListener = new MyListener();
    Date time;
    int hour;
    private String mac;
    private Toolbar toolbar;
    //    private ImageButton waterShare;
    private TextView waterText, tv_wateryield_friendtext, tv_wateryield_value, tv_wateryield_facetest, tv_wateryield_distribution;
    private ImageView iv_wateryield_face;
    private int volum, rank;
    private CupRecordList cupRecordList;
    private CupRecord cupRecord;
    private CupRecord[] cupRecords;
    float volumCount;
    int volumDrink;
    private String imagePath;
    private Handler waterhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    rank = msg.arg1;
                    tv_wateryield_friendtext.setText(msg.arg1 + "");
                    break;
            }
        }
    };

    public WaterQuantityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_cup_wateryield, container, false);
        mac = getArguments().getString("MAC");
        cup = (Cup) OznerDeviceManager.Instance().getDevice(mac);
        volum = getArguments().getInt("volum");
        try {
            volumCount = Float.parseFloat(cup.getAppValue(PageState.DRINK_GOAL).toString());
            if (volum <= volumCount) {
                volumDrink = (int) ((volum * 100) / volumCount);
            } else {
                volumDrink = 100;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        initView(view);//界面初始化
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UiUpdateAsyncTask asyncTask = new UiUpdateAsyncTask();
        asyncTask.execute("water");
    }

    @Override
    public void onResume() {
        super.onResume();
        initView(getView());
        if(((OznerApplication)(getActivity().getApplication())).isLanguageCN()) {
            ((MainActivity) getActivity()).isShouldResume = false;
        }else{
            ((MainEnActivity) getActivity()).isShouldResume = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(((OznerApplication)(getActivity().getApplication())).isLanguageCN()) {
            ((MainActivity) getActivity()).isShouldResume = true;
        }else{
            ((MainEnActivity) getActivity()).isShouldResume = true;
        }
    }

    private void initData() {
//        getWaterRank();
        //好友排名
//        NetJsonObject volmnRank = OznerCommand.VolumeSensor(getActivity(), mac, cup.Type(), volum);
//        if (volmnRank.state > 0) {
//            try {
//                Message message = new Message();
//                message.arg1 = volmnRank.state;
//                message.what = 1;
//                try {
//                    Thread.sleep(2000);
//                    waterhandler.sendMessage(message);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }


        //日
        time = new Date(new Date().getTime() / 86400000 * 86400000);
        cupRecordList = cup.Volume();
        if (cupRecordList != null) {
            cupRecords = cupRecordList.getRecordByDate(time, CupRecordList.QueryInterval.Hour);
            if (cupRecords != null) {
                for (int i = 0; i < dataDay.length; i++) {
                    for (int j = 0; j < cupRecords.length; j++) {
                        dataDay[cupRecords[j].start.getHours()] = cupRecords[j].Volume;
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
                return 400;
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
            cupRecords = cupRecordList.getRecordByDate(timeWeek, CupRecordList.QueryInterval.Week);
            if (cupRecords != null) {
                for (int i = 0; i < dataWeek.length; i++) {
                    for (int j = 0; j < cupRecords.length; j++) {
                        if (cupRecords[j].start.getDay() != 0) {
                            dataWeek[cupRecords[j].start.getDay()] = cupRecords[j].Volume;
                        } else {
                            Date today = new Date();
                            if (today.getDay() == 0) {
                                dataWeek[6] = cupRecords[j].Volume;
                            } else {
                                dataWeek[today.getDay()] = cupRecords[j].Volume;
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
                return 3000;
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
                        if (cupRecords[j].start.getDate() != 0) {
                            dataMonth[cupRecords[j].start.getDate() - 1] = cupRecords[j].Volume;
                        } else {
                            dataMonth[4] = cupRecords[j].Volume;
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
                            return 3000;
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

    private void getWaterRank() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetJsonObject volmnRank = OznerCommand.VolumeSensor(getActivity(), mac, cup.Type(), volum);
                if (volmnRank.state > 0) {
                    try {
                        Message message = new Message();
                        message.arg1 = volmnRank.state;
                        message.what = 1;
                        try {
                            Thread.sleep(2000);
                            waterhandler.sendMessage(message);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private class UiUpdateAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            initData();
//            if(rank!=0){

            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {

        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            if (adapterDay != null)
                volumeChartView.setAdapter(adapterDay);
        }

        //onCancelled方法用于在取消执行中的任务时更改UI
        @Override
        protected void onCancelled() {

        }
    }

    private void initView(View view) {
        OznerApplication.changeTextFont((ViewGroup) view);
        //日周月选择器  taoran
        tv_wateryield_value = (TextView) view.findViewById(R.id.tv_wateryield_value);
        tv_wateryield_value.setText(volumDrink + "%");
        OznerApplication.setControlNumFace(tv_wateryield_value);
        iv_wateryield_face = (ImageView) view.findViewById(R.id.iv_wateryield_face);
        tv_wateryield_facetest = (TextView) view.findViewById(R.id.tv_wateryield_facetest);
        tv_wateryield_distribution = (TextView) view.findViewById(R.id.tv_wateryield_distribution);
        if (volumDrink <= 30) {
            iv_wateryield_face.setBackgroundResource(R.drawable.jingbao);
            tv_wateryield_facetest.setText(getResources().getString(R.string.water_faceLow));
        } else if (volumDrink > 30 && volumDrink <= 60) {
            iv_wateryield_face.setBackgroundResource(R.drawable.yiban);
            tv_wateryield_facetest.setText(getResources().getString(R.string.water_faceMid));
        } else if (volumDrink > 60 && volumDrink <= 100) {
            iv_wateryield_face.setBackgroundResource(R.drawable.lianghao);
            tv_wateryield_facetest.setText(getResources().getString(R.string.water_faceHight));
        }


        tv1 = (TextView) view.findViewById(R.id.tv_wateryield_time_day);
        tv2 = (TextView) view.findViewById(R.id.tv_wateryield_time_week);
        tv3 = (TextView) view.findViewById(R.id.tv_wateryield_time_month);
        tv1.setOnClickListener(myListener);
        tv2.setOnClickListener(myListener);
        tv3.setOnClickListener(myListener);

        toolbar = (Toolbar) view.findViewById(R.id.cup_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
//        waterShare = (ImageButton) view.findViewById(R.id.cup_toolbar_share);
//        waterShare.setVisibility(View.VISIBLE);
//        waterShare.setOnClickListener(myListener);
        waterText = (TextView) view.findViewById(R.id.cup_toolbar_text);
        waterText.setText(getResources().getString(R.string.wateryield_text));
        healthKnow = (LinearLayout) view.findViewById(R.id.wateryield_health_know_layout);
        healthKnow.setOnClickListener(myListener);
        volumeChartView = (UIXVolumeChartView) view.findViewById(R.id.volumeView);
        volumeChartViewWeek = (UIXVolumeChartViewWeek) view.findViewById(R.id.volumeViewWeek);
        wateryield_health_buy_layout = (LinearLayout) view.findViewById(R.id.wateryield_health_buy_layout);
        wateryield_health_buy_layout.setOnClickListener(myListener);
        wateryield_consult_layout = (LinearLayout) view.findViewById(R.id.wateryield_consult_layout);
        wateryield_consult_layout.setOnClickListener(myListener);
        tv_wateryield_friendtext = (TextView) view.findViewById(R.id.tv_wateryield_friendtext);
        OznerApplication.setControlNumFace(tv_wateryield_friendtext);
    }

    private class MyListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_wateryield_time_day:
                    tv1.setSelected(false);
                    tv2.setSelected(false);
                    tv3.setSelected(false);
                    volumeChartView.setVisibility(View.VISIBLE);
                    volumeChartViewWeek.setVisibility(View.GONE);
                    if (adapterDay != null) {
                        volumeChartView.setAdapter(adapterDay);
                    } else {
                        for (int i = 0; i < dataDay.length; i++) {
                            dataDay[i] = 0;
                        }
                    }
                    break;
                case R.id.tv_wateryield_time_week:
                    tv1.setSelected(true);
                    tv2.setSelected(true);
                    tv3.setSelected(false);
                    volumeChartView.setVisibility(View.GONE);
                    volumeChartViewWeek.setVisibility(View.VISIBLE);
                    if (adapterWeek != null) {
                        volumeChartViewWeek.setAdapter(adapterWeek);
                    } else {
                        for (int i = 0; i < dataWeek.length; i++) {
                            dataWeek[i] = 0;
                        }
                    }
                    break;
                case R.id.tv_wateryield_time_month:
                    tv1.setSelected(true);
                    tv2.setSelected(false);
                    tv3.setSelected(true);
                    volumeChartView.setVisibility(View.GONE);
                    volumeChartViewWeek.setVisibility(View.VISIBLE);
                    if (adapterMonth != null) {
                        volumeChartViewWeek.setAdapter(adapterMonth);
                    } else {
                        for (int i = 0; i < dataWeek.length; i++) {
                            dataWeek[i] = 0;
                        }
                    }
                    break;
                case R.id.cup_toolbar_share:
//                    Bitmap bitm = ShareView.createViewBitmap(getActivity(), "WATER", volum, rank, (int) volumCount);
//                    imagePath = FileUtils.saveBitmap(bitm);
//                    Log.e("tags", imagePath);
//                    ShareView.showShareToDialog(getActivity(), imagePath);
                    break;
                case R.id.wateryield_health_know_layout:
//                    getFragmentManager().beginTransaction().add(R.id.framen_main_con, new WaterKnowFragment()).addToBackStack(null).commit();
//                    getActivity().getSupportFragmentManager().executePendingTransactions();
                    Intent intent = new Intent(getActivity(), WaterKnowActivity.class);
                    intent.putExtra(PageState.MAC, mac);
                    getActivity().startActivity(intent);
                    break;
                case R.id.wateryield_consult_layout:
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
            }
        }
    }
}

package com.ozner.cup.WaterProbe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.cup.CChat.CChatFragment;
import com.ozner.cup.Command.CenterUrlContants;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.OznerDataHttp;
import com.ozner.cup.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.WaterProbe.WaterReplenishMeter.UIWRMView;
import com.ozner.cup.mycenter.WebActivity;
import com.ozner.device.OznerDeviceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by mengdongya on 2016/3/8.
 */
public class SkinDetailFragment extends Fragment implements View.OnClickListener {
    ImageView iv_face, iv_hands, iv_eyes, iv_bozi;
    TextView tv_week, tv_month, tv_skin_water, tv_skin_state, tv_skin_lastdata, tv_skin_average, tv_query_parts, toolbar_text;
    Toolbar toolbar;
    UIWRMView uiwrmView;
    LinearLayout llay_bottom_btn;
    int[] faceWaterM = new int[31];
    int[] faceOilyM = new int[31];
    int[] faceWaterW = new int[7];
    int[] faceOilyW = new int[7];
    int[] handsWaterM = new int[31];
    int[] handsOilyM = new int[31];
    int[] handsWaterW = new int[7];
    int[] handsOilyW = new int[7];
    int[] eyesWaterM = new int[31];
    int[] eyesOilyM = new int[31];
    int[] eyesWaterW = new int[7];
    int[] eyesOilyW = new int[7];
    int[] neckWaterM = new int[31];
    int[] neckOilyM = new int[31];
    int[] neckWaterW = new int[7];
    int[] neckOilyW = new int[7];
    String queryFace, queryHands, queryEyes, queryNeck = null;
    int queryFaceTimes, queryHandsTimes, queryEyesTimes, queryNeckTimes, todayValue = 0;
    SharedPreferences sh;
    String Mac;
    String mobile, usertoken;
    int state = 0, faceTotalValue, handTotalValue, neckTotalValue, eyesTotalValue = 0;
    int faceTodayValue, handTodayValue, neckTodayValue, eyesTodayValue = 0;
    WaterReplenishmentMeter waterReplenishmentMeter;
    Date dateTime = new Date();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Mac = getArguments().getString("MAC");
        state = Integer.parseInt(getArguments().getString("state"));
        waterReplenishmentMeter = (WaterReplenishmentMeter) OznerDeviceManager.Instance().getDevice(Mac);
        View view = inflater.inflate(R.layout.fragment_skin_detail, container, false);
        sh = getActivity().getSharedPreferences("SkinValues", Context.MODE_PRIVATE);
//        GetWaterRMAsyncTask getWaterRMAsyncTask = new GetWaterRMAsyncTask();
//        getWaterRMAsyncTask.execute();
        initView(view);
        initClick(view);
        OznerApplication.changeTextFont((ViewGroup) view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mobile = UserDataPreference.GetUserData(getContext(), UserDataPreference.Mobile, null);
        usertoken = OznerPreference.UserToken(getActivity());
    }

    private void initClick(View view) {
        llay_bottom_btn = (LinearLayout) view.findViewById(R.id.llay_bottom_btn);
        view.findViewById(R.id.ll_face).setOnClickListener(this);
        view.findViewById(R.id.ll_eyes).setOnClickListener(this);
        view.findViewById(R.id.ll_hands).setOnClickListener(this);
        view.findViewById(R.id.ll_bozi).setOnClickListener(this);
        tv_week.setOnClickListener(this);
        tv_month.setOnClickListener(this);
        view.findViewById(R.id.show_oily_instru).setOnClickListener(this);
        view.findViewById(R.id.show_water_instru).setOnClickListener(this);
        view.findViewById(R.id.skin_zixun).setOnClickListener(this);
        if (((OznerApplication) getActivity().getApplication()).isLoginPhone()) {
            llay_bottom_btn.setVisibility(View.VISIBLE);
            view.findViewById(R.id.skin_buy_jinghua).setOnClickListener(this);
            view.findViewById(R.id.skin_buy_jinghua).setVisibility(View.VISIBLE);
        } else {
            llay_bottom_btn.setVisibility(View.GONE);
            view.findViewById(R.id.skin_buy_jinghua).setVisibility(View.GONE);
        }
    }

    private void initData() {
        queryEyes = sh.getString(Mac + "eyes", "0");
        queryFace = sh.getString(Mac + "face", "0");
        queryHands = sh.getString(Mac + "hands", "0");
        queryNeck = sh.getString(Mac + "neck", "0");
    }

    private void setData() {
        switchWeek(true);
        int n = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        //修正周序号越界
        if (n == 1) {
            n = 6;
        } else {
            n -= 2;
        }
        int m = faceOilyW[n];
        if (m <= 12) {
            tv_skin_state.setText(getString(R.string.skin_dry));
        } else if (m > 12 && m <= 20) {
            tv_skin_state.setText(getString(R.string.skin_mid));
        } else if (m > 20) {
            tv_skin_state.setText(getString(R.string.skin_oily));
        }
        switch (state) {
            case 1:
                iv_face.setSelected(true);
                todayValue = faceTodayValue;
                try {
                    tv_query_parts.setText(getResources().getString(R.string.part_face));
                } catch (Exception e) {
                }
                tv_skin_lastdata.setText(queryFace + "%");
                if (todayValue != 0) {
                    tv_skin_water.setText(todayValue + "");
                    if (todayValue <= 32) {
                        tv_skin_state.setText(getString(R.string.dry));
                    } else if (todayValue > 32 && todayValue <= 42) {
                        tv_skin_state.setText(getString(R.string.normal));
                    } else if (todayValue > 42) {
                        tv_skin_state.setText(getString(R.string.wetness));
                    }
                } else {
                    tv_skin_water.setText("0");
                    tv_skin_state.setText(getString(R.string.text_null));
                }

                if (queryFaceTimes != 0) {
                    float a = faceTotalValue / queryFaceTimes;
//                    tv_skin_average.setText(faceTotalValue / queryFaceTimes + "%");
                    String avg = String.format(getString(R.string.avg_times), a, queryFaceTimes);
                    tv_skin_average.setText(avg);
                } else {
                    tv_skin_average.setText(String.format(getResources().getString(R.string.avg_times), 0.0f, 0));
                }
//                tv_times.setText("(" + queryFaceTimes + "次)");
                break;
            case 2:
                iv_hands.setSelected(true);
                todayValue = handTodayValue;
                try {
                    tv_query_parts.setText(getResources().getString(R.string.part_hand));
                } catch (Exception e) {
                }
                tv_skin_lastdata.setText(queryHands + "%");
                if (todayValue != 0) {
                    tv_skin_water.setText(todayValue + "");
                    if (todayValue <= 30) {
                        tv_skin_state.setText(getString(R.string.dry));
                    } else if (todayValue > 30 && todayValue <= 38) {
                        tv_skin_state.setText(getString(R.string.normal));
                    } else if (todayValue > 38) {
                        tv_skin_state.setText(getString(R.string.wetness));
                    }
                } else {
                    tv_skin_water.setText("0");
                    tv_skin_state.setText(getString(R.string.text_null));
                }
                if (queryHandsTimes != 0) {
//                    tv_skin_average.setText(handTotalValue / queryHandsTimes + "%");
                    float a = handTotalValue / queryHandsTimes;
                    String avg = String.format(getString(R.string.avg_times), a, queryHandsTimes);
                    tv_skin_average.setText(avg);
                } else {
                    tv_skin_average.setText(String.format(getResources().getString(R.string.avg_times), 0.0f, 0));
                }
//                tv_times.setText("(" + queryHandsTimes + "次)");
                break;
            case 3:
                iv_eyes.setSelected(true);
                todayValue = eyesTodayValue;
                try {
                    tv_query_parts.setText(getResources().getString(R.string.part_eyes));
                } catch (Exception e) {
                }
                tv_skin_lastdata.setText(queryEyes + "%");
                if (todayValue != 0) {
                    tv_skin_water.setText(todayValue + "");
                    if (todayValue <= 35) {
                        tv_skin_state.setText(getString(R.string.dry));
                    } else if (todayValue > 35 && todayValue <= 45) {
                        tv_skin_state.setText(getString(R.string.normal));
                    } else if (todayValue > 45) {
                        tv_skin_state.setText(getString(R.string.wetness));
                    }
                } else {
                    tv_skin_water.setText("0");
                    tv_skin_state.setText(getString(R.string.text_null));
                }
                if (queryEyesTimes != 0) {
//                    tv_skin_average.setText(eyesTotalValue / queryEyesTimes + "%");
                    float a = eyesTotalValue / queryEyesTimes;
                    String avg = String.format(getString(R.string.avg_times), a, queryEyesTimes);
                    tv_skin_average.setText(avg);
                } else {
                    tv_skin_average.setText(String.format(getResources().getString(R.string.avg_times), 0.0f, 0));
                }
//                tv_times.setText("(" + queryEyesTimes + "次)");
                break;
            case 4:
                iv_bozi.setSelected(true);
                todayValue = neckTodayValue;
                try {
                    tv_query_parts.setText(getResources().getString(R.string.part_bozi));
                } catch (Exception e) {
                }
                tv_skin_lastdata.setText(queryNeck + "%");
                if (todayValue != 0) {
                    tv_skin_water.setText(todayValue + "");
                    if (todayValue <= 35) {
                        tv_skin_state.setText(getString(R.string.dry));
                    } else if (todayValue > 35 && todayValue <= 45) {
                        tv_skin_state.setText(getString(R.string.normal));
                    } else if (todayValue > 45) {
                        tv_skin_state.setText(getString(R.string.wetness));
                    }
                } else {
                    tv_skin_water.setText("0");
                    tv_skin_state.setText(getString(R.string.text_null));
                }
                if (queryNeckTimes != 0) {
//                    tv_skin_average.setText(neckTotalValue / queryNeckTimes + "%");
                    float a = neckTotalValue / queryNeckTimes;
                    String avg = String.format(getString(R.string.avg_times), a, queryNeckTimes);
                    tv_skin_average.setText(avg);
                } else {
                    tv_skin_average.setText(String.format(getResources().getString(R.string.avg_times), 0.0f, 0));
                }
//                tv_times.setText("(" + queryNeckTimes + getString(R.string.count)+")");
                break;
            default:
                break;

        }
    }

    private void initView(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.cup_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        if (((OznerApplication) (getActivity().getApplication())).isLoginPhone()) {
            view.findViewById(R.id.llay_cupHolder).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.llay_cupHolder).setVisibility(View.GONE);
        }

        toolbar_text = (TextView) view.findViewById(R.id.cup_toolbar_text);
        toolbar_text.setText(getResources().getString(R.string.water_repl_meter_detail));
        iv_face = (ImageView) view.findViewById(R.id.iv_face);
        iv_hands = (ImageView) view.findViewById(R.id.iv_hands);
        iv_eyes = (ImageView) view.findViewById(R.id.iv_eyes);
        iv_bozi = (ImageView) view.findViewById(R.id.iv_bozi);
        tv_week = (TextView) view.findViewById(R.id.tv_week);
        tv_month = (TextView) view.findViewById(R.id.tv_month);
        tv_skin_water = (TextView) view.findViewById(R.id.tv_skin_water);  //皮肤湿度，大号字体的那个
        tv_skin_state = (TextView) view.findViewById(R.id.tv_skin_state); //今日肌肤状态
        tv_skin_lastdata = (TextView) view.findViewById(R.id.tv_skin_lastdata); //上次检测值
        tv_skin_average = (TextView) view.findViewById(R.id.tv_skin_average); //平均值
//        tv_times = (TextView) view.findViewById(R.id.tv_times);  //检测次数
        tv_query_parts = (TextView) view.findViewById(R.id.tv_query_parts);  //部位   脸部，手，眼，颈
        uiwrmView = (UIWRMView) view.findViewById(R.id.wrm_View);
        switchWeek(true);
//        uiwrmView.setWeekData(listOilyW, listWaterW);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onResume() {
        super.onResume();
        new GetWaterRMAsyncTask().execute();
        new GetTimesAsyncTask().execute();
        initData();
        setData();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ll_face:
                iv_face.setSelected(true);
                iv_eyes.setSelected(false);
                iv_bozi.setSelected(false);
                iv_hands.setSelected(false);
                tv_query_parts.setText(getResources().getString(R.string.part_face));
                tv_skin_lastdata.setText(queryFace + "%");
                new GetWaterRMAsyncTask().execute();
                state = 1;
                setData();
                switchWeek(!tv_month.isSelected());
                break;
            case R.id.ll_eyes:
                iv_face.setSelected(false);
                iv_eyes.setSelected(true);
                iv_bozi.setSelected(false);
                iv_hands.setSelected(false);
                tv_query_parts.setText(getResources().getString(R.string.part_eyes));
                tv_skin_lastdata.setText(queryEyes + "%");
                new GetWaterRMAsyncTask().execute();
                state = 3;
                setData();
                switchWeek(!tv_month.isSelected());
                break;
            case R.id.ll_hands:
                iv_face.setSelected(false);
                iv_eyes.setSelected(false);
                iv_bozi.setSelected(false);
                iv_hands.setSelected(true);
                tv_query_parts.setText(getResources().getString(R.string.part_hand));
                tv_skin_lastdata.setText(queryHands + "%");
                new GetWaterRMAsyncTask().execute();
                state = 2;
                setData();
                switchWeek(!tv_month.isSelected());
                break;
            case R.id.ll_bozi:
                iv_face.setSelected(false);
                iv_eyes.setSelected(false);
                iv_bozi.setSelected(true);
                iv_hands.setSelected(false);
                tv_query_parts.setText(getResources().getString(R.string.part_bozi));
                tv_skin_lastdata.setText(queryNeck + "%");
                new GetWaterRMAsyncTask().execute();
                state = 4;
                setData();
                switchWeek(!tv_month.isSelected());
                break;
            case R.id.tv_week:
                tv_week.setSelected(false);
                tv_week.setTextColor(getResources().getColor(R.color.white));
                tv_month.setSelected(false);
                tv_month.setTextColor(getResources().getColor(R.color.colorTds));
                switchWeek(true);
//                uiwrmView.setWeekData(listOilyW, listWaterW);
                break;
            case R.id.tv_month:
                tv_month.setSelected(true);
                tv_month.setTextColor(getResources().getColor(R.color.white));
                tv_week.setSelected(true);
                tv_week.setTextColor(getResources().getColor(R.color.colorTds));
                switchWeek(false);
//                uiwrmView.setMonthData(listOilyM, listWaterM);
                break;
            case R.id.show_oily_instru:
                getFragmentManager().beginTransaction().replace(R.id.framen_main_con, new WaterReplenishOilIntroduceFragment()).addToBackStack("instru").commitAllowingStateLoss();
                break;
            case R.id.show_water_instru:
                getFragmentManager().beginTransaction().replace(R.id.framen_main_con, new WaterReplenishIntroduceFragment()).addToBackStack("wainstru").commitAllowingStateLoss();
                break;
            case R.id.skin_zixun:
                getFragmentManager().beginTransaction().replace(R.id.framen_main_con, new CChatFragment()).commitAllowingStateLoss();
                ((MainActivity) (getActivity())).pagenow = PageState.ZIXUNYEMIAN;
                ((MainActivity) (getActivity())).footNavFragment.ShowContent(PageState.ZIXUNYEMIAN, "");
                break;
            case R.id.skin_buy_jinghua:
                Intent shopIntent = new Intent(getContext(), WebActivity.class);
                String shopUrl = CenterUrlContants.formatBuyReplenWaterUrl(mobile, usertoken, "zh", "zh");
                Log.e("tag", "购买精华液链接:" + shopUrl);
                shopIntent.putExtra(WebActivity.URL, shopUrl);
                startActivity(shopIntent);
                break;
        }
    }

    private void switchWeek(boolean isWeek) {
        switch (state) {
            case 1:
                if (isWeek) {
                    uiwrmView.setWeekData(faceOilyW, faceWaterW);
                } else {
                    uiwrmView.setMonthData(faceOilyM, faceWaterM);
                }
                break;
            case 2:
                if (isWeek) {
                    uiwrmView.setWeekData(handsOilyW, handsWaterW);
                } else {
                    uiwrmView.setMonthData(handsOilyM, handsWaterM);
                }
                break;
            case 3:
                if (isWeek) {
                    uiwrmView.setWeekData(eyesOilyW, eyesWaterW);
                } else {
                    uiwrmView.setMonthData(eyesOilyM, eyesWaterM);
                }
                break;
            case 4:
                if (isWeek) {
                    uiwrmView.setWeekData(neckOilyW, neckWaterW);
                } else {
                    uiwrmView.setMonthData(neckOilyM, neckWaterM);
                }
                break;
        }
    }

    class GetWaterRMAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params != null) {
                List<NameValuePair> pars = new ArrayList<>();
                pars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(getContext())));
                pars.add(new BasicNameValuePair("mac", Mac));
                String filterUrl = OznerPreference.ServerAddress(getContext()) + "OznerServer/GetBuShuiFenBu";
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(getContext(), filterUrl, pars);
//                Log.e("123456", "skinDetail+" + netJsonObject.value);
                return netJsonObject;
            }
            return null;

        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            faceTotalValue = 0;
            handTotalValue = 0;
            eyesTotalValue = 0;
            neckTotalValue = 0;
            faceTodayValue = 0;
            handTodayValue = 0;
            eyesTodayValue = 0;
            neckTodayValue = 0;
            if (netJsonObject != null && netJsonObject.state > 0) {
                try {
                    JSONObject jsonObject = netJsonObject.getJSONObject().getJSONObject("data");
                    JSONObject object = jsonObject.getJSONObject(PageState.FaceSkinValue);
                    JSONArray week = object.getJSONArray("week");
                    JSONArray month = object.getJSONArray("monty");
                    int a = week.length();
                    for (int i = 0; i < a; i++) {
                        JSONObject object1 = (JSONObject) week.get(i);
                        String date = object1.getString("updatetime");
                        date = date.substring(6, date.length() - 2);
                        long dateweek = Long.parseLong(date);
                        Date date1 = new Date(dateweek);
                        int index = date1.getDay();
                        if (index == dateTime.getDay() && date1.getDate() == dateTime.getDate()) {
                            faceTodayValue = (int) Float.parseFloat(object1.getString("snumber"));
                        }
                        faceWaterW[index - 1] = (int) Float.parseFloat(object1.getString("snumber"));
                        faceOilyW[index - 1] = (int) Float.parseFloat(object1.getString("ynumber"));
                    }

                    int b = month.length();
                    for (int i = 0; i < b; i++) {
                        JSONObject object1 = (JSONObject) month.get(i);
                        String date = object1.getString("updatetime");
                        date = date.substring(6, date.length() - 2);
                        long dateweek = Long.parseLong(date);
                        Date date1 = new Date(dateweek);
                        int index = date1.getDate();
                        float water = Float.parseFloat(object1.getString("snumber"));
                        faceWaterM[index - 1] = (int) (water + 0.5);
                        faceOilyM[index - 1] = (int) Float.parseFloat(object1.getString("ynumber"));
                        int times = Integer.parseInt(object1.getString("times"));
                        faceTotalValue += times * water;
                    }
                } catch (Exception e) {
                }

                try {
                    JSONObject jsonObject = netJsonObject.getJSONObject().getJSONObject("data");
                    JSONObject object = jsonObject.getJSONObject(PageState.HandSkinValue);
                    JSONArray week = object.getJSONArray("week");
                    JSONArray month = object.getJSONArray("monty");
                    int a = week.length();
                    for (int i = 0; i < a; i++) {
                        JSONObject object1 = (JSONObject) week.get(i);
                        String date = object1.getString("updatetime");
                        date = date.substring(6, date.length() - 2);
                        long dateweek = Long.parseLong(date);
                        Date date1 = new Date(dateweek);
                        int index = date1.getDay();
                        if (index == dateTime.getDay() && date1.getDate() == dateTime.getDate()) {
                            handTodayValue = (int) Float.parseFloat(object1.getString("snumber"));
                        }
                        handsWaterW[index - 1] = (int) Float.parseFloat(object1.getString("snumber"));
                        handsOilyW[index - 1] = (int) Float.parseFloat(object1.getString("ynumber"));
                    }

                    int b = month.length();
                    for (int i = 0; i < b; i++) {
                        JSONObject object1 = (JSONObject) month.get(i);
                        String date = object1.getString("updatetime");
                        date = date.substring(6, date.length() - 2);
                        long dateweek = Long.parseLong(date);
                        Date date1 = new Date(dateweek);
                        int index = date1.getDate();
                        float water = Float.parseFloat(object1.getString("snumber"));
                        handsWaterM[index - 1] = (int) (water + 0.5);
                        handsOilyM[index - 1] = (int) Float.parseFloat(object1.getString("ynumber"));
                        int times = Integer.parseInt(object1.getString("times"));
                        handTotalValue += times * water;
                    }
                } catch (Exception e) {
                }

                try {
                    JSONObject jsonObject = netJsonObject.getJSONObject().getJSONObject("data");
                    JSONObject object = jsonObject.getJSONObject(PageState.EyesSkinValue);
                    JSONArray week = object.getJSONArray("week");
                    JSONArray month = object.getJSONArray("monty");
                    int a = week.length();
                    for (int i = 0; i < a; i++) {
                        JSONObject object1 = (JSONObject) week.get(i);
                        String date = object1.getString("updatetime");
                        date = date.substring(6, date.length() - 2);
                        long dateweek = Long.parseLong(date);
                        Date date1 = new Date(dateweek);
                        int index = date1.getDay();
                        if (index == dateTime.getDay() && date1.getDate() == dateTime.getDate()) {
                            eyesTodayValue = (int) Float.parseFloat(object1.getString("snumber"));
                        }
                        eyesWaterW[index - 1] = (int) Float.parseFloat(object1.getString("snumber"));
                        eyesOilyW[index - 1] = (int) Float.parseFloat(object1.getString("ynumber"));
                    }

                    int b = month.length();
                    for (int i = 0; i < b; i++) {
                        JSONObject object1 = (JSONObject) month.get(i);
                        String date = object1.getString("updatetime");
                        date = date.substring(6, date.length() - 2);
                        long dateweek = Long.parseLong(date);
                        Date date1 = new Date(dateweek);
                        int index = date1.getDate();
                        float water = Float.parseFloat(object1.getString("snumber"));
                        eyesWaterM[index - 1] = (int) (water + 0.5);
                        eyesOilyM[index - 1] = (int) Float.parseFloat(object1.getString("ynumber"));
                        int times = Integer.parseInt(object1.getString("times"));
                        eyesTotalValue += times * water;
                    }
                } catch (Exception e) {
                }

                try {
                    JSONObject jsonObject = netJsonObject.getJSONObject().getJSONObject("data");
                    JSONObject object = jsonObject.getJSONObject(PageState.NeckSkinValue);
                    JSONArray week = object.getJSONArray("week");
                    JSONArray month = object.getJSONArray("monty");
                    int a = week.length();
                    for (int i = 0; i < a; i++) {
                        JSONObject object1 = (JSONObject) week.get(i);
                        String date = object1.getString("updatetime");
                        date = date.substring(6, date.length() - 2);
                        long dateweek = Long.parseLong(date);
                        Date date1 = new Date(dateweek);
                        int index = date1.getDay();
                        if (index == dateTime.getDay() && date1.getDate() == dateTime.getDate()) {
                            neckTodayValue = (int) Float.parseFloat(object1.getString("snumber"));
                        }
                        neckWaterW[index - 1] = (int) Float.parseFloat(object1.getString("snumber"));
                        neckOilyW[index - 1] = (int) Float.parseFloat(object1.getString("ynumber"));
                    }

                    int b = month.length();
                    for (int i = 0; i < b; i++) {
                        JSONObject object1 = (JSONObject) month.get(i);
                        String date = object1.getString("updatetime");
                        date = date.substring(6, date.length() - 2);
                        long dateweek = Long.parseLong(date);
                        Date date1 = new Date(dateweek);
                        int index = date1.getDate();
                        float water = Float.parseFloat(object1.getString("snumber"));
                        neckWaterM[index - 1] = (int) (water + 0.5);
                        neckOilyM[index - 1] = (int) Float.parseFloat(object1.getString("ynumber"));
                        int times = Integer.parseInt(object1.getString("times"));
                        neckTotalValue += times * water;
                    }
                } catch (Exception e) {
                }


            }
        }
    }

    class GetTimesAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        @Override
        protected NetJsonObject doInBackground(String... params) {
            if (params != null) {
                List<NameValuePair> pars = new ArrayList<>();
                pars.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(getContext())));
                pars.add(new BasicNameValuePair("mac", Mac));
                String filterUrl = OznerPreference.ServerAddress(getContext()) + "OznerDevice/GetTimesCountBuShui";
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(getContext(), filterUrl, pars);
//                Log.e("123456", netJsonObject.state + "GetTimesAsyncTask" + netJsonObject.value);
                return netJsonObject;
            }
            return null;

        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            queryFaceTimes = 0;
            queryHandsTimes = 0;
            queryEyesTimes = 0;
            queryNeckTimes = 0;
            if (netJsonObject != null && netJsonObject.state > 0) {
                try {
                    JSONArray jsonArray = netJsonObject.getJSONObject().getJSONArray("data");
                    for (int i = 0; i < 4; i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        String action = object.getString("action");
                        switch (action) {
                            case PageState.FaceSkinValue:
                                queryFaceTimes = Integer.parseInt(object.getString("times"));
                                break;
                            case PageState.HandSkinValue:
                                queryHandsTimes = Integer.parseInt(object.getString("times"));
                                break;
                            case PageState.EyesSkinValue:
                                queryEyesTimes = Integer.parseInt(object.getString("times"));
                                break;
                            case PageState.NeckSkinValue:
                                queryNeckTimes = Integer.parseInt(object.getString("times"));
                                break;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }

}

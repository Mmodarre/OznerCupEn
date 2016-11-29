package com.ozner.cup.mycenter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.OznerDataHttp;
import com.ozner.cup.R;
import com.ozner.cup.mycenter.CenterBean.CenterRankInfo2;
import com.ozner.cup.mycenter.CenterBean.RankType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/*
* Created by xinde on 2015/12/10
 */
public class CenterRankActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    final int PARISE_SUCCESS = 0;
    final int RANK_LOADED = 1;
    final int RESULT_FAIL = 2;
    //    RelativeLayout rlay_back;
    private String rankType;
    //    List<CenterRankInfo> rankInfolist = new ArrayList<CenterRankInfo>();
    List<CenterRankInfo2> rankInfolist = new ArrayList<>();
    CenterRankAdapter rankAdapter;
    ListView lv_rankList;
    //    TextView tv_title;
    String userid;
    ArrayList<Integer> likedPoslList = new ArrayList<>();
    MyHandle myHandle = new MyHandle();
    private TextView toolbar_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center_rank);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
//        if (android.os.Build.VERSION.SDK_INT >= 21) {
//            Window window = getWindow();
//            //更改状态栏颜色
//            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
//            //更改底部导航栏颜色(限有底部的手机)
//            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
//        }
        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_text.setText("");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (android.os.Build.VERSION.SDK_INT >= 23) {
//            toolbar.setBackgroundColor(getResources().getColor(R.color.MyCenter_ToolBar, null));
//        } else {
//            toolbar.setBackgroundColor(getResources().getColor(R.color.MyCenter_ToolBar));
//        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CenterRankActivity.this.finish();
            }
        });

        userid = UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.UserId, null);

        Intent dataIntent = getIntent();
        rankType = dataIntent.getStringExtra("rankType");

        if (rankType == null || "" == rankType) {
            rankType = RankType.CupType;
        }

//        tv_title = (TextView) findViewById(R.id.tv_title);
//        rlay_back = (RelativeLayout) findViewById(R.id.rlay_back);
        lv_rankList = (ListView) findViewById(R.id.lv_rankList);
//        rlay_back.setOnClickListener(this);
        Log.e("tag", "rankType:" + rankType);

        switch (rankType) {
            case RankType.WaterType:
                toolbar_text.setText(getResources().getString(R.string.Center_PurifierTdsRank));
                break;
            case RankType.CupType:
                toolbar_text.setText(getResources().getString(R.string.Center_CupTdsRank));
                break;
            case RankType.TapType:
                toolbar_text.setText(getResources().getString(R.string.Center_TapTdsRank));
                break;
            case RankType.CupVolumType:
                toolbar_text.setText(getResources().getString(R.string.Center_CupWaterRank));
                break;
        }

//        reloadData();
        rankAdapter = new CenterRankAdapter(CenterRankActivity.this, rankType);
        lv_rankList.setAdapter(rankAdapter);
        lv_rankList.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        new CenterRankAsyncTask().execute(rankType);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.rlay_back:
//                onBackPressed();
//                break;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (rankInfolist.get(position).getUserid().equals(userid)) {
            Log.e("RankActivity", "isMine");
            Intent likemeIntent = new Intent(this, LikeMeActivity.class);
            likemeIntent.putExtra("rankType", rankType);
            startActivity(likemeIntent);
        } else if (0 == rankInfolist.get(position).getIsLike()) {
            Log.e("RankActivity", "not Mine");
            new PariseFriendTask(this, rankType).execute(position);
        }
    }

    private class PariseFriendTask extends AsyncTask<Integer, Void, NetJsonObject> {
        private Context mContext;
        private String likeUrl;
        private String rankType;
        int pos = -1;

        public PariseFriendTask(Context context, String rankType) {
            this.mContext = context;
            this.rankType = rankType;
        }

        @Override
        protected void onPreExecute() {
            likeUrl = OznerPreference.ServerAddress(mContext) + "/OznerDevice/LikeOtherUser";
        }

        @Override
        protected NetJsonObject doInBackground(Integer... params) {
            if (params != null && params.length > 0) {
                pos = params[0];
                List<NameValuePair> parms = new ArrayList<>();
                parms.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(mContext)));
                parms.add(new BasicNameValuePair("likeuserid", rankInfolist.get(pos).getUserid()));
                parms.add(new BasicNameValuePair("type", rankType));
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(mContext, likeUrl, parms);
                return netJsonObject;
            }
            return null;
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (netJsonObject != null) {
                Log.e("tag", "CenterRankParise:" + netJsonObject.value);
                if (netJsonObject.state > 0 && pos > -1) {
                    rankInfolist.get(pos).setIsLike(1);
                    rankInfolist.get(pos).setLikeCount((rankInfolist.get(pos).getLikeCount() + 1));
                    rankAdapter.reloadData(rankInfolist, rankType);
                }
            }
        }
    }
    

    /*
    *点赞
     */
    private void praiseFriend(final Activity activity, final int pos) {
//        if (!likedPoslList.contains(pos)) {
        final String likeUrl = OznerPreference.ServerAddress(activity) + "/OznerDevice/LikeOtherUser";
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<NameValuePair> parms = new ArrayList<>();
                parms.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(activity)));
                parms.add(new BasicNameValuePair("likeuserid", rankInfolist.get(pos).getUserid()));
                parms.add(new BasicNameValuePair("type", rankType));
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, likeUrl, parms);
                Message message = new Message();

                if (netJsonObject.state > 0) {

                    message.what = PARISE_SUCCESS;
                    message.obj = pos;
                } else {
                    message.what = RESULT_FAIL;
                    message.obj = netJsonObject.state;
                }
                myHandle.sendMessage(message);
            }
        }).start();

    }

    class MyHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RANK_LOADED:
                    if (rankInfolist != null) {
                        rankAdapter.reloadData(rankInfolist, rankType);
                    } else {
                        Log.e("tag", "RankInfoList is null");
                    }
                    break;
                case PARISE_SUCCESS:
                    int pos = (int) msg.obj;
                    rankInfolist.get(pos).setVolume(rankInfolist.get(pos).getVolume() + 1);
                    rankAdapter.reloadData(rankInfolist, rankType);
                    break;
                case RESULT_FAIL:

                    break;
            }
            super.handleMessage(msg);
        }
    }


    private class CenterRankAsyncTask extends AsyncTask<String, Void, NetJsonObject> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(CenterRankActivity.this, "", getString(R.string.Center_Loading));
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            String type = params[0];

            List<NameValuePair> parms = new ArrayList<NameValuePair>();
            parms.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(CenterRankActivity.this)));
            NetJsonObject result;
            if (type != RankType.CupVolumType) {
                String tdsRankUrl = OznerPreference.ServerAddress(CenterRankActivity.this) + "/OznerDevice/TdsFriendRank";
                parms.add(new BasicNameValuePair("type", type));
                result = OznerDataHttp.OznerWebServer(CenterRankActivity.this, tdsRankUrl, parms);
            } else {
                String volumUrl = OznerPreference.ServerAddress(CenterRankActivity.this) + "/OznerDevice/VolumeFriendRank";
                result = OznerDataHttp.OznerWebServer(CenterRankActivity.this, volumUrl, parms);
            }
            return result;
        }

        @Override
        protected void onPostExecute(NetJsonObject result) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (result != null) {
                Log.e("tag", "task:" + result.value);
                if (result.state > 0) {
                    JSONObject jsonObject = result.getJSONObject();
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        if (jsonArray != null && jsonArray.length() > 0) {
                            rankInfolist = JSON.parseArray(jsonArray.toString(), CenterRankInfo2.class);
                            rankAdapter.reloadData(rankInfolist, rankType);
                        }
                    } catch (JSONException e) {
                        Log.e("tag", "CenterRankAsyncTask:" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}

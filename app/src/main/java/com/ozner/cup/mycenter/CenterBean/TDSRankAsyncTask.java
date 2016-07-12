package com.ozner.cup.mycenter.CenterBean;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.OznerDataHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by xinde on 2016/1/15.
 */
public class TDSRankAsyncTask extends AsyncTask<String, Void, NetJsonObject> {

    private Context mContext;
    private OnTDSRankLoadListener rankLoadListener;
    private String muserid = "";
    private List<CenterRankInfo2> rankInfolist;
    private String type;

    public interface OnTDSRankLoadListener {
        void onTDSRankLoaded(int rank);
    }

    public TDSRankAsyncTask(Context context, String type, OnTDSRankLoadListener listener) {
        this.mContext = context;
        this.rankLoadListener = listener;
        this.type = type;
        rankInfolist = new ArrayList<>();
    }

    @Override
    protected void onPreExecute() {
        muserid = UserDataPreference.GetUserData(mContext, UserDataPreference.UserId, "");
    }

    @Override
    protected NetJsonObject doInBackground(String... params) {
        List<NameValuePair> parms = new ArrayList<NameValuePair>();
        parms.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(mContext)));
        NetJsonObject result;
        if (type != RankType.CupVolumType) {
            String tdsRankUrl = OznerPreference.ServerAddress(mContext) + "/OznerDevice/TdsFriendRank";
            parms.add(new BasicNameValuePair("type", type));
            result = OznerDataHttp.OznerWebServer(mContext, tdsRankUrl, parms);
        } else {
            String volumUrl = OznerPreference.ServerAddress(mContext) + "/OznerDevice/VolumeFriendRank";
            result = OznerDataHttp.OznerWebServer(mContext, volumUrl, parms);
        }
        return result;
    }

    @Override
    protected void onPostExecute(NetJsonObject result) {
        int rank = 1;
        if (result != null) {
            Log.e("tag", "TDSRankAsyncTask:" + result.value);
            if (result.state > 0) {
                JSONObject jsonObject = result.getJSONObject();
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    if (jsonArray != null && jsonArray.length() > 0) {
                        rankInfolist = JSON.parseArray(jsonArray.toString(), CenterRankInfo2.class);
                        for (int i = 0; i < rankInfolist.size(); i++) {
                            if (rankInfolist.get(i).getUserid().equals(muserid)) {
                                rank = rankInfolist.get(i).getRank();
                                break;
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.e("tag", "TDSRankAsyncTask:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        if (rankLoadListener != null) {
            rankLoadListener.onTDSRankLoaded(rank);
        }
    }
}

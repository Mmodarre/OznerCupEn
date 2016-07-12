package com.ozner.cup.Guide;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ozner.cup.BaiduPush.OznerBroadcastAction;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.OznerDataHttp;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by cecha on 2016/5/23.
 */
public class LoginService extends Service{

    static int taskCount = 0;
    final int MaxCount = 6;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new UpdateUserInfoAsyncTask(getBaseContext()).execute();
        return super.onStartCommand(intent, flags, startId);
    }

    class UpdateUserInfoAsyncTask extends AsyncTask<String, Void, NetJsonObject> {

        private Context mContext;
        String usertoken = "";
        String deviceid = "";
        String updateUrl = "";

        public UpdateUserInfoAsyncTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            usertoken = OznerPreference.UserToken(mContext);
            deviceid = UserDataPreference.GetUserData(mContext, UserDataPreference.BaiduDeviceId, "");
            updateUrl = OznerPreference.ServerAddress(mContext) + "/OznerServer/UpdateUserInfo";
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (netJsonObject != null) {
                if (netJsonObject.state > 0) {
                    taskCount = 0;
                    Log.w("BaiduPush", "UpdateUserInfo:" + netJsonObject.value);
                    UserDataPreference.SetUserData(mContext, UserDataPreference.hasUpdateUserInfo, "true");
                } else {
                    Log.w("BaiduPush", "UpdateUserInfo:" + netJsonObject.value);
                    if (taskCount++ < MaxCount)
                        mContext.sendBroadcast(new Intent(OznerBroadcastAction.UpdateUserInfo));
                }
            } else {
                Log.w("BaiduPush", "UpdateUserInfo:null");
                if (taskCount++ < MaxCount)
                    mContext.sendBroadcast(new Intent(OznerBroadcastAction.UpdateUserInfo));
            }
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            List<NameValuePair> reqParms = new ArrayList<>();
            reqParms.add(new BasicNameValuePair("usertoken", usertoken));
            reqParms.add(new BasicNameValuePair("channel_id", "5"));
            reqParms.add(new BasicNameValuePair("device_id", deviceid));
            NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(mContext, updateUrl, reqParms);
            return netJsonObject;
        }
    }

}

package com.ozner.yiquan.CChat.bean;

import android.content.Context;
import android.os.AsyncTask;

import com.ozner.yiquan.Command.ChatCommand;

/**
 * Created by xinde on 2016/1/18.
 */
public class ChatControl {
    //咨询初始化
    public class ChatInitAsyncTask extends AsyncTask<String, Void, ChatCommand.OznerChatToken> {
        Context mContext;
        public ChatInitAsyncTask(Context context){
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected ChatCommand.OznerChatToken doInBackground(String... params) {
            //获取token
            ChatCommand.OznerChatToken tokenresult = ChatCommand.oznerChatGetToken();
            return tokenresult;
        }

        @Override
        protected void onPostExecute(ChatCommand.OznerChatToken o) {
            if (o!=null){
//                UserDataPreference.SetUserData(mContext, UserDataPreference.ChatUserTokenInfo,);
            }

            super.onPostExecute(o);
        }
    }

    //获取会员信息
    public class ChatGetUserInfoAsyncTask extends AsyncTask<String, Void, ChatCommand.OznerChatUserInfo> {

        @Override
        protected ChatCommand.OznerChatUserInfo doInBackground(String... params) {

            return null;
        }
    }

    public class ChatLoginAsyncTask extends AsyncTask<String, Void, ChatCommand.OznerChatLoginInfo> {

        @Override
        protected ChatCommand.OznerChatLoginInfo doInBackground(String... params) {

            return null;
        }
    }


}

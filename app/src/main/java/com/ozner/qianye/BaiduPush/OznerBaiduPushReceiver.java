package com.ozner.qianye.BaiduPush;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.baidu.android.pushservice.PushMessageReceiver;
import com.ozner.qianye.Command.ChatCommand;
import com.ozner.qianye.Command.UserDataPreference;
import com.ozner.qianye.MainActivity;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by xinde on 2016/1/4.
 */
public class OznerBaiduPushReceiver extends PushMessageReceiver {
    @Override
    public void onBind(Context context, int errorCode, String appid,
                       String userId, String channelId, String requestId) {
        Log.e("BDPushSDK", "onBind: errorCode->" + errorCode + ", appid->" + appid + ", userId->" + userId + ", channelId->" + channelId + ", requestId->" + requestId);
        if (channelId != null && channelId != "") {
            UserDataPreference.SetUserData(context, UserDataPreference.BaiduDeviceId, channelId);
            if (UserDataPreference.GetUserData(context, UserDataPreference.hasUpdateUserInfo, "false").equals("false")) {
                context.sendBroadcast(new Intent(com.ozner.qianye.BaiduPush.OznerBroadcastAction.UpdateUserInfo));
            }
        }
    }

    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        Log.e("BDPushSDK", "onUnbind: errorCode->" + errorCode + ", errorCode->" + errorCode);
    }

    @Override
    public void onSetTags(Context context, int errorCode,
                          List<String> sucessTags, List<String> failTags, String requestId) {
        Log.e("BDPushSDK", "onSetTags: errorCode->" + errorCode + ", sucessTags->" + sucessTags.size() + ", failTags->" + failTags.size() + ", requestId->" + requestId);
    }

    @Override
    public void onDelTags(Context context, int errorCode,
                          List<String> sucessTags, List<String> failTags, String requestId) {
        Log.e("BDPushSDK", "onDelTags: errorCode->" + errorCode + ", sucessTags->" + sucessTags.size() + ", failTags->" + failTags.size() + ", requestId->" + requestId);
    }

    @Override
    public void onListTags(Context context, int errorCode, List<String> tags,
                           String requestId) {
        Log.e("BDPushSDK", "onListTags: errorCode->" + errorCode + ", tags->" + tags.size() + ", requestId->" + requestId);
    }

    /*
    *透传消息
     */
    @Override
    public void onMessage(Context context, String message,
                          String customContentString) {
        Log.e("BDPushSDK", "onMessage: message->" + message + ", customContentString->" + customContentString);
        try {
            JSONObject resObj = new JSONObject(message);
            Log.e("BDPushSDK", "resObj_value:" + resObj.toString());
            String action = resObj.getJSONObject("custom_content").getString("action");
            Log.e("BDPushSDK", "Pusp:action:" + action);
            if (action.equals(com.ozner.qianye.BaiduPush.PushOperationAction.NewMessage)) {//新的留言
                Intent newMsgIntent = new Intent(com.ozner.qianye.BaiduPush.OznerBroadcastAction.NewMessage);
                newMsgIntent.putExtra("Address", "pushceiver");
                context.sendBroadcast(newMsgIntent);
            } else if (action.equals(com.ozner.qianye.BaiduPush.PushOperationAction.NewFriend)) {//验证信息通过
                Intent newFriendIntent = new Intent(com.ozner.qianye.BaiduPush.OznerBroadcastAction.NewFriend);
                newFriendIntent.putExtra("Address", "pushceiver");
                context.sendBroadcast(newFriendIntent);
            } else if (action.equals(com.ozner.qianye.BaiduPush.PushOperationAction.NewFriendVF)) {//有新的验证信息
                Intent vfIntent = new Intent(com.ozner.qianye.BaiduPush.OznerBroadcastAction.NewFriendVF);
                vfIntent.putExtra("Address", "pushceiver");
                context.sendBroadcast(vfIntent);
            } else if (action.equals(com.ozner.qianye.BaiduPush.PushOperationAction.NewRank)) {//有新的排名产生
                Intent newRankIntent = new Intent(com.ozner.qianye.BaiduPush.OznerBroadcastAction.NewRank);
                newRankIntent.putExtra("Address", "pushceiver");
                context.sendBroadcast(newRankIntent);
            } else if (action.equals(com.ozner.qianye.BaiduPush.PushOperationAction.Chat)) {//咨询
                Log.e("BDPushSDK", "pushMsg:chat咨询");
                ChatCommand.addPushMsgCount(context);
                Intent msgArrIntent = new Intent(com.ozner.qianye.BaiduPush.OznerBroadcastAction.ReceiveMessage);
//                String msg = resObj.getJSONObject("custom_content").getString("data");
                msgArrIntent.putExtra("Address", "pushceiver");
                msgArrIntent.putExtra(com.ozner.qianye.BaiduPush.PushBroadcastKey.MSG, message);
                context.sendBroadcast(msgArrIntent);
            } else if (action.equals(com.ozner.qianye.BaiduPush.PushOperationAction.LoginNotify)) {
                Intent loginIntent = new Intent(com.ozner.qianye.BaiduPush.OznerBroadcastAction.LoginNotify);
                String data = resObj.getJSONObject("custom_content").getString("data");
                JSONObject dataJo = new JSONObject(data);
                String loginUsertoken = dataJo.getString("token");
                String miei = dataJo.getString("miei");
                String loginUserid = dataJo.getString("userid");
                loginIntent.putExtra("Address", "pushceiver");
                loginIntent.putExtra(com.ozner.qianye.BaiduPush.PushBroadcastKey.LoginUserToken, loginUsertoken);
                loginIntent.putExtra(com.ozner.qianye.BaiduPush.PushBroadcastKey.LoginMIEI, miei);
                loginIntent.putExtra(com.ozner.qianye.BaiduPush.PushBroadcastKey.LoginUserid, loginUserid);
                context.sendBroadcast(loginIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("BDPushSDK", "Push:onMessage_Ex:" + e.getMessage());
        }
    }

    /*
    *通知消息点击
     */
    @Override
    public void onNotificationClicked(final Context context, String title,
                                      final String description, final String customContentString) {
        Log.e("BDPushSDK", "onNotificationClicked: title->" + title + ", description->" + description + ", customContentString->" + customContentString);
//        OznerCommand.setAppRunForegroud(context);
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainIntent);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent msgClickIntent = new Intent(com.ozner.qianye.BaiduPush.OznerBroadcastAction.ReceiveMessageClick);
                String msg;
                if (customContentString != null && customContentString != "") {
                    msg = customContentString;
                } else {
                    msg = description;
                }
                msgClickIntent.putExtra("Address", "pushreceiver");
                msgClickIntent.putExtra(com.ozner.qianye.BaiduPush.PushBroadcastKey.MSG, msg);
                context.sendBroadcast(msgClickIntent);
            }
        }, 500);
    }

    /*
    *通知消息到达
     */
    @Override
    public void onNotificationArrived(Context context, String title,
                                      String description, String customContentString) {
        Log.e("BDPushSDK", "onNotificationArrived: title->" + title + ", description->" + description + ", customContentString->" + customContentString);
//        Log.e("BDPushSDK","推送Receiver_运行模式："+ OznerCommand.isAppRunBackound(context));
//        String countStr = UserDataPreference.GetUserData(context, UserDataPreference.NewChatmsgCount, "0");
//        int chatNewCount = Integer.parseInt(countStr);
//        chatNewCount++;
//        UserDataPreference.SetUserData(context, UserDataPreference.NewChatmsgCount, String.valueOf(chatNewCount));
        ChatCommand.addPushMsgCount(context);
        Intent msgArrIntent = new Intent(com.ozner.qianye.BaiduPush.OznerBroadcastAction.ReceiveMessage);
        String msg;
        if (customContentString != null && customContentString != "") {
            msg = customContentString;
        } else {
            msg = description;
        }
        msgArrIntent.putExtra("Address", "pushceiver");
        msgArrIntent.putExtra(com.ozner.qianye.BaiduPush.PushBroadcastKey.MSG, msg);
        context.sendBroadcast(msgArrIntent);
    }
}

package com.ozner.yiquan.BaiduPush;

/**
 * Created by xinde on 2016/1/4.
 */
public class OznerBroadcastAction {
    //百度推送绑定成功
    public final static String UpdateUserInfo = "net.ozner.oznerproject.update_user_info";
    //接收到新消息
    public final static String ReceiveMessage = "net.ozner.oznerproject.receive_message";
    //接收新消息跳转到咨询页
    public final static String ReceiveMessageClick = "net.ozenr.ozenrproject.receive_message_click";
    //接收到其他用户验证Push
    public final static String NewFriendVF = "net.ozner.oznerproject.new_friend_vf";
    //其他用户接收验证消息
    public final static String NewFriend = "net.ozner.oznerproject.new_friend";
    //个人中心我的排名通知
    public final static String NewRank = "net.ozner.oznerproject.new_rank";
    //个人中心留言通知
    public final static String NewMessage = "net.ozenr.oznerproject.new_message";
    //登录通知
    public final static String LoginNotify = "net.ozner.oznerproject.login_notify";
    //退出登录通知
    public final static String Logout = "net.ozner.oznerproject.logout";

    //英文版个人中心设备点击广播
    public final static String EN_Center_Click = "en_center_click";
}

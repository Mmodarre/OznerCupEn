package com.ozner.yiquan.HttpHelper;

import android.support.annotation.Nullable;

import org.json.JSONObject;

/**
 * Created by C-sir@hotmail.com  on 2015/12/17.
 */
public class NetUserVfMessage {
    public NetUserVfMessage() {

    }

    //主键ID
    public int ID;
    //发送者手机号
    public String Mobile;
    //接收者手机号
    public String FriendMobile;
    //请求的消息内容
    public String RequestContent;
    //状态 1正在申请，2已经是好友
    public int Status;
    public int Disabled;
    //创建者
    public String CreateBy;
    //创建时间
    public String CreateTime;
    @Nullable
    public String Nickname;
    @Nullable
    public String Icon;
    public String OtherMobile;
    public int Score;

    public boolean fromJSONobject(JSONObject jsonObject) {
        try {
            ID = jsonObject.getInt("ID");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        try {
            Mobile = jsonObject.getString("Mobile");
            FriendMobile = jsonObject.getString("FriendMobile");
            RequestContent = jsonObject.getString("RequestContent");
            Status = jsonObject.getInt("Status");
            Disabled = jsonObject.getInt("Disabled");
            CreateBy = jsonObject.getString("CreateBy");
            CreateTime = jsonObject.getString("CreateTime");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        try {
            OtherMobile = jsonObject.getString("OtherMobile");
        } catch (Exception ex) {
            OtherMobile = null;
        }
        try {
            Nickname = jsonObject.getString("Nickname");
        } catch (Exception ex) {
            Nickname = null;
        }
        try {
            Icon = jsonObject.getString("Icon");
        } catch (Exception ex) {
            Icon = null;
        }
        try {
            Score = jsonObject.getInt("Score");
        } catch (Exception ex) {
            Score = 0;
        }
        return true;
    }

}

package com.ozner.yiquan.HttpHelper;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by C-sir@hotmail.com  on 2015/12/4.
 * 浩泽用户信息网络对象 支持序列化
 */
public class NetUserInfo implements Serializable{
    public NetUserInfo(){}
    public int state;
    public JSONObject userinfo;
}

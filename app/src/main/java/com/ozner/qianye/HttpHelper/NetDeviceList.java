package com.ozner.qianye.HttpHelper;

import org.json.JSONArray;

import java.io.Serializable;

/**
 * Created by C-sir@hotmail.com  on 2015/12/4.
 * 浩泽设备列表网络对象 支持序列化
 */
public class NetDeviceList implements Serializable {
    public NetDeviceList(){}
    public int state;
    private String devicelist;
    public void setDevicelist(String value)
    {
        devicelist=value;
    }
    public JSONArray getDevicelist()
    {
        try {
            return new JSONArray(devicelist);
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

}

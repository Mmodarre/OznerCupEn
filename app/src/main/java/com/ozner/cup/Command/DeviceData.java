package com.ozner.cup.Command;

import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by C-sir@hotmail.com  on 2015/12/3.
 */
public class DeviceData implements Serializable {
    private String mac;
    private String name;
    private String DeviceType;
    private String DeviceAddress;
    private String Settings;
    private OznerDevice OznerDevice;
    public DeviceData()
    {
    }
    //构造函数初始化mac
    public DeviceData(String mac)
    {
        this.mac=mac;
    }
    public DeviceData(String mac,OznerDevice baseDeviceIO)
    {
        this.mac=mac;
        this.OznerDevice=baseDeviceIO;
    }
    //获取MAC地址
    public String getMac()
    {
        return mac;
    }
    //设置MAC地址
    public void setMac(String mac)
    {
        this.mac=mac;
    }
    //设置基础IO对象
    public void  setOznerDevice(OznerDevice deviceIO)
    {
        OznerDevice=deviceIO;
    }
    //获取基础IO对象
    public OznerDevice getOznerDevice()
    {
        return OznerDevice;
    }
    public void setName(String name)
    {
        this.name=name;
    }
    public void setDeviceType(String deviceType)
    {
        this.DeviceType=deviceType;
    }
    public void setDeviceAddress(String address)
    {
        this.DeviceAddress=address;
    }
    public void setSettings(String settings)
    {
        this.Settings=settings;
    }
    public String getName()
    {
        return this.name;
    }
    public String getDeviceType()
    {
        return this.DeviceType;
    }
    public String getSettings()
    {
        return this.Settings;
    }
    public String getDeviceAddress()
    {
        return this.DeviceAddress;
    }
    //检查设备连接状态
    public BaseDeviceIO.ConnectStatus IsContented()
    {
        if(OznerDevice!=null)
        {
          return OznerDevice.connectStatus();
        }
        return BaseDeviceIO.ConnectStatus.Disconnect;
    }
    //刷新设备的连接
    public boolean RefreshContenct()
    {
        //刷新获取连接
        this.OznerDevice=OznerDeviceManager.Instance().getDevice(this.mac,this.DeviceType,this.Settings);
        if(this.OznerDevice!=null)
        {
            return true;
        }
        return false;
    }
    public boolean fromJSONObject(JSONObject jsonObject)
    {
        if(jsonObject!=null)
        {
            try {
                String mac = jsonObject.getString("Mac");
                this.mac=mac;
                if(mac==null||mac.length()<=0)
                    return false;
            }catch (Exception ex)
            {
             ex.printStackTrace();
                return false;
            }
            try{
                String data=jsonObject.get("Name").toString();
                this.name=data;
            }catch (JSONException ex){ex.printStackTrace();
                this.name=null;}
            try{
                String data=jsonObject.get("DeviceType").toString();
                this.DeviceType=data;
            }catch (JSONException ex){ex.printStackTrace();
                this.DeviceType=null;}
            try{
                String data=jsonObject.get("DeviceAddress").toString();
                this.DeviceAddress=data;
            }catch (JSONException ex){ex.printStackTrace();
                this.DeviceAddress=null;}
            try{
                String data=jsonObject.get("Settings").toString();
                this.Settings=data;
            }catch (JSONException ex){ex.printStackTrace();
                this.Settings=null;}
            return true;
        }
        return false;
    }
    public String toString()
    {
        return "Name:"+this.name+"Mac:"+this.mac+"DeviceType"+this.DeviceType;
    }
}

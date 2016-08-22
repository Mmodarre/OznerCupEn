package com.ozner.yiquan.Command;

/**
 * Created by C-sir@hotmail.com on 2015/11/25.
 */
public abstract interface FootFragmentListener {
    //内容区域切换
    public void ShowContent(int i,String mac);
    public void ChangeRawRecord();
    public void CupSensorChange(String address);
    public void DeviceDataChange();
    public void ContentChange(String mac,String state);
    public void RecvChatData(String data);
}

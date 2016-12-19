package com.ozner.qianye.Command;

import com.ozner.cup.Cup;
import com.ozner.cup.CupRecord;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by C-sir@hotmail.com  on 2015/12/10.
 */
public class CupData {
    private static Date getLastDayOfDay(Date date,int lastday)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_YEAR,lastday);
        return c.getTime();
    }
    public static List<CupRecord> getCupData(String kind,String mac)
    {
        OznerDevice s= OznerDeviceManager.Instance().getDevice(mac);
        Cup cup=(Cup)s;
        List<CupRecord> records=new ArrayList<CupRecord>();
        Calendar c = Calendar.getInstance();     //实例化对象
        c.setFirstDayOfWeek(Calendar.MONDAY);   //设置星期一为每周第一天
        Date time=c.getTime();
        int day=0;
        switch (kind)
        {
            case "WEEK":
                day=c.get(Calendar.DAY_OF_WEEK);    //当前周几
                break;
            case "MONTH":
                day=c.get(Calendar.DAY_OF_MONTH);    //本月第几天
                break;
        }
        for(int i=1;i<=day;i++)
        {
            try {
                Date date = getLastDayOfDay(time, i * -1);
                CupRecord cupRecord = cup.Volume().getRecordByDate(date);
                records.add(cupRecord);
            }catch (Exception ex)
            {
                ex.printStackTrace();
                //赋值默认数据
            }
        }
        return records;
    }
}

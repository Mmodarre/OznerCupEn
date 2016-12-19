package com.ozner.qianye.ACSqlLite;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ozner.qianye.Command.NetCacheWork;
import com.ozner.qianye.Command.OznerCommand;
import com.ozner.qianye.Command.UserDataPreference;
import com.ozner.qianye.HttpHelper.NetJsonObject;
import com.ozner.qianye.UIView.FileUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by C-sir@hotmail.com  on 2016/1/12.
 */
public class CCacheWorking extends AsyncTask<NetCacheWork, Integer, List<NetCacheWork>> {
    //已经换成的任务池
    List<NetCacheWork> listnetworked;
    public boolean IsRunning;
    public Context context;
    public static class WorkAction{
        //## Add Device
        public static String AddDevice="AddDevice";
        //## Delete Device
        public static String DeleteDevice="DeleteDevice";
        //## GetImage
        public static String GetImage="GetImage";
    }
//    private static class LazyHolder {
//        private static final CCacheWorking INSTANCE = new CCacheWorking();
//    }
    public CCacheWorking (Context context){
        this.context=context;
    }
    //获取实例化对象
//    public static final CCacheWorking getInstance() {
//        return LazyHolder.INSTANCE;
//    }    //获取实例化对象
//    public static final CCacheWorking getInstance(Context context) {
//        CCacheWorking Instance=LazyHolder.INSTANCE;
//        if(Instance.getStatus()==Status.FINISHED)
//        {
//
//            return new CCacheWorking();
//        }
//        Instance.context=context;
//        return Instance;
//    }
    @Override
    protected void onPreExecute() {
    }

    //doInBackground方法内部执行后台任务,不可在此方法内修改UI
    @Override
    protected List<NetCacheWork> doInBackground(NetCacheWork... params) {
             List<NetCacheWork> listnotdoworks=new ArrayList<NetCacheWork>();
                listnotdoworks = com.ozner.qianye.ACSqlLite.CSqlCommand.getInstance().GetNetCacheWorks(context);
            listnetworked = new ArrayList<NetCacheWork>();
            if (listnotdoworks != null && listnotdoworks.size() > 0) {

                //循环查找缓存任务
                for (NetCacheWork netCacheWork :listnotdoworks) {
                    NetJsonObject addressult=new NetJsonObject();
                    Log.e("CSIR", "NetCache" + "-Do Working......"+netCacheWork.action);
                    JSONObject data = null;
                    try {
                        data = new JSONObject(netCacheWork.data);
                    } catch (org.json.JSONException ex) {
                        data = null;
                    }
                    if (data != null) {
                        addressult.state=0;
                        switch (netCacheWork.action) {
                            case "AddDevice": {
                             addressult = DoAddDevice(data);
                                break;
                                }
                            case "DeleteDevice": {
                                addressult = DoDeleteDevice(data);
                                break;
                            }
                            case "GetImage":
                                DoGetImage(data);
                                addressult.state=1;
                                break;
                            default:
                                break;
                        }
                    }
                    if(addressult.state>0)
                    {
                        //执行成功
                        listnetworked.add(netCacheWork);
                    }else
                    {
                        if(netCacheWork.failcount>3)
                        {
                            //超过失败的次数
                            listnetworked.add(netCacheWork);
                        }
                        else
                        {
                            //增加失败次数
                            netCacheWork.failcount++;
                            com.ozner.qianye.ACSqlLite.CSqlCommand.getInstance().UpdateNetCacheFaildCount(this.context,netCacheWork);
                        }
                    }
                }
            }
            return null;

    }
    private NetJsonObject DoAddDevice(JSONObject jsonObject)
    {
        try {
            String mac = "";
            try{mac=jsonObject.getString("Mac");}catch (Exception ex){return null;}
            String name = "";
            try{name=jsonObject.getString("Name");}catch (Exception ex){name="";}
            String Settings = "";
            try{Settings=jsonObject.getString("Settings");}catch (Exception ex){Settings="";}
            String AppData = "";
            try{AppData=jsonObject.getString("AppData");}catch (Exception ex){AppData="";}
            String DeviceType="";
            try{DeviceType=jsonObject.getString("DeviceType");}catch (Exception ex){return null;}
            return OznerCommand.AddDeviceV2(context, mac, name, DeviceType, Settings, AppData);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    private NetJsonObject DoDeleteDevice(JSONObject jsonObject)
    {
        try{
            String mac=jsonObject.getString("Mac");
            return OznerCommand.DeleteDevice(context, mac);
        }catch (Exception ex){ex.printStackTrace();}
        return null;
    }
    private void DoGetImage(JSONObject jsonObject)
    {
        String url="";
        try {
            url = jsonObject.getString("url");
        }catch (Exception ex)
        {
            url="";
        }
        if(url!=null&&url.length()>0) {
            Log.e("CSIR","SAVE THE HEAD IMAGE TO"+url);
            ImageLoader.getInstance().loadImage(url, new SimpleImageLoadingListener() {
                        public void onLoadingComplete(String imageUri, android.view.View view, android.graphics.Bitmap loadedImage) {
                            String path=   FileUtils.saveBitmap(loadedImage,UserDataPreference.GetUserData(context, UserDataPreference.UserId, "OznerUser")) ;
                            Log.e("CSIR","SAVE THE HEAD IMAGE"+path);
                        }
                    }
            );
        }

    }
    //onProgressUpdate方法用于更新进度信息
    @Override
    protected void onProgressUpdate(Integer... progresses) {

    }

    //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
    @Override
    protected void onPostExecute(List<NetCacheWork> result) {
        if(listnetworked!=null&&listnetworked.size()>0)
        {
            for (NetCacheWork work:listnetworked)
            {
                Log.e("CSIR", "NetCache" + "-Did Working......"+work.action);
                com.ozner.qianye.ACSqlLite.CSqlCommand.getInstance().RemoveNetCacheWorks(context,work);
            }
        }
            IsRunning=false;
    }

    //onCancelled方法用于在取消执行中的任务时更改UI
    @Override
    protected void onCancelled() {

    }

}

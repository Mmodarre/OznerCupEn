package com.ozner.cup.Command;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.lang.ref.WeakReference;

/**
 * Created by ozner_67 on 2016/12/6.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class BDLocationHelper {
    private static final String TAG = "BDLocationHelper";
    private WeakReference<Context> mContext;
    private LocationClient mLocationClient = null;
    private MyLocationListener myLocationListener = new MyLocationListener();

    public BDLocationHelper(Context applicationContext) {
        mContext = new WeakReference<Context>(applicationContext);
        mLocationClient = new LocationClient(applicationContext);
        mLocationClient.registerLocationListener(myLocationListener);
        initLocation();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 2000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(false);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    /**
     * 开始定位
     */
    public void startLocation() {
        if (mLocationClient != null) {
            mLocationClient.start();
            mLocationClient.requestLocation();
        }
    }

    /**
     * 结束定位
     */
    public void stopLocation() {
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
    }


    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.e(TAG, "onReceiveLocation:errCode: " + bdLocation.getLocType() + " ,BuildName:" + bdLocation.getBuildingName() + " ,City:" + bdLocation.getCity()
                    + " ,Country:" + bdLocation.getCountry()
                    + " , Lat:" + bdLocation.getLatitude() + " , lng:" + bdLocation.getLongitude());
            if (bdLocation.getCity() != null) {
                String city = bdLocation.getCity();
                if (city.endsWith("市")) {
                   city = city.substring(0, city.length() - 1);
                }
                Log.e(TAG, "onReceiveLocation: City:" + city);
                OznerPreference.SetValue(mContext.get(), OznerPreference.BDLocation, city);
//                OznerPreference.SetValue(mContext.get(), OznerPreference.BDLocation, "北京");
                stopLocation();
            } else {
                OznerPreference.SetValue(mContext.get(), OznerPreference.BDLocation, "");
            }
        }
    }
}

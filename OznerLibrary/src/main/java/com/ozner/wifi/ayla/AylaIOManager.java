package com.ozner.wifi.ayla;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.alibaba.fastjson.JSONObject;
import com.aylanetworks.aaml.AylaCache;
import com.aylanetworks.aaml.AylaDevice;
import com.aylanetworks.aaml.AylaDeviceManager;
import com.aylanetworks.aaml.AylaHost;
import com.aylanetworks.aaml.AylaHostScanResults;
import com.aylanetworks.aaml.AylaModule;
import com.aylanetworks.aaml.AylaNetworks;
import com.aylanetworks.aaml.AylaRestService;
import com.aylanetworks.aaml.AylaSetup;
import com.aylanetworks.aaml.AylaSystemUtils;
import com.aylanetworks.aaml.AylaUser;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.IOManager;
import com.ozner.util.HttpUtil;
import com.ozner.util.dbg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by zhiyongxu on 16/4/26.
 */
public class AylaIOManager extends IOManager {
    final static String gblAmlDeviceSsidRegex = "^OZNER_WATER-[0-9A-Fa-f]{12}";

    static String lanIpServiceBaseURL(String lanIp) {
        String url = String.format("http://%s/", lanIp);
        return url;
    }

    public AylaIOManager(Context context) {
        super(context);
        AylaSetup.init(context, gblAmlDeviceSsidRegex, "super app");
        AylaSystemUtils.serviceType=AylaNetworks.AML_DEVELOPMENT_SERVICE;
        AylaSystemUtils.setServicelocationWithCountryCode("CN");
        AylaSystemUtils.loggingLevel=AylaNetworks.AML_LOGGING_LEVEL_INFO;
        AylaSystemUtils.slowConnection=AylaNetworks.YES;

        AylaSystemUtils.saveCurrentSettings();
    }


    @Override
    public void removeDevice(BaseDeviceIO io) {
        final AylaIO aylaIO=(AylaIO)io;
        aylaIO.aylaDevice.unregisterDevice(new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==AylaNetworks.AML_ERROR_OK)
                {
                    dbg.i("Ayla unregisterDevice complete");
                }else
                {
                    dbg.i("Ayla unregisterDevice Error:"+msg.toString());
                }
                super.handleMessage(msg);
            }
        });
    }

    public static boolean isAylaSSID(String ssid)
    {
        return ssid.matches(gblAmlDeviceSsidRegex);
    }
    @Override
    public void Start(String user,String token) {
        AylaCache.clearAll();
        AylaUser.ssoLogin(new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==AylaNetworks.AML_ERROR_OK)
                {
                    String jsonResults=msg.obj.toString();
                    AylaUser aylaUser = AylaSystemUtils.gson.fromJson(jsonResults,  AylaUser.class);
                    AylaUser.setCurrent(aylaUser);
                    dbg.i("AylaSSO Complete");
                    AylaDevice.getDevices(new Handler()
                    {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what==AylaNetworks.AML_ERROR_OK)
                            {
                                String jsonResults=msg.obj.toString();
                                AylaDevice[] devices = AylaSystemUtils.gson.fromJson(jsonResults,  AylaDevice[].class);
                                for (AylaDevice device : devices)
                                {
                                    dbg.i("load:"+device.toString());
                                    createAylaIO(device);

                                }
                            }

                            super.handleMessage(msg);
                        }
                    });
                }else
                {
                    if (msg.obj!=null) {

                        dbg.e("AylaError:%d Msg:%s",msg.what,msg.obj.toString());
                    }
                }
                super.handleMessage(msg);
            }

        },user,"",token,"a_ozner_water_mobile-cn-id","a_ozner_water_mobile-cn-7331816");

    }

    @Override
    public void Stop() {

    }

    public AylaIO createAylaIO(AylaDevice device)
    {
        AylaIO io=new AylaIO(context(),device);
        doAvailable(io);
        return io;
    }

}

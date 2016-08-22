package com.ozner.yiquan.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.ozner.yiquan.Command.VibratorUtil;

/**
 * Created by C-sir@hotmail.com  on 2016/1/21.
 */
public class AlarmReceiver  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if("ELITOR_CLOCK".equals(intent.getAction())){
            Log.e("CSIR","ALEARM RECEIVER");
            VibratorUtil.Vibrate(context,3000);
            Toast.makeText(context, intent.getStringExtra("Message"), Toast.LENGTH_LONG).show();
            return;
        }
    }
}

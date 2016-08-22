package com.ozner.yiquan.slideleft;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.R;

/**
 * Created by admin on 2015/12/9.
 */
public class CustomToast {

    /**
     * @param context 上下文
     * @param view    要显示在哪个view下面
     * @param time    显示时间Toast.LENGTH_LONG 或者short
     */
    public static void makeText(Activity context, View view, int time) {
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                (ViewGroup) context.findViewById(R.id.toast_view));
        OznerApplication.changeTextFont((ViewGroup) layout);
        TextView title = (TextView) layout.findViewById(R.id.textview);
        title.setText("你已成功添加此设备");
        Toast toast = new Toast(context);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        //   y坐标：
        int y = location[1] - 28;
        float yOffset = y + view.getHeight();
        toast.setGravity(Gravity.TOP, 0, (int) yOffset);
        toast.setDuration(time);
        toast.setView(layout);
        toast.show();


    }
}

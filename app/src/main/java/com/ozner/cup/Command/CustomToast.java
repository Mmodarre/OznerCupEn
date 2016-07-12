package com.ozner.cup.Command;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by xinde on 2016/1/11.
 */
public class CustomToast {
    public static void showToastCenter(Context context,String msg){
        Toast toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
}

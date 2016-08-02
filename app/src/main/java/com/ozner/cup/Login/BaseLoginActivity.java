package com.ozner.cup.Login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ozner.cup.BaiduPush.PushBroadcastKey;
import com.ozner.cup.R;

/**
 * Created by ozner_67 on 2016/8/2.
 */
public class BaseLoginActivity extends AppCompatActivity {
    protected void checkOtherLogin() {
        try {
            boolean isOtherLogin = getIntent().getBooleanExtra(PushBroadcastKey.IsOtherLogin, false);
            Log.e("tag", "OtherLogin:" + isOtherLogin);
            if (isOtherLogin) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setMessage(getString(R.string.login_other_device));
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ensure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("tag", "OtherLogin_Ex:" + e.getMessage());
        }
    }
}

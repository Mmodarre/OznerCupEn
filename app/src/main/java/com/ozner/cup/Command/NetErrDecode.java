package com.ozner.cup.Command;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.ozner.cup.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xinde on 2016/1/13.
 */
public class NetErrDecode {
    public static Map<Integer, Integer> ErrMap = new HashMap<>();

    static {
        ErrMap.put(-10011, R.string.Code_Accept_nofound);
        ErrMap.put(-10010, R.string.Code_AddFriend_exists);
        ErrMap.put(-10007, R.string.Code_C_TokenNull);
        ErrMap.put(-10002, R.string.Code_Code_TimeOut);
        ErrMap.put(-10003, R.string.Code_Code_Wrong);
        ErrMap.put(-10015, R.string.Code_Device_Notfound);
        ErrMap.put(-10004, R.string.Code_Exception);
        ErrMap.put(0, R.string.Code_Failed);
        ErrMap.put(-10013, R.string.Code_Friend_NoGx);
        ErrMap.put(-10014, R.string.Code_Friend_NoUser);
        ErrMap.put(-10008, R.string.Code_P_NotNull);
        ErrMap.put(-10009, R.string.Code_P_params_empty);
        ErrMap.put(-10012, R.string.Code_P_params_error);
        ErrMap.put(1, R.string.Code_Success);
        ErrMap.put(-10005, R.string.Code_W_AddUser);
        ErrMap.put(-10006, R.string.Code_W_TokenWrite);
        ErrMap.put(-10001, R.string.Code_W_Uname);
        ErrMap.put(-10016, R.string.Code_Baidu_idwrong);
        ErrMap.put(-10017, R.string.Code_Baidu_nouser);
        ErrMap.put(-10018, R.string.Code_Like_Liked);
        ErrMap.put(-10019, R.string.Code_Fiflter_CodeNotfound);
        ErrMap.put(-10020, R.string.Code_Fiflter_CodeUsed);
        ErrMap.put(-10021, R.string.Code_Fiflter_DeviceNotfound);
        ErrMap.put(-10022, R.string.Code_UserNotFound);
        ErrMap.put(-10023, R.string.Code_Login_Error);
    }

    public static String getErrMsg(Context context, int errCode) {
        if (ErrMap.containsKey(errCode)) {
            try {
                return context.getString(ErrMap.get(errCode));
            } catch (Exception ex) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static void ShowErrMsgDialog(Context context, int errCode, String defMsg) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        if (ErrMap.containsKey(errCode)) {
            alertDialog.setMessage(context.getString(ErrMap.get(errCode)));
        } else {
            alertDialog.setMessage(defMsg);
        }

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, context.getString(R.string.ensure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

}

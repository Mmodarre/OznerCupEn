package com.ozner.yiquan.mycenter;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;

import com.ozner.yiquan.R;

/**
 * Created by xinde on 2015/12/22.
 */
public class LoadingDialog extends Dialog {
    private static LoadingDialog loading = null;

    public LoadingDialog(Context context) {
        super(context);
    }

    public LoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected LoadingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static LoadingDialog createLoading(Context context) {
        loading = new LoadingDialog(context, R.style.LoadingDialog);
        loading.setContentView(R.layout.loading_dialog);
        loading.getWindow().getAttributes().gravity = Gravity.CENTER;
        loading.setCanceledOnTouchOutside(false);
        return loading;
    }

}

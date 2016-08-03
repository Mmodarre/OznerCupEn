package com.ozner.cup.mycenter.CheckForUpdate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.HttpHelper.NetJsonObject;
import com.ozner.cup.HttpHelper.OznerDataHttp;
import com.ozner.cup.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by ozner_67 on 2016/5/17.
 */
public class OznerUpdateManager {
    private static final String TAG = "tag";
    /*检查网络APK版本*/
    private static final int CHECK_NET_VERSON = 1;
    /*更新下载进度*/
    private static final int UPDATE_UI = 2;
    private static final int DOWN_UPDATE = 5;
    private static final int DOWN_OVER = 6;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 4;
    private int isMustUpdate = 0;//是否必须更新
    private int netVersionCode = 0;//网络软件版本号
    private String downLoadUrl = "";//下载链接
    private String folderPath = "";//文件下载路径
    private String instalPackageName = "";//下载的文件名
    private Context mContext;
    private boolean isShowMsg = false;
    private CheckVersionListener checkListener;
    /* 更新进度条 */
    private ProgressBar mProgress;
    private TextView tv_loadper;
    private Dialog mDownloadDialog;
    private boolean isChecking = false;
    private int progress;
    private boolean interceptFlag = false;
    private Thread downLoadThread;

    public OznerUpdateManager(Context context, boolean isShowToastMsg) {
        this.mContext = context;
        this.isShowMsg = isShowToastMsg;
        this.folderPath = Environment.getExternalStorageDirectory() + "/Download/";
        Log.e(TAG, "OznerUpdateManager: folderPath:" + folderPath);
    }

    public interface CheckVersionListener {
        public void CheckVersionState(boolean isCheckComplete);
    }

    public void setCheckVersionListener(CheckVersionListener listener) {
        this.checkListener = listener;
    }

    /*
    *检测软件更新
     */
    public void checkUpdate() {
        if (checkListener != null) {
            checkListener.CheckVersionState(false);
        }
        if (!isChecking) {
            getNetVerson();
        } else {
            Toast.makeText(mContext, "正在检查更新", Toast.LENGTH_SHORT).show();
        }
    }

    private void getNetVerson() {
        isChecking = true;
        final String checkVerUrl = OznerPreference.ServerAddress(mContext) + "/OznerServer/GetNewVersion";
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<NameValuePair> pars = new ArrayList<NameValuePair>();
//                pars.add(new BasicNameValuePair("code", String.valueOf(getVersionCode(mContext))));
                pars.add(new BasicNameValuePair("os", "android"));
//                pars.add(new BasicNameValuePair("appname", mContext.getPackageName()));
//                pars.add(new BasicNameValuePair("appname", "com.hoyo.ozner.hoyoproject"));
                NetJsonObject result = OznerDataHttp.OznerWebServer(mContext, checkVerUrl, pars);
                Message message = new Message();
                message.what = CHECK_NET_VERSON;
                message.obj = result;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            Log.e("tag", "updateManage:curVersion: " + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        if (checkListener != null) {
            checkListener.CheckVersionState(true);
        }

        // 构造对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.soft_update_title);
        builder.setMessage(R.string.soft_update_info);
        // 更新
        builder.setPositiveButton(R.string.soft_update_updatebtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });

        if (0 == isMustUpdate) {
            // 稍后更新
            builder.setNegativeButton(R.string.soft_update_later, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (checkListener != null) {
                        checkListener.CheckVersionState(true);
                    }
                    dialog.dismiss();
                }
            });
        }
        Dialog noticeDialog = builder.create();
        if (1 == isMustUpdate) {
            noticeDialog.setCanceledOnTouchOutside(false);
            noticeDialog.setCancelable(false);
        }
        try {
            if (mContext != null) {
                noticeDialog.show();
            }
        } catch (Exception ex) {

        }
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        Log.e(TAG, "showDownloadDialog: 开始下载");
        // 构造软件下载对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.soft_updating);
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.update_dialog_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        tv_loadper = (TextView) v.findViewById(R.id.tv_loadper);
        builder.setView(v);
        mDownloadDialog = builder.create();
        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.setCancelable(false);
        mDownloadDialog.show();
        // 下载文件
        downloadApk();
    }

    /*
    *检查是否拥有权限
     */
    private boolean hasPermission(@NonNull String permission) {
        if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{permission},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }


    /*
    *下载文件
     */
    public void downloadApk() {
        Log.e(TAG, "downloadApk");
        downLoadThread = new Thread(downloadRunnable);
        downLoadThread.start();
    }


    private Runnable downloadRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(downLoadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(folderPath);
                if (!file.exists()) {
                    file.mkdir();
                }
                File ApkFile = new File(file, instalPackageName);
                ApkFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(ApkFile);

                BufferedInputStream bis = new BufferedInputStream(is);

                int count = 0;
                byte buf[] = new byte[1024];

                do {
                    int numread = bis.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    //更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        //下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                    fos.flush();
                } while (!interceptFlag);//点击取消就停止下载.

                fos.close();
                is.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("tag", "下载更新：" + ex.getMessage());
                if (checkListener != null) {
                    checkListener.CheckVersionState(true);
                }
            }
        }
    };


    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //检查版本
                case CHECK_NET_VERSON:
                    NetJsonObject result = (NetJsonObject) msg.obj;
                    if (result != null) {
                        if (result.state > 0) {
                            Log.e("tag", "UpdateManager:" + result.value);
                            try {
                                JSONObject data = result.getJSONObject().getJSONObject("data");
                                downLoadUrl = data.getString("url");
                                isMustUpdate = data.getInt("mustupdata");
                                netVersionCode = data.getInt("versioncode");
//                                String updateCon = data.getString("updatecon");
                                instalPackageName = downLoadUrl.substring(downLoadUrl.lastIndexOf("/") + 1);

                                if (netVersionCode > getVersionCode(mContext)) {
                                    showNoticeDialog();
                                } else {
                                    if (isShowMsg) {
                                        Toast.makeText(mContext, mContext.getString(R.string.soft_update_no), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("tag", "update_Ex:" + e.getMessage());
                                if (isShowMsg)
                                    Toast.makeText(mContext, "数据解析异常", Toast.LENGTH_SHORT).show();
                            }
                        } else {
//                            if (LogUtilsLC.APP_DBG)
                            Log.e("tag", "update:" + result.value);
                            if (isShowMsg) {
                                Toast.makeText(mContext, mContext.getString(R.string.soft_update_no), Toast.LENGTH_SHORT).show();
                            }
                        }

                    } else {
//                        if (LogUtilsLC.APP_DBG)
                        Log.e("tag", "update:retrn null");
                        if (isShowMsg)
                            Toast.makeText(mContext, mContext.getString(R.string.soft_update_check_err), Toast.LENGTH_SHORT).show();
//                            MessageHelper.showToastCenter(mContext, mContext.getString(R.string.soft_update_check_err));
                    }
                    if (checkListener != null) {
                        checkListener.CheckVersionState(true);
                    }

                    isChecking = false;
                    break;
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    tv_loadper.setText(progress + "%");
                    break;
                case DOWN_OVER:
                    System.out.println("下载完成");
                    if (mDownloadDialog != null)
                        mDownloadDialog.cancel();
                    installApk();
                    break;

            }
        }
    };

    /**
     * 安装apk
     *
     * @param
     */
    private void installApk() {
        File apkfile = new File(folderPath + "/" + instalPackageName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        Log.e("tag", "installApk: Slience:" + Uri.parse("file://" + apkfile.toString()));
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);

    }
}

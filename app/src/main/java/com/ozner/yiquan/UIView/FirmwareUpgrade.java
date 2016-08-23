package com.ozner.yiquan.UIView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.cup.Cup;
import com.ozner.yiquan.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.FirmwareTools;
import com.ozner.device.OznerDeviceManager;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by taoran on 2016/1/22.
 */
public class FirmwareUpgrade extends Activity implements FirmwareTools.FirmwareUpateInterface{
    private RoundProgressBar progressBar;
    private ImageView iv_loade;
    private int progress = 0,old=0,news=0;
    private TextView tv_firmwar_uploadValue,tv_firmware_choose,tv_firmware_cancel,tv_firmware_hint;
    private String Mac,path;
    private Cup mCup;
    private LinearLayout laly_firmwar_uploadValue;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firmwar_layout);
        Intent intent=getIntent();
        Mac=intent.getStringExtra("MAC");
        mCup = (Cup) OznerDeviceManager.Instance().getDevice(Mac);
        path=intent.getStringExtra("path");
        Log.e("tagFF", "mac=====" + Mac + "==path=====" + path);
//        if(!"null".equals(path)){
//            File file = new File(path);
//            if(file.isFile()){
//                file.delete();
//            }
//            file.exists();
//        }else{
//            Toast.makeText(FirmwareUpgrade.this, "路径为空", Toast.LENGTH_LONG).show();
////         FirmwareUpgrade.this.finish();
//        }
        initView();
    }

    private void initView() {
        progressBar=(RoundProgressBar)findViewById(R.id.firmwar_pb);
        iv_loade=(ImageView)findViewById(R.id.iv_firmware_loade);
        laly_firmwar_uploadValue=(LinearLayout)findViewById(R.id.laly_firmwar_uploadValue);
        tv_firmwar_uploadValue=(TextView)findViewById(R.id.tv_firmwar_uploadValue);
        tv_firmware_cancel=(TextView)findViewById(R.id.tv_firmware_cancel);
        tv_firmware_hint=(TextView)findViewById(R.id.tv_firmware_hint);
//        old=Integer.parseInt(tv_firmwar_uploadValue.getText().toString());
        iv_loade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCup.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                    Log.e("updata","click");
                    mCup.firmwareTools().udateFirmware(path);
                }
                tv_firmware_choose.setVisibility(View.GONE);
            }
        });
        mCup.firmwareTools().setFirmwareUpateInterface(this);
        tv_firmware_choose=(TextView)findViewById(R.id.tv_firmware_choose);
        tv_firmware_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                mCup.setAppdata("checkUpdate",sdf.format(new Date()));
                File file = new File(path);
                if(file.isFile()){
                    file.delete();
                }
                file.exists();
                FirmwareUpgrade.this.finish();
            }
        });
    }

    //设备升级
    @Override
    public void onFirmwareUpdateStart(String Address) {
        Log.e("updata","开始升级。。。。");
        laly_firmwar_uploadValue.setVisibility(View.VISIBLE);
        tv_firmware_hint.setText(R.string.firmware_hintTwo);
    }
    @Override
    public void onFirmwarePosition(String Address, int Position, final int size) {
        Log.e("updata", String.format("进度:%d/%d", Position, size));
        NumberFormat nt = NumberFormat.getPercentInstance();
        //设置百分数精确度2即保留两位小数
        nt.setMinimumFractionDigits(0);
//        float baifen = (float)Position/size;
        int position=Integer.parseInt(String.format("%d",Position));
        float baifen = (float)position/size;
        String progress=nt.format(baifen).substring(0,nt.format(baifen).length()-1);
        Log.e("updata",Integer.parseInt(progress)+"");
        progressBar.setProgress(Integer.parseInt(progress));
        tv_firmwar_uploadValue.setText(nt.format(baifen));
    }
    @Override
    public void onFirmwareComplete(String Address) {
        Log.e("updata", "升级完成");
        tv_firmware_hint.setText(R.string.firmware_hintFour);
        progressBar.setProgress(0);
        iv_loade.setImageResource(R.drawable.firmware_success);
        iv_loade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                mCup.setAppdata("checkUpdate", sdf.format(new Date()));
                File file = new File(path);
                if(file.isFile()){
                    file.delete();
                }
                file.exists();
                FirmwareUpgrade.this.finish();
            }
        });
    }

    @Override
    public void onFirmwareFail(String Address) {
        Log.e("updata", "升级失败"+"Address==="+Address);
        laly_firmwar_uploadValue.setVisibility(View.GONE);
        tv_firmware_hint.setText(R.string.firmware_hintThird);
        progressBar.setProgress(0);
        iv_loade.setImageResource(R.drawable.firmware_fair);
        tv_firmware_choose.setVisibility(View.VISIBLE);
        tv_firmware_choose.setText(R.string.firmware_uploadRe);
        tv_firmware_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCup.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {

                    iv_loade.setImageResource(R.drawable.firmware_upload);
//                    mCup.firmwareTools().udateFirmware(path);
                }
                tv_firmware_choose.setVisibility(View.GONE);
                tv_firmware_cancel.setVisibility(View.GONE);
            }
        });
        tv_firmware_cancel.setVisibility(View.VISIBLE);
        tv_firmware_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                mCup.setAppdata("checkUpdate",sdf.format(new Date()));
                FirmwareUpgrade.this.finish();
            }
        });
    }
}

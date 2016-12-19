package com.ozner.qianye.Command;

import android.widget.TextView;

import java.math.BigDecimal;

/**
 * Created by xinde on 2016/1/14.
 * 单位换算
 */
public class UintControl {
    public static float PerUsozTO_ML = 29.57f;
    public static float PerUkozTo_ML = 28.41f;

    public static double mlTransTo(double value, int toUnitType) {
        double resVal = value;
        if (toUnitType == MeasurementUnit.VolumUnit.DL) {
            resVal = value / 100.f;
        } else if (toUnitType == MeasurementUnit.VolumUnit.OZ) {
            resVal = value / PerUsozTO_ML;
        }
        return resVal;
    }

    /*
    *毫升转成其他单位
     */
    public static void mlTransTo(TextView valueView, TextView unitView, double value, int toUnitType) {
        if (valueView != null) {
            if (toUnitType == MeasurementUnit.VolumUnit.DL) {//毫升->分升
                double dlresult = value / 100.0f;
                BigDecimal dlbd = new BigDecimal(dlresult);
                valueView.setText(String.valueOf(dlbd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()));
                if (unitView != null) {
                    unitView.setText("dl");
                }
            } else if (toUnitType == MeasurementUnit.VolumUnit.OZ) {//毫升->盎司
                double ozresult = value / PerUsozTO_ML;
                BigDecimal ozbd = new BigDecimal(ozresult);
                valueView.setText(String.valueOf(ozbd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()));
                if (unitView != null) {
                    unitView.setText("oz");
                }
            } else if (toUnitType == MeasurementUnit.VolumUnit.ML) {
                valueView.setText(String.valueOf(value));
                if (unitView != null) {
                    unitView.setText("ml");
                }
            }
        }
    }

    public static double temCentTransTo(double value, int toUnitType) {
        double resVal = value;
        if (toUnitType == MeasurementUnit.TempUnit.FAHRENHEIT) {
            resVal = value * 33.8f;
        }
        return resVal;
    }

    /*
    * 摄氏单位转华氏单位
     */
    public static void temCentTransTo(TextView valueView, TextView unitView, double value, int toUnitType) {
        if (valueView != null) {
            if (toUnitType == MeasurementUnit.TempUnit.CENTIGRADE) {//摄氏温度
                BigDecimal bd = new BigDecimal(value);
                valueView.setText(String.valueOf(bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()));
                if (unitView != null) {
                    unitView.setText("℃");
                }
            } else if (toUnitType == MeasurementUnit.TempUnit.FAHRENHEIT) {//华氏温度
                double temresult = value * 33.8f;
                BigDecimal bd = new BigDecimal(temresult);
                valueView.setText(String.valueOf(bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue()));
                if (unitView != null) {
                    unitView.setText("℉");
                }
            }
        }
    }
}

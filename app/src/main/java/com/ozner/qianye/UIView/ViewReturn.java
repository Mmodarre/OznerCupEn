package com.ozner.qianye.UIView;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by taoran on 2015/11/24.
 */
public class ViewReturn {

    public static TDSChartView chartView;
    public static UIXWaterDetailProgress progressView;
    public static UIXVolumeChartView volumeView;
    public static RelativeLayout relativeLayout2;
    public  static LinearLayout relativeLayout1;
    public static int x1, x2, x3,x4,x5,x6;
    public static Integer integer;

    public static void setChartView(TDSChartView view) {

        chartView = view;
    }

    public static void setProgressView(UIXWaterDetailProgress view) {
        progressView = view;
    }

    public static void setVolumeView(UIXVolumeChartView view) {

        volumeView = view;
    }

    public static void setProgressRelativeLayout(LinearLayout layout) {

        relativeLayout1 = layout;
    }

    public static void setChartRelativeLayout(RelativeLayout layout) {

        relativeLayout2 = layout;
    }

    public static TDSChartView getChartView() {
        return chartView;
    }

    public static UIXWaterDetailProgress getProgressView() {
        return progressView;
    }

    public static UIXVolumeChartView getVolumeView() {
        return volumeView;
    }

    public static LinearLayout getProgressRelativeLayout() {
        return relativeLayout1;
    }

    public static RelativeLayout getChartRelativeLayout() {
        return relativeLayout2;
    }


    public static void setCountHot(int x) {
        x1 = x;
    }

    public static void setCountNor(int x) {
        x2 = x;
    }

    public static void setCountBad(int x) {
        x3 = x;
    }

    public static int getCountHot() {
        return x1;
    }

    public static int getCountNor() {
        return x2;
    }

    public static int getCountBad() {
        return x3;
    }


    public static void setCountHot1(int x) {
        x4 = x;
    }

    public static void setCountNor1(int x) {
        x5 = x;
    }

    public static void setCountBad1(int x) {
        x6 = x;
    }

    public static int getCountHot1() {
        return x4;
    }

    public static int getCountNor1() {
        return x5;
    }

    public static int getCountBad1() {
        return x6;
    }














}

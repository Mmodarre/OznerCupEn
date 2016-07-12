package com.ozner.cup.WaterProbe.PurifierExp;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by xinde on 15/11/27.
 */
public abstract class UIZChartAdapter {
    public enum ViewMode {Day, Week, Month}

    private Date viewDate = new Date();
    private AdapterListener adapterListener;
    private final String[] weekday = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    public interface AdapterListener {
        void onUpdate(UIZChartAdapter adapter);
    }

    protected void init() {

    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;

    }

    public String getPostionText(int postion) {
        switch (getViewMode()) {
            case Day:
                return String.valueOf(postion);
            case Week:
                // return String.valueOf(postion+1);
                return weekday[postion];
            case Month:
                if (postion == 0) {
                    Calendar c = Calendar.getInstance();
                    int month = c.get(Calendar.MONTH) +1;

                    return month + "月1日";
                }
                return String.valueOf(postion + 1);
        }
        return "";
    }

    /**
     * 开始跟新
     */
    public void update() {
        if (adapterListener != null) {
            adapterListener.onUpdate(this);
        }
    }

    /**
     * 获取当前模式下最大数据量，日=24，周=7，月=当月天数
     *
     * @return
     */
    public int getMaxCount() {
        switch (getViewMode()) {
            case Day:
                return 24;
            case Week:
                return 7;
            case Month: {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(viewDate);
                return calendar.getActualMaximum(Calendar.DATE);
            }
        }
        return 1;
    }

    /**
     * 数据长度
     *
     * @return
     */
    public abstract int count();

    public abstract int getValue(int Index);

    public abstract int getMax();

    public int getMin() {
        return 0;
    }

    /**
     * 获取当前查看的时间
     *
     * @return
     */
    public Date getViewDate() {
        return viewDate;
    }

    /**
     * 设置当前查看时间
     *
     * @param viewDate
     */
    public void setViewDate(Date viewDate) {
        this.viewDate = viewDate;
    }

    public abstract ViewMode getViewMode();


}

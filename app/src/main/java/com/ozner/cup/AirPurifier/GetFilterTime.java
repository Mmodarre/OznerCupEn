package com.ozner.cup.AirPurifier;

/**
 * Created by taoran on 2016/6/3.
 */
public class GetFilterTime {
    public static final float TIMECOMMEND=60000;//机器工作时间要除以的数（常量）
    public static final int getFilter(int workTime){
        return Math.round((1-(workTime/TIMECOMMEND))*100);
    }
}

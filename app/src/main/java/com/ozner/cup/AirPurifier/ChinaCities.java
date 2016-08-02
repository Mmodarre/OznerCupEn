package com.ozner.cup.AirPurifier;

import java.util.HashMap;

/**
 * Created by ozner_67 on 2016/8/2.
 */
public class ChinaCities {
    private static HashMap<String, String> cityMap = new HashMap<>();



    /**
     * 城市中文名转拼音
     * @param name 中文名
     *
     * @return 拼音，没有则原样返回
     */
    public static String getCityEnString(String name) {
        if (cityMap.containsKey(name)) {
            return cityMap.get(name);
        } else {
            return name;
        }
    }
}

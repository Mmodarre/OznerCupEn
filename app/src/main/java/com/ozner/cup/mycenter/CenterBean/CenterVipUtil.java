package com.ozner.cup.mycenter.CenterBean;

import java.util.HashMap;

/**
 * Created by ozner_67 on 2016/7/28.
 */
public class CenterVipUtil {
    private static HashMap<String, String> vipMap = new HashMap<>();

    static {
        vipMap.put("银卡", "Silver ");
        vipMap.put("金卡", "Gold ");
        vipMap.put("铂金卡", "Platinum ");
        vipMap.put("银钻", "Silver Diamond ");
        vipMap.put("金钻", "Gold Diamond ");
        vipMap.put("1级合作卡", "Level 1 Cooperation card ");
        vipMap.put("2级合作卡", "Level 2 Cooperation card ");
        vipMap.put("智卡", "Smart Card ");
        vipMap.put("品牌合作卡", "Co-branding card ");
        vipMap.put("联席代理人", "Co-agents ");
        vipMap.put("伙伴卡", "Partners Cards ");
    }

    public static boolean hasValue(String key) {
        return vipMap.containsKey(key);
    }

    public static String getEnValue(String key) {
        if (vipMap.containsKey(key)) {
            return vipMap.get(key);
        } else {
            return "";
        }
    }
}

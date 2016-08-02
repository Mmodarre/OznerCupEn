package com.ozner.cup.AirPurifier;

import java.util.HashMap;

/**
 * Created by ozner_67 on 2016/8/2.
 *
 * 由于部分拼音太长，不用这个文件做转换了
 */
public class ChinaCities {
    private static HashMap<String, String> cityMap = new HashMap<>();

    static {
        cityMap.put("上海","shanghai");
        cityMap.put("北京","beijing");
        cityMap.put("广州","guangzhou");
        cityMap.put("深圳","shenzhen");
        cityMap.put("天津","tianjin");
        cityMap.put("重庆","chongqing");
        cityMap.put("成都","chengdu");

        cityMap.put("杭州","hangzhou");
        cityMap.put("南京","nanjing");
        cityMap.put("济南","jinan");
        cityMap.put("青岛","qingdao");
        cityMap.put("大连","dalian");
        cityMap.put("宁波","ningbo");
        cityMap.put("厦门","xiamen");
        cityMap.put("石家庄","shijiaz");
        cityMap.put("沈阳","shenyang");
        cityMap.put("哈尔滨","haerbin");
        cityMap.put("福州","fuzhou");
        cityMap.put("武汉","wuhan");
        cityMap.put("昆明","kunming");
        cityMap.put("兰州","lanzhou");
        cityMap.put("台北","taibei");
        cityMap.put("南宁","nanning");
        cityMap.put("银川","yinchuan");
        cityMap.put("太原","taiyuuan");
        cityMap.put("长春","changchun");
        cityMap.put("合肥","hefei");
        cityMap.put("郑州","zhengzhou");
        cityMap.put("长沙","changsha");
        cityMap.put("贵阳","guiyang");
        cityMap.put("西安","xi'an");
        cityMap.put("西宁","xining");
        cityMap.put("拉萨","lasa");
        cityMap.put("海口","haikou");

    }

    /**
     * 城市中文名转拼音
     *
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

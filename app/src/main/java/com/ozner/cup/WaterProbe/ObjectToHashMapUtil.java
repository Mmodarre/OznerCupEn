package com.ozner.cup.WaterProbe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mengdongya on 2016/3/17.
 */
public class ObjectToHashMapUtil {
    public static HashMap<String, ArrayList> objectToMap(Object entity){
        HashMap<String, ArrayList> parameter = new HashMap<String, ArrayList>();
        Field[]   fields   =   entity.getClass().getDeclaredFields();
        for(int i = 0; i < fields.length; i++){
            String fieldName =  fields[i].getName();
            Object object = null;
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getMethodName = "get" + firstLetter + fieldName.substring(1);
            Method getMethod;
            try {
                getMethod = entity.getClass().getMethod(getMethodName, new Class[] {});
                object = getMethod.invoke(entity, new Object[] {});
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(object != null){
                parameter.put(fieldName, (ArrayList) object);
            }
        }
        return parameter;
    }
}

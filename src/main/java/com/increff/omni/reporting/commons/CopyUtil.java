package com.increff.omni.reporting.commons;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashMap;

public class CopyUtil {

    public static void copy(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }

    public static void copyExclude(Object source, Object target, String[] properties) {
        BeanUtils.copyProperties(source, target, properties);
    }

    public static void copyInclude(Object source, Object target, String[] properties) {
        BeanWrapper src = new BeanWrapperImpl(source);
        BeanWrapper trg = new BeanWrapperImpl(target);
        for (String propertyName : properties) {
            trg.setPropertyValue(propertyName, src.getPropertyValue(propertyName));
        }
    }

    public static void copyMap(Object source, Object target, HashMap<String, String> propertiesMap) {
        BeanWrapper src = new BeanWrapperImpl(source);
        BeanWrapper trg = new BeanWrapperImpl(target);
        for (String srcProperty : propertiesMap.keySet()) {
            String tgtProperty = propertiesMap.get(srcProperty);
            trg.setPropertyValue(tgtProperty, src.getPropertyValue(srcProperty));
        }
    }

}

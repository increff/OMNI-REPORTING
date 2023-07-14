package com.increff.omni.reporting.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConvertUtil {

    public static <T> T convert(Object source, Class<T> clazz) {
        T target = null;
        try {
            target = clazz.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error instantiating object", e);
        }
        CopyUtil.copy(source, target);
        return target;
    }

    public static <T> T convert(Object source, Class<T> clazz, String[] properties) {
        T target = null;
        try {
            target = clazz.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error instantiating object", e);
        }
        CopyUtil.copyInclude(source, target, properties);
        return target;
    }

    public static <T> List<T> convert(Collection<?> sourceList, Class<T> clazz) {
        List<T> targetList = new ArrayList<T>(sourceList.size());
        for (Object source : sourceList) {
            targetList.add(convert(source, clazz));
        }
        return targetList;
    }

    public static <T> Collection<T> convert(Collection<?> sourceList, Class<T> clazz, String[] properties) {
        List<T> targetList = new ArrayList<T>(sourceList.size());
        for (Object source : sourceList) {
            targetList.add(convert(source, clazz, properties));
        }
        return targetList;
    }

}

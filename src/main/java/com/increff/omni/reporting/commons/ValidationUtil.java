package com.increff.omni.reporting.commons;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

public class ValidationUtil {
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    public ValidationUtil() {
    }

    public static <T> Set<ConstraintViolation<T>> validate(T t) {
        return factory.getValidator().validate(t, new Class[0]);
    }
}

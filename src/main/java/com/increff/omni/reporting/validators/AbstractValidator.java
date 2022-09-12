package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.model.ValidationModel;

public abstract class AbstractValidator {

    public abstract void add(ValidationModel validation);

    //Parameter to be list of input controls with input
    public abstract boolean validate();

}

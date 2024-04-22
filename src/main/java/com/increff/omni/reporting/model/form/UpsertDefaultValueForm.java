package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
public class UpsertDefaultValueForm {
    @NotNull
    private List<DefaultValueForm> defaultValueForms;
    @NotNull
    private List<DefaultValueForm> validationGroupsValueForms;
}
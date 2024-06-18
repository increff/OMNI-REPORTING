package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class ChartLegendsForm {
    @NotNull
    private Map<String, String> legends = new HashMap<>();
}

package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ChartLegendsForm {
    @NotNull
    private Map<String, String> legends = new HashMap<>();
}

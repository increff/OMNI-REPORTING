package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.BenchmarkDirection;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultBenchmarkForm {

    private Double defaultBenchmark;

    private BenchmarkDirection benchmarkDirection;

    private String benchmarkDesc;

    @NotNull
    private Integer reportId;
} 
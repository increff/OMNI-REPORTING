package com.increff.omni.reporting.model.data;

import lombok.Getter;
import lombok.Setter;
import com.increff.omni.reporting.model.constants.BenchmarkDirection;

@Getter
@Setter
public class DefaultBenchmarkData {
    private Double value;
    private BenchmarkDirection benchmarkDirection;
    private String benchmarkDesc;
    private Integer reportId;
}

package com.increff.omni.reporting.model.data;

import java.time.ZonedDateTime;

import com.increff.omni.reporting.model.constants.BenchmarkDirection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BenchmarkData {
    private String lastUpdatedBy;
    private ZonedDateTime lastUpdatedAt;
    private BenchmarkDirection benchmarkDirection;
    private String benchmarkDesc;
    private Integer reportId;
    private Double value;
}

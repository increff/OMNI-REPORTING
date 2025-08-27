package com.increff.omni.reporting.model.data;

import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BenchmarkData extends DefaultBenchmarkData {
    private String lastUpdatedBy;
    private ZonedDateTime lastUpdatedAt;
}

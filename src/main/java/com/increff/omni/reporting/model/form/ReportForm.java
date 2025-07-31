package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.BenchmarkDirection;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.ReportType;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class ReportForm {

    @NotEmpty
    private String name;
    @NotNull
    private ReportType type;
    @NotNull
    private Integer directoryId;
    @NotEmpty
    private String alias;

    private Integer schemaVersionId;
    @NotNull
    private Boolean isEnabled = true;
    @NotNull
    private Boolean canSchedule = false;
    private Integer minFrequencyAllowedSeconds;
    @NotNull
    private Boolean isChart = false;
    @NotNull
    private ChartType chartType;

    private Double defaultBenchmark;

    private BenchmarkDirection benchmarkDirection;
    
    @Size(max = 255)
    private String benchmarkDesc;
    @NotNull
    private Map<String, String> legends = new HashMap<>();

}

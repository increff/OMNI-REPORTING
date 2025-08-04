package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class BenchmarkForm {
    @NotEmpty
    private List<Benchmark> benchmarks;
    
    @Getter
    @Setter
    public static class Benchmark {
        @NotNull
        private Integer reportId;
        @NotNull
        private Double benchmark;
    }
} 


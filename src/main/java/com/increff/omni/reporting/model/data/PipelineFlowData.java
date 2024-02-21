package com.increff.omni.reporting.model.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor // convert util fails w/out a no-args constructor when using all-args-constructor
public class PipelineFlowData {
 private Integer pipelineId;
 private String folderName;
}

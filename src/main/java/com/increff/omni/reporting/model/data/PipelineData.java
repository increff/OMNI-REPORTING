package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.PipelineType;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class PipelineData {
    private String name;
    private PipelineType type;
    private Integer id;
    private PipelineConfigData configs;
}

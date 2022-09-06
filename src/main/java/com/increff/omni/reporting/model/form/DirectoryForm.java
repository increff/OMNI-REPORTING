package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class DirectoryForm {

    @NotNull
    private String directoryName;
    @NotNull
    private Integer parentId;
}

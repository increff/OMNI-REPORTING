package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class InputControlForm {

    @NotNull
    private String displayName;

    @NotNull
    private String paramName;

    @NotNull
    private InputControlScope scope;

    @NotNull
    private InputControlType type;

    @NotNull
    private Integer schemaVersionId;

    private Integer reportId;

    private String query;

    private List<String> values = new ArrayList<>();

}

package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.InputControlType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InputControlUpdateForm {

    @NotNull
    private String displayName;

    @NotNull
    private String paramName;

    @NotNull
    private InputControlType type;

    private String query;

    private List<String> values = new ArrayList<>();

}

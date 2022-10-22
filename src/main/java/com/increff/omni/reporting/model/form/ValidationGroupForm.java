package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.ValidationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ValidationGroupForm {

    @NotEmpty
    private String groupName;
    @NotNull
    private ValidationType validationType;
    private Integer validationValue = 0; // For Date range, this value specifies how many days a range can have
    @NotEmpty
    private List<Integer> controlIds = new ArrayList<>();
}

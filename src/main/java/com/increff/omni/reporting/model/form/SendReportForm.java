package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class SendReportForm {

    @NotEmpty
    private List<String> emails;

    @Length(max = 255)
    private String comment;
} 
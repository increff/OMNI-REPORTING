package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class IntegrationOrgForm {
    @NotNull
    private OrganizationForm organizationForm;
    @NotNull
    private ConnectionForm connectionForm;
    @NotNull
    private String schemaVersionName;
}

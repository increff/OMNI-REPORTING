package com.increff.account.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class DomainResourceForm {
    @NotNull
    private Integer resourceId;
    @NotNull
    @Length(min = 1)
    private String value;
    private String description;
}

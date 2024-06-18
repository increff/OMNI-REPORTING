package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Setter
@Getter
public class FavouriteForm {

    @NotNull
    private Integer favId;
}

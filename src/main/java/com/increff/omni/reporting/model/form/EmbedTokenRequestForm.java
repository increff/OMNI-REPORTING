package com.increff.omni.reporting.model.form;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmbedTokenRequestForm {

    @NotBlank(message = "data is required")
    @JsonProperty("data")
    private String encryptedCredentials;
}
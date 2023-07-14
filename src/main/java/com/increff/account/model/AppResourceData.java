package com.increff.account.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppResourceData {

    private Integer id;
    private String appName;
    private String resource;
    private String description;
}

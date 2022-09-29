package com.increff.omni.reporting.model;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class SqlParams {

    private String host;
    private String username;
    private String password;
    private String query;
    private File outFile;
    private File errFile;

}

package com.increff.omni.reporting.config;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

@Getter
@Setter
public class EmailProps {

    private String fromEmail;
    private List<String> toEmails;

    private String username;
    private String password;
    private String smtpHost;
    private String smtpPort;
    private File attachment;

    private String subject = "Test email.";
    private String content = "<h1> Hello </h1> This is a <b>test</b> email";
}

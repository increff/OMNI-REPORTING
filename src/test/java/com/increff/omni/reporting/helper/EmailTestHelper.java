package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.form.SendDashboardForm;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class EmailTestHelper {

    public static SendDashboardForm createSendDashboardForm() {
        SendDashboardForm form = new SendDashboardForm();
        form.setEmails(Arrays.asList("test@example.com", "test2@example.com"));
        form.setComment("Test dashboard email");
        return form;
    }

    public static MultipartFile createMockPdfFile() {
        String content = "Mock PDF content";
        return new MockMultipartFile(
            "test.pdf",
            "test.pdf",
            "application/pdf",
            content.getBytes()
        );
    }

    public static MultipartFile createLargeMockPdfFile(int sizeMB) {
        byte[] content = new byte[sizeMB * 1024 * 1024]; // Create file of specified MB size
        return new MockMultipartFile(
            "test.pdf",
            "test.pdf",
            "application/pdf",
            content
        );
    }
}
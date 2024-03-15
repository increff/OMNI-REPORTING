package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.form.DirectoryForm;
import com.increff.omni.reporting.pojo.DirectoryPojo;

public class DirectoryTestHelper {

    public static DirectoryPojo getDirectoryPojo(String directoryName, Integer parentId) {
        DirectoryPojo pojo = new DirectoryPojo();
        pojo.setDirectoryName(directoryName);
        pojo.setParentId(parentId);
        return pojo;
    }

    public static DirectoryForm getDirectoryForm(String directoryName, Integer parentId) {
        DirectoryForm directoryForm = new DirectoryForm();
        directoryForm.setDirectoryName(directoryName);
        directoryForm.setParentId(parentId);
        return directoryForm;
    }
}

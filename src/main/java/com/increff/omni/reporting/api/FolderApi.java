package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@Log4j
public class FolderApi {

    @Autowired
    private ApplicationProperties properties;

    public void deleteOlderFiles() {
        File directory = new File(properties.getOutDir());
        if (!directory.exists()) {
            return;
        }
        // Delete files older than 10 minutes
        File[] listFiles = directory.listFiles();
        if(Objects.isNull(listFiles) || listFiles.length == 0)
            return;
        long purgeTime = System.currentTimeMillis() - (30 * 60 * 1000);
        for (File file : listFiles) {
            if (file.lastModified() < purgeTime) {
                FileUtil.delete(file);
            }
        }
    }

    public File getFileForExtension(Integer id, String extension) throws IOException, ApiException {
        String fileName = "report-" + id + "_" + UUID.randomUUID() + extension;
        return getFile(fileName);
    }

    public File getErrFile(Integer id, String extension) throws IOException, ApiException {
        String fileName = "report-err-" + id + "_" + UUID.randomUUID() + extension;
        return getFile(fileName);
    }

    public File getFile(String fileName) throws ApiException, IOException {
        File directory  = new File(properties.getOutDir());
        if(!directory.exists() && !directory.mkdir())
            throw new ApiException(ApiStatus.BAD_DATA, "Failed to make directory");
        File file = new File(properties.getOutDir(), fileName);
        if(!file.createNewFile())
            log.error("Error while creating file");
        return file;
    }

}


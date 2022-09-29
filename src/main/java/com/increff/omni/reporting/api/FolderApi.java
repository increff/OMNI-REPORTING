package com.increff.omni.reporting.api;

import com.increff.omni.reporting.util.FileUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Service
public class FolderApi {

    @Value("${query.outDir}")
    private String outDir;

    public void deleteFilesOlderThan1Hr() {
        File directory = new File(outDir);
        if (!directory.exists()) {
            return;
        }
        // Delete files older than 10 minutes
        File[] listFiles = directory.listFiles();
        if(Objects.isNull(listFiles) || listFiles.length == 0)
            return;
        long purgeTime = System.currentTimeMillis() - (60 * 60 * 1000);
        for (File file : listFiles) {
            if (file.lastModified() < purgeTime) {
                FileUtil.delete(file);
            }
        }
    }

    public File getFileForExtension(Integer id, String extension) throws IOException, ApiException {
        String fileName = "report-" + id + (new Date()).getTime() + extension;
        return getFile(fileName);
    }

    public File getErrFile(Integer id, String extension) throws IOException, ApiException {
        String fileName = "report-err-" + id + (new Date()).getTime() + extension;
        return getFile(fileName);
    }

    public File getFile(String fileName) throws IOException, ApiException {
        File file = new File(outDir, fileName);
        if(file.createNewFile())
            throw new ApiException(ApiStatus.BAD_DATA, "Error while creating file : " + fileName);
        return file;
    }


}


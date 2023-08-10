package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.util.FileUtil;
import com.nextscm.commons.spring.common.ApiException;
import lombok.extern.log4j.Log4j;
//import org.junit.Before;
//import org.junit.Test;
import lombok.extern.log4j.Log4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.*;

@Log4j
public class FolderApiTest extends AbstractTest {

    @Autowired
    private FolderApi folderApi;
    @Autowired
    private ApplicationProperties properties;

    @BeforeEach
    public void createDir(){
        File dir = new File(properties.getOutDir());
        if(!dir.exists()){
            if(!dir.mkdir()) {
                log.error("Error in creating directory");
            }
        }
    }


    @Test
    public void testDeleteUnusedFiles() throws IOException, ApiException{
        //clear any files in test directory if any
        File file = new File(properties.getOutDir());
        Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(FileUtil::delete);
        //create 10 files
        int n = 10;
        while(n > 0) {
            folderApi.getFile("file"+ n + ".txt");
            n--;
        }
        assertEquals(10, Objects.requireNonNull(file.listFiles()).length);

        //delete files
        folderApi.deleteOlderFiles();
        assertEquals(10, Objects.requireNonNull(file.listFiles()).length);
        //modify last modified of all files
        for (File t : Objects.requireNonNull(file.listFiles())) {
            if(!t.setLastModified(System.currentTimeMillis() - 61 * 60 * 1000))
                log.error("Error in modifying last modified time");
        }
        folderApi.deleteOlderFiles();
        assertEquals(0, Objects.requireNonNull(file.listFiles()).length);
    }

    @Test
    public void testFileWithExtension() throws IOException, ApiException{
        File file = folderApi.getFileForExtension(1, ".txt");
        assertTrue(file.getName().contains(".txt"));
        FileUtil.delete(file);
        assertFalse(file.exists());
    }

    @Test
    public void testErrFileWithExtension() throws IOException, ApiException{
        File file = folderApi.getErrFile(1, ".txt");
        assertTrue(file.getName().contains(".txt"));
        assertTrue(file.getName().contains("report-err"));
        FileUtil.delete(file);
        assertFalse(file.exists());
    }

}

package com.increff.omni.reporting.util;

import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

@Log4j
public class FileUtil {

    public static void closeQuietly(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            log.error("Error closing closeable", e);
        }
    }

    public static boolean delete(File f) {
        if (f == null) {
            return true;
        }
        try {
            return f.delete();
        } catch (SecurityException e) {
            log.error("Error deleting file, " + f.getAbsolutePath(), e);
            return false;
        }
    }

    public static void createFileResponse(File file, HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());

        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            FileUtils.copyFile(file, response.getOutputStream());
            outputStream.flush();
        } finally {
            FileUtil.closeQuietly(outputStream);
        }
    }

}

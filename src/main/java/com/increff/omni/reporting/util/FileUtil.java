package com.increff.omni.reporting.util;

import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Log4j
public class FileUtil {

    private static final double MB = 1024 * 1024;

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

    public static double getSizeInMb(long size) {
        return roundOff(size / MB);
    }

    public static double roundOff(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

}

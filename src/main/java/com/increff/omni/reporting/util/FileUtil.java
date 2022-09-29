package com.increff.omni.reporting.util;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class FileUtil {

    private static Logger logger = Logger.getLogger(FileUtil.class);

    public static void closeQuietly(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            logger.error("Error closing closeable", e);
        }
    }

    public static boolean delete(File f) {
        if (f == null) {
            return true;
        }
        try {
            return f.delete();
        } catch (SecurityException e) {
            logger.error("Error deleting file, " + f.getAbsolutePath(), e);
            return false;
        }
    }

}

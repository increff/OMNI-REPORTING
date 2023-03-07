package com.increff.omni.reporting.util;

import com.increff.omni.reporting.api.FolderApi;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

@Log4j
public class FileUtil {

    @Autowired
    private FolderApi folderApi;

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

    public static Integer getCsvFromTsv(File file, File csvFile) throws IOException {
        Integer noOfRows = 0;
        BufferedReader TSVFile =
                new BufferedReader(new FileReader(file));
        PrintWriter printer = new PrintWriter(csvFile);
        String dataRow = TSVFile.readLine(); // Read first line.
        while (dataRow != null) {
            noOfRows++;
            List<String> values = Arrays.asList(dataRow.split("\t"));
            ListIterator<String> it = values.listIterator();
            while (it.hasNext()) {
                String v = it.next();
                v = v.replace("\"", "'");
                if(v.contains(","))
                    it.set("\"" + v + "\"");
            }
            dataRow = String.join("\t", values);
            String csvRow = dataRow.replaceAll("\t",",");
            printer.write(csvRow);
            printer.write("\n");
            dataRow = TSVFile.readLine(); // Read next line of data.
        }
        printer.close();
        TSVFile.close();
        return noOfRows;
    }

    public static void createFileResponse(File file, HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "inline");
        response.setHeader("Content-length", String.valueOf(file.length()));
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

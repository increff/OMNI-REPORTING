package com.increff.omni.reporting.util;

import com.increff.omni.reporting.api.FolderApi;
import lombok.extern.log4j.Log4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

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
                v = v.replace("\"", "");
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

    public static List<Map<String, String>> getJsonDataFromFile(File sourceFile, char delimiter) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        Reader in = new FileReader(sourceFile);
        Iterable<CSVRecord> records =
                CSVFormat.DEFAULT.builder().setDelimiter(delimiter).setHeader().setSkipHeaderRecord(true).build().parse(in);
        for (CSVRecord record : records) {
            Map<String, String> value = record.toMap();
            data.add(value);
        }
        in.close();
        return data;
    }

    public static int getNumberOfRows(File file) throws IOException, InterruptedException {

        Process p = Runtime.getRuntime().exec("wc -l " + file);
        p.waitFor();
        BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        int lineCount = 0;
        while ((line = b.readLine()) != null) {
            line = line.trim();
            String[] parts = line.split(" ");
            lineCount = Integer.parseInt(parts[0]);
        }
        return lineCount;
    }

    public static double getSizeInMb(long size) {
        return roundOff(size / MB);
    }

    public static double roundOff(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

}

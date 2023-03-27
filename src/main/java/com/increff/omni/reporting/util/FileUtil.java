package com.increff.omni.reporting.util;

import com.increff.omni.reporting.api.FolderApi;
import com.nextscm.commons.lang.StringUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static int writeCsvFromResultSet(ResultSet resultSet, File file) throws SQLException, IOException {
        int noOfRows = 0;
        FileWriter fileWriter = new FileWriter(file);
        CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<String> headers = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            headers.add(metaData.getColumnName(i));
        }
        csvPrinter.printRecord(headers);
        noOfRows++;
        while (resultSet.next()) {
            List<String> data = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String d = resultSet.getString(i);
                if (!StringUtil.isEmpty(d))
                    d = d.replace('\r', ' ').replace('\n', ' ');
                data.add(d);
            }
            csvPrinter.printRecord(data);
            noOfRows++;
        }
        csvPrinter.flush();
        csvPrinter.close();
        fileWriter.close();
        resultSet.close();
        return noOfRows;
    }

    public static Map<String, String> getMapFromResultSet(ResultSet resultSet) throws SQLException {
        Map<String, String> fMap = new HashMap<>();
        while (resultSet.next()) {
            try {
                String key = resultSet.getString(1);
                if(!StringUtil.isEmpty(key))
                    key = key.replace('\r', ' ').replace('\n', ' ');
                String value = resultSet.getString(2);
                if(!StringUtil.isEmpty(value))
                    value = value.replace('\r', ' ').replace('\n', ' ');
                fMap.put(key, value);
            } catch (SQLException e) {
                log.error("Couldn't got values");
            }
        }
        return fMap;
    }

    public static List<Map<String, String>> getJsonDataFromFile(File sourceFile, char delimiter) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        Reader in = new FileReader(sourceFile);
        Iterable<CSVRecord> records =
                CSVFormat.DEFAULT.builder().setDelimiter(delimiter).setHeader().setSkipHeaderRecord(true).build()
                        .parse(in);
        for (CSVRecord record : records) {
            Map<String, String> value = record.toMap();
            data.add(value);
        }
        in.close();
        return data;
    }

    public static Integer getNumberOfRows(File file) throws IOException, InterruptedException {

        Process p = Runtime.getRuntime().exec("wc -l " + file);
        p.waitFor();
        BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
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

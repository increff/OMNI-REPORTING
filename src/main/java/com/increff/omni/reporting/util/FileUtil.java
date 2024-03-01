package com.increff.omni.reporting.util;

import com.increff.omni.reporting.api.FolderApi;
import com.nextscm.commons.lang.StringUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Log4j
public class FileUtil {

    @Autowired
    private FolderApi folderApi;

    private static final double MB = 1024 * 1024;
    public static final String FILTER_QUERY_DISPLAY_NAME_COLUMN = "name";
    public static final String FILTER_QUERY_DISPLAY_VALUE_COLUMN = "id";

    private final static String TIME_ZONE_PATTERN_WITHOUT_ZONE = "yyyy-MM-dd HH:mm:ss";

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
        BufferedWriter writer = new BufferedWriter(fileWriter);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<String> headers = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            headers.add(metaData.getColumnLabel(i));
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
        writer.close();
        fileWriter.close();
        resultSet.close();
        return noOfRows;
    }

    public static int writeCsvFromMongoDocuments(List<Document> documents, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter writer = new BufferedWriter(fileWriter);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
        Set<String> headers = new HashSet<>();
        for (Document document : documents) {
            headers.addAll(document.keySet());
        }
        csvPrinter.printRecord(headers);
        for (Document document : documents) {
            List<String> data = new ArrayList<>();
            for (String header : headers) {
                String d = getValueInString(document, header);
                if (!StringUtil.isEmpty(d))
                    d = d.replace('\r', ' ').replace('\n', ' ');
                data.add(d);
            }
            csvPrinter.printRecord(data);
        }
        csvPrinter.flush();
        csvPrinter.close();
        writer.close();
        fileWriter.close();
        return documents.size();
    }

    public static String getValueInString(Document document, String header) {
        Object value = document.get(header);
        if (value == null) {
            return "";
        }
        return value.toString();
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
        resultSet.close();
        return fMap;
    }

    public static Map<String, String> getMapFromMongoResultSet(List<Document> documents) {
        Map<String, String> fMap = new HashMap<>();
        for (Document document : documents) {
            String key = document.getInteger(FILTER_QUERY_DISPLAY_VALUE_COLUMN).toString();
            String value = document.getString(FILTER_QUERY_DISPLAY_NAME_COLUMN);
            fMap.put(key, value);
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

    public static int getNumberOfRows(File file) throws IOException, InterruptedException {
        int lineCount = 0;
        Process p = Runtime.getRuntime().exec("wc -l " + file);
        p.waitFor();
        try (BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = b.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split(" ");
                lineCount = Integer.parseInt(parts[0]);
            }
            p.destroy();
        }
        return lineCount;
    }

    public static double getSizeInMb(long size) {
        return roundOff(size / MB);
    }

    public static double roundOff(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    public static void writeDummyContentToFile(File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("dummy"); // Write "dummy" to the file
        writer.close();
    }

    public static String getCustomizedFileName(boolean isZip, String timezone, String name) {
        return name + " - " + ZonedDateTime.now().withZoneSameInstant(
                ZoneId.of(timezone)).format(DateTimeFormatter.ofPattern(TIME_ZONE_PATTERN_WITHOUT_ZONE))
                + (isZip ? ".zip" :
                ".csv");
    }

    public static String getPipelineFilename(Integer reportId, String name, String timezone) {
        return "reportId_" + reportId + "_" + getCustomizedFileName(false, timezone, name);
    }
}

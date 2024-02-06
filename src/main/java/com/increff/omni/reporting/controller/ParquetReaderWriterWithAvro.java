package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.api.DBConnectionApi;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParquetReaderWriterWithAvro {

    @Autowired
    private DBConnectionApi dbConnectionApi;

    private static final Schema SCHEMA;
    private static final Path OUT_PATH = new Path("./sample.parquet");

    static {
                String schemaJson = "{ \"type\":\"record\", \"name\":\"champ_channel_credential_key_pojo\", \"fields\":["
                + "{\"name\":\"channel_name\", \"type\":\"string\"},"
                + "{\"name\":\"credential_key\", \"type\":\"string\"}"
                + "]}";
        SCHEMA = new Schema.Parser().parse(schemaJson);
    }

    public static void main(String[] args) throws IOException, SQLException {
        String url = "jdbc:mysql://localhost:3306/champ_prod";
        String user = "root";
        String password = "564800As@@";
        Connection conn = DriverManager.getConnection(url, user, password);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT channel_name,credential_key FROM champ_prod.champ_channel_credential_key_pojo");

        List<GenericData.Record> sampleData = new ArrayList<>();

        while (rs.next()) {
            GenericData.Record record = new GenericData.Record(SCHEMA);
                record.put("channel_name", rs.getString("channel_name"));
                record.put("credential_key", rs.getString("credential_key"));
                sampleData.add(record);
        }


        ParquetReaderWriterWithAvro writerReader = new ParquetReaderWriterWithAvro();
        writerReader.writeToParquet(sampleData, OUT_PATH);
//        writerReader.readFromParquet(OUT_PATH);
    }

//    @SuppressWarnings("unchecked")
//    public void readFromParquet(Path filePathToRead) throws IOException {
//        try (ParquetReader<GenericData.Record> reader = AvroParquetReader
//                .<GenericData.Record>builder(filePathToRead)
//                .withConf(new Configuration())
//                .build()) {
//
//            GenericData.Record record;
//            while ((record = reader.read()) != null) {
//                System.out.println(record);
//            }
//        }
//    }

    public void writeToParquet(List<GenericData.Record> recordsToWrite, Path fileToWrite) throws IOException {
        try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter
                .<GenericData.Record>builder(fileToWrite)
                .withSchema(SCHEMA)
                .withConf(new Configuration())
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build()) {

            for (GenericData.Record record : recordsToWrite) {
                writer.write(record);
            }
        }
    }
}

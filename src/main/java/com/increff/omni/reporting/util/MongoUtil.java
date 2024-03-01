package com.increff.omni.reporting.util;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Log4j
public class MongoUtil {

    private static final String MONGO_VAR_NAME_SEPARATOR = "##";
    private static final String MONGO_PIPELINE_STAGE_SEPARATOR = "// NEW STAGE";

    public static Integer MONGO_READ_TIMEOUT_SEC; // loaded from application.properties post construct
    public static Integer MONGO_CONNECT_TIMEOUT_SEC;

    public static List<Document> parseMongoPipeline(String pipeline) {
        List<Document> stages = new ArrayList<>();
        for (String stage : parseMongoQuery(pipeline, MONGO_PIPELINE_STAGE_SEPARATOR)) {
            log.debug("parseMongoPipeline.Stage:\n" + stage);
            System.out.println("Stage:\n" + stage); // todo : remove later
            stages.add(Document.parse(stage));
        }
        log.debug("parseMongoPipeline.Parsed pipeline: " + stages);
        log.debug("parseMongoPipeline.Stage size : " + stages.size());
        return stages;
    }

    public static List<String> parseMongoQuery(String query, String delimiter) {
        return Arrays.asList(query.split(delimiter));
    }

    public static String getValueAfterEquals(String str, String delimiter) {
        return str.split(delimiter)[0].split("=")[1].trim();
    }

    public static String deleteFirstLine(String str, String delimiter) {
        return str.substring(str.indexOf(delimiter) + delimiter.length());
    }

    public static List<Document> executeMongoPipeline(String host, String username, String password, String query) throws ApiException {
        String collectionName = getValueAfterEquals(query, MONGO_VAR_NAME_SEPARATOR);
        query = deleteFirstLine(query, MONGO_VAR_NAME_SEPARATOR);
        String databaseName = getValueAfterEquals(query, MONGO_VAR_NAME_SEPARATOR);
        query = deleteFirstLine(query, MONGO_VAR_NAME_SEPARATOR);
        return executeMongoPipeline(host, username, password, databaseName, collectionName, parseMongoPipeline(query));
    }


    public static List<Document> executeMongoPipeline(String host, String username, String password, String databaseName, String collectionName, List<Document> stages) throws ApiException {
        ConnectionString connString = getConnectionString(host, username, password);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(MONGO_CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS);
                    builder.readTimeout(MONGO_READ_TIMEOUT_SEC, TimeUnit.SECONDS);
                })
                .build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {

            MongoDatabase database;
            database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            AggregateIterable<Document> result = collection.aggregate(stages).allowDiskUse(true);
            List<Document> results = new ArrayList<>();
            result.into(results);
            return results;
        } catch (Exception e) {
            log.error("Error in executing mongo query : " + e.getMessage());
            throw new ApiException(ApiStatus.BAD_DATA, "Error in executing mongo query : " + e.getMessage());
        }
    }

    private static ConnectionString getConnectionString(String host, String username, String password) {
        String connectionString = "mongodb+srv://";
        if(Objects.nonNull(username) && Objects.nonNull(password) && !username.isEmpty()) {
            connectionString += username + ":" + password + "@" + host;
        } else {
            connectionString += host;
        }
        return new ConnectionString(connectionString);
    }

    public static void testConnection(String host, String username, String password) throws ApiException {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://" + username + ":" + password + "@" + host)) {
            MongoDatabase database = mongoClient.getDatabase("admin");
            MongoIterable<String> collections = database.listCollectionNames();
            for (String collection : collections) {
                log.debug("Collection: " + collection);
            }
        } catch (Exception e) {
            log.error("Error in testing mongo connection : " + e.getMessage());
            throw new ApiException(ApiStatus.BAD_DATA, "Error in testing mongo connection : " + e.getMessage());
        }
    }

}

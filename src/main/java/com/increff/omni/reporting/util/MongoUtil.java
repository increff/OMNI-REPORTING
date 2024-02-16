package com.increff.omni.reporting.util;

import com.mongodb.client.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.bson.Document;

import java.util.*;

@Log4j
public class MongoUtil {

    private static final String MONGO_PIPELINE_STAGE_SEPARATOR = "/\\* NEW STAGE \\*/";

    public static List<Document> parseMongoPipeline(String pipeline) {
        List<Document> stages = new ArrayList<>();
        for (String stage : parseMongoQuery(pipeline, MONGO_PIPELINE_STAGE_SEPARATOR)) {
            log.info("Stage:\n" + stage);
            System.out.println("Stage:\n" + stage);
            stages.add(Document.parse(stage));
        }
        log.debug("Parsed pipeline: " + stages);
        return stages;
    }

    public static List<String> parseMongoQuery(String query, String delimiter) {
        return Arrays.asList(query.split(delimiter));
    }

    public static String getValueAfterEquals(String str) {
        return str.split("\n")[0].split("=")[1];
    }

    public static String deleteFirstLine(String str) {
        return str.substring(str.indexOf("\n") + 1);
    }

    public static List<Document> executeMongoPipeline(String host, String username, String password, String query) throws ApiException {
        String collectionName = getValueAfterEquals(query);
        query = deleteFirstLine(query);
        String databaseName = getValueAfterEquals(query);
        query = deleteFirstLine(query);
        return executeMongoPipeline(host, username, password, databaseName, collectionName, parseMongoPipeline(query));
    }


    public static List<Document> executeMongoPipeline(String host, String username, String password, String databaseName, String collectionName, List<Document> stages) throws ApiException {
        //try (MongoClient mongoClient = MongoClients.create("mongodb://" + username + ":" + password + "@" + host)) {
        //mongodb://localhost:27017/
        String connectionString = "mongodb://";
        if(Objects.nonNull(username) && Objects.nonNull(password) && !username.isEmpty()) {
            connectionString += username + ":" + password + "@" + host;
        } else {
            connectionString += host;
        }
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {

            MongoDatabase database;
            database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            AggregateIterable<Document> result = collection.aggregate(stages);
            List<Document> results = new ArrayList<>();
            result.into(results); // TODO: Should write directly to file instead of collecting in list?
            return results;

            // todo : remove comments
//            JSONArray combinedArray = new JSONArray();
//            for(Document r : results) {
//                combinedArray.put(new JSONObject(r.toJson()));
//
//            // convertCombinedArrayToList(combinedArray);
//
//            // Print the combined JSONArray
//            System.out.println(combinedArray.toString(4));
//
//            Map<String, Object> map = new HashMap<>();
//
//            // convert Document to JSON string
//            //String json = results.toString();
//            resultString += results.toString();
//
//
//            mongoClient.close();
//
//            System.out.println(resultString);
//            return resultString;
//            return QueryExecutionDto.executeMongoQuery(collection, queryString);
        } catch (Exception e) {
            log.error("Error in executing mongo query : " + e.getMessage());
            throw new ApiException(ApiStatus.BAD_DATA, "Error in executing mongo query : " + e.getMessage());
        }
    }

    public static void testConnection(String host, String username, String password) throws ApiException {
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + username + ":" + password + "@" + host)) {
            MongoDatabase database = mongoClient.getDatabase("admin");
            MongoIterable<String> collections = database.listCollectionNames();
            for (String collection : collections) {
                log.info("Collection: " + collection);
            }
        } catch (Exception e) {
            log.error("Error in testing mongo connection : " + e.getMessage());
            throw new ApiException(ApiStatus.BAD_DATA, "Error in testing mongo connection : " + e.getMessage());
        }
    }

}

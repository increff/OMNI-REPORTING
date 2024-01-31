// Runs a database command by using the Java driver

package com.increff.omni.reporting.controller;

import com.hazelcast.org.json.JSONArray;
import com.hazelcast.org.json.JSONObject;
import com.mongodb.client.*;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;

import com.mongodb.MongoException;
import org.bson.conversions.Bson;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.increff.omni.reporting.controller.AggregationConverter.convertAggregationQuery;
import static com.increff.omni.reporting.controller.ConvertCombinedArrayToList.convertCombinedArrayToList;


@Component
@Order(1)
public class RunCommand {

    RunCommand() {
        // Replace the uri string with your MongoDB deployment's connection string
        String uri = "mongodb://localhost:27017/<database>"; // mongodb://localhost:27017/<database>
        String databaseName = "mongoTest";
        String collectionName = "orders";
        String queryString = "{ \"x\": 1 }"; // show dbs not working // dbStats
        List<Document> stages = Arrays.asList(
                new Document("$match", new Document("x", 1)),
                new Document("$group", new Document("_id", null).append("results", new Document("$push", "$$ROOT"))),
                new Document("$replaceRoot", new Document("newRoot",
                        new Document("$mergeObjects", Arrays.asList(
                                new Document("x", 1), // Add any additional fields here
                                new Document("$arrayElemAt",
                                        Arrays.asList(new Document("$push", "$$ROOT"), 0)))))));


        queryString = "db.orders.insertMany([\n" +
                "    { orderId: 1, productId: 101, quantity: 2 },\n" +
                "    { orderId: 2, productId: 102, quantity: 3 },\n" +
                "    { orderId: 3, productId: 101, quantity: 1 },\n" +
                "    { orderId: 4, productId: 103, quantity: 2 }\n" +
                "])\n" +
                "\n" +
                "db.products.insertMany([\n" +
                "    { productId: 101, category: \"Electronics\", price: 50.00 },\n" +
                "    { productId: 102, category: \"Clothing\", price: 25.00 },\n" +
                "    { productId: 103, category: \"Electronics\", price: 75.00 }\n" +
                "])\n" +
                "\n" +
                "db.orders.aggregate([\n" +
                "    {\n" +
                "        $lookup: {\n" +
                "            from: \"products\",\n" +
                "            localField: \"productId\",\n" +
                "            foreignField: \"productId\",\n" +
                "            as: \"product\"\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        $unwind: \"$product\"\n" +
                "    },\n" +
                "    {\n" +
                "        $project: {\n" +
                "            _id: 0,\n" +
                "            category: \"$product.category\",\n" +
                "            revenue: { $multiply: [\"$quantity\", \"$product.price\"] }\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        $group: {\n" +
                "            _id: \"$category\",\n" +
                "            totalRevenue: { $sum: \"$revenue\" }\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        $sort: { totalRevenue: -1 }\n" +
                "    }\n" +
                "])\n";

        // using db.runCommand db.runCommand({
        //  aggregate: "orders",
        //  pipeline: [
        //    {
        //      $lookup: {
        //        from: "products",
        //        localField: "productId",
        //        foreignField: "productId",
        //        as: "product"
        //      }
        //    },
        //    {
        //      $unwind: "$product"
        //    },
        //    {
        //      $project: {
        //        _id: 0,
        //        category: "$product.category",
        //        revenue: { $multiply: ["$quantity", "$product.price"] }
        //      }
        //    },
        //    {
        //      $group: {
        //        _id: "$category",
        //        totalRevenue: { $sum: "$revenue" }
        //      }
        //    },
        //    {
        //      $sort: { totalRevenue: -1 }
        //    }
        //  ],
        //  cursor: { batchSize: 50 }
        //});

        queryString = "db.orders.aggregate([ { $lookup: { from: \"products\", localField: \"productId\", foreignField: \"productId\", as: \"product\" } }, { $unwind: \"$product\" }, { $project: { _id: 0, category: \"$product.category\", revenue: { $multiply: [\"$quantity\", \"$product.price\"] } } }, { $group: { _id: \"$category\", totalRevenue: { $sum: \"$revenue\" } } }, { $sort: { totalRevenue: -1 } } ])";


        stages = new ArrayList<>(); // This requires hardcoded database name and collection name
        stages.add(Document.parse("{ $lookup: { from: \"products\", localField: \"productId\", foreignField: \"productId\", as: \"product\" } }"));
        stages.add(Document.parse("{ $unwind: \"$product\" }"));
        stages.add(Document.parse("{ $project: { _id: 0, category: \"$product.category\", revenue: { $multiply: [\"$quantity\", \"$product.price\"] } } }"));
        stages.add(Document.parse("{ $group: { _id: \"$category\", totalRevenue: { $sum: \"$revenue\" } } }"));
        stages.add(Document.parse("{ $sort: { totalRevenue: 1 } }"));
        for (int i=0;i<stages.size();i++){
            mongoTest(uri, databaseName, collectionName, queryString, stages.subList(0,i+1));
        }

        hardcodedCommand(uri, databaseName, collectionName);
        //        runCommand(uri, databaseName, collectionName, "dbStats");
        runCommand(uri, databaseName, collectionName, "dbStats");
        // Using run command for aggregate is not working. Same is working in mongo shell but not in java driver
        // runCommand(uri, databaseName, collectionName, "{   aggregate: \"orders\",   pipeline: [     {       $lookup: {         from: \"products\",         localField: \"productId\",         foreignField: \"productId\",         as: \"product\"       }     },     {       $unwind: \"$product\"     },     {       $project: {         _id: 0,         category: \"$product.category\",         revenue: { $multiply: [\"$quantity\", \"$product.price\"] }       }     },     {       $group: {         _id: \"$category\",         totalRevenue: { $sum: \"$revenue\" }       }     },     {       $sort: { totalRevenue: -1 }     }   ],   cursor: { batchSize: 50 } }");
        //mongoTest(uri, databaseName, collectionName, queryString, stages);
    }



    public String mongoTest(String uri, String databaseName, String collectionName, String queryString, List<Document> stages) {

                String resultString = "";
        // Connect to the MongoDB server
        MongoClient mongoClient = MongoClients.create(uri);

        // Access the "myDatabase" database
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        // Access the "myCollection" collection
        MongoCollection<Document> collection = database.getCollection(collectionName);

        // Define the query as a string
        // String queryString = "{ \"x\": 1 }";

        // Parse the query string into a Document object
        // Document query = Document.parse(queryString);

        // Execute the query
//        MongoCursor<Document> cursor = collection.aggregate(query).iterator();
//
//        // Process the results

//        while (cursor.hasNext()) {
//            Document result = cursor.next();
//            System.out.println(result.toJson());
//            resultString += result.toJson();
//        }
//
//        // Close the cursor and the MongoDB client
//        cursor.close();

        // Define the stages for the aggregation pipeline
//        List<Document> stages = Arrays.asList(
//                new Document("$match", new Document("x", 1)),
//                new Document("$group", new Document("_id", null).append("results", new Document("$push", "$$ROOT"))),
//                new Document("$replaceRoot", new Document("newRoot",
//                        new Document("$mergeObjects", Arrays.asList(
//                                new Document("x", 1), // Add any additional fields here
//                                new Document("$arrayElemAt",
//                                        Arrays.asList(new Document("$push", "$$ROOT"), 0)))))));


//        List<Document> stages = convertAggregationQuery(queryString);
//        System.out.println(stages);

        // Build and execute the dynamic aggregation pipeline
        AggregateIterable<Document> result = collection.aggregate(stages);

        // get all the results into a list
        List<Document> results = new ArrayList<>();
        result.into(results);
        JSONArray combinedArray = new JSONArray();
        for(Document r : results) {
            combinedArray.put(new JSONObject(r.toJson()));
        }
        convertCombinedArrayToList(combinedArray);

        // Print the combined JSONArray
        System.out.println(combinedArray.toString(4));

        Map<String, Object> map = new HashMap<>();

        // convert Document to JSON string
        //String json = results.toString();
        resultString += results.toString();


        mongoClient.close();

        System.out.println(resultString);
        return resultString;
    }


    // convert List<Document> to Map<String, Object>
    public static Map<String, Object> convertListToMap(List<Document> list)
    {

        // create an empty map
        Map<String, Object> map = new HashMap<>();

        // Iterate through the list
        for (Document document : list) {

            // Get the id
            String id = document.getString("id");

            // Get the name
            String name = document.getString("name");

            // Put the id name pair into the map
            map.put(id, name);
        }

        // Return the map
        return map;
    }

    public String runCommand(String uri, String databaseName, String collectionName, String commandName) {
                try (MongoClient mongoClient = MongoClients.create(uri)) {

            MongoDatabase database = mongoClient.getDatabase(databaseName);

            try {

                // Define the query as a string
                //String queryString = "{ \"x\": 1 }";

                // Parse the query string into a Document object
//                Document query = Document.parse(queryString);
//
//                // Execute the query
//                MongoCursor<Document> cursor = collection.find(query).iterator();
//
//                // Process the results
//                while (cursor.hasNext()) {
//                    Document result = cursor.next();
//                    System.out.println(result.toJson());
//                }
//
//                // Close the cursor and the MongoDB client
//                cursor.close();
//                mongoClient.close();

                Bson command = new BsonDocument(commandName, new BsonInt64(1));

                System.out.println(commandName + " " + command);
                // Retrieves statistics about the specified database
                Document commandResult = database.runCommand(command);

                // Prints the database statistics
                System.out.println(commandName + " " + commandResult.toJson());
                return commandName + " " + commandResult.toJson();

            } catch (MongoException me) {
                // Prints a message if any exceptions occur during the command execution
                System.err.println("An error occurred: " + me);
                return "An error occurred: " + me;
            }
        }

    }

    public String hardcodedCommand(String uri, String databaseName, String collectionName) {
        try (MongoClient mongoClient = MongoClients.create(uri)) {

            MongoDatabase database = mongoClient.getDatabase(databaseName);

            try {


                // Define the aggregation pipeline
                List<Document> pipeline = Arrays.asList(
                        new Document("$lookup",
                                new Document("from", "products")
                                        .append("localField", "productId")
                                        .append("foreignField", "productId")
                                        .append("as", "product")),
                        new Document("$unwind", "$product"),
                        new Document("$project",
                                new Document("_id", 0)
                                        .append("category", "$product.category")
                                        .append("revenue",
                                                new Document("$multiply", Arrays.asList("$quantity", "$product.price")))),
                        new Document("$group",
                                new Document("_id", "$category")
                                        .append("totalRevenue", new Document("$sum", "$revenue"))),
                        new Document("$sort", new Document("totalRevenue", -1))
                        //{cursor: { batchSize: batch_size }}
                        //new Document("cursor", new Document("batchSize", 50))
                );

                // Execute the command
                Document command = new Document("aggregate", collectionName).append("pipeline", pipeline).append("cursor", new Document("batchSize", 50));
                Document result = database.runCommand(command);

                // Process the result
                System.out.println(result.toJson());

                return result.toJson();

            } catch (MongoException me) {
                // Prints a message if any exceptions occur during the command execution
                System.err.println("An error occurred: " + me);
                return "An error occurred: " + me;
            }
        }

    }

}


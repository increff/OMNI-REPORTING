//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
//import org.bson.Document;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class HardcodedCommand {
//    public static void main(String[] args) {
//        // Connect to the MongoDB server
//        MongoClient mongoClient = new MongoClient("localhost", 27017);
//
//        // Access the "myDatabase" database
//        MongoDatabase database = mongoClient.getDatabase("myDatabase");
//
//        // Access the "orders" collection
//        MongoCollection<Document> collection = database.getCollection("orders");
//
//        // Define the aggregation pipeline
//        List<Document> pipeline = Arrays.asList(
//                new Document("$lookup",
//                        new Document("from", "products")
//                                .append("localField", "productId")
//                                .append("foreignField", "productId")
//                                .append("as", "product")),
//                new Document("$unwind", "$product"),
//                new Document("$project",
//                        new Document("_id", 0)
//                                .append("category", "$product.category")
//                                .append("revenue",
//                                        new Document("$multiply", Arrays.asList("$quantity", "$product.price")))),
//                new Document("$group",
//                        new Document("_id", "$category")
//                                .append("totalRevenue", new Document("$sum", "$revenue"))),
//                new Document("$sort", new Document("totalRevenue", -1)));
//
//        // Execute the command
//        Document command = new Document("aggregate", "orders").append("pipeline", pipeline);
//        Document result = database.runCommand(command);
//
//        // Process the result
//        System.out.println(result.toJson());
//
//        // Close the MongoDB client
//        mongoClient.close();
//    }
//}

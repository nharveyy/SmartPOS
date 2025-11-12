package oop.licao.smartpos.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * MongoConnection.java
 * Handles connection to MongoDB Atlas for the SmartPOS database.
 * Provides a shared database instance for all DAOs.
 */
public class MongoConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // Returns a shared MongoDatabase instance, connects if not yet initialized
    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            try {
                System.out.println("Connecting to MongoDB Atlas...");

                String uri = "mongodb+srv://admin:g2WWDuaR9p5raTW2@cluster0.uu2qv2w.mongodb.net/SmartPOS?retryWrites=true&w=majority&appName=Cluster0";
                mongoClient = MongoClients.create(uri);
                database = mongoClient.getDatabase("SmartPOS");

                System.out.println("Connected to MongoDB Atlas (SmartPOS database) successfully!");
                System.out.println("Collections found in SmartPOS database:");
                database.listCollectionNames().forEach(System.out::println);

            } catch (Exception e) {
                System.err.println("Failed to connect to MongoDB Atlas: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return database;
    }

    // Closes MongoDB connection when application shuts down
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
    }
}

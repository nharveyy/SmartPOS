package oop.licao.smartpos.controller;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import oop.licao.smartpos.model.Transaction;
import org.bson.Document;

/**
 * ReportsController.java
 * Displays transaction history for admin reports view by fetching data from MongoDB.
 */
public class ReportsController {

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> idColumn;
    @FXML private TableColumn<Transaction, String> userColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, Double> totalColumn;

    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    private static final String CONNECTION_STRING =
            "mongodb+srv://admin:g2WWDuaR9p5raTW2@cluster0.uu2qv2w.mongodb.net/SmartPOS?retryWrites=true&w=majority&appName=Cluster0";

    @FXML
    private void initialize() {
        // Bind columns to Transaction model properties
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        loadTransactions(); // auto-load data on open
    }

    // Public so AdminController can refresh transactions manually
    public void loadTransactions() {
        transactions.clear();
        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase("SmartPOS");
            MongoCollection<Document> collection = database.getCollection("Transactions");

            // Retrieve and parse transaction data
            FindIterable<Document> docs = collection.find();
            for (Document doc : docs) {
                String id = doc.containsKey("invoiceId")
                        ? doc.getString("invoiceId")
                        : doc.getObjectId("_id").toHexString();
                String user = doc.containsKey("paymentMethod")
                        ? doc.getString("paymentMethod")
                        : doc.getString("user");
                String date = doc.getString("date");
                Double total = doc.getDouble("total");
                double totalVal = total != null ? total : 0.0;

                transactions.add(new Transaction(id, user, date, totalVal));
            }

            transactionTable.setItems(transactions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

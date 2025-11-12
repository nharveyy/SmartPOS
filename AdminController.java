package oop.licao.smartpos.controller;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import oop.licao.smartpos.dao.UserDAO;
import oop.licao.smartpos.model.Transaction;
import org.bson.Document;

import java.io.IOException;
import java.util.List;

/**
 * AdminController.java
 * Handles all admin panel features such as showing dashboard info, managing users,
 * viewing reports, and changing the admin password.
 */
public class AdminController {

    @FXML private StackPane mainContent; // container for switching admin views

    private final UserDAO userDAO = new UserDAO(); // handles all user DB operations

    // Report table elements (active only when reports-view.fxml is loaded)
    @FXML private TableView<Transaction> reportTable;
    @FXML private TableColumn<Transaction, String> invoiceCol;
    @FXML private TableColumn<Transaction, String> dateCol;
    @FXML private TableColumn<Transaction, String> paymentCol;
    @FXML private TableColumn<Transaction, Double> totalCol;

    // MongoDB connection string for SmartPOS database
    private static final String CONNECTION_STRING =
            "mongodb+srv://admin:g2WWDuaR9p5raTW2@cluster0.uu2qv2w.mongodb.net/SmartPOS?retryWrites=true&w=majority&appName=Cluster0";

    @FXML
    public void initialize() {
        // Load dashboard view by default
        showDashboard(null);
    }

    // -----------------------------
    // DASHBOARD VIEW
    // -----------------------------
    @FXML
    private void showDashboard(ActionEvent e) {
        // Creates introductory layout and text for the admin dashboard
        VBox introCard = new VBox(10);
        introCard.setAlignment(Pos.TOP_LEFT);
        introCard.setStyle("-fx-padding: 30; -fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        // Title and basic system information
        Label title = new Label("Welcome to SmartPOS");
        title.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("An Object-Oriented Point of Sale System for Small Businesses");
        subtitle.setStyle("-fx-font-size: 16; -fx-text-fill: #7f8c8d; -fx-padding: 0 0 15 0;");

        // Overview description
        Label overview = new Label("""
            SmartPOS is a JavaFX-based Point of Sale system designed to assist small-scale businesses 
            in managing sales, inventory, and transaction records efficiently. It provides a unified 
            solution for product management, receipt generation, and automated reporting, reducing 
            manual errors and improving productivity.
            """);
        overview.setWrapText(true);
        overview.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50;");

        // Problem and solution sections
        Label problemTitle = new Label("Problem Statement");
        problemTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        Label problemDesc = new Label("""
            Small-scale businesses often rely on manual methods for tracking sales and inventory, which 
            can lead to inefficiencies and errors in operations. Without a proper system, it becomes 
            difficult to maintain accurate sales records, manage stock levels, and generate receipts.
            """);
        problemDesc.setWrapText(true);
        problemDesc.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50;");

        Label solutionTitle = new Label("Proposed Solution");
        solutionTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        Label solutionDesc = new Label("""
            SmartPOS addresses these issues by providing a digital platform where staff can process 
            sales transactions, automatically update inventory levels, and generate printable receipts. 
            The system also produces daily, weekly, and monthly reports, enabling owners to easily 
            track business performance. Built with Object-Oriented Programming principles, SmartPOS 
            ensures modularity, scalability, and easy maintenance for future upgrades.
            """);
        solutionDesc.setWrapText(true);
        solutionDesc.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50;");

        // Combine everything and show
        introCard.getChildren().addAll(title, subtitle, overview, problemTitle, problemDesc, solutionTitle, solutionDesc);
        mainContent.getChildren().setAll(introCard);
    }

    // -----------------------------
    // USERS MANAGEMENT VIEW
    // -----------------------------
    @FXML
    private void showUsers(ActionEvent e) {
        // TableView setup for listing users
        TableView<Document> userTable = new TableView<>();
        TableColumn<Document, String> nameCol = new TableColumn<>("Name");
        TableColumn<Document, String> emailCol = new TableColumn<>("Email");
        TableColumn<Document, Void> actionCol = new TableColumn<>("Actions");

        // Column for full name
        nameCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getString("firstName") + " " + c.getValue().getString("lastName")));

        // Column for email
        emailCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getString("email")));

        // Column for edit/delete buttons
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            {
                // Open edit dialog when clicked
                editBtn.setOnAction(e2 -> openEditDialog(getTableView().getItems().get(getIndex())));
                // Delete user from DB
                delBtn.setOnAction(e2 -> {
                    Document doc = getTableView().getItems().get(getIndex());
                    userDAO.deleteUser(doc.getString("email"));
                    showUsers(null); // refresh list
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(5, editBtn, delBtn)); // add buttons to cell
            }
        });

        // Load all users from MongoDB
        List<Document> users = userDAO.getAllUsers();
        userTable.setItems(FXCollections.observableArrayList(users));
        userTable.getColumns().addAll(nameCol, emailCol, actionCol);

        // Display table in main content area
        mainContent.getChildren().setAll(userTable);
    }

    // Opens modal dialog for editing user details
    private void openEditDialog(Document userDoc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/admin/edit-user.fxml"));
            Parent root = loader.load();
            EditUserController controller = loader.getController();
            controller.setUserData(userDoc);
            controller.setOnSaveCallback(this::showUsers);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Edit User");
            dialog.setScene(new Scene(root));
            dialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // -----------------------------
    // PRODUCT MANAGEMENT VIEW
    // -----------------------------
    @FXML
    private void openProducts(ActionEvent e) {
        try {
            // Load the full-screen product manager page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/admin/product-manager.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Product Manager");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // -----------------------------
    // REPORTS VIEW
    // -----------------------------
    @FXML
    private void showReports(ActionEvent e) {
        try {
            // Load reports FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/admin/reports-view.fxml"));
            Parent reportsRoot = loader.load();

            // Access ReportsController to load transaction data
            Object ctrl = loader.getController();
            if (ctrl instanceof ReportsController) {
                ((ReportsController) ctrl).loadTransactions(); // fetch and display transactions
            }

            // Show reports content in main area
            mainContent.getChildren().setAll(reportsRoot);
        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load Reports view: " + ex.getMessage()).showAndWait();
        }
    }

    // -----------------------------
    // ADMIN SETTINGS (Change Password)
    // -----------------------------
    @FXML
    private void showAdminMenu(ActionEvent e) {
        // Creates layout for password change form
        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 20;");

        Label title = new Label("Change Admin Password");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("Enter new password");
        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        // Save button updates admin password in MongoDB
        saveBtn.setOnAction(ae -> {
            if (newPass.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Password cannot be empty").showAndWait();
                return;
            }
            userDAO.updateAdminPassword(newPass.getText());
            new Alert(Alert.AlertType.INFORMATION, "Admin password updated!").showAndWait();
            newPass.clear();
        });

        box.getChildren().addAll(title, newPass, saveBtn);
        mainContent.getChildren().setAll(box);
    }

    // -----------------------------
    // LOGOUT HANDLER
    // -----------------------------
    @FXML
    private void handleLogout(ActionEvent e) {
        try {
            // Redirects to login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/login.fxml"));
            Scene scene = new Scene(loader.load(), 600, 500);
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Login");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

package oop.licao.smartpos.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import oop.licao.smartpos.model.CartItem;
import oop.licao.smartpos.util.CartService;
import oop.licao.smartpos.util.NavigationUtil;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CheckoutController {

    @FXML private ComboBox<String> paymentCombo;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> productCol;
    @FXML private TableColumn<CartItem, Integer> qtyCol;
    @FXML private TableColumn<CartItem, Double> subtotalCol;
    @FXML private TableColumn<CartItem, Void> actionCol;  // ✅ NEW column for +, -, delete buttons
    @FXML private Label totalLabel;

    private final String[] PAYMENT_METHODS = {"Cash", "Credit Card", "GCash", "Debit Card"};
    private Runnable onCheckoutSuccess;

    public void setOnCheckoutSuccess(Runnable onCheckoutSuccess) {
        this.onCheckoutSuccess = onCheckoutSuccess;
    }

    @FXML
    public void initialize() {
        paymentCombo.setItems(FXCollections.observableArrayList(PAYMENT_METHODS));

        productCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getProduct().getProductName()));
        qtyCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getQuantity()));
        subtotalCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getSubtotal()));

        // ✅ Add interactive buttons
        addActionButtonsToTable();

        loadCart();
    }

    private void loadCart() {
        cartTable.setItems(FXCollections.observableList(CartService.getCart().getItems()));
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        totalLabel.setText("Total: ₱" + String.format("%.2f", CartService.getCart().getTotal()));
    }

    // ✅ Adds the +, -, and Delete buttons inside the table
    private void addActionButtonsToTable() {
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("+");
            private final Button subBtn = new Button("-");
            private final Button delBtn = new Button("🗑");

            {
                addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                subBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                addBtn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    item.setQuantity(item.getQuantity() + 1);
                    CartService.getCart().updateTotal();
                    refreshTable();
                });

                subBtn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                    } else {
                        CartService.getCart().removeItem(item.getProduct());
                    }
                    CartService.getCart().updateTotal();
                    refreshTable();
                });

                delBtn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    CartService.getCart().removeItem(item.getProduct());
                    CartService.getCart().updateTotal();
                    refreshTable();
                });
            }

            private void refreshTable() {
                cartTable.refresh();
                updateTotalLabel();
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(5, addBtn, subBtn, delBtn));
            }
        });
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        String method = paymentCombo.getValue();
        if (method == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a payment method").showAndWait();
            return;
        }

        new Alert(Alert.AlertType.INFORMATION, "Payment via " + method + " successful!").showAndWait();

        String invoiceId = "INV-" + System.currentTimeMillis();
        double total = CartService.getCart().getTotal();
        LocalDateTime date = LocalDateTime.now();

        saveTransactionToMongo(invoiceId, method, total, date);
        ReceiptControllerHelper.showReceiptWindow(invoiceId, method, CartService.getCart());

        if (onCheckoutSuccess != null) onCheckoutSuccess.run();

        CartService.getCart().clear();
        NavigationUtil.goToHome((Node) event.getSource());
    }

    private void saveTransactionToMongo(String invoiceId, String paymentMethod, double total, LocalDateTime date) {
        try (MongoClient mongoClient = MongoClients.create(
                "mongodb+srv://admin:g2WWDuaR9p5raTW2@cluster0.uu2qv2w.mongodb.net/SmartPOS?retryWrites=true&w=majority&appName=Cluster0")) {

            MongoDatabase database = mongoClient.getDatabase("SmartPOS");
            MongoCollection<Document> collection = database.getCollection("Transactions");

            List<Document> items = new ArrayList<>();
            for (CartItem item : CartService.getCart().getItems()) {
                Document itemDoc = new Document("productName", item.getProduct().getProductName())
                        .append("quantity", item.getQuantity())
                        .append("subtotal", item.getSubtotal());
                items.add(itemDoc);
            }

            Document transactionDoc = new Document("invoiceId", invoiceId)
                    .append("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .append("paymentMethod", paymentMethod)
                    .append("total", total)
                    .append("items", items);

            collection.insertOne(transactionDoc);
            System.out.println("✅ Transaction saved to MongoDB: " + invoiceId);

        } catch (Exception e) {
            System.err.println("❌ Error saving transaction: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        NavigationUtil.goToCart((Node) event.getSource());
    }

    @FXML private void handleBackToCart(ActionEvent e) { NavigationUtil.goToCart((Node) e.getSource()); }
    @FXML private void handleBackToShop(ActionEvent e) { NavigationUtil.goToHome((Node) e.getSource()); }
    @FXML private void handleLogout(ActionEvent e) { NavigationUtil.goToLogin((Node) e.getSource()); }
}

package oop.licao.smartpos.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import oop.licao.smartpos.dao.ProductDAO;
import oop.licao.smartpos.model.CartItem;
import oop.licao.smartpos.util.CartService;

public class CartController {

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> productColumn;
    @FXML private TableColumn<CartItem, Double> priceColumn;
    @FXML private TableColumn<CartItem, Integer> qtyColumn;
    @FXML private TableColumn<CartItem, Double> subtotalColumn;
    @FXML private TableColumn<CartItem, Void> actionColumn; // ✅ new column for buttons
    @FXML private Label totalLabel;

    private final ProductDAO productDAO = new ProductDAO();

    @FXML
    public void initialize() {
        setupTable();
        loadCart();
    }

    // ✅ Set up all table columns and action buttons
    private void setupTable() {
        productColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getProduct().getProductName()));
        priceColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getProduct().getPrice()));
        qtyColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getQuantity()));
        subtotalColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getSubtotal()));

        // ✅ Add the +, -, 🗑 buttons per row
        addActionButtonsToTable();
    }

    private void loadCart() {
        cartTable.setItems(FXCollections.observableList(CartService.getCart().getItems()));
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        totalLabel.setText("Total: ₱" + String.format("%.2f", CartService.getCart().getTotal()));
    }

    // ✅ Adds the three buttons in each row
    private void addActionButtonsToTable() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("+");
            private final Button subBtn = new Button("-");
            private final Button delBtn = new Button("🗑");

            {
                addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                subBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                addBtn.setOnAction(e -> modifyQuantity(1));
                subBtn.setOnAction(e -> modifyQuantity(-1));
                delBtn.setOnAction(e -> removeItem());
            }

            private void modifyQuantity(int change) {
                CartItem item = getTableView().getItems().get(getIndex());
                int newQty = item.getQuantity() + change;

                if (newQty <= 0) {
                    CartService.getCart().removeItem(item.getProduct());
                } else {
                    item.setQuantity(newQty);
                }
                CartService.getCart().updateTotal();
                refreshCart();
            }

            private void removeItem() {
                CartItem item = getTableView().getItems().get(getIndex());
                CartService.getCart().removeItem(item.getProduct());
                CartService.getCart().updateTotal();
                refreshCart();
            }

            private void refreshCart() {
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

    // ✅ Proceed to checkout
    @FXML
    private void handleCheckout(ActionEvent event) {
        if (CartService.getCart().getItems().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Your cart is empty!").showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/customer/checkout.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            CheckoutController checkoutController = loader.getController();
            checkoutController.setOnCheckoutSuccess(this::loadCart);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Checkout");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading Checkout screen.").showAndWait();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/customer/home.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Shop");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToCart(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/customer/cart.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Cart");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to reload Cart page.").showAndWait();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/login.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Login");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to logout.").showAndWait();
        }
    }
}

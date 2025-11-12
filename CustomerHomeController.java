package oop.licao.smartpos.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import oop.licao.smartpos.dao.ProductDAO;
import oop.licao.smartpos.model.Cart;
import oop.licao.smartpos.model.CartItem;
import oop.licao.smartpos.model.Category;
import oop.licao.smartpos.model.Product;
import oop.licao.smartpos.util.CartService;
import oop.licao.smartpos.util.MockDataService;

import java.io.File;
import java.util.*;

/**
 * CustomerHomeController.java
 * Controls the customer home view — displays products, handles category filtering,
 * adds items to the cart, and manages navigation to cart and login screens.
 */
public class CustomerHomeController {

    @FXML private ComboBox<Category> categoryCombo;
    @FXML private FlowPane productFlowPane;
    @FXML private Label cartItemCount;

    private List<Product> currentProducts = new ArrayList<>(); // stores currently displayed products

    @FXML
    public void initialize() {
        loadCategories();   // initialize category options
        loadAllProducts();  // load all products from DB
        updateCartCount();  // show total items in cart
    }

    // Loads category list into combo box
    private void loadCategories() {
        categoryCombo.setItems(FXCollections.observableArrayList(MockDataService.getCategories()));
        categoryCombo.setOnAction(this::handleCategoryChange);
    }

    // Loads all products from database
    private void loadAllProducts() {
        currentProducts = new ProductDAO().findAll();
        displayProducts(currentProducts);
    }

    // Handles category selection change
    private void handleCategoryChange(ActionEvent event) {
        Category selected = categoryCombo.getValue();
        if (selected == null) {
            loadAllProducts(); // show all if none selected
        } else {
            currentProducts = new ProductDAO().findByCategoryID(selected.getCategoryID());
            displayProducts(currentProducts);
        }
    }

    // Shows all products (resets filter)
    @FXML
    private void handleShowAll(ActionEvent event) {
        categoryCombo.getSelectionModel().clearSelection();
        loadAllProducts();
    }

    // Displays product cards dynamically in flow pane
    private void displayProducts(List<Product> products) {
        productFlowPane.getChildren().clear();
        for (Product p : products) {
            productFlowPane.getChildren().add(createProductCard(p));
        }
    }

    // Creates a product card with image, details, and add-to-cart button
    private VBox createProductCard(Product product) {
        VBox card = new VBox(5);
        card.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: white;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // Display product image if available
        if (product.getImagePath() != null) {
            File f = new File(product.getImagePath());
            if (f.exists()) imageView.setImage(new Image(f.toURI().toString()));
        }

        Label nameLabel = new Label(product.getProductName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label priceLabel = new Label("₱" + product.getPrice());
        Label descLabel = new Label(product.getDescription());
        Label stockLabel = new Label("Stock: " + product.getStock());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(120);

        // Quantity controls
        Button minusBtn = new Button("-");
        Button plusBtn = new Button("+");
        TextField qtyField = new TextField("1");
        qtyField.setPrefWidth(35);
        qtyField.setEditable(false);

        HBox qtyBox = new HBox(5, minusBtn, qtyField, plusBtn);

        // Decrease quantity
        minusBtn.setOnAction(e -> {
            int qty = Integer.parseInt(qtyField.getText());
            if (qty > 1) qtyField.setText(String.valueOf(qty - 1));
        });

        // Increase quantity up to stock limit
        plusBtn.setOnAction(e -> {
            int qty = Integer.parseInt(qtyField.getText());
            if (qty < product.getStock()) qtyField.setText(String.valueOf(qty + 1));
        });

        // Add to cart button
        Button addButton = new Button("Add to Cart");
        addButton.setOnAction(e -> handleAddToCart(product, Integer.parseInt(qtyField.getText())));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, descLabel, stockLabel, qtyBox, addButton);
        return card;
    }

    // Adds selected product to cart with specified quantity
    private void handleAddToCart(Product product, int quantity) {
        // Reload product from DB to ensure latest stock
        Product fresh = new ProductDAO().findByID(product.getProductID());
        if (fresh == null) {
            new Alert(Alert.AlertType.ERROR, "Product not found in database!").showAndWait();
            return;
        }

        // Prevent adding more than available stock
        if (quantity > fresh.getStock()) {
            new Alert(Alert.AlertType.WARNING, "Not enough stock! Available: " + fresh.getStock()).showAndWait();
            return;
        }

        Cart cart = CartService.getCart();
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item -> item.getProduct().getProductID().equals(fresh.getProductID()))
                .findFirst();

        // If product already exists, increase quantity
        if (existing.isPresent()) {
            int newQty = existing.get().getQuantity() + quantity;
            if (newQty > fresh.getStock()) {
                new Alert(Alert.AlertType.WARNING, "Only " + fresh.getStock() + " items in stock!").showAndWait();
                return;
            }
            existing.get().setQuantity(newQty);
        } else {
            cart.addItem(fresh, quantity); // add as new item
        }

        // Update cart and notify user
        CartService.getCart().updateTotal();
        updateCartCount();
        new Alert(Alert.AlertType.INFORMATION,
                quantity + " × " + fresh.getProductName() + " added to cart!").showAndWait();
    }

    // Updates cart item counter in the UI
    private void updateCartCount() {
        int count = CartService.getCart().getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        cartItemCount.setText("Cart: " + count + " item(s)");
    }

    // Navigates to cart screen
    @FXML
    private void handleViewCart(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/customer/cart.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Your Cart");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Logs out and returns to login page
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/login.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) productFlowPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Login");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to logout").showAndWait();
        }
    }

    // Refreshes product list from database (called by other controllers)
    public void refreshProducts() {
        loadAllProducts();
    }
}

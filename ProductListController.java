package oop.licao.smartpos.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.beans.property.SimpleObjectProperty;
import java.io.File;
import oop.licao.smartpos.dao.ProductDAO;
import oop.licao.smartpos.model.Product;

/**
 * ProductListController.java
 * Displays all products in a table for admin view and allows editing/deleting.
 */
public class ProductListController {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, ImageView> imageColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String> descColumn;

    private final ProductDAO dao = new ProductDAO(); // DAO for database access

    @FXML
    public void initialize() {
        setupTableColumns(); // configure table columns
        loadProducts();      // load all products from DB
    }

    // Configures table column bindings to Product model properties
    private void setupTableColumns() {
        imageColumn.setCellValueFactory(param -> {
            String imagePath = param.getValue().getImagePath();
            ImageView view = new ImageView();
            view.setFitWidth(60);
            view.setFitHeight(60);
            view.setPreserveRatio(true);
            if (imagePath != null) {
                File f = new File(imagePath);
                if (f.exists()) view.setImage(new Image(f.toURI().toString()));
            }
            return new SimpleObjectProperty<>(view);
        });

        // Bind table columns to Product properties
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryID"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    // Loads all products from MongoDB
    private void loadProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList(dao.findAll());
        productTable.setItems(products);
    }

    // Refreshes product list
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadProducts();
    }

    // Navigates back to AddProduct view
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/admin/add-product.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            Stage stage = (Stage) productTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Add Product");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load Add Product form: " + e.getMessage()).showAndWait();
        }
    }

    // Opens selected product for editing
    @FXML
    private void handleEditProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a product to edit.").showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/admin/add-product.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            AddProductController controller = loader.getController();
            controller.loadProductData(selected);

            Stage stage = (Stage) productTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Edit Product");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open edit form: " + e.getMessage()).showAndWait();
        }
    }

    // Deletes selected product after confirmation
    @FXML
    private void handleDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a product to delete.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete '" + selected.getProductName() + "'?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    dao.deleteProduct(selected.getProductID());
                    loadProducts();
                    new Alert(Alert.AlertType.INFORMATION, "Product deleted successfully.").showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to delete product: " + e.getMessage()).showAndWait();
                }
            }
        });
    }
}

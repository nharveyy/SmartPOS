package oop.licao.smartpos.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import oop.licao.smartpos.dao.MongoConnection;
import oop.licao.smartpos.dao.ProductDAO;
import oop.licao.smartpos.model.Product;
import org.bson.Document;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProductManagerController.java
 * Provides full admin control for managing products — add, edit, delete, and view.
 * This version includes null-safety to support FXML layouts where category fields were removed.
 */
public class ProductManagerController {

    @FXML private TabPane tabPane;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, ImageView> imageColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, String> descColumn;

    @FXML private TextField addNameField, addPriceField, addStockField;
    @FXML private TextArea addDescArea;
    @FXML private Label addImageLabel;

    // ComboBoxes are kept as null-safe placeholders since they were removed from FXML
    @FXML private ComboBox<String> addCategoryCombo = null;
    @FXML private ComboBox<String> editProductCombo, editCategoryCombo = null;

    @FXML private TextField editNameField, editPriceField, editStockField;
    @FXML private TextArea editDescArea;
    @FXML private Label editImageLabel;

    private File selectedAddImage, selectedEditImage;
    private final ProductDAO productDAO = new ProductDAO();
    private Product selectedProduct; // product currently selected for editing

    @FXML
    public void initialize() {
        setupTable();      // configure table columns
        loadProducts();    // load all product data
        loadCategories();  // populate categories if field exists
        if (editProductCombo != null) {
            editProductCombo.setOnAction(e -> loadSelectedProduct());
        }
    }

    // ---------- Setup Methods ----------
    private void setupTable() {
        imageColumn.setCellValueFactory(param -> {
            String imagePath = param.getValue().getImagePath();
            ImageView view = new ImageView();
            view.setFitWidth(60);
            view.setFitHeight(60);
            if (imagePath != null && !imagePath.isEmpty()) {
                File f = new File(imagePath);
                if (f.exists()) view.setImage(new Image(f.toURI().toString()));
            }
            return new javafx.beans.property.SimpleObjectProperty<>(view);
        });
        nameColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProductName()));
        priceColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getPrice()));
        stockColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getStock()));
        categoryColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategoryID()));
        descColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));
    }

    // Loads all products and populates combo box for editing
    private void loadProducts() {
        List<Product> products = productDAO.findAll();
        productTable.setItems(FXCollections.observableArrayList(products));
        if (editProductCombo != null) {
            editProductCombo.setItems(FXCollections.observableArrayList(
                    products.stream().map(Product::getProductName).collect(Collectors.toList())
            ));
        }
    }

    // Loads category names only if combo boxes exist
    private void loadCategories() {
        if (addCategoryCombo == null && editCategoryCombo == null) return;

        MongoCollection<Document> collection = MongoConnection.getDatabase().getCollection("products");
        Set<String> categoryNames = new HashSet<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                if (doc.containsKey("categoryName")) {
                    categoryNames.add(doc.getString("categoryName"));
                }
            }
        }
        List<String> sorted = new ArrayList<>(categoryNames);
        Collections.sort(sorted);

        if (addCategoryCombo != null) addCategoryCombo.setItems(FXCollections.observableArrayList(sorted));
        if (editCategoryCombo != null) editCategoryCombo.setItems(FXCollections.observableArrayList(sorted));
    }

    // ---------- Add Product ----------
    @FXML
    private void handleChooseAddImage(ActionEvent e) {
        selectedAddImage = chooseImageFile();
        if (selectedAddImage != null) addImageLabel.setText(selectedAddImage.getName());
    }

    @FXML
    private void handleAddProduct(ActionEvent e) {
        try {
            if (addNameField.getText().isBlank() || addPriceField.getText().isBlank()
                    || addStockField.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Please fill all fields").showAndWait();
                return;
            }

            // Create product object (category set to "Uncategorized" if removed)
            Product p = new Product(
                    UUID.randomUUID().toString(),
                    addNameField.getText(),
                    Double.parseDouble(addPriceField.getText()),
                    Integer.parseInt(addStockField.getText()),
                    addDescArea.getText(),
                    selectedAddImage != null ? selectedAddImage.getAbsolutePath() : "",
                    (addCategoryCombo != null && addCategoryCombo.getValue() != null)
                            ? addCategoryCombo.getValue()
                            : "Uncategorized"
            );

            productDAO.insert(p);
            new Alert(Alert.AlertType.INFORMATION, "Product added successfully!").showAndWait();
            clearAddForm();
            loadProducts();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error adding product: " + ex.getMessage()).showAndWait();
        }
    }

    private void clearAddForm() {
        addNameField.clear();
        addPriceField.clear();
        addStockField.clear();
        addDescArea.clear();
        addImageLabel.setText("(No image selected)");
        if (addCategoryCombo != null) addCategoryCombo.getSelectionModel().clearSelection();
    }

    // ---------- Edit Product ----------
    private void loadSelectedProduct() {
        String selectedName = editProductCombo != null ? editProductCombo.getValue() : null;
        if (selectedName == null) return;

        selectedProduct = productDAO.findAll().stream()
                .filter(p -> p.getProductName().equals(selectedName))
                .findFirst()
                .orElse(null);

        if (selectedProduct != null) {
            editNameField.setText(selectedProduct.getProductName());
            editPriceField.setText(String.valueOf(selectedProduct.getPrice()));
            editStockField.setText(String.valueOf(selectedProduct.getStock()));
            editDescArea.setText(selectedProduct.getDescription());
            if (editCategoryCombo != null)
                editCategoryCombo.setValue(selectedProduct.getCategoryID());
            editImageLabel.setText(selectedProduct.getImagePath() != null
                    ? new File(selectedProduct.getImagePath()).getName()
                    : "(No image)");
        }
    }

    @FXML
    private void handleChooseEditImage(ActionEvent e) {
        selectedEditImage = chooseImageFile();
        if (selectedEditImage != null) editImageLabel.setText(selectedEditImage.getName());
    }

    @FXML
    private void handleUpdateProduct(ActionEvent e) {
        if (selectedProduct == null) {
            new Alert(Alert.AlertType.WARNING, "Select a product first").showAndWait();
            return;
        }

        try {
            selectedProduct.setProductName(editNameField.getText());
            selectedProduct.setPrice(Double.parseDouble(editPriceField.getText()));
            selectedProduct.setStock(Integer.parseInt(editStockField.getText()));
            selectedProduct.setDescription(editDescArea.getText());
            if (editCategoryCombo != null && editCategoryCombo.getValue() != null)
                selectedProduct.setCategoryID(editCategoryCombo.getValue());
            if (selectedEditImage != null)
                selectedProduct.setImagePath(selectedEditImage.getAbsolutePath());

            productDAO.updateProduct(selectedProduct);
            new Alert(Alert.AlertType.INFORMATION, "Product updated successfully!").showAndWait();
            loadProducts();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error updating product: " + ex.getMessage()).showAndWait();
        }
    }

    // ---------- Delete Product ----------
    @FXML
    private void handleDeleteProduct(ActionEvent e) {
        Product p = productTable.getSelectionModel().getSelectedItem();
        if (p == null) {
            new Alert(Alert.AlertType.WARNING, "Select a product to delete.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + p.getProductName() + "?", ButtonType.YES, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                productDAO.deleteProduct(p.getProductID());
                loadProducts();
            }
        });
    }

    // ---------- Utility ----------
    private File chooseImageFile() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        return chooser.showOpenDialog(null);
    }

    @FXML
    private void handleRefresh(ActionEvent e) {
        loadProducts();
        loadCategories();
    }

    @FXML
    private void handleBackToDashboard(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/admin/admin-dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Admin Dashboard");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent e) {
        try {
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

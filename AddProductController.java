package oop.licao.smartpos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import oop.licao.smartpos.dao.ProductDAO;
import oop.licao.smartpos.model.Product;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * AddProductController.java
 * Handles adding, editing, deleting, and viewing product data.
 * Connects UI form fields to ProductDAO for database operations.
 */
public class AddProductController {

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea descriptionArea;
    @FXML private Label imageLabel;
    @FXML private ImageView imageView;

    private File selectedImageFile; // holds image file selected by user
    private final ProductDAO dao = new ProductDAO(); // data access object for product operations
    private Product editingProduct; // used when updating an existing product

    @FXML
    public void initialize() {
        // Initialize default categories if none exist
        if (categoryCombo.getItems().isEmpty()) {
            categoryCombo.getItems().addAll("Beverages", "Snacks", "Others");
        }
        imageLabel.setText("(No image)");
    }

    // Opens file chooser to select product image
    @FXML
    private void handleChooseImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Product Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        selectedImageFile = chooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            imageLabel.setText(selectedImageFile.getName());
            imageView.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    // Saves new product or updates existing one
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateFields()) return; // ensure required fields are valid

        try {
            Product product = new Product();

            // Create new or reuse existing product ID
            if (editingProduct == null) {
                product.setProductID(UUID.randomUUID().toString());
            } else {
                product.setProductID(editingProduct.getProductID());
            }

            // Populate product fields from form
            product.setProductName(nameField.getText().trim());
            product.setCategoryID(categoryCombo.getValue() != null ? categoryCombo.getValue().trim() : "");
            product.setDescription(descriptionArea.getText().trim());
            product.setPrice(Double.parseDouble(priceField.getText().trim()));
            product.setStock(Integer.parseInt(stockField.getText().trim()));

            // Handle image assignment
            if (selectedImageFile != null) {
                product.setImagePath(saveImage(selectedImageFile)); // save selected image
            } else if (editingProduct != null && editingProduct.getImagePath() != null) {
                product.setImagePath(editingProduct.getImagePath()); // keep previous image
            } else {
                product.setImagePath(null); // no image provided
            }

            // Insert or update product in database
            if (editingProduct == null) {
                dao.insert(product); // create new product record
                showInformation("Product saved successfully!");
            } else {
                dao.updateProduct(product); // update existing record
                showInformation("Product updated successfully!");
            }

            clearFields(); // reset form after saving

        } catch (NumberFormatException nfe) {
            showError("Price and Stock must be valid numbers.");
        } catch (Exception e) {
            showError("Error saving/updating product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Puts form into edit mode for an existing product
    @FXML
    private void handleEditProduct(ActionEvent event) {
        if (editingProduct == null) {
            showError("No product selected for editing.");
            return;
        }
        nameField.requestFocus(); // focus name field for editing
    }

    // Deletes the currently selected product
    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        if (editingProduct == null) {
            showError("Select a product to delete first.");
            return;
        }

        // Confirm deletion before proceeding
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Product");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this product?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    dao.deleteProduct(editingProduct.getProductID());
                    showInformation("Product deleted successfully!");
                    clearFields();
                } catch (Exception e) {
                    showError("Failed to delete product: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // Loads product list screen
    @FXML
    private void handleViewProducts(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/oop/licao/smartpos/admin/product-list.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Failed to open product list: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Clears all input fields
    @FXML
    private void handleCancel(ActionEvent event) {
        clearFields();
    }

    // Logs out and returns to login screen
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/oop/licao/smartpos/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Failed to logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Placeholder action for adding a new category (not yet implemented)
    @FXML
    private void handleNewCategory(ActionEvent event) {
        showInformation("This feature (Add New Category) will be available soon!");
    }

    // Loads product data into form for editing
    public void loadProductData(Product product) {
        setEditingProduct(product);
    }

    // Assigns product to be edited and populates fields
    public void setEditingProduct(Product product) {
        this.editingProduct = product;
        if (product == null) return;

        nameField.setText(product.getProductName());
        priceField.setText(String.valueOf(product.getPrice()));
        stockField.setText(String.valueOf(product.getStock()));
        categoryCombo.setValue(product.getCategoryID());
        descriptionArea.setText(product.getDescription());

        // Load product image if available
        if (product.getImagePath() != null) {
            File f = new File(product.getImagePath());
            if (f.exists()) {
                imageView.setImage(new Image(f.toURI().toString()));
                imageLabel.setText(f.getName());
            } else {
                imageView.setImage(null);
                imageLabel.setText("(No image)");
            }
        } else {
            imageView.setImage(null);
            imageLabel.setText("(No image)");
        }
    }

    // Validates input fields before saving
    private boolean validateFields() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty() ||
                priceField.getText() == null || priceField.getText().trim().isEmpty() ||
                stockField.getText() == null || stockField.getText().trim().isEmpty() ||
                categoryCombo.getValue() == null || categoryCombo.getValue().trim().isEmpty()) {
            showError("Please fill in all required fields.");
            return false;
        }
        try {
            Double.parseDouble(priceField.getText().trim());
            Integer.parseInt(stockField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Price and Stock must be numeric.");
            return false;
        }
        return true;
    }

    // Saves selected image to local folder and returns file path
    private String saveImage(File file) throws IOException {
        File dir = new File("images");
        if (!dir.exists()) dir.mkdirs();
        File dest = new File(dir, file.getName());
        try (FileInputStream in = new FileInputStream(file)) {
            Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return dest.getAbsolutePath();
    }

    // Resets all form inputs
    private void clearFields() {
        nameField.clear();
        priceField.clear();
        stockField.clear();
        categoryCombo.getSelectionModel().clearSelection();
        descriptionArea.clear();
        imageLabel.setText("(No image)");
        imageView.setImage(null);
        selectedImageFile = null;
        editingProduct = null;
    }

    // Displays error alert
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // Displays information alert
    private void showInformation(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

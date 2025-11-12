package oop.licao.smartpos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import oop.licao.smartpos.model.Cart;
import oop.licao.smartpos.model.CartItem;
import oop.licao.smartpos.util.NavigationUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ReceiptController.java
 * Displays the receipt after a successful checkout including item details and totals.
 */
public class ReceiptController {

    @FXML private Label invoiceLabel;
    @FXML private Label dateLabel;
    @FXML private Label totalLabel;
    @FXML private Label paymentLabel;
    @FXML private VBox itemsBox;

    // Populates receipt fields and item list
    public void setInvoiceData(String invoiceId, String paymentMethod, Cart cart) {
        invoiceLabel.setText("INVOICE: " + invoiceId);
        dateLabel.setText("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        totalLabel.setText("TOTAL: ₱" + String.format("%.2f", cart.getTotal()));
        paymentLabel.setText("Payment: " + paymentMethod);

        itemsBox.getChildren().clear();
        for (CartItem item : cart.getItems()) {
            String line = String.format("%s x%d = ₱%.2f",
                    item.getProduct().getProductName(),
                    item.getQuantity(),
                    item.getSubtotal());
            itemsBox.getChildren().add(new javafx.scene.control.Label(line));
        }
    }

    // Navigates back to shopping page
    @FXML
    private void handleContinueShopping(ActionEvent event) {
        NavigationUtil.goToHome((Node) event.getSource());
    }

    // Logs out and goes to login screen
    @FXML
    private void handleLogout(ActionEvent event) {
        NavigationUtil.goToLogin((Node) event.getSource());
    }

    // Placeholder for receipt download functionality
    @FXML
    private void handleDownload() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Receipt Saved");
        alert.setHeaderText(null);
        alert.setContentText("Invoice saved to receipts/ folder");
        alert.showAndWait();
    }
}

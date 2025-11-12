package oop.licao.smartpos.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import oop.licao.smartpos.model.Cart;

/**
 * ReceiptControllerHelper.java
 * Utility class to open and display the receipt window after checkout.
 */
public class ReceiptControllerHelper {
    public static void showReceiptWindow(String invoiceId,
                                         String paymentMethod,
                                         Cart cart) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ReceiptControllerHelper.class.getResource(
                            "/oop/licao/smartpos/customer/receipt.fxml"));
            Scene scene = new Scene(loader.load(), 600, 500);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Invoice #" + invoiceId);

            // Pass data to receipt controller
            ReceiptController controller = loader.getController();
            controller.setInvoiceData(invoiceId, paymentMethod, cart);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Failed to load receipt: " + e.getMessage()).showAndWait();
        }
    }
}

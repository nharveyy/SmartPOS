package oop.licao.smartpos.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * NavigationUtil.java
 * Provides centralized navigation across SmartPOS screens (login, home, cart, etc.).
 * Simplifies view switching in controllers.
 */
public class NavigationUtil {

    // Loads the given FXML file and switches the current scene
    private static void goTo(Node node, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Failed to load view: " + fxmlPath + "\n" + e.getMessage()).showAndWait();
        }
    }

    // Navigates to customer home (shop)
    public static void goToHome(Node node) {
        goTo(node, "/oop/licao/smartpos/customer/home.fxml", "SmartPOS - Shop");
    }

    // Navigates to shopping cart
    public static void goToCart(Node node) {
        goTo(node, "/oop/licao/smartpos/customer/cart.fxml", "SmartPOS - Your Cart");
    }

    // Navigates to checkout page
    public static void goToCheckout(Node node) {
        goTo(node, "/oop/licao/smartpos/customer/checkout.fxml", "SmartPOS - Checkout");
    }

    // Navigates to login page
    public static void goToLogin(Node node) {
        goTo(node, "/oop/licao/smartpos/login.fxml", "SmartPOS - Login");
    }
}

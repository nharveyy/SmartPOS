package oop.licao.smartpos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import oop.licao.smartpos.dao.UserDAO;

/**
 * LoginController.java
 * Handles user authentication and navigation to respective dashboards.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final UserDAO userDAO = new UserDAO(); // DAO for user validation

    // Handles login button click and validates user credentials
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        // Basic validation for empty fields
        if (email.isEmpty() || pass.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please fill in all fields").showAndWait();
            return;
        }

        // Verify credentials and get user role
        String role = userDAO.validateUserRole(email, pass);
        if (role == null) {
            new Alert(Alert.AlertType.ERROR, "Invalid credentials").showAndWait();
            return;
        }

        // Redirect user to the appropriate dashboard
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            if (role.equals("admin")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/admin/admin-dashboard.fxml"));
                stage.setScene(new Scene(loader.load(), 1200, 700));
                stage.setTitle("SmartPOS - Admin Dashboard");
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/customer/home.fxml"));
                stage.setScene(new Scene(loader.load(), 1000, 700));
                stage.setTitle("SmartPOS - Customer Home");
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load next screen").showAndWait();
        }
    }

    // Navigates to account creation (registration) screen
    @FXML
    private void handleCreateAccount(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/register.fxml"));
            Scene scene = new Scene(loader.load(), 500, 600);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Create Account");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open registration form").showAndWait();
        }
    }
}

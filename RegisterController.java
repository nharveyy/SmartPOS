package oop.licao.smartpos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import oop.licao.smartpos.dao.UserDAO;

/**
 * RegisterController.java
 * Handles user registration logic and form validation for new account creation.
 */
public class RegisterController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private final UserDAO userDAO = new UserDAO(); // DAO for user registration

    // Handles registration button click
    @FXML
    private void handleRegister(ActionEvent event) {
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        // Validate required fields
        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please fill in all fields").showAndWait();
            return;
        }

        // Validate email format
        if (!email.contains("@") || !email.contains(".")) {
            new Alert(Alert.AlertType.WARNING, "Invalid email format").showAndWait();
            return;
        }

        // Validate password match
        if (!pass.equals(confirm)) {
            new Alert(Alert.AlertType.WARNING, "Passwords do not match").showAndWait();
            return;
        }

        // Register new user in MongoDB
        boolean success = userDAO.registerUser(first, last, email, pass);
        if (!success) {
            new Alert(Alert.AlertType.ERROR, "Email already registered").showAndWait();
            return;
        }

        new Alert(Alert.AlertType.INFORMATION, "Account created successfully! You can now log in.")
                .showAndWait();
        goToLogin(event);
    }

    // Navigates back to login screen
    @FXML
    private void handleBackToLogin(ActionEvent event) {
        goToLogin(event);
    }

    // Helper method to go to login page
    private void goToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/licao/smartpos/login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 600);
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Login");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to return to login").showAndWait();
        }
    }
}

package oop.licao.smartpos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import oop.licao.smartpos.dao.UserDAO;
import org.bson.Document;

import java.util.function.Consumer;

/**
 * EditUserController.java
 * Handles editing existing user data within the admin panel.
 * Updates user records in MongoDB through UserDAO.
 */
public class EditUserController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private Document userDoc; // currently selected user document
    private final UserDAO userDAO = new UserDAO(); // handles database operations
    private Consumer<ActionEvent> onSaveCallback; // callback to refresh user list after save

    // Loads selected user data into text fields
    public void setUserData(Document userDoc) {
        this.userDoc = userDoc;
        firstNameField.setText(userDoc.getString("firstName"));
        lastNameField.setText(userDoc.getString("lastName"));
        emailField.setText(userDoc.getString("email"));
        passwordField.setText(userDoc.getString("password"));
    }

    // Sets callback to execute after saving (refresh parent view)
    public void setOnSaveCallback(Consumer<ActionEvent> callback) {
        this.onSaveCallback = callback;
    }

    // Handles save button click — validates input and updates user in DB
    @FXML
    private void handleSave(ActionEvent event) {
        if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "All fields required").showAndWait();
            return;
        }

        // Update user record in MongoDB
        userDAO.updateUser(emailField.getText(),
                firstNameField.getText(),
                lastNameField.getText(),
                passwordField.getText());

        new Alert(Alert.AlertType.INFORMATION, "User updated successfully!").showAndWait();

        // Trigger callback and close dialog
        if (onSaveCallback != null) onSaveCallback.accept(event);
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    // Closes dialog without saving
    @FXML
    private void handleCancel(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}

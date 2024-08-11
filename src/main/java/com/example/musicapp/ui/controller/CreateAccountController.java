package com.example.musicapp.ui.controller;

import com.example.musicapp.data.database.controller.DatabaseController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CreateAccountController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private CheckBox adsCheckBox;
    @FXML
    private CheckBox dirCheckBox;
    @FXML
    private CheckBox usersCheckBox,adminCheckBox;

    private DatabaseController databaseController;

    public void setDatabaseController(DatabaseController databaseController) {
        this.databaseController = databaseController;
    }

    @FXML
    private void handleCreateAccount() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        boolean ads = adsCheckBox.isSelected();
        boolean dir = dirCheckBox.isSelected();
        boolean users = usersCheckBox.isSelected();
        boolean admin = adminCheckBox.isSelected();

        if (!password.equals(confirmPassword)) {
            showErrorPopup("Passwords do not match!");
            return;
        }

        boolean success = databaseController.createAccount(username, password,admin, ads, dir, users);
        if (success) {
            closeDialog();
        } else {
            showErrorPopup("Failed to create account. Please try again.");
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showErrorPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

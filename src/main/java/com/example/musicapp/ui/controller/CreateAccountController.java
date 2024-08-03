package com.example.musicapp.ui.controller;

import com.example.musicapp.ui.controller.ControlPanelController;
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
    private CheckBox adsCheckBox;
    @FXML
    private CheckBox dirCheckBox;
    @FXML
    private CheckBox usersCheckBox;

    private DatabaseController databaseController;

    public void setDatabaseController(DatabaseController databaseController) {
        this.databaseController = databaseController;
    }

    @FXML
    private void createAccount() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean ads = adsCheckBox.isSelected();
        boolean dir = dirCheckBox.isSelected();
        boolean users = usersCheckBox.isSelected();

        if (username.isEmpty() || password.isEmpty()) {
            ControlPanelController.showErrorPopup("Username or password is empty");
            return;
        }

        databaseController.createAccount(username, password, ads, dir, users);
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}

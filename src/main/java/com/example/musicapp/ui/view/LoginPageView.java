package com.example.musicapp.ui.view;

import com.example.musicapp.data.database.controller.DatabaseController;
import com.example.musicapp.data.modle.User;
import com.example.musicapp.main.App;
import com.example.musicapp.ui.controller.LoginPageController;
import com.sun.glass.ui.View;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import static com.example.musicapp.ui.view.AlertsView.showErrorMessage;
import static com.example.musicapp.ui.view.AlertsView.showWarningMessage;

public class LoginPageView {
    private Stage primaryStage;
    private String state = "register";
    private DatabaseController databaseController;
    private LoginPageController loginPageController;
    private static LoginPageView instanse;

    public LoginPageView(Stage stage, LoginPageController loginPageController) {
        this.primaryStage = stage;
        this.databaseController = DatabaseController.getInstance();
        this.loginPageController = loginPageController;
    }
    public static LoginPageView getInstance(Stage primaryStage, LoginPageController loginPageController) {
        if (instanse == null) {
            synchronized (LoginPageView.class) {
                if (instanse == null) {
                    instanse = new LoginPageView(primaryStage, loginPageController);
                }
            }
        }
        return instanse;
    }


    public void showAuthScene(){
        VBox pane = new VBox(10); // 10px spacing between elements
        pane.setPadding(new javafx.geometry.Insets(20)); // 20px padding around the VBox

        if ("register".equals(state)) {
            System.out.println("Showing register");

            Label title = new Label("Register");
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            TextField usernameField = new TextField();
            usernameField.setPromptText("Enter username");

            TextField passwordField = new TextField();
            passwordField.setPromptText("Enter password");

            TextField confirmPasswordField = new TextField();
            confirmPasswordField.setPromptText("Confirm password");

            Button confirmButton = new Button("Confirm");
            confirmButton.setOnMouseClicked(e -> {
                try {
                    if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                        showWarningMessage("Please check your password again", "The passwords don't match");
                    } else if (databaseController.getUser(usernameField.getText()) != null) {
                        showWarningMessage("User with this username already exists", "Error");
                    } else {
                        databaseController.createAccount(usernameField.getText(), passwordField.getText(), false, false, false, false);
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });

            Button switchToLoginButton = new Button("Already have an account? Login here");
            switchToLoginButton.setOnMouseClicked(e -> {
                state = "login";
                showAuthScene();
            });

            pane.getChildren().addAll(title, usernameField, passwordField, confirmPasswordField, confirmButton, switchToLoginButton);
        } else {
            System.out.println("Showing login");

            Label title = new Label("Login");
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            TextField usernameField = new TextField();
            usernameField.setPromptText("Enter username");

            TextField passwordField = new TextField();
            passwordField.setPromptText("Enter password");

            Button loginButton = new Button("Login");

            loginButton.setOnMouseClicked(e -> {
                 User user = databaseController.login(usernameField.getText(), passwordField.getText());

                System.out.println("loginPageController"+loginPageController);
                if (user != null) {
                    loginPageController.setUser(user);
                } else {
                    showErrorMessage("Username or Password is incorrect. Please check again", "Login Error");
                }
            });

            Button switchToRegisterButton = new Button("Don't have an account? Sign up here");
            switchToRegisterButton.setOnMouseClicked(e -> {
                state = "register";
                showAuthScene();
            });

            pane.getChildren().addAll(title, usernameField, passwordField, loginButton, switchToRegisterButton);
        }
        Scene loginScene = new Scene(pane,500,500);
        Platform.runLater(() -> {
            primaryStage.setScene(loginScene);
            //primaryStage.setFullScreen(true);
        });
    }


}

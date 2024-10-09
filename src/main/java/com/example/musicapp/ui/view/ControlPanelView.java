package com.example.musicapp.ui.view;

import com.example.musicapp.data.modle.User;
import com.example.musicapp.ui.controller.ControlPanelController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class ControlPanelView  {

    private ControlPanelController  controlPanelController;
    public ControlPanelView(ControlPanelController  controlPanelController){
        this.controlPanelController = controlPanelController;
    }

    public VBox createFileBox(File file) {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

        Label label = new Label(file.getName());
        Button button = new Button("Remove");
        button.setOnAction(event -> {
            controlPanelController.adsFile.remove(file);
            controlPanelController.adsPane.getChildren().remove(vbox);
        });

        vbox.getChildren().addAll(label, button);
        return vbox;
    }
    public HBox createUserBox(User user) {
        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setPadding(new Insets(10, 10, 10, 10));

        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setPrefWidth(150);

        CheckBox adsCheckBox = new CheckBox("Ads");
        adsCheckBox.setSelected(user.hasAds());

        CheckBox dirCheckBox = new CheckBox("Dir");
        dirCheckBox.setSelected(user.hasDir());

        CheckBox usersCheckBox = new CheckBox("Users");
        usersCheckBox.setSelected(user.hasUsersAccess());
        CheckBox adminCheckBox = new CheckBox("Admin");
        usersCheckBox.setSelected(user.hasUsersAccess());

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        saveButton.setOnAction(event -> {
            boolean ads = adsCheckBox.isSelected();
            boolean dir = dirCheckBox.isSelected();
            boolean users = usersCheckBox.isSelected();
            boolean admin = adminCheckBox.isSelected();
            controlPanelController.databaseController.updateUserAccess(user.getUsername(),admin, ads, dir, users);
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteButton.setOnAction(event -> {
            controlPanelController.databaseController.deleteUser(user.getUsername());
            controlPanelController.usersPaneScrolle.getChildren().remove(userBox);
        });

        userBox.getChildren().addAll(usernameLabel, adsCheckBox, dirCheckBox, usersCheckBox, saveButton, deleteButton);

        return userBox;
    }

}

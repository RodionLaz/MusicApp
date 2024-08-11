package com.example.musicapp.ui.controller;

import com.example.musicapp.data.database.controller.DatabaseController;
import com.example.musicapp.data.modle.Access;
import com.example.musicapp.data.modle.User;
import com.example.musicapp.ui.controller.CreateAccountController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import com.example.musicapp.data.database.controller.DatabaseController;

import static com.example.musicapp.ui.view.AlertsView.showErrorMessage;

public class ControlPanelController implements Initializable {

    @FXML
    private TilePane adsPane, usersPane, dirPane, usersPaneScrolle;
    @FXML
    private Button addMore, remove;
    @FXML
    private VBox usersBox;
    @FXML
    private VBox Main;
    @FXML
    private Pane adsBtns, userBtns;

    private MainPageController mainPageController;
    private Stage controlPanelStage;
    private Scene controlPanelScene;
    private List<File> adsFile = new ArrayList<>();
    private List<File> selectedToRemove = new ArrayList<>();
    private Parent root;
    private DatabaseController databaseController;

    public ControlPanelController() {
       this.databaseController  = DatabaseController.getInstance();
        if (databaseController == null) {
            System.err.println("DatabaseController instance is null!");
        }
    }

    public void setMainPageController(MainPageController mainPageController) {
        this.mainPageController = mainPageController;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Controller initialized");
    }

    public void show(Access access) {

        controlPanelStage = new Stage();
        if (controlPanelScene == null) {
            controlPanelScene = new Scene(root);
        }
        adsPane.getChildren().clear();
        usersPaneScrolle.getChildren().clear();
        dirPane.getChildren().clear();
        userBtns.getChildren().clear();

        if (access.hasAds()) {
            System.out.println(Objects.requireNonNull(getClass().getResource("/com/example/musicapp/Ads")).getPath());
            getAdFiles(Objects.requireNonNull(getClass().getResource("/com/example/musicapp/Ads")).getPath());
            showFiles();
            HBox hBox = new HBox(10);
            hBox.setAlignment(Pos.CENTER);

            Button addMoreButton = new Button("Add More");
            addMoreButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            addMoreButton.setOnAction(event -> addMore());

            Button removeButton = new Button("Remove Selected");
            removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            removeButton.setOnAction(event -> removefunc());

            hBox.getChildren().addAll(addMoreButton, removeButton);

            adsBtns.getChildren().add(hBox);
            controlPanelStage.setScene(controlPanelScene);
        }

        if (access.hasDir()) {
            Button changeMusicDir = new Button("Change music folder");
            changeMusicDir.setOnMouseClicked(e -> mainPageController.selectMusicFolder());
            dirPane.getChildren().
                    add(changeMusicDir);
        }
        if (access.hasUsersAccess()) {
            List<User> users = databaseController.getUsers();
            for (User user : users) {
                usersPaneScrolle.getChildren().add(createUserBox(user));
            }
            HBox hBox = new HBox(10);
            hBox.setAlignment(Pos.CENTER);

            Button addNewUserButton = new Button("Add New User");
            addNewUserButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            addNewUserButton.setOnAction(event -> showCreateAccountDialog());

            hBox.getChildren().add(addNewUserButton);
            userBtns.getChildren().add(hBox);
            controlPanelStage.setScene(controlPanelScene);
        }

        controlPanelStage.initModality(Modality.APPLICATION_MODAL);
        controlPanelStage.showAndWait();
    }


    private void showCreateAccountDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/musicapp/CreateAccountDialog.fxml"));
            Parent createAccountDialogRoot = loader.load();
            CreateAccountController createAccountController = loader.getController();

            createAccountController.setDatabaseController(databaseController);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Create Account");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(createAccountDialogRoot));
            dialogStage.showAndWait();

            // Refresh user list after closing the dialog
            usersPaneScrolle.getChildren().clear();
            List<User> users = databaseController.getUsers();
            for (User user : users) {
                usersPaneScrolle.getChildren().add(createUserBox(user));
            }

        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage(e.getMessage(),"ERROR");
        }
    }

    private HBox createUserBox(User user) {
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
            databaseController.updateUserAccess(user.getUsername(),admin, ads, dir, users);
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteButton.setOnAction(event -> {
            databaseController.deleteUser(user.getUsername());
            usersPaneScrolle.getChildren().remove(userBox);
        });

        userBox.getChildren().addAll(usernameLabel, adsCheckBox, dirCheckBox, usersCheckBox, saveButton, deleteButton);

        return userBox;
    }

    private void getAdFiles(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    adsFile.add(file);
                }
            }
        }
    }

    private void showFiles() {
        adsPane.getChildren().clear();
        for (File file : adsFile) {
            VBox fileBox = createFileBox(file);
            adsPane.getChildren().add(fileBox);
        }
    }

    private VBox createFileBox(File file) {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

        Label label = new Label(file.getName());
        Button button = new Button("Remove");
        button.setOnAction(event -> {
            adsFile.remove(file);
            adsPane.getChildren().remove(vbox);
        });

        vbox.getChildren().addAll(label, button);
        return vbox;
    }

    private void addMore() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Advertisement");
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(controlPanelStage);
        if (selectedFiles != null) {
            adsFile.addAll(selectedFiles);
            showFiles();
        }
    }

    private void removefunc() {
        adsFile.removeAll(selectedToRemove);
        selectedToRemove.clear();
        showFiles();
    }

    public static void showErrorPopup(String message) {
        Platform.runLater(() -> {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            VBox vbox = new VBox(new Text(message));
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(15));

            Scene scene = new Scene(vbox);
            dialogStage.setScene(scene);
            dialogStage.show();
        });
    }
}
package com.example.musicapp.ui.controller;

import com.example.musicapp.data.database.controller.DatabaseController;
import com.example.musicapp.data.modle.User;
import com.example.musicapp.ui.controller.CreateAccountController;
import com.example.musicapp.ui.service.FilesService;
import com.example.musicapp.ui.view.ControlPanelView;
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
    public TilePane adsPane;
    @FXML
    protected TilePane usersPane;
    @FXML
    protected TilePane dirPane;
    @FXML
    public TilePane usersPaneScrolle;
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
    public List<File> adsFile = new ArrayList<>();
    private List<File> selectedToRemove = new ArrayList<>();
    private Parent root;
    public DatabaseController databaseController;
    private ControlPanelView controlPanelView;

    public ControlPanelController() {
       this.databaseController  = DatabaseController.getInstance();
       this.controlPanelView = new ControlPanelView(this);
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

    public void show(User user) {

        controlPanelStage = new Stage();
        if (controlPanelScene == null) {
            controlPanelScene = new Scene(root);
        }
        adsPane.getChildren().clear();
        usersPaneScrolle.getChildren().clear();
        dirPane.getChildren().clear();
        userBtns.getChildren().clear();

        if (user.hasAds()) {
            FilesService.addFilesToListByPath(Objects.requireNonNull(getClass().getResource("/com/example/musicapp/Ads")).getPath(),adsFile);
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

        if (user.hasDir()) {
            Button changeMusicDir = new Button("Change music folder");
            changeMusicDir.setOnMouseClicked(e -> mainPageController.selectMusicFolder());
            dirPane.getChildren().
                    add(changeMusicDir);
        }
        if (user.hasUsersAccess()) {
            List<User> users = databaseController.getUsers();
            for (User user2 : users) {
                usersPaneScrolle.getChildren().add(controlPanelView.createUserBox(user2));
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


            usersPaneScrolle.getChildren().clear();
            List<User> users = databaseController.getUsers();
            for (User user : users) {
                usersPaneScrolle.getChildren().add(controlPanelView.createUserBox(user));
            }

        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage(e.getMessage(),"ERROR");
        }
    }



    private void showFiles() {
        adsPane.getChildren().clear();
        for (File file : adsFile) {
            VBox fileBox = controlPanelView.createFileBox(file);
            adsPane.getChildren().add(fileBox);
        }
    }



    private void addMore() {
        FilesService.addFilesToListByFileChooser(adsFile,controlPanelStage);
        showFiles();
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
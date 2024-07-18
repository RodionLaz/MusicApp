package com.example.musicapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ControlPanelController implements Initializable {

    @FXML
    private TilePane adsPane,usersPane,dirPane,usersPaneScrolle;
    @FXML
    private Button addMore,remove;
    @FXML
    private VBox usersBox;
    @FXML
    private VBox Main;
    @FXML
    private Pane adsBtns,userBtns;


    private MainPageController mainPageController;
    private Stage controlPanelStage;
    private Scene controlPanelScene;
    private List<File> adsFile = new ArrayList<>();
    private List<File> selectedToRemove = new ArrayList<>();
    private static final String DB_URL = "jdbc:sqlite:DB.db";
    private Parent root;

    public void setMainPageController(MainPageController mainPageController){
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
        if (controlPanelScene == null){
            controlPanelScene = new Scene(root);
        }
        adsPane.getChildren().clear();
        usersPaneScrolle.getChildren().clear();
        dirPane.getChildren().clear();
        userBtns.getChildren().clear();


        if (access.getAds()) {
            System.out.println(getClass().getResource("Ads").getPath());
            getAdFiles(getClass().getResource("Ads").getPath());
            showFiles();
            HBox hBox = new HBox(10);
            hBox.setAlignment(Pos.CENTER);

            // Create the "Add More" button
            Button addMoreButton = new Button("Add More");
            addMoreButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            addMoreButton.setOnAction(event -> addMore());

            // Create the "Remove Selected" button
            Button removeButton = new Button("Remove Selected");
            removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            removeButton.setOnAction(event -> removefunc());

            // Add buttons to the HBox
            hBox.getChildren().addAll(addMoreButton, removeButton);


            adsBtns.getChildren().add(hBox);
            controlPanelStage.setScene(controlPanelScene);
        }
        if (access.getDir()) {
            Button changeMusicDir = new Button("Change music folder");
            changeMusicDir.setOnMouseClicked(e->{
                mainPageController.selectMusicFolder();
            });
            dirPane.getChildren().add(changeMusicDir);
        }
        if (access.getUsers()) {
            showUsers();

            Button addUser = new Button("Add User");
            addUser.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            addUser.setOnMouseClicked(event -> {
                Stage createAccountStage = new Stage();
                Pane pane = new Pane();

                TextField textField1 = new TextField();
                textField1.setLayoutX(20);
                textField1.setLayoutY(20);
                textField1.setPromptText("Enter username");

                TextField textField2 = new TextField();
                textField2.setLayoutX(20);
                textField2.setLayoutY(60);
                textField2.setPromptText("Enter password");

                CheckBox adsCheckBox = new CheckBox("Ads");
                CheckBox dirCheckBox = new CheckBox("Dir");
                CheckBox usersCheckBox = new CheckBox("Users");

                adsCheckBox.setLayoutX(20);
                adsCheckBox.setLayoutY(100);
                dirCheckBox.setLayoutX(20);
                dirCheckBox.setLayoutY(130);
                usersCheckBox.setLayoutX(20);
                usersCheckBox.setLayoutY(160);

                Button confirm = new Button("Confirm");
                confirm.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-radius: 5px; -fx-background-radius: 5px;");
                confirm.setLayoutX(20);
                confirm.setLayoutY(190);
                confirm.setOnMouseClicked(e2 -> {
                    createAccount(textField1.getText(), textField2.getText(), adsCheckBox.isSelected(), dirCheckBox.isSelected(), usersCheckBox.isSelected());
                    createAccountStage.close();
                });

                pane.getChildren().addAll(textField1, textField2, adsCheckBox, dirCheckBox, usersCheckBox, confirm);

                Scene createAccountScene = new Scene(pane, 300, 250);
                createAccountStage.setScene(createAccountScene);
                createAccountStage.showAndWait();
                showUsers();
            });

            Button updateDb = new Button("Update Database");
            updateDb.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            updateDb.setOnMouseClicked(event -> updateDatabase());
            userBtns.getChildren().addAll(updateDb, addUser);


        }

        controlPanelStage.showAndWait();
    }
    public void updateDatabase() {
        for (Node node : usersPaneScrolle.getChildren()) {
            if (node instanceof Pane) {
                Pane itemPane = (Pane) node;
                Label usernameLabel = (Label) itemPane.getChildren().get(0);
                CheckBox adsCheckBox = (CheckBox) itemPane.getChildren().get(1);
                CheckBox dirCheckBox = (CheckBox) itemPane.getChildren().get(2);
                CheckBox usersCheckBox = (CheckBox) itemPane.getChildren().get(3);

                String username = usernameLabel.getText();
                boolean adsSelected = adsCheckBox.isSelected();
                boolean dirSelected = dirCheckBox.isSelected();
                boolean usersSelected = usersCheckBox.isSelected();

                String strSql = "UPDATE users SET Ads = ?, Dir = ?, Users = ? WHERE username = ?";
                try (Connection con = DriverManager.getConnection(DB_URL);
                     PreparedStatement pstmt = con.prepareStatement(strSql)) {
                    pstmt.setBoolean(1, adsSelected);
                    pstmt.setBoolean(2, dirSelected);
                    pstmt.setBoolean(3, usersSelected);
                    pstmt.setString(4, username);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void showUsers() {
        String strSql = "SELECT * FROM users";
        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = con.prepareStatement(strSql);
             ResultSet rs = pstmt.executeQuery()) {

            usersPaneScrolle.getChildren().clear();

            while (rs.next()) {
                Pane itemPane = new Pane();
                itemPane.setPrefSize(250, 100);
                itemPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-padding: 10;");

                String usernameText = rs.getString("username");
                boolean adsSelected = rs.getBoolean("Ads");
                boolean dirSelected = rs.getBoolean("Dir");
                boolean usersSelected = rs.getBoolean("Users");

                Label username = new Label(usernameText);
                username.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

                CheckBox ads = new CheckBox("Ads");
                ads.setSelected(adsSelected);
                CheckBox dir = new CheckBox("Dir");
                dir.setSelected(dirSelected);
                CheckBox users = new CheckBox("Users");
                users.setSelected(usersSelected);

                Button removeUser = new Button("Delete");
                removeUser.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-border-radius: 5px; -fx-background-radius: 5px;");
                removeUser.setLayoutX(190);
                removeUser.setLayoutY(60);

                removeUser.setOnMouseClicked(event -> {
                    String removesql = "DELETE FROM users WHERE username = ?";
                    try (Connection delCon = DriverManager.getConnection(DB_URL);
                         PreparedStatement pstmt2 = delCon.prepareStatement(removesql)) {
                        pstmt2.setString(1, usernameText);
                        pstmt2.executeUpdate();
                        Platform.runLater(() -> usersPaneScrolle.getChildren().remove(itemPane));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

                username.setLayoutX(10);
                username.setLayoutY(10);
                ads.setLayoutX(10);
                ads.setLayoutY(40);
                dir.setLayoutX(70);
                dir.setLayoutY(40);
                users.setLayoutX(130);
                users.setLayoutY(40);

                itemPane.getChildren().addAll(username, ads, dir, users, removeUser);
                Platform.runLater(() -> usersPaneScrolle.getChildren().add(itemPane));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void removefunc(){
        if (selectedToRemove.isEmpty()) {
            showErrorPopup("No files selected for removal.");
            return;
        }

        for (File file : selectedToRemove) {
            if (file.delete()) {
                adsFile.remove(file);
                System.out.println("Deleted file: " + file.getName());
            } else {
                System.out.println("Failed to delete file: " + file.getName());
            }
        }

        showFiles();
        showErrorPopup("The selected file(s) have been deleted.");
    }
    public void addMore(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files and Videos", "*.jpg", "*.png", "*.mp4", "*.avi");
        fileChooser.getExtensionFilters().add(extFilter);
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(controlPanelStage);

        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                if (!adsFile.contains(file)) {
                    Path sourcePath = Paths.get(file.getPath());
                    Path destinationPath = null;
                    try {
                        destinationPath = Paths.get(getClass().getResource("Ads").toURI());
                        Files.copy(sourcePath, destinationPath.resolve(sourcePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (URISyntaxException | IOException e) {
                        throw new RuntimeException(e);
                    }

                    adsFile.add(file);
                } else {
                    System.out.println("File already exists: " + file.getName());
                }
            }
            for (File f : adsFile){
                System.out.println(f.getName());
            }
            showFiles();
        } else {
            System.out.println("No file selected");
        }
    }
    public void showFiles() {
        adsPane.getChildren().clear();

        for (File file : adsFile) {
            VBox itemContainer = new VBox();
            itemContainer.setPrefSize(250, 80);  // Smaller size
            itemContainer.setSpacing(5);  // Less spacing
            itemContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
            itemContainer.setPadding(new Insets(5));  // Less padding

            HBox fileInfoBox = new HBox();
            fileInfoBox.setSpacing(5);  // Less spacing
            fileInfoBox.setAlignment(Pos.CENTER_LEFT);

            Text filename = new Text(file.getName());
            filename.setStyle("-fx-font-size: 12px; -fx-fill: #333333;");

            CheckBox checkBox = new CheckBox();
            checkBox.setStyle("-fx-border-color: #999999; -fx-border-width: 1;");
            checkBox.setOnAction(e -> {
                if (!selectedToRemove.contains(file)) {
                    selectedToRemove.add(file);
                } else {
                    selectedToRemove.remove(file);
                }
            });

            fileInfoBox.getChildren().addAll(filename, checkBox);

            itemContainer.getChildren().addAll(fileInfoBox);

            itemContainer.setOnMouseClicked(e -> {
                if (!selectedToRemove.contains(file)) {
                    checkBox.setSelected(!checkBox.isSelected());
                    selectedToRemove.add(file);
                } else {
                    checkBox.setSelected(!checkBox.isSelected());
                    selectedToRemove.remove(file);
                }
            });

            adsPane.getChildren().add(itemContainer);
        }

        System.out.println("Done adding files.");
    }
    public static class Access {
        private boolean ads, dir, users;

        public Access(boolean ads, boolean dir, boolean users) {
            this.ads = ads;
            this.dir = dir;
            this.users = users;
        }

        public boolean getAds() {
            return ads;
        }

        public boolean getDir() {
            return dir;
        }

        public boolean getUsers() {
            return users;
        }

        public void setAds(boolean ads) {
            this.ads = ads;
        }

        public void setDir(boolean dir) {
            this.dir = dir;
        }

        public void setUsers(boolean users) {
            this.users = users;
        }
    }
    public Access login(String username, String password) {
        String sql = "SELECT Ads, Dir, Users, password FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    boolean ads = rs.getBoolean("Ads");
                    boolean dir = rs.getBoolean("Dir");
                    boolean users = rs.getBoolean("Users");

                    if (BCrypt.checkpw(password, storedHash)) {
                        System.out.println("Login successful!");
                        return new Access(ads, dir, users);
                    } else {
                        showErrorPopup("Username or Password are incorrect");
                        return null;
                    }
                } else {
                    showErrorPopup("Username or Password are incorrect");
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void getAdFiles(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png") || file.getName().endsWith(".mp4") || file.getName().endsWith(".avi")) {
                        adsFile.add(file);
                    }
                } else if (file.isDirectory()) {
                    getAdFiles(file.getPath());
                }
            }
        } else {
            System.out.println("The specified folder does not exist or is not a directory.");
        }
    }
    public Connection createDb() {
        try {
            File dbFile = new File("DB.db");
            if (!dbFile.exists()) {
                Connection conn = DriverManager.getConnection(DB_URL);
                String sql = "CREATE TABLE IF NOT EXISTS users (\n"
                        + "    username TEXT PRIMARY KEY,\n"
                        + "    password TEXT NOT NULL,\n"
                        + "    Ads BOOLEAN,\n"
                        + "    Dir BOOLEAN,\n"
                        + "    Users BOOLEAN\n"
                        + ");";
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
                System.out.println("Table 'users' created successfully.");
                return conn;
            } else {
                return DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void showErrorPopup(String errorMessage) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Error");

        Label label = new Label(errorMessage);
        Button closeButton = new Button("Close");
        closeButton.setOnMouseClicked(e -> stage.close());

        StackPane layout = new StackPane();
        layout.getChildren().addAll(label, closeButton);
        Scene scene = new Scene(layout, 250, 150);

        stage.setScene(scene);
        stage.showAndWait();
    }
    public void createAccount(String username, String password,boolean Ads,boolean Dir,boolean Users){

        if (username.isEmpty() || password.isEmpty()) {
            showErrorPopup("Username or password is empty");
            return ;
        }
        File dbFile = new File("DB.db");
        if (!dbFile.exists()) {
            return ;
        }

        String sql = "INSERT INTO users (username, password, Ads, Dir, Users) VALUES (?, ?, ?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = createDb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setBoolean(3, Ads);
            pstmt.setBoolean(4, Dir);
            pstmt.setBoolean(5, Users);

            pstmt.executeUpdate();
            System.out.println("User inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean createFirstAccount(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showErrorPopup("Username or password is empty");
            return false;
        }

        String sql = "INSERT INTO users (username, password, Ads, Dir, Users) VALUES (?, ?, ?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = createDb();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setBoolean(3, true);
            pstmt.setBoolean(4, true);
            pstmt.setBoolean(5, true);

            pstmt.executeUpdate();
            System.out.println("User inserted successfully.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

package com.example.musicapp.ui.controller;

import com.example.musicapp.main.App;
import com.example.musicapp.data.database.controller.DatabaseController;
import com.example.musicapp.data.modle.Access;
import com.example.musicapp.data.modle.Song;
import com.example.musicapp.ui.view.MainPageView;
import com.example.musicapp.ui.view.SelectDiskView;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;

import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.mpatric.mp3agic.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;


import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbServices;
import javax.usb.event.UsbServicesEvent;
import javax.usb.event.UsbServicesListener;

import static com.example.musicapp.ui.view.AlertsView.showErrorMessage;
import static com.example.musicapp.ui.view.AlertsView.showInformationMessage;


public class MainPageController implements Initializable {


    @FXML
    private Label currentSongLabel;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label percentageLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button adminPanelbtn;
    @FXML
    private TilePane contentPane;
    @FXML
    private TextField searchField;
    @FXML
    private Label texttexttext,usernameLabel,AdminLabel;
    @FXML
    private BorderPane borderPane;
    @FXML
    private HBox messageBox;
    @FXML
    private Button sortByArtistButton, logout,sortByAlbumButton, sortBySongButton, searchButton,transferButton, cancelButton, pauseButton,clearSelectedSongs;
    @FXML
    private ChoiceBox<String> sortBy = new ChoiceBox<>();
    private SelectDiskView selectDiskView;
    public List<Song> SelectedFiles = new ArrayList<>();
    public List<Song> songs = new ArrayList<>();
    private final Logger LOGGER = Logger.getLogger(MainPageController.class.getName());
    private Stage primaryStage;
    private String currentShow =  "Songs";
    private String currentScene = "Ads";
    private String SelectedPath;
    private boolean initialized = false;
    private List<CheckBox> checkboxes = new ArrayList<>();
    private Map<String, Boolean> checkboxStateMap = new HashMap<>();
    private Scene adsScene;
    private Scene mainScene;
    private Boolean accountCreated = false;
    private volatile boolean initialEventsProcessed = false;
    private ControlPanelController CPC;
    private Stage controlPanelStage;
    protected List<File> adsFiles = new ArrayList<>();
    private int currentIndex = 0;
    private static final int SLIDE_DURATION = 5;
    private DatabaseController databaseController;
    private MainPageView mainPageView;
    private Access access;
    private LoginPageController loginPageController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        selectDiskView=new SelectDiskView();
        mainPageView = MainPageView.getInstance(currentSongLabel, scrollPane, percentageLabel, progressBar, adminPanelbtn,
                contentPane, searchField, texttexttext, borderPane, messageBox, sortByArtistButton, sortByAlbumButton,
                sortBySongButton, searchButton, transferButton, cancelButton, pauseButton, clearSelectedSongs, sortBy);
        this.databaseController  = DatabaseController.getInstance();
        if (databaseController == null) {
            System.err.println("DatabaseController instance is null!");
        }
        SelectDiskController selectDiskController = new SelectDiskController(selectDiskView);
        selectMusicFolder();
        try {
            initControlPanel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mainScene =  App.getPrimaryScene();
        primaryStage =App.getPrimaryStage();
        loginPageController = LoginPageController.getInstance(primaryStage);
        adminPanelbtn.setOnMouseClicked(e->{
            CPC.show(access);
        });
        loadAds();
        StackPane mainPane = new StackPane();
        adsScene = new Scene(mainPane);
        //switchToAdsScene();
        //organize();

        searchButton.setOnMouseClicked(event -> {
            String Search = searchField.getText();
            if(searchField.getText().isEmpty()){
                songs = findMP3(SelectedPath);
                mainPageView.showitemsBySongsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
                return;
            }
            songs = searchSongs(songs,Search);
            String selectedOption = sortBy.getValue();
            switch (selectedOption){
                case "Songs":
                    songs.sort(Comparator.comparing(Song::getSongName,Comparator.nullsLast(Comparator.naturalOrder())));
                    System.out.println("sorting by Songs");
                    break;
                case "Albums":
                    songs.sort(Comparator.comparing(Song::getAlbumName,Comparator.nullsLast(Comparator.naturalOrder())));
                    System.out.println("sorting by Albums");
                    break;
                case "Artists":
                    songs.sort(Comparator.comparing(Song::getArtistName,Comparator.nullsLast(Comparator.naturalOrder())));
                    System.out.println("sorting by Arsits");
                    break;
                case "Year":
                    songs.sort(Comparator.comparing(Song::getYear,Comparator.nullsLast(Comparator.naturalOrder())));
                    System.out.println("sorting by Years");
                    break;
                default:
                    System.out.println("sorting by Songs");
                    mainPageView.showitemsBySongsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
                    break;
            }
            switch (currentShow){
                case "Songs":

                    mainPageView.showitemsBySongsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
                    System.out.println("showing by Songs");
                    break;
                case "Albums":
                    mainPageView.showitemsByAlbumsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
                    System.out.println("showing by Albums");
                    break;
                case "Artists":
                    mainPageView.showitemsByArtsitsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
                    System.out.println("showing by Arsits");
                    break;
                default:
                    System.out.println("showing by Songs");
                    mainPageView.showitemsBySongsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
                    break;
            }
        });
        sortBy.getItems().addAll("Songs","Artists","Albums","Year");
        sortBy.setValue("Songs");
        sortBy.setOnAction(e -> {
            String selectedOption = sortBy.getValue();
            switch (selectedOption){
                case "Songs":
                    songs.sort(Comparator.comparing(Song::getSongName,Comparator.nullsLast(Comparator.naturalOrder())));
                    System.out.println("sorting by Songs");
                    break;
                case "Albums":
                    songs.sort(Comparator.comparing(Song::getAlbumName,Comparator.nullsLast(Comparator.naturalOrder())));
                    System.out.println("sorting by Albums");
                    break;
                case "Artists":
                    songs.sort(Comparator.comparing(Song::getArtistName,Comparator.nullsLast(Comparator.naturalOrder())));
                    System.out.println("sorting by Arsits");
                    break;
                case "Year":
                    songs.sort(Comparator.comparing(Song::getYear, Comparator.nullsLast(Comparator.naturalOrder())));

                    System.out.println("sorting by Years");
                    break;
            }
            switch (currentShow){
                case "Songs":
                    mainPageView.showitemsBySongsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
                    System.out.println("showing by Songs");
                    break;
                case "Albums":
                    mainPageView.showitemsByAlbumsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
                    System.out.println("showing by Albums");
                    break;
                case "Artists":
                    mainPageView.showitemsByArtsitsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
                    System.out.println("showing by Arsits");
                    break;
            }
        });
        transferButton.setOnMouseClicked(event -> {
            if (SelectedFiles.isEmpty()) {
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("No Songs Selected");

                VBox vbox = new VBox(20);
                vbox.setStyle("-fx-padding: 20; -fx-alignment: center;");

                Label text = new Label("No songs were selected");
                text.setStyle("-fx-font-size: 16px; -fx-text-fill: #333;");

                Button closeButton = new Button("Close");
                closeButton.setOnAction(e -> stage.close());
                closeButton.setStyle("-fx-font-size: 14px;");

                vbox.getChildren().addAll(text, closeButton);

                Scene scene = new Scene(vbox, 300, 150); // Adjust scene size as per your content
                stage.setScene(scene);
                stage.showAndWait();
                return;
            }
            selectDiskController.show(SelectedFiles,percentageLabel,cancelButton,pauseButton,progressBar,currentSongLabel,transferButton);
        });
        sortByAlbumButton.setOnMouseClicked(event -> {
            currentShow = "Albums";
            mainPageView.showitemsByAlbumsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
        });
        sortByArtistButton.setOnMouseClicked(event -> {
            currentShow = "Artists";
            mainPageView.showitemsByArtsitsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
        });
        sortBySongButton.setOnMouseClicked(event -> {
            currentShow = "Songs";
            mainPageView.showitemsBySongsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
        });
        clearSelectedSongs.setOnMouseClicked(e ->{
            SelectedFiles.clear();
            for (CheckBox checkBox : checkboxes){
                if (checkBox.isSelected()){
                    checkBox.setSelected(false);
                }
            }

        });
        listenForUSBEvents();
        loginPageController.ChangeToLoginScene();

    }


    public void adminAccses(){
        if (!access.hasAdmin()){
            adminPanelbtn.setVisible(false); // Makes the button invisible
            adminPanelbtn.setManaged(false); // Removes it from the layout management
            adminPanelbtn.setDisable(true);  // Disables the button, preventing any user interaction
        }else{
            adminPanelbtn.setVisible(true);
            adminPanelbtn.setManaged(true);
            adminPanelbtn.setDisable(false);
        }

    }
    public void loadAds(){

        File folder = new File(getClass().getResource("/com/example/musicapp/Ads").getPath());
        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") ||
                        fileName.endsWith(".mp4") || fileName.endsWith(".mov") || fileName.endsWith(".m4v")) {
                    adsFiles.add(file);
                }
            }
        }
    }
    public void selectMusicFolder(){
        songs.clear();
        while (songs.isEmpty() /*|| !accountCreated*/){
            Stage noSongsPopUp = new Stage();
            noSongsPopUp.initModality(Modality.APPLICATION_MODAL);

            VBox vbox = new VBox(10);
            vbox.setStyle("-fx-padding: 10;");
            Label text = new Label("Select the folder with songs");
            Button btn = new Button("Select Folder");
            vbox.getChildren().addAll(text, btn);
            if (!accountCreated){


                Button btn2 = new Button("Create first Admin account");
                btn2.setOnMouseClicked(e -> {
                    Stage createAccountStage = new Stage();
                    Pane pane = new Pane();
                    // Create TextFields for input
                    TextField textField1 = new TextField();
                    textField1.setLayoutX(20);
                    textField1.setLayoutY(20);
                    textField1.setPromptText("Enter username");

                    TextField textField2 = new TextField();
                    textField2.setLayoutX(20);
                    textField2.setLayoutY(60);
                    textField2.setPromptText("Enter password");

                    Button confirm = new Button("Confirm");
                    confirm.setLayoutX(20);
                    confirm.setLayoutY(100);
                    confirm.setOnMouseClicked(e2->{
                        Boolean result = databaseController.createFirstAccount(textField1.getText(),textField2.getText());
                        if (result){
                            System.out.println("account created");
                            accountCreated = true;
                            createAccountStage.close();
                            vbox.getChildren().clear();
                            vbox.getChildren().addAll(text, btn);
                        }
                    });
                    // Add TextFields to the pane
                    pane.getChildren().addAll(textField1, textField2,confirm);

                    Scene createAccountScene = new Scene(pane, 300, 200);

                    // Set the Scene onto the Stage
                    createAccountStage.setScene(createAccountScene);

                    // Show the Stage
                    createAccountStage.show();
                });
                vbox.getChildren().addAll(btn2);
            }

            noSongsPopUp.setTitle("No Songs Found");

            Scene scene = new Scene(vbox, 400, 300); // Adjust scene size as per your content
            noSongsPopUp.setScene(scene);
            btn.setOnAction(e -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Select Folder");
                directoryChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Music"));
                File selectedDirectory = directoryChooser.showDialog(primaryStage);

                if (selectedDirectory != null) {
                    System.out.println("Selected Directory: " + selectedDirectory.getAbsolutePath());
                    SelectedPath = selectedDirectory.getAbsolutePath();
                    songs = findMP3(selectedDirectory.getAbsolutePath());
                    mainPageView.showitemsBySongsOrder(songs,checkboxes,checkboxStateMap,SelectedFiles);
                    noSongsPopUp.close(); // Close the popup after selecting the folder

                } else {
                    System.out.println("No Directory selected");
                }
            });

            noSongsPopUp.showAndWait();
        }
    }
    private void switchToMainScene() {
        Platform.runLater(() -> {
            Scene now = primaryStage.getScene();

            primaryStage.setScene(mainScene);
            primaryStage.setFullScreen(true);
        });
    }
    private void switchToAdsScene() {
        Platform.runLater(() -> {
            mainPageView.startSlideshow(primaryStage,adsFiles);
            //primaryStage.setScene(adsScene);
            primaryStage.setFullScreen(true);
        });
    }
    public List<Song> searchSongs(List<Song> songs, String searchText) {
        List<Song> results = new ArrayList<>();

        String searchLower = searchText.toLowerCase();

        for (Song song : songs) {
            if (song.getSongName().toLowerCase().contains(searchLower) ||
                    song.getArtistName().toLowerCase().contains(searchLower) ||
                    song.getAlbumName().toLowerCase().contains(searchLower) ||
                    song.getYear().toLowerCase().contains(searchLower)) {
                results.add(song);
            }
        }

        return results;
    }
    public void initControlPanel() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/musicapp/Control-Panel.fxml"));
        Parent root = loader.load();
        CPC = loader.getController();
        CPC.setRoot(root);
        CPC.setMainPageController(this);

    }
    public void setAccess(Access access){
        this.access = access;
    }
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;

    }
    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
    }
    public static List<Song> removeDuplicates(List<Song> songs) {
        Set<String> seenNames = new LinkedHashSet<>(); // LinkedHashSet preserves insertion order
        List<Song> uniqueSongs = new ArrayList<>();

        for (Song song : songs) {
            if (seenNames.add(song.getSongName())) { // Add returns true if the name was not already present
                uniqueSongs.add(song);
            }
        }

        return uniqueSongs;
    }
    public List<Song> findMP3(String directoryPath) {
        List<Song> songs = new ArrayList<>();
        JFileChooser fileChooser = new JFileChooser();
        FileSystemView fileSystemView = fileChooser.getFileSystemView();
        LOGGER.info("Default Music Directory: " + directoryPath);
        List<File> mp3Files = new ArrayList<>();

        // Recursively search for MP3s
        searchForMP3s(new File(directoryPath), mp3Files);

        // Handle the list of found MP3s (e.g., display information)
        for (File mp3File : mp3Files) {
            try {
                Mp3File mp3info = new Mp3File(mp3File.getPath());
                if (mp3info.hasId3v2Tag()) {
                    ID3v2 id3v2Tag = mp3info.getId3v2Tag();
                    Song song = new Song(id3v2Tag.getTitle(), id3v2Tag.getArtist(), id3v2Tag.getAlbum(),
                            id3v2Tag.getYear(), mp3File.getPath());
                    songs.add(song);

                }
            } catch (InvalidDataException e) {
                LOGGER.log(Level.SEVERE, "InvalidDataException: No MPEG frames found in file: " + mp3File.getPath(), e);
            } catch (UnsupportedTagException e) {
                LOGGER.log(Level.SEVERE, "UnsupportedTagException: " + e.getMessage(), e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IOException: " + e.getMessage(), e);
            }
        }
        return songs;
    }
    public void listenForUSBEvents() {
        UsbServicesListener listener = new UsbServicesListener() {
            @Override
            public void usbDeviceAttached(UsbServicesEvent event) {
                if (initialEventsProcessed) {
                    UsbDevice device = event.getUsbDevice();
                    Platform.runLater(() -> {
                        System.out.println("connected");
                        mainPageView.showMessage("A device has been connected");
                        if ("Ads".equals(currentScene)) {
                            System.out.println("switching to main");
                            loginPageController.ChangeToLoginScene();
                            currentScene = "Main";
                        }
                    });
                }
            }

            @Override
            public void usbDeviceDetached(UsbServicesEvent event) {
                if (initialEventsProcessed) {
                    UsbDevice device = event.getUsbDevice();
                    Platform.runLater(() -> {
                        mainPageView.showMessage("A device has been disconnected");
                        if ("Main".equals(currentScene)) {
                            System.out.println("switching to ads");
                            access = null;
                            switchToAdsScene();
                            currentScene = "Ads";
                        }
                    });
                }
            }
        };

        // Start listening in a separate thread
        Thread usbThread = new Thread(() -> {
            try {
                UsbServices services = UsbHostManager.getUsbServices();
                services.addUsbServicesListener(listener);

                // Wait a bit to allow initial events to be processed
                Thread.sleep(1000);

                initialEventsProcessed = true;  // Set the flag after initialization is done
            } catch (UsbException | InterruptedException e) {
                e.printStackTrace();
                showInformationMessage( e.getMessage(),"INFO");
            }
        });
        usbThread.start();
    }
    private void searchForMP3s(File directory, List<File> mp3Files) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            LOGGER.warning("Invalid directory: " + directory);
            return; // Handle invalid directory
        }

        File[] files = directory.listFiles();

        if (files == null) {
            LOGGER.warning("No files found in directory: " + directory);
            return; // Handle potential null pointer
        }

        for (File file : files) {
            if (file.isDirectory()) {
                searchForMP3s(file, mp3Files); // Recursively search subdirectories
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".mp3")) {
                mp3Files.add(file);
            }
        }
    }
    private String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
    public void organize() {
        String musicDirectoryPath = SelectedPath;
        File musicDirectory = new File(musicDirectoryPath);

        if (!musicDirectory.exists() || !musicDirectory.isDirectory()) {
            LOGGER.warning("Music directory does not exist or is not a directory.");
            return;
        }

        processDirectory(musicDirectory, musicDirectory);
    }
    private void processDirectory(File directory, File baseDirectory) {
        File[] files = directory.listFiles();

        if (files == null) {
            LOGGER.warning("Failed to list files in directory: " + directory.getPath());
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file, baseDirectory);
            } else if (file.getName().toLowerCase().endsWith(".mp3")) {
                try {
                    Mp3File mp3file = new Mp3File(file.getPath());
                    if (mp3file.hasId3v2Tag()) {
                        ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                        String[] artists = id3v2Tag.getArtist().split("/");
                        String album = id3v2Tag.getAlbum();

                        if (artists == null || artists[0].isEmpty()) {
                            artists[0] = "Unknown Artist";
                        }
                        if (album == null || album.isEmpty()) {
                            album = "Unknown Album";
                        }

                        for (int i = 0; i < artists.length; i++) {
                            artists[i] = sanitizeFileName(artists[i]);
                        }

                        album = sanitizeFileName(album);

                        for (String artist : artists) {
                            File artistDir = new File(baseDirectory, artist);
                            if (!artistDir.exists()) {
                                artistDir.mkdirs();
                            }

                            File albumDir = new File(artistDir, album);
                            if (!albumDir.exists()) {
                                albumDir.mkdirs();
                            }

                            File targetFile = new File(albumDir,sanitizeFileName(file.getName()));
                            if (!targetFile.exists()) {
                                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                LOGGER.info("Moved file: " + file.getName() + " to " + targetFile.getPath());
                            } else {
                                LOGGER.info("File already exists in target directory: " + targetFile.getPath());
                            }
                        }
                    }else {
                        LOGGER.log(Level.SEVERE, "File Has no tag: " + file.getPath());
                    }

                } catch (IOException | UnsupportedTagException | InvalidDataException | InvalidPathException e) {
                    e.printStackTrace();
                    showErrorMessage(e.getMessage(),"ERROR");
                }
            }
        }
    }
}
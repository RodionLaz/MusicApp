package com.example.musicapp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;



import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.mpatric.mp3agic.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

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
    private TilePane contentPane;
    @FXML
    private TextField searchField;
    @FXML
    private Label texttexttext;
    @FXML
    private Button sortByArtistButton, sortByAlbumButton, sortBySongButton, searchButton,transferButton, cancelButton, pauseButton;


    public List<String> SelectedFiles = new ArrayList<>();
    public List<Song> songs = new ArrayList<>();
    private final Logger LOGGER = Logger.getLogger(MainPageController.class.getName());
    private Stage primaryStage;
    private String currentShow =  "Songs";
    private String SelectedPath;
    private Map<String, Boolean> checkboxStateMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SelectDisk SD = new SelectDisk();

        showitemsBySongsOrder();
        searchButton.setOnMouseClicked(event -> {
            String Search = searchField.getText();
            if(searchField.getText().isEmpty()){
                songs = findMP3(SelectedPath);
                showitemsBySongsOrder();
                return;
            }
            songs = searchSongs(songs,Search);
            switch (currentShow){
                case "Songs":
                    showitemsBySongsOrder();
                    System.out.println("showing by Songs");
                    break;
                case "Albums":
                    showitemsByAlbumsOrder();
                    System.out.println("showing by Albums");
                    break;
                case "Arsits":
                    showitemsByArtsitsOrder();
                    System.out.println("showing by Arsits");
                    break;
                default:
                    System.out.println("showing by Songs");
                    showitemsBySongsOrder();
                    break;
            }


        });
        while (songs.isEmpty()){
            Stage noSongsPopUp = new Stage();
            noSongsPopUp.initModality(Modality.APPLICATION_MODAL);

            VBox vbox = new VBox(10);
            vbox.setStyle("-fx-padding: 10;");
            Label text = new Label("Select the folder with songs");
            Button btn = new Button("Select Folder");
            noSongsPopUp.setTitle("No Songs Found");
            vbox.getChildren().addAll(text, btn);
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
                    showitemsBySongsOrder();
                    noSongsPopUp.close(); // Close the popup after selecting the folder
                } else {
                    System.out.println("No Directory selected");
                }
            });

            noSongsPopUp.showAndWait();
        }
        //organize();
        transferButton.setOnMouseClicked(event -> {
            SD.show(SelectedFiles,percentageLabel,cancelButton,pauseButton,progressBar,currentSongLabel,transferButton);

        });
        sortByAlbumButton.setOnMouseClicked(event -> {
            currentShow = "Albums";
            showitemsByAlbumsOrder();
        });
        sortByArtistButton.setOnMouseClicked(event -> {
            currentShow = "Arsits";
            showitemsByArtsitsOrder();
        });
        sortBySongButton.setOnMouseClicked(event -> {
            currentShow = "Songs";
            showitemsBySongsOrder();
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
    public void showitemsBySongsOrder() {
        if (songs == null) {
            System.out.println("songs list is null");
            return; // Or handle the null case as needed
        }

        try {
            songs.sort(Comparator.comparing(Song::getSongName));
            songs = removeDuplicates(songs);
            contentPane.getChildren().clear();
            for (Song song : songs) {
                addItem(song.getSongName(), song.getArtistName(), song.getAlbumName(), song.getYear(), song.getPath());
            }
        } catch (NullPointerException e) {
            System.out.println("NullPointerException occurred during sorting: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error analysis
        }
    }
    public void showitemsByArtsitsOrder() {
        if (songs == null) {
            System.out.println("songs list is null");
            return; // Or handle the null case as needed
        }

        try {
            List<String> ArtsitsSongs = new ArrayList<>();
            songs.sort(Comparator.comparing(Song::getArtistName,Comparator.nullsLast(Comparator.naturalOrder())));
            songs = removeDuplicates(songs);
            contentPane.getChildren().clear();
            for (Song song : songs) {
                if(!ArtsitsSongs.contains(song)){
                    addItemArtist(song.getArtistName());
                }

            }
        } catch (NullPointerException e) {
            System.out.println("NullPointerException occurred during sorting: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error analysis
        }
    }

    public void showitemsByAlbumsOrder() {
        if (songs == null) {
            System.out.println("songs list is null");
            return; // Or handle the null case as needed
        }

        try {
            List<String> ArtsitsSongs = new ArrayList<>();
            songs.sort(Comparator.comparing(Song::getArtistName,Comparator.nullsLast(Comparator.naturalOrder())));
            songs = removeDuplicates(songs);
            contentPane.getChildren().clear();
            for (Song song : songs) {
                if(!ArtsitsSongs.contains(song)){
                    addItemAlbum(song.getAlbumName());
                }

            }
        } catch (NullPointerException e) {
            System.out.println("NullPointerException occurred during sorting: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error analysis
        }
    }
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    // Function to remove duplicates based on songName
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
    public void addItem(String labelText, String artistName, String album, String year, String path) {
        Pane itemPane = new Pane();
        itemPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        itemPane.setPrefSize(350, 120); // Adjust size as per your content

        // CheckBox for selection
        CheckBox checkBox = new CheckBox();
        checkBox.setLayoutX(130); // Adjust CheckBox position as needed
        checkBox.setLayoutY(80); // Adjust CheckBox position as needed
        itemPane.getChildren().add(checkBox);

        // Label for the main text (Song)
        Label label = new Label("Song: " + labelText);
        label.setLayoutX(30); // Adjust Label position to accommodate CheckBox
        label.setLayoutY(10);
        label.setMaxWidth(310); // Adjust Label width based on CheckBox position
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;"); // CSS for label
        itemPane.getChildren().add(label);

        // Label for ArtistName
        Label artistLabel = new Label("Artist: " + artistName);
        artistLabel.setLayoutX(30);
        artistLabel.setLayoutY(30);
        artistLabel.setMaxWidth(310); // Adjust Label width based on CheckBox position
        artistLabel.setWrapText(true);
        artistLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
        itemPane.getChildren().add(artistLabel);

        // Label for Album
        Label albumLabel = new Label("Album: " + album);
        albumLabel.setLayoutX(30);
        albumLabel.setLayoutY(50);
        albumLabel.setMaxWidth(310); // Adjust Label width based on CheckBox position
        albumLabel.setWrapText(true);
        albumLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
        itemPane.getChildren().add(albumLabel);

        // Label for Year
        Label yearLabel = new Label("Year: " + year);
        yearLabel.setLayoutX(30);
        yearLabel.setLayoutY(70);
        yearLabel.setMaxWidth(310); // Adjust Label width based on CheckBox position
        yearLabel.setWrapText(true);
        yearLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
        itemPane.getChildren().add(yearLabel);

        // Set the initial state of the CheckBox from the map
        checkBox.setSelected(checkboxStateMap.getOrDefault(path, false));

        // Update the map when the CheckBox state changes
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            checkboxStateMap.put(path, newValue);
        });
        itemPane.setOnMouseClicked(event -> {
            boolean found = false;
            List<String> pathsToRemove = new ArrayList<>();

            for (String filePath : SelectedFiles) {
                if (filePath.equals(path)) {
                    System.out.println("Found in list, removing: " + path);
                    pathsToRemove.add(filePath);
                    found = true;
                }
            }

            if (found) {
                SelectedFiles.removeAll(pathsToRemove);
                checkBox.setSelected(false);
            } else {
                System.out.println("Not found in list, adding: " + path);
                SelectedFiles.add(path);
                checkBox.setSelected(true);
            }
        });

        // Add the itemPane to the contentPane
        contentPane.getChildren().add(itemPane);
    }
    public void addItemArtist( String artistName) {
        Pane itemPane = new Pane();
        itemPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        itemPane.setPrefSize(350, 120); // Adjust size as per your content



        // Label for the main text (Song)
        Label label = new Label("Artist: " + artistName);
        label.setLayoutX(30); // Adjust Label position to accommodate CheckBox
        label.setLayoutY(10);
        label.setMaxWidth(310); // Adjust Label width based on CheckBox position
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;"); // CSS for label
        itemPane.getChildren().add(label);



        itemPane.setOnMouseClicked(event -> {
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Songs by " + artistName);

            VBox vbox = new VBox(10);
            vbox.setStyle("-fx-padding: 10;");

            for (Song song : songs) {
                if (song.getArtistName().equals(artistName)) {
                    TilePane itemPane2 = new TilePane();
                    itemPane2.setPrefColumns(1);
                    itemPane2.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
                    itemPane2.setPrefSize(300, 100);

                    // Label for the main text (Song)
                    Label label2 = new Label("Song: " + song.getSongName());
                    label2.setMaxWidth(280); // Adjust Label width based on TilePane size
                    label2.setWrapText(true);
                    label2.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;"); // CSS for label
                    itemPane2.getChildren().add(label2);

                    // Label for ArtistName
                    Label artistLabel = new Label("Artist: " + artistName);
                    artistLabel.setMaxWidth(280); // Adjust Label width based on TilePane size
                    artistLabel.setWrapText(true);
                    artistLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
                    itemPane2.getChildren().add(artistLabel);

                    // Label for Album
                    Label albumLabel = new Label("Album: " + song.getAlbumName());
                    albumLabel.setMaxWidth(280); // Adjust Label width based on TilePane size
                    albumLabel.setWrapText(true);
                    albumLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
                    itemPane2.getChildren().add(albumLabel);

                    // CheckBox for selection
                    CheckBox checkBox = new CheckBox();
                    checkBox.setLayoutX(130); // Adjust CheckBox position as needed
                    checkBox.setLayoutY(80); // Adjust CheckBox position as needed
                    itemPane2.getChildren().add(checkBox);

                    // Set the initial state of the CheckBox from the map
                    checkBox.setSelected(checkboxStateMap.getOrDefault(song.getPath(), false));

                    // Update the map when the CheckBox state changes
                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        checkboxStateMap.put(song.getPath(), newValue);
                    });
                    itemPane2.setOnMouseClicked(e -> {
                        boolean found = false;
                        List<String> pathsToRemove = new ArrayList<>();

                        for (String filePath : SelectedFiles) {
                            if (filePath.equals(song.getPath())) {
                                System.out.println("Found in list, removing: " + song.getPath());
                                pathsToRemove.add(filePath);
                                found = true;
                            }
                        }

                        if (found) {
                            SelectedFiles.removeAll(pathsToRemove);
                            checkBox.setSelected(false);
                        } else {
                            System.out.println("Not found in list, adding: " + song.getPath());
                            SelectedFiles.add(song.getPath());
                            checkBox.setSelected(true);
                        }

                    });

                    vbox.getChildren().add(itemPane2);
                }
            }

            Scene scene = new Scene(vbox, 400, 300); // Adjust scene size as per your content
            popupStage.setScene(scene);
            popupStage.showAndWait();
        });


        contentPane.getChildren().add(itemPane);
    }
    public void addItemAlbum( String albumName) {
        Pane itemPane = new Pane();
        itemPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        itemPane.setPrefSize(350, 120); // Adjust size as per your content



        // Label for the main text (Song)
        Label label = new Label("Album: " + albumName);
        label.setLayoutX(30); // Adjust Label position to accommodate CheckBox
        label.setLayoutY(10);
        label.setMaxWidth(310); // Adjust Label width based on CheckBox position
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;"); // CSS for label
        itemPane.getChildren().add(label);




        itemPane.setOnMouseClicked(event -> {
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Songs From  " + albumName);

            VBox vbox = new VBox(10);
            vbox.setStyle("-fx-padding: 10;");

            for (Song song : songs) {
                if (song.getAlbumName().equals(albumName)) {
                    TilePane itemPane2 = new TilePane();
                    itemPane2.setPrefColumns(1);
                    itemPane2.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
                    itemPane2.setPrefSize(300, 100);

                    // Label for the main text (Song)
                    Label label2 = new Label("Song: " + song.getSongName());
                    label2.setMaxWidth(280); // Adjust Label width based on TilePane size
                    label2.setWrapText(true);
                    label2.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;"); // CSS for label
                    itemPane2.getChildren().add(label2);

                    // Label for ArtistName
                    Label artistLabel = new Label("Artist: " + song.getArtistName());
                    artistLabel.setMaxWidth(280); // Adjust Label width based on TilePane size
                    artistLabel.setWrapText(true);
                    artistLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
                    itemPane2.getChildren().add(artistLabel);

                    // Label for Album
                    Label albumLabel = new Label("Album: " + song.getAlbumName());
                    albumLabel.setMaxWidth(280); // Adjust Label width based on TilePane size
                    albumLabel.setWrapText(true);
                    albumLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
                    itemPane2.getChildren().add(albumLabel);

                    // CheckBox for selection
                    CheckBox checkBox = new CheckBox();
                    checkBox.setLayoutX(130); // Adjust CheckBox position as needed
                    checkBox.setLayoutY(80); // Adjust CheckBox position as needed
                    itemPane2.getChildren().add(checkBox);
                    // Set the initial state of the CheckBox from the map
                    checkBox.setSelected(checkboxStateMap.getOrDefault(song.getPath(), false));

                    // Update the map when the CheckBox state changes
                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        checkboxStateMap.put(song.getPath(), newValue);
                    });
                    itemPane2.setOnMouseClicked(e -> {
                        boolean found = false;
                        List<String> pathsToRemove = new ArrayList<>();

                        for (String filePath : SelectedFiles) {
                            if (filePath.equals(song.getPath())) {
                                System.out.println("Found in list, removing: " + song.getPath());
                                pathsToRemove.add(filePath);
                                found = true;
                            }
                        }

                        if (found) {
                            SelectedFiles.removeAll(pathsToRemove);
                            checkBox.setSelected(false);
                        } else {
                            System.out.println("Not found in list, adding: " + song.getPath());
                            SelectedFiles.add(song.getPath());
                            checkBox.setSelected(true);
                        }

                    });

                    vbox.getChildren().add(itemPane2);
                }
            }

            Scene scene = new Scene(vbox, 400, 300); // Adjust scene size as per your content
            popupStage.setScene(scene);
            popupStage.showAndWait();
        });


        contentPane.getChildren().add(itemPane);
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
        String musicDirectoryPath = System.getProperty("user.home") + File.separator + "Music";
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
                    LOGGER.log(Level.SEVERE, "Error processing file: " + file.getPath(), e);
                }
            }
        }
    }








}
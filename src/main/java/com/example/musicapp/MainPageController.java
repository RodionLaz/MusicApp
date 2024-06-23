package com.example.musicapp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.stage.DirectoryChooser;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        SelectDisk SD = new SelectDisk();

        //songs = findMP3();
        showitemsBySongsOrder();
        searchButton.setOnMouseClicked(event -> {
            String Search = searchField.getText();
            if(searchField.getText().isEmpty()){
                songs = findMP3();
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
        if(searchField.getText().isEmpty()){
            songs = findMP3();
            showitemsBySongsOrder();
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
            songs.sort(Comparator.comparing(Song::getArtistName,Comparator.nullsLast(Comparator.naturalOrder())));
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
    public void showitemsByAlbumsOrder() {
        if (songs == null) {
            System.out.println("songs list is null");
            return; // Or handle the null case as needed
        }

        try {
            contentPane.getChildren().clear();
            songs.sort(Comparator.comparing(Song::getAlbumName,Comparator.nullsLast(Comparator.naturalOrder())));
            songs = removeDuplicates(songs);
            for (Song song : songs) {
                addItem(song.getSongName(), song.getArtistName(), song.getAlbumName(), song.getYear(), song.getPath());
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



    public List<Song> findMP3() {
        List<Song> songs = new ArrayList<>();
        JFileChooser fileChooser = new JFileChooser();
        FileSystemView fileSystemView = fileChooser.getFileSystemView();
        // Get the default music directory
        String directoryPath = System.getProperty("user.home") + File.separator + "Music";

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
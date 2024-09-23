package com.example.musicapp.ui.view;

import com.example.musicapp.data.modle.Song;
import com.example.musicapp.ui.controller.MainPageController;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

import static com.example.musicapp.ui.controller.MainPageController.removeDuplicates;


public class MainPageView {


    private Label currentSongLabel;
    private ScrollPane scrollPane;
    private Label percentageLabel;
    private ProgressBar progressBar;
    private Button adminPanelbtn;
    private static TilePane contentPane;
    private TextField searchField;
    private Label texttexttext;
    private BorderPane borderPane;
    private HBox messageBox;
    private Button sortByArtistButton, sortByAlbumButton, sortBySongButton, searchButton, transferButton, cancelButton, pauseButton, clearSelectedSongs;
    private ChoiceBox<String> sortBy = new ChoiceBox<>();

    private static final int SLIDE_DURATION = 5;

    private static int currentIndex = 0;


    private static MainPageView instance;
    private List<File> adsFiles = new ArrayList<>();

    private MainPageView(Label currentSongLabel, ScrollPane scrollPane, Label percentageLabel, ProgressBar progressBar,
                         Button adminPanelbtn, TilePane contentPane, TextField searchField, Label texttexttext,
                         BorderPane borderPane, HBox messageBox, Button sortByArtistButton, Button sortByAlbumButton,
                         Button sortBySongButton, Button searchButton, Button transferButton, Button cancelButton,
                         Button pauseButton, Button clearSelectedSongs, ChoiceBox<String> sortBy) {
        this.currentSongLabel = currentSongLabel;
        this.scrollPane = scrollPane;
        this.percentageLabel = percentageLabel;
        this.progressBar = progressBar;
        this.adminPanelbtn = adminPanelbtn;
        this.contentPane = contentPane;
        this.searchField = searchField;
        this.texttexttext = texttexttext;
        this.borderPane = borderPane;
        this.messageBox = messageBox;
        this.sortByArtistButton = sortByArtistButton;
        this.sortByAlbumButton = sortByAlbumButton;
        this.sortBySongButton = sortBySongButton;
        this.searchButton = searchButton;
        this.transferButton = transferButton;
        this.cancelButton = cancelButton;
        this.pauseButton = pauseButton;
        this.clearSelectedSongs = clearSelectedSongs;
        this.sortBy = sortBy;

        scrollPane.setStyle("-fx-pref-width: 30px; -fx-pref-height: 15px;");
    }
    public static MainPageView getInstance(Label currentSongLabel, ScrollPane scrollPane, Label percentageLabel, ProgressBar progressBar,
                                           Button adminPanelbtn, TilePane contentPane, TextField searchField, Label texttexttext,
                                           BorderPane borderPane, HBox messageBox, Button sortByArtistButton, Button sortByAlbumButton,
                                           Button sortBySongButton, Button searchButton, Button transferButton, Button cancelButton,
                                           Button pauseButton, Button clearSelectedSongs, ChoiceBox<String> sortBy) {
        if (instance == null) {
            synchronized (MainPageView.class) {
                if (instance == null) {
                    instance = new MainPageView(currentSongLabel, scrollPane, percentageLabel, progressBar, adminPanelbtn,
                            contentPane, searchField, texttexttext, borderPane, messageBox, sortByArtistButton, sortByAlbumButton,
                            sortBySongButton, searchButton, transferButton, cancelButton, pauseButton, clearSelectedSongs, sortBy);
                }
            }
        }
        return instance;
    }
    public static void startSlideshow(Stage stage,List<File> adsFiles) {
        StackPane root = new StackPane();
        ImageView imageView = new ImageView();
        MediaView mediaView = new MediaView();
        root.getChildren().addAll(imageView, mediaView);
        Scene scene = new Scene(root, 800, 600); // Set your desired size
        stage.setScene(scene);
        stage.setTitle("Media Slideshow");
        stage.show();
        if (!adsFiles.isEmpty()) {
            showNextSlide(imageView, mediaView,adsFiles);
        } else {
            Label noFilesLabel = new Label("No media files found in the specified folder.");
            root.getChildren().add(noFilesLabel);
            StackPane.setAlignment(noFilesLabel, Pos.CENTER);
        }
    }
    public static void showNextSlide(ImageView imageView, MediaView mediaView,List<File> adsFiles) {
        if (currentIndex >= adsFiles.size()) {
            currentIndex = 0;
        }

        File file = adsFiles.get(currentIndex);
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
            showImage(imageView, mediaView, file,adsFiles);
        } else if (fileName.endsWith(".mp4") || fileName.endsWith(".mov") || fileName.endsWith(".m4v")) {
            showVideo(imageView, mediaView, file,adsFiles);
        }

        currentIndex++;
    }
    public static void showImage(ImageView imageView, MediaView mediaView, File file,List<File> adsFiles) {
        imageView.setImage(new Image(file.toURI().toString()));
        imageView.setVisible(true);
        mediaView.setVisible(false);

        Duration duration = Duration.seconds(SLIDE_DURATION);
        PauseTransition pause = new PauseTransition(duration);
        pause.setOnFinished(event -> showNextSlide(imageView, mediaView,adsFiles));
        pause.play();
    }
    public static void showVideo(ImageView imageView, MediaView mediaView, File file,List<File> adsFiles) {
        Media media = new Media(file.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        imageView.setVisible(false);
        mediaView.setVisible(true);

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.dispose();
            showNextSlide(imageView, mediaView,adsFiles);
        });

        mediaPlayer.play();
    }
    public static void showitemsBySongsOrder(List<Song> songs, List<CheckBox> checkboxes, Map<String, Boolean> checkboxStateMap,List<Song> SelectedFiles) {
        if (songs == null) {
            System.out.println("songs list is null");
            return;
        }

        try {
            songs.sort(Comparator.comparing(Song::getSongName));
            songs = removeDuplicates(songs);
            contentPane.getChildren().clear();
            for (Song song : songs) {
                addItem(song,checkboxes,checkboxStateMap,SelectedFiles);
            }
        } catch (NullPointerException e) {
            System.out.println("NullPointerException occurred during sorting: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error analysis
        }
    }
    public static void showitemsByArtsitsOrder(List<Song> songs, List<CheckBox> checkboxes, Map<String, Boolean> checkboxStateMap,List<Song> SelectedFiles) {
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
                if(!ArtsitsSongs.contains(song.getArtistName())){
                    ArtsitsSongs.add(song.getArtistName());
                    addItemArtist(song.getArtistName(),songs,checkboxes,checkboxStateMap,SelectedFiles);
                }

            }
        } catch (NullPointerException e) {
            System.out.println("NullPointerException occurred during sorting: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error analysis
        }
    }
    public static void showitemsByAlbumsOrder(List<Song> songs, List<CheckBox> checkboxes, Map<String, Boolean> checkboxStateMap,List<Song> SelectedFiles) {
        if (songs == null) {
            System.out.println("songs list is null");
            return; // Or handle the null case as needed
        }

        try {
            List<String> ArtsitsAlbums = new ArrayList<>();
            songs.sort(Comparator.comparing(Song::getArtistName,Comparator.nullsLast(Comparator.naturalOrder())));
            songs = removeDuplicates(songs);
            contentPane.getChildren().clear();
            for (Song song : songs) {
                if(!ArtsitsAlbums.contains(song.getAlbumName())){
                    ArtsitsAlbums.add(song.getAlbumName());
                    addItemAlbum(song.getAlbumName(),songs,checkboxes,checkboxStateMap,SelectedFiles);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("NullPointerException occurred during sorting: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error analysis
        }
    }
    public static void addItem(Song song, List<CheckBox> checkboxes, Map<String, Boolean> checkboxStateMap,List<Song> SelectedFiles) {
        Pane itemPane = new Pane();
        itemPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        itemPane.setPrefSize(350, 120); // Adjust size as per your content

        // CheckBox for selection
        CheckBox checkBox = new CheckBox();
        checkboxes.add(checkBox);
        checkBox.setLayoutX(130); // Adjust CheckBox position as needed
        checkBox.setLayoutY(80); // Adjust CheckBox position as needed
        itemPane.getChildren().add(checkBox);

        // Label for the main text (Song)
        Label label = new Label("Song: " + song.getSongName());
        label.setLayoutX(30); // Adjust Label position to accommodate CheckBox
        label.setLayoutY(10);
        label.setMaxWidth(310); // Adjust Label width based on CheckBox position
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;"); // CSS for label
        itemPane.getChildren().add(label);

        // Label for ArtistName
        Label artistLabel = new Label("Artist: " + song.getArtistName());
        artistLabel.setLayoutX(30);
        artistLabel.setLayoutY(30);
        artistLabel.setMaxWidth(310); // Adjust Label width based on CheckBox position
        artistLabel.setWrapText(true);
        artistLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
        itemPane.getChildren().add(artistLabel);

        // Label for Album
        Label albumLabel = new Label("Album: " + song.getAlbumName());
        albumLabel.setLayoutX(30);
        albumLabel.setLayoutY(50);
        albumLabel.setMaxWidth(310); // Adjust Label width based on CheckBox position
        albumLabel.setWrapText(true);
        albumLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
        itemPane.getChildren().add(albumLabel);

        // Label for Year
        Label yearLabel = new Label("Year: " + song.getYear());
        yearLabel.setLayoutX(30);
        yearLabel.setLayoutY(70);
        yearLabel.setMaxWidth(310); // Adjust Label width based on CheckBox position
        yearLabel.setWrapText(true);
        yearLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
        itemPane.getChildren().add(yearLabel);

        // Set the initial state of the CheckBox from the map
        checkBox.setSelected(checkboxStateMap.getOrDefault(song.getPath(), false));

        // Update the map when the CheckBox state changes
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            checkboxStateMap.put(song.getPath(), newValue);
        });
        itemPane.setOnMouseClicked(event -> {
            boolean found = false;
            List<String> pathsToRemove = new ArrayList<>();

            for (Song song1 : SelectedFiles) {
                String filePath = song1.getPath();
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
                SelectedFiles.add(song);
                checkBox.setSelected(true);
            }
        });

        // Add itemPane to contentPane
        contentPane.getChildren().add(itemPane);
    }
    public static void addItemArtist(String artistName,List<Song> songs, List<CheckBox> checkboxes, Map<String, Boolean> checkboxStateMap,List<Song> SelectedFiles) {
        Pane itemPane = new Pane();
        itemPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        itemPane.setPrefSize(350, 120); // Adjust size as per your content

        // Label for the main text (Song)
        Label label = new Label("Artist: " + artistName);
        label.setLayoutX(30);
        label.setLayoutY(10);
        label.setMaxWidth(310);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        itemPane.getChildren().add(label);

        itemPane.setOnMouseClicked(event -> {
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Songs by " + artistName);

            VBox vbox = new VBox(10); // Create a VBox to hold the items
            vbox.setStyle("-fx-padding: 10;");
            ScrollPane scrollPane = new ScrollPane(vbox); // Set VBox as content of the ScrollPane
            scrollPane.setFitToWidth(true); // Ensure the ScrollPane resizes to fit the content width

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
                    checkboxes.add(checkBox);
                    checkBox.setSelected(checkboxStateMap.getOrDefault(song.getPath(), false)); // Set initial state
                    itemPane2.getChildren().add(checkBox);

                    Button preview = new Button("Preview");
                    itemPane2.getChildren().add(preview);
                    preview.setOnMouseClicked(e->{
                        Media media = new Media(Paths.get(song.getPath()).toUri().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(media);

                        // Start the media at 10 seconds
                        mediaPlayer.setOnReady(() -> {
                            mediaPlayer.seek(Duration.seconds(20));
                            mediaPlayer.play();
                        });

                        // Stop the media after 5 seconds from the start position
                        mediaPlayer.setStopTime(Duration.seconds(28));

                    });



                    // Update the map when the CheckBox state changes
                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        checkboxStateMap.put(song.getPath(), newValue);
                    });

                    itemPane2.setOnMouseClicked(e -> {
                        if (SelectedFiles.contains(song)) {
                            SelectedFiles.remove(song);
                            checkBox.setSelected(false);
                        } else {
                            SelectedFiles.add(song);
                            checkBox.setSelected(true);
                        }
                    });

                    vbox.getChildren().add(itemPane2); // Add each itemPane2 to the VBox
                }
            }

            Scene scene = new Scene(scrollPane, 400, 300); // Create a scene with the ScrollPane
            popupStage.setScene(scene); // Set the scene to the stage
            popupStage.showAndWait(); // Show the stage and wait for it to be closed
        });

        contentPane.getChildren().add(itemPane); // Add the itemPane to the main content pane
    }
    public static void addItemAlbum(String albumName,List<Song> songs,List<CheckBox> checkboxes, Map<String, Boolean> checkboxStateMap,List<Song> SelectedFiles) {
        Pane itemPane = new Pane();
        itemPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        itemPane.setPrefSize(350, 120); // Adjust size as per your content

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
                    checkboxes.add(checkBox);
                    checkBox.setLayoutX(130); // Adjust CheckBox position as needed
                    checkBox.setLayoutY(80); // Adjust CheckBox position as needed
                    itemPane2.getChildren().add(checkBox);
                    // Set the initial state of the CheckBox from the map
                    checkBox.setSelected(checkboxStateMap.getOrDefault(song.getPath(), false));

                    // Update the map when the CheckBox state changes
                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        checkboxStateMap.put(song.getPath(), newValue);
                    });

                    Button preview = new Button("Preview");
                    itemPane2.getChildren().add(preview);
                    preview.setOnMouseClicked(e->{
                        Media media = new Media(Paths.get(song.getPath()).toUri().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(media);

                        // Start the media at 10 seconds
                        mediaPlayer.setOnReady(() -> {
                            mediaPlayer.seek(Duration.seconds(20));
                            mediaPlayer.play();
                        });

                        // Stop the media after 5 seconds from the start position
                        mediaPlayer.setStopTime(Duration.seconds(28));

                    });

                    itemPane2.setOnMouseClicked(e -> {
                        boolean found = false;
                        List<Song> pathsToRemove = new ArrayList<>();

                        for (Song song1 : SelectedFiles) {
                            String filePath = song1.getPath();
                            if (song1.equals(song.getPath())) {
                                System.out.println("Found in list, removing: " + song.getPath());
                                pathsToRemove.add(song);
                                found = true;
                            }
                        }

                        if (found) {
                            SelectedFiles.removeAll(pathsToRemove);
                            checkBox.setSelected(false);
                        } else {
                            System.out.println("Not found in list, adding: " + song.getPath());
                            SelectedFiles.add(song);
                            checkBox.setSelected(true);
                        }

                    });

                    vbox.getChildren().add(itemPane2);
                }
            }
            ScrollPane scrollPane1 = new ScrollPane(vbox);
            Scene scene = new Scene(scrollPane1, 400, 300); // Adjust scene size as per your content
            popupStage.setScene(scene);
            popupStage.showAndWait();
        });


        contentPane.getChildren().add(itemPane);
    }
    public void showMessage(String messageText) {
        Label messageLabel = new Label(messageText);
        messageLabel.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-padding: 10;");
        messageBox.getChildren().add(messageLabel);

        // Initial position at the bottom left
        messageLabel.setTranslateY(50);

        // Create the upwards animation
        TranslateTransition upTransition = new TranslateTransition(Duration.seconds(1), messageLabel);
        upTransition.setByY(-50);

        // Pause for 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        // Create the downwards animation
        TranslateTransition downTransition = new TranslateTransition(Duration.seconds(1), messageLabel);
        downTransition.setByY(50);

        // Sequential transition to combine the animations
        SequentialTransition sequentialTransition = new SequentialTransition(upTransition, pause, downTransition);
        sequentialTransition.setOnFinished(event -> messageBox.getChildren().remove(messageLabel));
        sequentialTransition.play();
    }
}

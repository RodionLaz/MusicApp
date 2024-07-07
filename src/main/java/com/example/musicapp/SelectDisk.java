package com.example.musicapp;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.filechooser.FileSystemView;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbServices;
import javax.usb.event.UsbServicesEvent;
import javax.usb.event.UsbServicesListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;



public class SelectDisk {
    ThreadGroup group = new ThreadGroup("Transfares");
    private Task<Void> copyTask;
    private AtomicBoolean isPaused = new AtomicBoolean(false);


    private VBox vbox;
    private Stage stage;
    private Label percentageLabel;
    private Button cancelButton;
    private Button pauseButton;
    private ProgressBar progressBar;
    private Label currentSongLabel;
    private Button transferButton;
    private List<Song> selectedFiles;
    private File selectedRoot = new File("D:\\");
    private Map<String, Boolean> checkboxStateMap = new HashMap<>();
    private List<CheckBox> checkBoxes = new ArrayList<>();

    public void finalCheck(Stage popupStage){
        vbox.getChildren().clear();
        for (Song song : selectedFiles){
            TilePane itemPane = new TilePane();
            itemPane.setPrefColumns(1);
            itemPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
            itemPane.setPrefSize(300, 100); // Adjust size as per your content


            // Label for the main text (Song)
            Label label2 = new Label("Song: " + song.getSongName());
            label2.setMaxWidth(280); // Adjust Label width based on TilePane size
            label2.setWrapText(true);
            label2.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;"); // CSS for label
            itemPane.getChildren().add(label2);

            // Label for ArtistName
            Label artistLabel = new Label("Artist: " + song.getArtistName());
            artistLabel.setMaxWidth(280); // Adjust Label width based on TilePane size
            artistLabel.setWrapText(true);
            artistLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
            itemPane.getChildren().add(artistLabel);

            // Label for Album
            Label albumLabel = new Label("Album: " + song.getAlbumName());
            albumLabel.setMaxWidth(280); // Adjust Label width based on TilePane size
            albumLabel.setWrapText(true);
            albumLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;"); // CSS for label
            itemPane.getChildren().add(albumLabel);

            // CheckBox for selection
            CheckBox checkBox = new CheckBox();
            checkBoxes.add(checkBox);
            checkBox.setLayoutX(130); // Adjust CheckBox position as needed
            checkBox.setLayoutY(80); // Adjust CheckBox position as needed
            itemPane.getChildren().add(checkBox);

            checkBox.setSelected(checkboxStateMap.getOrDefault(song.getPath(), true));
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                checkboxStateMap.put(song.getPath(), newValue);
                if(newValue){
                    selectedFiles.add(song);
                }else{
                    selectedFiles.remove(song);
                }
            });
            vbox.getChildren().add(itemPane);
        }
        Button transfare = new Button("Transfare selected songs");
        transfare.setOnMouseClicked(event ->{
            listAllLocalStorages(selectedFiles);

        });
        vbox.getChildren().add(transfare);

    }
    public void listAllLocalStorages(List<Song> selectedFiles) {
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        File[] roots = File.listRoots();
        vbox.getChildren().clear();

        for (File root : roots) {
            if (root.getPath().equals("C:\\")) {
                continue; // Skip processing for C:\ drive
            }else {
                System.out.println(root);
            }
            TilePane itemPane = new TilePane();
            itemPane.setPrefColumns(1);
            itemPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
            itemPane.setPrefSize(300, 100); // Adjust size as per your content

            long usableSpace = root.getUsableSpace();
            String driveName = fileSystemView.getSystemDisplayName(root);
            String driveType = fileSystemView.getSystemTypeDescription(root);

            Label label = new Label("Name : " + driveName);
            label.setMaxWidth(280); // Limit label width
            label.setWrapText(true); // Wrap text if it exceeds the width

            Label label2 = new Label("Usable Space: " + usableSpace / (1024 * 1024 * 1024) + " GB");
            label2.setMaxWidth(280); // Limit label width
            label2.setWrapText(true); // Wrap text if it exceeds the width

            VBox driveInfoBox = new VBox(5);
            driveInfoBox.getChildren().addAll(label, label2);

            itemPane.getChildren().add(driveInfoBox);
            itemPane.setOnMouseClicked(event -> {
                selectedRoot = root;
                if (selectedRoot==null){
                    System.out.println("its null");

                }else {
                    if (copyTask == null || copyTask.isDone()) {
                        System.out.println(selectedRoot);
                        selectedRoot=null;
                        transfare(selectedFiles, root, percentageLabel, cancelButton, pauseButton, progressBar, currentSongLabel);

                        System.out.println("selectedRoot set to nul;");
                    } else {
                        isPaused.set(!isPaused.get());
                    }
                }
                pauseButton.setOnMouseClicked(e3-> {
                    isPaused.set(!isPaused.get());
                });
                cancelButton.setOnAction(e2 -> {
                    if (copyTask != null) {
                        copyTask.cancel();
                        percentageLabel.textProperty().unbind();
                        percentageLabel.setText("0%");
                        progressBar.progressProperty().unbind();
                        progressBar.setProgress(0.0);

                    }
                });

                Stage stage = (Stage) itemPane.getScene().getWindow();
                stage.close();
                transfare(selectedFiles, root, percentageLabel, cancelButton, pauseButton, progressBar, currentSongLabel);
            });
            vbox.getChildren().add(itemPane);
        }


    }

    public void listenForUSBEvents() {
        UsbServicesListener listener = new UsbServicesListener() {
            @Override
            public void usbDeviceAttached(UsbServicesEvent event) {
                UsbDevice device = event.getUsbDevice();
                System.out.println("Device connected: " + device);
                Platform.runLater(() -> {

                    listAllLocalStorages(selectedFiles);
                });
            }

            @Override
            public void usbDeviceDetached(UsbServicesEvent event) {
                UsbDevice device = event.getUsbDevice();
                System.out.println("Device disconnected: " + device);
                Platform.runLater(() -> {

                    listAllLocalStorages(selectedFiles);
                });

            }
        };

        // Start listening in a separate thread
        Thread usbThread = new Thread(() -> {
            try {
                UsbServices services = UsbHostManager.getUsbServices();
                services.addUsbServicesListener(listener);
            } catch (UsbException e) {
                e.printStackTrace();
            }
        });
        usbThread.start();

    }


    public void transfare(List<Song> selectedSongs, File root, Label percentageLabel, Button cancelButton, Button pauseButton, ProgressBar progressBar, Label currentSongLabel) {
        List<File> selectedSongsFile = new ArrayList<>();
        for ( Song song: selectedSongs) {
            String songPath = song.getPath();
            File songFile = new File(songPath);
            selectedSongsFile.add(songFile);
        }

        copyTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                long totalSize = selectedSongsFile.stream().mapToLong(File::length).sum();
                long copiedSize = 0;

                for (File file : selectedSongsFile) {
                    if (isCancelled()) {
                        break;
                    }

                    while (isPaused.get()) {
                        Thread.sleep(100);
                    }

                    Path sourcePath = file.toPath();
                    try {
                        Mp3File mp3file = new Mp3File(file.getPath());
                        if (mp3file.hasId3v2Tag()) {
                            ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                            String artist = id3v2Tag.getArtist();
                            String album = id3v2Tag.getAlbum();

                            // Construct target directory path
                            Path targetDir = Paths.get(root.getPath(), "Music", artist, album);

                            // Check if the directory exists, create if not
                            if (!Files.exists(targetDir)) {
                                Files.createDirectories(targetDir);
                                System.out.println("Directory created successfully: " + targetDir);
                            }

                            // Construct target file path
                            Path targetPath = targetDir.resolve(sourcePath.getFileName());

                            // Copy file
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            copiedSize += Files.size(sourcePath);

                            // Update progress
                            updateProgress(copiedSize, totalSize);
                            updateMessage(file.getName());

                            // Simulate copy delay
                            Thread.sleep(500);
                        }
                    } catch (IOException | UnsupportedTagException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };

        // Bind UI elements to task properties
        progressBar.progressProperty().bind(copyTask.progressProperty());
        currentSongLabel.textProperty().bind(copyTask.messageProperty());
        percentageLabel.textProperty().bind(copyTask.progressProperty().multiply(100).asString("%.0f%%"));


        new Thread(copyTask).start();

    }
    public void show(List<Song> selectedFiles,Label percentageLabel,Button cancelButton,Button pauseButton,ProgressBar progressBar,Label currentSongLabel,Button transferButton) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        VBox vbox2 = new VBox(10);
        vbox2.setStyle("-fx-padding: 10;");
        vbox = vbox2;
        this.percentageLabel = percentageLabel;
        this.cancelButton = cancelButton;
        this.pauseButton = pauseButton;
        this.progressBar = progressBar;
        this.currentSongLabel = currentSongLabel;
        this.transferButton = transferButton;
        this.selectedFiles = selectedFiles;
        finalCheck(popupStage);

        listenForUSBEvents();
        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> popupStage.close());

        vbox.getChildren().add(closeButton);

        Scene scene = new Scene(vbox, 400, 300); // Adjust scene size as per your content
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
}

package com.example.musicapp;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import java.util.List;
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
    private List<String> selectedFiles;

    public void listAllLocalStorages(List<String> selectedFiles) {
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        File[] roots = File.listRoots();
        vbox.getChildren().clear();

        for (File root : roots) {
            if (root.getPath().equals("C:\\")) {
                continue; // Skip processing for C:\ drive
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
                System.out.println("Clicked");
                transferButton.setOnAction(e -> {
                    if (copyTask == null || copyTask.isDone()) {
                        transfare(selectedFiles, root, percentageLabel, cancelButton, pauseButton, progressBar, currentSongLabel);
                    } else {
                        isPaused.set(!isPaused.get());
                    }
                });
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


    public void transfare(List<String> selectedSongs, File root, Label percentageLabel, Button cancelButton, Button pauseButton, ProgressBar progressBar, Label currentSongLabel) {
        List<File> selectedSongsFile = new ArrayList<>();
        for (String songPath : selectedSongs) {
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
    public void show(List<String> selectedFiles,Label percentageLabel,Button cancelButton,Button pauseButton,ProgressBar progressBar,Label currentSongLabel,Button transferButton) {
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
        listAllLocalStorages(selectedFiles);
        listenForUSBEvents();
        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> popupStage.close());

        vbox.getChildren().add(closeButton);

        Scene scene = new Scene(vbox, 400, 300); // Adjust scene size as per your content
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
}

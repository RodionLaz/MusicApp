package com.example.musicapp;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class SelectDisk {

    public static void listAllLocalStorages(VBox vbox,List<String> selectedFiles) {
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        File[] roots = File.listRoots();


        for (File root : roots) {
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
            itemPane.setOnMouseClicked(event-> {
                System.out.println("Clicked");
                for (String f : selectedFiles) {
                    Path source = Paths.get(f);
                    System.out.println("path : " + f);
                    System.out.println("Root : " + root.getPath());

                    try {
                        System.out.println("Source : " + source);

                        Mp3File mp3info = new Mp3File(source);

                        if (mp3info.hasId3v2Tag()) {
                            ID3v2 id3v2Tag = mp3info.getId3v2Tag();
                            String artist = id3v2Tag.getArtist();
                            String album = id3v2Tag.getAlbum();

                            // Create target directory path using Path
                            Path targetDir = Paths.get(root.getPath() + "/Music", artist, album);

                            // Check if the directory exists
                            if (!Files.exists(targetDir)) {
                                // Create directories if they don't exist using Files.createDirectories
                                Files.createDirectories(targetDir);
                                System.out.println("Directory created successfully: " + targetDir);
                            }

                            // Construct the target file path within the directory
                            Path target = targetDir.resolve(source.getFileName());

                            // Copy the file using Files.copy with REPLACE_EXISTING
                            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("File copied (replacing if existed): " + target);
                        }
                        System.out.println("DONE");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

            });
            vbox.getChildren().add(itemPane);
        }


    }

    public void show(List<String> selectedFiles) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);

        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10;");
        listAllLocalStorages(vbox,selectedFiles);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> popupStage.close());

        vbox.getChildren().add(closeButton);

        Scene scene = new Scene(vbox, 400, 300); // Adjust scene size as per your content
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
}

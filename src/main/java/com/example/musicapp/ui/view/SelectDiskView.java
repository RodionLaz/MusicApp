package com.example.musicapp.ui.view;

import com.example.musicapp.data.modle.Song;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.List;
import java.util.Map;

public class SelectDiskView {
    private VBox vbox;
    private Label percentageLabel;
    private Button cancelButton;
    private Button pauseButton;
    private ProgressBar progressBar;
    private Label currentSongLabel;
    private List<CheckBox> checkBoxes;

    public SelectDiskView() {
        this.vbox = new VBox(10);
        this.vbox.setStyle("-fx-padding: 10;");
    }

    public void show(Stage popupStage, List<Song> selectedFiles, Map<String, Boolean> checkboxStateMap, Button transferButton) {
        popupStage.initModality(Modality.APPLICATION_MODAL);
        finalCheck(selectedFiles, checkboxStateMap);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> popupStage.close());
        vbox.getChildren().add(closeButton);

        Scene scene = new Scene(vbox, 400, 300);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    private void finalCheck(List<Song> selectedFiles, Map<String, Boolean> checkboxStateMap) {
        vbox.getChildren().clear();
        for (Song song : selectedFiles) {
            TilePane itemPane = createItemPane(song, checkboxStateMap);
            vbox.getChildren().add(itemPane);
        }
        Button transferButton = new Button("Transfer selected songs");
        vbox.getChildren().add(transferButton);
    }

    private TilePane createItemPane(Song song, Map<String, Boolean> checkboxStateMap) {
        TilePane itemPane = new TilePane();
        itemPane.setPrefColumns(1);
        itemPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        itemPane.setPrefSize(300, 100);

        Label songLabel = createLabel("Song: " + song.getSongName(), 14, "#333333");
        Label artistLabel = createLabel("Artist: " + song.getArtistName(), 12, "#666666");
        Label albumLabel = createLabel("Album: " + song.getAlbumName(), 12, "#666666");

        CheckBox checkBox = createCheckBox(song, checkboxStateMap);

        itemPane.getChildren().addAll(songLabel, artistLabel, albumLabel, checkBox);
        return itemPane;
    }

    private Label createLabel(String text, int fontSize, String color) {
        Label label = new Label(text);
        label.setMaxWidth(280);
        label.setWrapText(true);
        label.setStyle(String.format("-fx-font-size: %dpx; -fx-text-fill: %s;", fontSize, color));
        return label;
    }

    private CheckBox createCheckBox(Song song, Map<String, Boolean> checkboxStateMap) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(checkboxStateMap.getOrDefault(song.getPath(), true));
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> checkboxStateMap.put(song.getPath(), newValue));
        checkBoxes.add(checkBox);
        return checkBox;
    }

    public void updateStorageList(List<File> roots, FileSystemView fileSystemView, List<File> excludedRoots) {
        vbox.getChildren().clear();
        for (File root : roots) {
            if (excludedRoots.contains(root)) {
                continue;
            }
            TilePane itemPane = createStorageItemPane(root, fileSystemView);
            vbox.getChildren().add(itemPane);
        }
    }

    private TilePane createStorageItemPane(File root, FileSystemView fileSystemView) {
        TilePane itemPane = new TilePane();
        itemPane.setPrefColumns(1);
        itemPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        itemPane.setPrefSize(300, 100);

        long usableSpace = root.getUsableSpace();
        String driveName = fileSystemView.getSystemDisplayName(root);

        Label nameLabel = createLabel("Name: " + driveName, 14, "#333333");
        Label spaceLabel = createLabel("Usable Space: " + usableSpace / (1024 * 1024 * 1024) + " GB", 12, "#666666");

        VBox driveInfoBox = new VBox(5);
        driveInfoBox.getChildren().addAll(nameLabel, spaceLabel);
        itemPane.getChildren().add(driveInfoBox);

        return itemPane;
    }

    public void bindTask(Task<Void> copyTask) {
        progressBar.progressProperty().bind(copyTask.progressProperty());
        currentSongLabel.textProperty().bind(copyTask.messageProperty());
        percentageLabel.textProperty().bind(copyTask.progressProperty().multiply(100).asString("%.0f%%"));
    }

    public void setControls(Label percentageLabel, Button cancelButton, Button pauseButton, ProgressBar progressBar, Label currentSongLabel) {
        this.percentageLabel = percentageLabel;
        this.cancelButton = cancelButton;
        this.pauseButton = pauseButton;
        this.progressBar = progressBar;
        this.currentSongLabel = currentSongLabel;
    }

    public VBox getVbox() {
        return vbox;
    }
}

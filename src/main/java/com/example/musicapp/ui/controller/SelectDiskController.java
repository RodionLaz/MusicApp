package com.example.musicapp.ui.controller;

import com.example.musicapp.data.modle.Song;
import com.example.musicapp.ui.view.SelectDiskView;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import javax.swing.filechooser.FileSystemView;
import javax.usb.*;
import javax.usb.event.UsbServicesEvent;
import javax.usb.event.UsbServicesListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SelectDiskController {
    private SelectDiskView view;
    private AtomicBoolean isPaused = new AtomicBoolean(false);
    private Task<Void> copyTask;
    private List<Song> selectedFiles;
    private File selectedRoot = new File("D:\\");
    private Map<String, Boolean> checkboxStateMap = new HashMap<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public SelectDiskController(SelectDiskView view) {
        this.view = view;
    }

    public void show(List<Song> selectedFiles, Label percentageLabel, Button cancelButton, Button pauseButton, ProgressBar progressBar, Label currentSongLabel, Button transferButton) {
        this.selectedFiles = selectedFiles;
        view.setControls(percentageLabel, cancelButton, pauseButton, progressBar, currentSongLabel);
        view.show(new Stage(), selectedFiles, checkboxStateMap, transferButton);
        listenForUSBEvents();
    }

    public void listAllLocalStorages() {
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        File[] roots = File.listRoots();
        List<File> excludedRoots = Collections.singletonList(new File("C:\\"));

        view.updateStorageList(Arrays.asList(roots), fileSystemView, excludedRoots);
    }

    private void startTransferTask() {
        copyTask = createTransferTask();
        view.bindTask(copyTask);
        executorService.submit(copyTask);
    }

    private Task<Void> createTransferTask() {
        List<File> selectedSongsFile = selectedFiles.stream().map(song -> new File(song.getPath())).collect(Collectors.toList());

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                long totalSize = selectedSongsFile.stream().mapToLong(File::length).sum();
                long copiedSize = 0;

                for (File file : selectedSongsFile) {
                    if (isCancelled()) break;

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

                            Path targetDir = Paths.get(selectedRoot.getPath(), "Music", artist, album);
                            if (!Files.exists(targetDir)) {
                                Files.createDirectories(targetDir);
                            }

                            Path targetPath = targetDir.resolve(sourcePath.getFileName());
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            copiedSize += Files.size(sourcePath);

                            updateProgress(copiedSize, totalSize);
                            updateMessage(file.getName());

                            Thread.sleep(500);
                        }
                    } catch (IOException | UnsupportedTagException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
    }

    private void listenForUSBEvents() {
        UsbServicesListener listener = new UsbServicesListener() {
            @Override
            public void usbDeviceAttached(UsbServicesEvent event) {
                Platform.runLater(SelectDiskController.this::listAllLocalStorages);
            }

            @Override
            public void usbDeviceDetached(UsbServicesEvent event) {
                Platform.runLater(SelectDiskController.this::listAllLocalStorages);
            }
        };

        executorService.submit(() -> {
            try {
                UsbServices services = UsbHostManager.getUsbServices();
                services.addUsbServicesListener(listener);
            } catch (UsbException e) {
                e.printStackTrace();
            }
        });
    }

    public void handleTransferButtonAction() {
        startTransferTask();
    }

    public void handleCancelButtonAction() {
        if (copyTask != null) {
            copyTask.cancel();
        }
    }

    public void handlePauseButtonAction() {
        isPaused.set(!isPaused.get());
    }
}

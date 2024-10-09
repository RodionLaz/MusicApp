package com.example.musicapp.ui.service;

import com.example.musicapp.data.modle.Song;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.example.musicapp.ui.view.AlertsView.showErrorMessage;

public class FilesService {

    public static void addFilesToListByPath(String path,List<File> SaveList){
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") ||
                        fileName.endsWith(".mp4") || fileName.endsWith(".mov") || fileName.endsWith(".m4v")) {
                    SaveList.add(file);
                }
            }
        }
    }
    public static void addFilesToListByFileChooser(List<File> SaveList, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Files");
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            SaveList.addAll(selectedFiles);
        }
    }
    public static void addFilesToListByFileChooser(List<File> SaveList, Stage stage,String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            SaveList.addAll(selectedFiles);
        }
    }
    public static String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
    public static List<Song> findMP3sInDirectory(String directoryPath) {
        List<Song> songs = new ArrayList<>();
        File directory = new File(directoryPath);

        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory: " + directoryPath);
            return songs;
        }

        // Recursively search for MP3 files
        File[] files = directory.listFiles();
        if (files == null) {
            System.out.println("No files found in directory: " + directoryPath);
            return songs;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Recursive call to search subdirectories
                songs.addAll(findMP3sInDirectory(file.getAbsolutePath()));
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".mp3")) {
                try {
                    // Extract MP3 metadata
                    Mp3File mp3info = new Mp3File(file.getPath());
                    if (mp3info.hasId3v2Tag()) {
                        ID3v2 id3v2Tag = mp3info.getId3v2Tag();
                        Song song = new Song(id3v2Tag.getTitle(), id3v2Tag.getArtist(), id3v2Tag.getAlbum(),
                                id3v2Tag.getYear(), file.getPath());
                        songs.add(song);
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // Handle any exceptions that occur while reading MP3 metadata
                }
            }
        }

        return songs;
    }
    public static void organize(String SelectedPath) {
        File musicDirectory = new File(SelectedPath);

        if (!musicDirectory.exists() || !musicDirectory.isDirectory()) {
            System.out.println("Warning: Music directory does not exist or is not a directory.");
            return;
        }

        processDirectory(musicDirectory, musicDirectory);
    }

    private static void processDirectory(File directory, File baseDirectory) {
        File[] files = directory.listFiles();

        if (files == null) {
            System.out.println("Warning: Failed to list files in directory: " + directory.getPath());
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

                            File targetFile = new File(albumDir, sanitizeFileName(file.getName()));
                            if (!targetFile.exists()) {
                                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("Moved file: " + file.getName() + " to " + targetFile.getPath());
                            } else {
                                System.out.println("File already exists in target directory: " + targetFile.getPath());
                            }
                        }
                    } else {
                        System.out.println("Error: File has no tag: " + file.getPath());
                    }

                } catch (IOException | UnsupportedTagException | InvalidDataException | InvalidPathException e) {
                    e.printStackTrace();
                    showErrorMessage(e.getMessage(), "ERROR");
                }
            }
        }
    }

    public static Task<Void> createTransferTask(List<File> selectedSongsFile, File selectedRoot, AtomicBoolean isPaused ) {

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

}

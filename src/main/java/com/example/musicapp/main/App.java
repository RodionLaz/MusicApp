package com.example.musicapp.main;

import com.example.musicapp.ui.controller.MainPageController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;
    private static Scene mainScene;
    @Override
    public void start(Stage primaryStage) {
        try {

            App.primaryStage = primaryStage;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/musicapp/Main-Page.fxml"));
            Parent root = loader.load();
            mainScene = new Scene(root, 1000, 1000);

            MainPageController controller = loader.getController();


            controller.setPrimaryStage(primaryStage);
            controller.setMainScene(mainScene);
            primaryStage.setTitle("Music Transfare");

            primaryStage.setScene(mainScene);
            primaryStage.setFullScreen(true);
            primaryStage.show();


        } catch (IOException e) {
           e.printStackTrace();

        }
    }
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    public static Scene getPrimaryScene() {
        return mainScene;
    }
    public static void main(String[] args) {
        launch(args);
    }
}
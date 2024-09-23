package com.example.musicapp.main;

import com.example.musicapp.ui.controller.MainPageController;
import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;
    private static Scene mainScene;
    private static MainPageController controller;
    @Override
    public void start(Stage primaryStage) {
        try {

            App.primaryStage = primaryStage;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/musicapp/Main-Page.fxml"));
            Parent root = loader.load();
            mainScene = new Scene(root, 1000, 1000);

            controller = loader.getController();
            controller.setMainPageController(controller);

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
    public static MainPageController getMainPageController(){

     return controller;
    }
    public static void main(String[] args) {
        launch(args);
    }
}
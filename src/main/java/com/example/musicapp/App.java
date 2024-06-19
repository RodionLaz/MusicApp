package com.example.musicapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



import java.io.IOException;

public class App extends Application {


    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Main-Page.fxml"));
            Parent root = loader.load();

            // Get the controller instance
            MainPageController controller = loader.getController();
            // Pass the stage to the controller
            controller.setPrimaryStage(primaryStage);

            primaryStage.setTitle("JavaFX App");
            primaryStage.setScene(new Scene(root, 1000, 1000));
            primaryStage.show();

        } catch (IOException e) {
           // e.printStackTrace();
            // Handle or log the exception as needed
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
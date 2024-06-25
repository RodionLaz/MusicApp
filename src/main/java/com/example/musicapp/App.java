package com.example.musicapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App extends Application {


    @Override
    public void start(Stage primaryStage) {
        try {


//            try{
//                System.setProperty("java.library.path", "D:\\libusb4java.dll");
//                Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
//                fieldSysPath.setAccessible(true);
//                fieldSysPath.set(null, null);
//            }catch (Exception e){
//                e.printStackTrace();
//            }

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
           e.printStackTrace();

        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
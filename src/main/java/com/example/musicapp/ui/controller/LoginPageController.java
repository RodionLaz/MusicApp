package com.example.musicapp.ui.controller;


import com.example.musicapp.data.modle.User;
import com.example.musicapp.main.App;
import com.example.musicapp.ui.view.LoginPageView;
import javafx.stage.Stage;

public class LoginPageController {

    private static LoginPageController instance;
    private LoginPageView loginPageView;
    private MainPageController mainPageController;

    private LoginPageController(Stage primaryStage) {

        this.loginPageView = LoginPageView.getInstance(primaryStage, this);
    }

    public void setMainPageController(MainPageController mainPageController) {
        this.mainPageController = App.getMainPageController();
    }

    public static LoginPageController getInstance(Stage primaryStage) {
        if (instance == null) {
            synchronized (LoginPageController.class) {
                if (instance == null) {
                    instance = new LoginPageController(primaryStage);
                }
            }
        }
        return instance;
    }

    public static LoginPageController getInstance() {
        return instance;
    }

    public void setUser(User user) {

        setMainPageController(App.getMainPageController());
        System.out.println("mainPageController 2"+mainPageController);
        mainPageController.switchToMainScene(user);
    }

    public void ChangeToLoginScene() {
        loginPageView.showAuthScene();
    }

}

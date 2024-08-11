package com.example.musicapp.ui.controller;

import com.example.musicapp.data.modle.Access;
import com.example.musicapp.ui.view.LoginPageView;
import javafx.stage.Stage;

public class LoginPageController {
    private Access access;
    private static LoginPageController instanse;
    private LoginPageView loginPageView;
    private Stage primaryStage;

    private LoginPageController(Stage primaryStage){
        this.primaryStage = primaryStage;
        loginPageView = LoginPageView.getInstance(primaryStage);
    }
    public static LoginPageController getInstance(Stage primaryStage){
        if (instanse ==null){
            synchronized (LoginPageController.class){
                if (instanse ==null){
                    instanse = new LoginPageController(primaryStage);
                    return instanse;
                }
            }
        }
        return instanse;
    }
    public static LoginPageController getInstance(){
        return instanse;
    }

    public void setAccess(Access access) {
        this.access = access;
    }
    public Access getAccess() {
        return access;
    }
    public void ChangeToLoginScene(){
        loginPageView.showAuthScene();
    }
    public Access login(Access access){
        return getAccess();
    }
}

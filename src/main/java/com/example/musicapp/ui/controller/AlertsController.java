package com.example.musicapp.ui.controller;

import com.example.musicapp.data.database.controller.DatabaseController;
import com.example.musicapp.ui.view.AlertsView;

public class AlertsController {
    private static AlertsController instanse;

    public static AlertsController getInstance(){
        if (instanse ==null){
            synchronized (AlertsController.class){
                if (instanse ==null){
                    instanse = new AlertsController();
                    return instanse;
                }
            }
        }
        return instanse;
    }


    public void showInformationMessage(String message, String title) {
        AlertsView.showInformationMessage(message, title);
    }

    public void showWarningMessage(String message, String title) {
        AlertsView.showWarningMessage(message, title);
    }

    public void showErrorMessage(String message, String title) {
        AlertsView.showErrorMessage(message, title);
    }

    public boolean showConfirmationDialog(String message, String title) {
        return AlertsView.showConfirmationDialog(message, title);
    }
}

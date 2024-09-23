package com.example.musicapp.data.user.controller;

import com.example.musicapp.data.modle.User;

public class UserController {

    private static UserController instance;
    private User user;
    private UserController(User user){
        this.user = user;
    }

    public void clearUser(){
        this.user = null;
    }
    public static UserController getInstance(User user){
        if (instance == null){
            synchronized (UserController.class){
                if ( instance==null){
                    instance = new UserController(user);
                    return instance;
                }else{
                    return instance;
                }
            }
        }else {
            return instance;
        }
    }
    public static UserController getInstance(){
        if (instance == null){
            return null;
        }else {
            return instance;
        }
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}

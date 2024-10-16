package com.example.musicapp.data.modle;

public class User {
    private String username;
    private String password;
    private boolean admin;
    private boolean ads;
    private boolean dir;
    private boolean users;

    public User(String username, String password, boolean ads, boolean dir, boolean users, boolean admin) {
        this.username = username;
        this.password = password;
        this.ads = ads;
        this.dir = dir;
        this.users = users;
        this.admin = admin;
    }

    public User(String username, boolean ads, boolean dir,boolean admin, boolean users) {
        this.username = username;
        this.admin = admin;
        this.ads = ads;
        this.dir = dir;
        this.users = users;
    }

    public boolean isAdmin() {
        return admin;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean hasAds() {
        return ads;
    }

    public void setAds(boolean ads) {
        this.ads = ads;
    }

    public boolean hasDir() {
        return dir;
    }

    public void setDir(boolean dir) {
        this.dir = dir;
    }

    public boolean hasUsersAccess() {
        return users;
    }

    public void setUsers(boolean users) {
        this.users = users;
    }
}

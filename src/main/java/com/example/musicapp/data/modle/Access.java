package com.example.musicapp.data.modle;

public class Access {
    private boolean ads;
    private boolean dir;
    private boolean users;
    private boolean admin;

    public Access(boolean ads, boolean dir, boolean users,boolean admin) {
        this.ads = ads;
        this.dir = dir;
        this.users = users;
        this.admin = admin;
    }

    public boolean hasAdmin() {
        return admin;
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

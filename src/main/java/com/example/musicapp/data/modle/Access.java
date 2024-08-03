package com.example.musicapp.data.modle;

public class Access {
    private boolean ads;
    private boolean dir;
    private boolean users;

    public Access(boolean ads, boolean dir, boolean users) {
        this.ads = ads;
        this.dir = dir;
        this.users = users;
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

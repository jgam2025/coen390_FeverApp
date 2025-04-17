package com.example.coen390_feverapp;

public class Profile {

    private int userId;
    private String name;

    public Profile(String name, int userId) {
        this.userId = userId;
        this.name = name;
    }
    public int getUserId() {
        return userId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}

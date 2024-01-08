package com.itstep.coursework.db.models;

public class LoginFormModel {
    private String username;
    private String password;

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

    public String getInvalidFields() {
        if(getUsername() == null) return "username";
        else if(getPassword() == null) return "password";
        return null;
    }
}

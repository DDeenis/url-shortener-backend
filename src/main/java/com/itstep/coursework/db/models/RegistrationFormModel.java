package com.itstep.coursework.db.models;

import java.util.HashMap;

public class RegistrationFormModel {
    private String username;
    private String password;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getInvalidFields() {
        if(getUsername() == null) return "username";
        else if(getPassword() == null) return "password";
        else if(getEmail() == null) return "email";
        return null;
    }
}

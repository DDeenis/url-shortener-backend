package com.itstep.coursework.db.models;

public class EditProfileFormModel {
    private String username;
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

    public String getInvalidFields() {
        if(getUsername() == null) return "username";
        else if(getEmail() == null) return "email";
        return null;
    }
}

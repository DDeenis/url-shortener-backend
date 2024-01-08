package com.itstep.coursework.db.models;

public class ChangePasswordFormModel {
    private String currentPassword;
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getInvalidFields() {
        if(getCurrentPassword() == null) return "currentPassword";
        else if(getNewPassword() == null) return "newPassword";
        return null;
    }
}

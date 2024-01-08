package com.itstep.coursework.db.dto;

import com.itstep.coursework.db.entities.User;

import java.util.Date;

public class UserDto {
    private String id;
    private String username;
    private String email;
    private Date registerAt;
    private Date deleteAt;

    public UserDto(User user) {
        setId(user.getId());
        setUsername(user.getUsername());
        setEmail(user.getEmail());
        setRegisterAt(user.getRegisterAt());
        setDeleteAt(user.getDeleteAt());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getRegisterAt() {
        return registerAt;
    }

    public void setRegisterAt(Date registerAt) {
        this.registerAt = registerAt;
    }

    public Date getDeleteAt() {
        return deleteAt;
    }

    public void setDeleteAt(Date deleteAt) {
        this.deleteAt = deleteAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

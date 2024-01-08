package com.itstep.coursework.db.entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class User {
    private String id;
    private String username;
    private String email;
    private String salt;
    private String passDK;
    private Date registerAt;
    private Date deleteAt;

    public User(ResultSet resultSet) throws SQLException {
        setId(resultSet.getString("id"));
        setUsername(resultSet.getString("username"));
        setEmail(resultSet.getString("email"));
        setSalt(resultSet.getString("salt"));
        setPassDK(resultSet.getString("passDK"));

        Timestamp moment = resultSet.getTimestamp("registerAt");
        this.setRegisterAt(moment == null ? null : new Date(moment.getTime()));
        moment = resultSet.getTimestamp("deletedAt");
        this.setDeleteAt(moment == null ? null : new Date(moment.getTime()));
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

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getPassDK() {
        return passDK;
    }

    public void setPassDK(String passDK) {
        this.passDK = passDK;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

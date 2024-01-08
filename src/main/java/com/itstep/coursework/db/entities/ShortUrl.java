package com.itstep.coursework.db.entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class ShortUrl {
    private String id;
    private String userId;
    private String originalUrl;
    private int redirects;
    private boolean deactivated;
    private Date createdAt;

    public ShortUrl(String id, String userId, String originalUrl, int redirects, boolean deactivated, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.originalUrl = originalUrl;
        this.redirects = redirects;
        this.deactivated = deactivated;
        this.createdAt = createdAt;
    }

    public ShortUrl(ResultSet resultSet) throws SQLException {
        setId(resultSet.getString("id"));
        setUserId(resultSet.getString("userId"));
        setOriginalUrl(resultSet.getString("originalUrl"));
        setRedirects(resultSet.getInt("redirects"));
        setDeactivated(resultSet.getBoolean("deactivated"));

        Timestamp moment = resultSet.getTimestamp("createdAt");
        this.setCreatedAt(moment == null ? null : new Date(moment.getTime()));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public int getRedirects() {
        return redirects;
    }

    public void setRedirects(int redirects) {
        this.redirects = redirects;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

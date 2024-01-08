package com.itstep.coursework.db.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShortUrlFilters {
    private final String userId;
    private final String query;
    private final int page;
    private final int pageSize;
    private final Date after;

    public ShortUrlFilters(String userId, String query, int page, int pageSize, Date after) {
        this.userId = userId;
        this.query = query;
        this.page = page;
        this.pageSize = pageSize;
        this.after = after;
    }

    public String getQuery() {
        return query;
    }

    public String getUserId() {
        return userId;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Date getAfter() {
        return after;
    }

    public String[] getInvalidParameters() {
        List<String> parameters = new ArrayList<>();

        if(userId == null || userId.isEmpty()) {
            parameters.add("userId");
        }
        if(page <= 0) {
            parameters.add("page");
        }
        if(pageSize <= 0) {
            parameters.add("pageSize");
        }

        String[] arr = new String[parameters.size()];
        return parameters.toArray(arr);
    }
}

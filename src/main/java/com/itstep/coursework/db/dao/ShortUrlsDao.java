package com.itstep.coursework.db.dao;

import com.itstep.coursework.db.entities.ShortUrl;
import com.itstep.coursework.db.entities.ShortUrlFilters;
import com.itstep.coursework.db.models.ShortenUrlModel;
import com.itstep.coursework.services.random.NanoIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShortUrlsDao {
    final DataSource dataSource;

    @Value("${app.db-prefix}")
    private String dbPrefix;

    @Autowired
    public ShortUrlsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean install() {
        String sql = "create table if not exists " + dbPrefix + "short_urls ("
                + "`id` char(21) primary key charset latin1 collate latin1_general_cs,"
                + "`userId` bigint unsigned,"
                + "`originalUrl` varchar(4096) not null,"
                + "`redirects` int not null default 0,"
                + "`deactivated` bool not null default false,"
                + "`createdAt` datetime not null default now()"
                + ") engine = InnoDB, default charset = utf8mb4 collate utf8mb4_unicode_ci";
        try(Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public ShortUrl create(ShortenUrlModel model, String userId) {
        String sql = "insert into " + dbPrefix + "short_urls (`id`, `userId`, `originalUrl`, `redirects`, `deactivated`, `createdAt`) values (?, ?, ?, ?, ?, ?)";
        String shortUrlId = NanoIdService.randomNanoId();
        int redirects = 0;
        boolean deactivated = false;
        Instant createdAt = Instant.now();
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, shortUrlId);
            statement.setString(2, userId);
            statement.setString(3, model.getOriginalUrl());
            statement.setInt(4, redirects);
            statement.setBoolean(5, deactivated);
            statement.setTimestamp(6, new Timestamp(createdAt.toEpochMilli()));
            statement.executeUpdate();
            return new ShortUrl(
                shortUrlId,
                userId,
                model.getOriginalUrl(),
                0,
                false,
                Date.from(createdAt)
            );
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public ShortUrl getById(String id) {
        String sql = "select * from " + dbPrefix + "short_urls where `id`=?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return new ShortUrl(resultSet);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }

        return null;
    }

    public ShortUrl[] getByFilters(ShortUrlFilters filters) {
        String sql = "select * from " + dbPrefix + "short_urls where `userId`=?";
        List<ShortUrl> urls = new ArrayList<>();

        String userId = filters.getUserId();
        String query = filters.getQuery();
        int page = filters.getPage();
        int pageSize = filters.getPageSize();
        java.util.Date after = filters.getAfter();

        boolean withQueryFilter = query != null && !query.isEmpty();
        boolean withDateFilter = after != null;

        if(withQueryFilter) {
            sql += " and (`originalUrl` like ? or `id` like ?)";
            query = String.format("%%%s%%", query);
        }

        if(withDateFilter) {
            sql += " and date(`createdAt`) > ?";
        }

        sql += " order by `createdAt` desc limit ? offset ?";

        int argIndex = 1;
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(argIndex++, userId);
            if(withQueryFilter) {
                statement.setString(argIndex++, query);
                statement.setString(argIndex++, query);
            }
            if(withDateFilter) {
                statement.setDate(argIndex++, new java.sql.Date(after.getTime()));
            }
            statement.setInt(argIndex++, pageSize + 1);
            statement.setInt(argIndex, (page - 1) * pageSize);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                urls.add(new ShortUrl(resultSet));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        ShortUrl[] array = new ShortUrl[urls.size()];
        return urls.toArray(array);
    }

    public boolean incrementRedirects(String id) {
        String sql = "update " + dbPrefix + "short_urls set `redirects`=`redirects`+1 where id=?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public int countAll(String userId) {
        String sql = "select count(*) as `counterAll` from " + dbPrefix + "short_urls where `userId`=?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getInt("counterAll");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return 0;
    }

    public boolean setDeactivated(String id, boolean deactivated) {
        String sql = "update " + dbPrefix + "short_urls set `deactivated`=? where id=?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, deactivated);
            statement.setString(2, id);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}

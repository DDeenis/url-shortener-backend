package com.itstep.coursework.db.dao;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.itstep.coursework.services.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;

@Service
public class TokensDao {
    final DataSource dataSource;
    private final JwtService jwtService;

    @Value("${app.db-prefix}")
    private String dbPrefix;
    private final int tokenMaxAge = 24 * 60 * 60;

    @Autowired
    public TokensDao(DataSource dataSource, JwtService jwtService) {
        this.dataSource = dataSource;
        this.jwtService = jwtService;
    }

    public boolean install() {
        String sql = "create table if not exists " + dbPrefix + "tokens ("
                + "`jti` binary(16) primary key default (uuid_to_bin(uuid())),"
                + "`sub` bigint unsigned not null comment 'user-id',"
                + "`exp` datetime not null,"
                + "`iat` datetime not null default current_timestamp"
                + ") engine = InnoDB, default charset = utf8mb4 collate utf8mb4_unicode_ci";
        try(Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public String create(String userId) {
        String jti = UUID.randomUUID().toString();
        Instant iat = Instant.now();
        Instant exp = iat.plusSeconds(tokenMaxAge);
        String token = jwtService.createToken(jti, userId, iat, exp);

        if(token == null) return null;

        String sql = "insert into " + dbPrefix + "tokens (`jti`, `sub`, `iat`, `exp`) values (uuid_to_bin(?),?,?,?)";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, jti);
            statement.setString(2, userId);
            statement.setTimestamp(3, new Timestamp(iat.toEpochMilli()));
            statement.setTimestamp(4, new Timestamp(exp.toEpochMilli()));
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }

        return token;
    }

    public boolean remove(String jti) {
        String sql = "delete from " + dbPrefix + "tokens where `jti`=uuid_to_bin(?)";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, jti);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean removeAllForUser(String userId) {
        String sql = "delete from " + dbPrefix + "tokens where `sub`=?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public String getByUserId(String userId) {
        String sql = "select bin_to_uuid(`jti`) as jti, `sub`, `iat`, `exp` from "
                + dbPrefix
                + "tokens where `sub`=? and `exp`>now()";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return jwtService.createToken(
                    resultSet.getString("jti"),
                    resultSet.getString("sub"),
                    resultSet.getTimestamp("iat").toInstant(),
                    resultSet.getTimestamp("exp").toInstant()
                );
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

        return null;
    }

    public DecodedJWT verifyAndDecode(String tokenString) {
        if(tokenString == null || tokenString.isEmpty()) {
            return null;
        }

        DecodedJWT tokenData = jwtService.verifyToken(tokenString);

        if(tokenData == null || jwtService.isTokenExpired(tokenData)) {
            return null;
        }

        String sql = "select bin_to_uuid(`jti`) as jti, `sub`, `iat`, `exp` from "
                + dbPrefix
                + "tokens where `jti`=uuid_to_bin(?) and `exp`>now()";

        try(Connection connection = dataSource.getConnection()) {
            boolean isTokenFound = false;

            try(PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, tokenData.getId());
                ResultSet resultSet = statement.executeQuery();
                isTokenFound = resultSet.next();
            }

            if(isTokenFound) {
                return tokenData;
            }

            sql = "delete from " + dbPrefix + "tokens where `jti`=uuid_to_bin(?)";
            try(PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, tokenData.getId());
                statement.executeUpdate();
            }

            return null;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}

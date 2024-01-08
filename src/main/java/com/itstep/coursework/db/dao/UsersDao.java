package com.itstep.coursework.db.dao;

import com.itstep.coursework.db.entities.User;
import com.itstep.coursework.db.models.EditProfileFormModel;
import com.itstep.coursework.db.models.RegistrationFormModel;
import com.itstep.coursework.services.kdf.KdfService;
import com.itstep.coursework.services.random.RandomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Objects;

@Service
public class UsersDao {
    final DataSource dataSource;
    private final RandomService randomService;
    private final KdfService kdfService;

    @Value("${app.db-prefix}")
    private String dbPrefix;

    @Autowired
    public UsersDao(DataSource dataSource, RandomService randomService, KdfService kdfService) {
        this.dataSource = dataSource;
        this.randomService = randomService;
        this.kdfService = kdfService;
    }

    public boolean install() {
        String sql = "create table if not exists " + dbPrefix + "users ("
                + "`id` bigint unsigned primary key default (uuid_short()),"
                + "`username` varchar(64) not null unique,"
                + "`email` varchar(128) not null unique,"
                + "`salt` varchar(16) not null comment 'RFC 2898 -- Salt',"
                + "`passDK` varchar(64) not null comment 'RFC 2898 -- Derived key',"
                + "`registerAt` datetime not null default current_timestamp,"
                + "`deletedAt` datetime null"
                + ") engine = InnoDB, default charset = utf8mb4 collate utf8mb4_unicode_ci";
        try(Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean create(RegistrationFormModel formModel) {
        String sql = "insert into " + dbPrefix + "users (`username`, `email`, `salt`, `passDK`) values (?,?,?,?)";
        String salt = randomService.randomHex(16);
        String passDK = kdfService.getDerivedKey(formModel.getPassword(), salt);
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, formModel.getUsername());
            statement.setString(2, formModel.getEmail());
            statement.setString(3, salt);
            statement.setString(4, passDK);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean update(EditProfileFormModel formModel, String userId) {
        String sql = "update " + dbPrefix + "users set `username` = ?, `email` = ? where `id` = ?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, formModel.getUsername());
            statement.setString(2, formModel.getEmail());
            statement.setString(3, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean updatePassword(User user, String newPassword) {
        String sql = "update " + dbPrefix + "users set `passDK` = ? where `id` = ?";
        String passDK = kdfService.getDerivedKey(newPassword, user.getSalt());
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passDK);
            statement.setString(2, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean isUsernameAvailable(String username) {
        User user = getByUsername(username);
        return user == null;
    }

    public User getByUsername(String username) {
        String sql = "select * from " + dbPrefix + "users where `username`=?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return new User(resultSet);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }

        return null;
    }

    public User getById(String userId) {
        String sql = "select * from " + dbPrefix + "users where `id`=?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return new User(resultSet);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }

        return null;
    }

    public boolean verifyPassword(User user, String password) {
        String passDK = kdfService.getDerivedKey(password, user.getSalt());
        return Objects.equals(user.getPassDK(), passDK);
    }
}

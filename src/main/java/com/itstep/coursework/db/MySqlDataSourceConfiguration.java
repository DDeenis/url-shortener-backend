package com.itstep.coursework.db;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.util.Objects;
import com.mysql.cj.log.StandardLogger;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration(proxyBeanMethods = false)
public class MySqlDataSourceConfiguration {
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
        public DataSource dataSource() {
            try {
                InputStreamReader inputStream = new InputStreamReader(
                        Objects.requireNonNull(
                                this.getClass().getClassLoader().getResourceAsStream("db_config.json")
                        )
                );
                JsonObject object = JsonParser.parseReader(inputStream).getAsJsonObject();

                JsonObject planetScaleConfig = object.get("PlanetScale").getAsJsonObject();
                String url = planetScaleConfig.get("url").getAsString();
                String username = planetScaleConfig.get("username").getAsString();
                String password = planetScaleConfig.get("password").getAsString();

                inputStream.close();

                HikariDataSource ds = new HikariDataSource();
                ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
                ds.setJdbcUrl(url);
                ds.setUsername(username);
                ds.setPassword(password);
                ds.setMaxLifetime(30000);
                ds.setConnectionTimeout(5000);
                ds.setValidationTimeout(5000);
                ds.setMaximumPoolSize(10);

                return ds;
//                return DataSourceBuilder
//                        .create()
//                        .type(SimpleDriverDataSource.class)
//                        .driverClassName("com.mysql.cj.jdbc.Driver")
//                        .url(url)
//                        .username(username)
//                        .password(password)
//                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
}

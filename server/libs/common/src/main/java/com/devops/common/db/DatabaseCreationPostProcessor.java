package com.devops.common.db;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runs during the BeanFactory post-processing phase — before any bean
 * (including HikariCP's DataSource) is instantiated. Connects to the
 * default 'postgres' database and creates the service database if it
 * does not yet exist.
 *
 * Picked up automatically by any service that includes this lib in its
 * component-scan base packages (scanBasePackages = "com.devops.common").
 */
@Component
public class DatabaseCreationPostProcessor implements BeanFactoryPostProcessor {

    private final Environment env;

    public DatabaseCreationPostProcessor(Environment env) {
        this.env = env;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        String url = env.getRequiredProperty("spring.datasource.url");
        String username = env.getRequiredProperty("spring.datasource.username");
        String password = env.getRequiredProperty("spring.datasource.password");

        // Derive the target DB name from the URL (last path segment, strip query
        // params)
        String dbName = url.substring(url.lastIndexOf('/') + 1).split("\\?")[0];
        String masterUrl = url.substring(0, url.lastIndexOf('/')) + "/postgres";

        try (Connection conn = DriverManager.getConnection(masterUrl, username, password);
                Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE " + dbName);
        } catch (SQLException e) {
            // 42P04 = duplicate_database — already exists, nothing to do
            if (!"42P04".equals(e.getSQLState())) {
                throw new IllegalStateException("Failed to create database '" + dbName + "'", e);
            }
        }
    }
}

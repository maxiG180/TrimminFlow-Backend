package com.trimminflow.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/")
    public String root() {
        return "TrimminFlow Backend is running!";
    }

    @GetMapping("/api/health")
    public String health() {
        return "TrimminFlow Backend API is healthy!";
    }

    @GetMapping("/api/health/db")
    public Map<String, Object> databaseHealth() {
        Map<String, Object> response = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            response.put("status", "UP");
            response.put("database", "PostgreSQL");
            response.put("url", connection.getMetaData().getURL());
            response.put("driverName", connection.getMetaData().getDriverName());
            response.put("driverVersion", connection.getMetaData().getDriverVersion());
            response.put("message", "Database connection successful");
        } catch (SQLException e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("message", "Database connection failed");
        }
        return response;
    }
}

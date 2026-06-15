package com.guide.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {
    
    // Данные БД
    private static final String URL = "jdbc:postgresql://localhost:5432/dbsubmicron";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123";

    private static Connection getConnection () throws SQLException {
        // Подключение к БД
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

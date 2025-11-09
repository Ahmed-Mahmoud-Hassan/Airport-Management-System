package com.yousif.attemp2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * DatabaseConnection - Singleton class to manage database connections
 */
public class DatabaseConnection {
    // Singleton instance
    private static DatabaseConnection instance;
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/airport_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Yousifxzz@#223";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Connection object
    private Connection connection;
    
    // Private constructor for singleton pattern
    private DatabaseConnection() {
        try {
            // Load JDBC driver
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get the singleton instance of DatabaseConnection
     * @return The DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Get a connection to the database
     * @return A Connection object
     * @throws SQLException If the connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            } catch (SQLException e) {
                System.err.println("Failed to connect to database: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }
    
    /**
     * Close the database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Initialize the database by executing the SQL script
     * @return true if initialization was successful, false otherwise
     */
    public boolean initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // First try to connect to MySQL server without specifying database
            String rootUrl = "jdbc:mysql://localhost:3306/";
            conn = DriverManager.getConnection(rootUrl, DB_USER, DB_PASSWORD);
            
            // Read SQL script from file
            InputStream inputStream = getClass().getResourceAsStream("/com/yousif/attemp2/airport_management_database.sql");
            if (inputStream == null) {
                // Try loading from alternative location
                inputStream = getClass().getResourceAsStream("/airport_management_database.sql");
            }
            
            if (inputStream == null) {
                System.err.println("Could not find SQL initialization script");
                return false;
            }
            
            // Read script content
            StringBuilder scriptContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    scriptContent.append(line).append("\n");
                }
            }
            
            // Split script into separate statements
            String[] statements = scriptContent.toString().split(";");
            
            // Execute each statement
            stmt = conn.createStatement();
            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    stmt.execute(trimmedStatement + ";");
                }
            }
            
            System.out.println("Database initialized successfully");
            return true;
            
        } catch (SQLException | IOException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Close resources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Test the database connection
     * @return true if the connection is successful, false otherwise
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            boolean isValid = conn.isValid(5); // Test if connection is valid with 5 sec timeout
            return isValid;
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Execute a SELECT query and return the result set
     * @param query The SQL query to execute
     * @param params Optional parameters for the prepared statement
     * @return ResultSet containing the query results
     * @throws SQLException If there is an error executing the query
     */
    public java.sql.ResultSet executeQuery(String query, Object... params) throws SQLException {
        var conn = getConnection();
        var stmt = conn.prepareStatement(query);
        
        // Set parameters if any
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        
        return stmt.executeQuery();
    }
    
    /**
     * Execute an UPDATE, INSERT, or DELETE query
     * @param query The SQL query to execute
     * @param params Optional parameters for the prepared statement
     * @return The number of rows affected
     * @throws SQLException If there is an error executing the query
     */
    public int executeUpdate(String query, Object... params) throws SQLException {
        var conn = getConnection();
        var stmt = conn.prepareStatement(query);
        
        // Set parameters if any
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        
        return stmt.executeUpdate();
    }
} 
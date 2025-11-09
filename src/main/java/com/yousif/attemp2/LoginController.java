package com.yousif.attemp2;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * LoginController - Handles the login functionality
 */
public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label errorMessage;
    
    @FXML
    private VBox loginPanel;
    
    @FXML
    private VBox signupPanel;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField newUsernameField;
    
    @FXML
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Label signupErrorMessage;
    
    @FXML
    private ToggleButton userPortalToggle;
    
    @FXML
    private ToggleButton adminPortalToggle;
    
    // Simple in-memory user database (username -> UserData)
    private final Map<String, UserData> userCredentials = new HashMap<>();
    
    @FXML
    public void initialize() {
        // Add demo users
        userCredentials.put("admin", new UserData("admin", "admin123", "Admin User", "admin@airport.com", true));
        userCredentials.put("operator", new UserData("operator", "pass123", "Operator User", "operator@airport.com", true));
        userCredentials.put("manager", new UserData("manager", "manager456", "Manager User", "manager@airport.com", true));
        userCredentials.put("user1", new UserData("user1", "user123", "Regular User", "user1@example.com", false));
        
        // Clear any error messages
        errorMessage.setText("");
        signupErrorMessage.setText("");
        
        // Set up Enter key press to trigger login
        passwordField.setOnAction(event -> login());
        confirmPasswordField.setOnAction(event -> signUp());
    }
    
    /**
     * Handle login attempt
     */
    @FXML
    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }
        
        // Check credentials against database
        UserData userData = getUserFromDatabase(username, password);
        
        if (userData != null) {
            // Login successful - show appropriate application
            try {
                boolean isAdmin = adminPortalToggle.isSelected();
                
                if (isAdmin && !userData.isAdmin()) {
                    showError("This account doesn't have admin access");
                    return;
                }
                
                if (isAdmin) {
                    loadAdminPortal(userData);
                } else {
                    loadUserPortal(userData);
                }
                
                // Update last login time in database
                updateLastLoginTime(username);
                
            } catch (IOException e) {
                showError("Failed to load application: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Login failed
            showError("Invalid username or password");
        }
    }
    
    /**
     * Get user data from database
     * @param username The username to check
     * @param password The password to check
     * @return UserData if credentials are valid, null otherwise
     */
    private UserData getUserFromDatabase(String username, String password) {
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            try (java.sql.Connection conn = dbConnection.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "SELECT user_id, username, password, full_name, email, is_admin " +
                    "FROM users WHERE username = ?")) {
                
                stmt.setString(1, username);
                java.sql.ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    
                    // In a real app, you'd use password hashing, but for this demo we check plaintext
                    if (password.equals(storedPassword)) {
                        return new UserData(
                            rs.getString("username"),
                            storedPassword,  // We store the password in UserData for demo purposes
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getBoolean("is_admin"),
                            rs.getInt("user_id")
                        );
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
            
            // Fall back to demo users if database is unavailable
            return getFromInMemoryUsers(username, password);
        }
        
        return null;
    }
    
    /**
     * Update the last login time for a user
     * @param username The username to update
     */
    private void updateLastLoginTime(String username) {
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            try (java.sql.Connection conn = dbConnection.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET last_login = NOW() WHERE username = ?")) {
                
                stmt.setString(1, username);
                stmt.executeUpdate();
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error updating last login time: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Fallback method to check in-memory user credentials
     * This is used if the database is unavailable
     * @param username The username to check
     * @param password The password to check
     * @return UserData if credentials are valid, null otherwise
     */
    private UserData getFromInMemoryUsers(String username, String password) {
        UserData userData = userCredentials.get(username);
        if (userData != null && userData.getPassword().equals(password)) {
            return userData;
        }
        return null;
    }
    
    @FXML
    private void signUp() {
        String fullName = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validation
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showSignupError("All fields are required");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showSignupError("Passwords do not match");
            return;
        }
        
        if (isUsernameTaken(username)) {
            showSignupError("Username already exists");
            return;
        }
        
        if (!isValidEmail(email)) {
            showSignupError("Please enter a valid email address");
            return;
        }
        
        // Create new user account (regular user, not admin)
        boolean success = addUserToDatabase(username, password, fullName, email, false);
        
        if (success) {
            // Add to in-memory map as well for fallback
            userCredentials.put(username, new UserData(username, password, fullName, email, false));
            
            // Show success message
            showSignupError("Account created successfully! Please log in.");
            
            // Switch to login panel after short delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::showLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showSignupError("Failed to create account. Please try again later.");
        }
    }
    
    /**
     * Check if a username is already taken
     * @param username The username to check
     * @return true if taken, false otherwise
     */
    private boolean isUsernameTaken(String username) {
        // First check in-memory map
        if (userCredentials.containsKey(username)) {
            return true;
        }
        
        // Then check database
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            try (java.sql.Connection conn = dbConnection.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE username = ?")) {
                
                stmt.setString(1, username);
                java.sql.ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error checking if username exists: " + e.getMessage());
            e.printStackTrace();
            
            // If we can't check the database, rely on in-memory map only
            return userCredentials.containsKey(username);
        }
        
        return false;
    }
    
    /**
     * Add a new user to the database
     * @param username The username
     * @param password The password
     * @param fullName The full name
     * @param email The email address
     * @param isAdmin Whether the user is an admin
     * @return true if successful, false otherwise
     */
    private boolean addUserToDatabase(String username, String password, String fullName, String email, boolean isAdmin) {
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            try (java.sql.Connection conn = dbConnection.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, full_name, email, is_admin) " +
                    "VALUES (?, ?, ?, ?, ?)")) {
                
                stmt.setString(1, username);
                stmt.setString(2, password); // In a real app, use password hashing
                stmt.setString(3, fullName);
                stmt.setString(4, email);
                stmt.setBoolean(5, isAdmin);
                
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error adding user to database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @FXML
    private void showSignUp() {
        loginPanel.setVisible(false);
        signupPanel.setVisible(true);
        clearSignupForm();
    }
    
    @FXML
    private void showLogin() {
        loginPanel.setVisible(true);
        signupPanel.setVisible(false);
        clearLoginForm();
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    private void clearLoginForm() {
        usernameField.clear();
        passwordField.clear();
        errorMessage.setText("");
    }
    
    private void clearSignupForm() {
        nameField.clear();
        emailField.clear();
        newUsernameField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        signupErrorMessage.setText("");
    }
    
    private void showError(String message) {
        errorMessage.setText(message);
    }
    
    private void showSignupError(String message) {
        signupErrorMessage.setText(message);
    }
    
    private void loadAdminPortal(UserData userData) throws IOException {
        // Get current stage
        Stage stage = (Stage) usernameField.getScene().getWindow();
        
        // Load admin portal view (existing main-view.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent root = loader.load();
        
        // Create new scene and show
        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("styles/main.css").toExternalForm());
        
        stage.setTitle("ELkaror International Airport - Admin Portal");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        
        // Get the main controller and pass user data
        // MainController mainController = loader.getController();
        // mainController.setCurrentUser(userData);
    }
    
    private void loadUserPortal(UserData userData) throws IOException {
        // Get current stage
        Stage stage = (Stage) usernameField.getScene().getWindow();
        
        // Load user portal view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("user-portal-view.fxml"));
        Parent root = loader.load();
        
        // Create new scene and show
        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("styles/main.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/user-portal.css").toExternalForm());
        
        stage.setTitle("ELkaror International Airport - User Portal");
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.show();
        
        // Get the user portal controller and pass user data
        UserPortalController userPortalController = loader.getController();
        userPortalController.setUserData(userData);
    }
    
    /**
     * User data class to store authenticated user information
     */
    public static class UserData {
        private final String username;
        private final String password;
        private final String fullName;
        private final String email;
        private final boolean isAdmin;
        private final int userId;
        
        public UserData(String username, String password, String fullName, String email, boolean isAdmin) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.email = email;
            this.isAdmin = isAdmin;
            this.userId = -1; // Default value for in-memory users
        }
        
        public UserData(String username, String password, String fullName, String email, boolean isAdmin, int userId) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.email = email;
            this.isAdmin = isAdmin;
            this.userId = userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        public String getEmail() {
            return email;
        }
        
        public boolean isAdmin() {
            return isAdmin;
        }
        
        public int getUserId() {
            return userId;
        }
    }
} 
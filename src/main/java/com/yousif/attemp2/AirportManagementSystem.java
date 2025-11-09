package com.yousif.attemp2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AirportManagementSystem extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Show a loading indicator while we initialize the database
        ProgressIndicator progress = new ProgressIndicator();
        StackPane loadingPane = new StackPane(progress);
        Scene loadingScene = new Scene(loadingPane, 300, 200);
        stage.setScene(loadingScene);
        stage.setTitle("ELkaror International Airport - Loading");
        stage.show();
        
        // Create a background task to initialize database
        Task<Boolean> initTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // Test database connection first
                DatabaseConnection dbConnection = DatabaseConnection.getInstance();
                boolean isConnected = dbConnection.testConnection();
                
                if (!isConnected) {
                    // Try to initialize database
                    return dbConnection.initializeDatabase();
                }
                
                return true;
            }
        };
        
        // Handle successful database init
        initTask.setOnSucceeded(event -> {
            boolean success = initTask.getValue();
            
            if (success) {
                loadMainApplication(stage);
            } else {
                // Show error and exit
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, 
                        "Failed to connect to the database. The application will now exit.", 
                        ButtonType.OK);
                    alert.setTitle("Database Error");
                    alert.setHeaderText("Database Connection Failed");
                    alert.showAndWait();
                    Platform.exit();
                });
            }
        });
        
        // Handle database init failure
        initTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable exception = initTask.getException();
                Alert alert = new Alert(Alert.AlertType.ERROR, 
                    "Database initialization failed: " + exception.getMessage() + 
                    "\nThe application will continue with demo data.", 
                    ButtonType.OK);
                alert.setTitle("Database Error");
                alert.setHeaderText("Database Initialization Failed");
                alert.showAndWait();
                
                // Continue with the application anyway, using sample data
                loadMainApplication(stage);
            });
        });
        
        // Start the initialization task
        new Thread(initTask).start();
    }
    
    private void loadMainApplication(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(AirportManagementSystem.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            scene.getStylesheets().add(AirportManagementSystem.class.getResource("styles/main.css").toExternalForm());
            stage.setTitle("ELkaror International Airport - Login");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Failed to load application: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR, 
                "Failed to load application: " + e.getMessage(), ButtonType.OK);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to Load Application");
            alert.showAndWait();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
package com.yousif.attemp2;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class UserPortalController implements DataService.DataChangeListener {
    
    @FXML
    private BorderPane mainContainer;
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private VBox menuBar;
    
    @FXML
    private Button dashboardBtn;
    
    @FXML
    private Button flightInfoBtn;
    
    @FXML
    private Button bookingBtn;
    
    @FXML
    private Button checkInBtn;
    
    @FXML
    private Button baggageBtn;
    
    @FXML
    private Button waitingRoomBtn;
    
    @FXML
    private Button notificationsBtn;
    
    @FXML
    private Button servicesBtn;
    
    @FXML
    private Button logoutBtn;
    
    @FXML
    private StackPane contentArea;
    
    // Reference to the shared data service
    private final DataService dataService = DataService.getInstance();
    
    private LoginController.UserData userData;
    private Button currentActiveButton;
    private Object currentController;
    
    @FXML
    public void initialize() {
        // Register as a listener for data changes
        dataService.addDataChangeListener(this);
        
        // Set default active button and view
        setActiveButton(dashboardBtn);
        showUserDashboard();
    }
    
    /**
     * Handle data changes from the DataService
     */
    @Override
    public void onDataChanged(DataService.DataType dataType) {
        // Refresh the current view when relevant data changes
        switch (dataType) {
            case FLIGHTS:
                if (currentController instanceof UserFlightInfoController) {
                    ((UserFlightInfoController) currentController).refreshData();
                }
                break;
                
            case NOTIFICATIONS:
                // Show a notification badge or update notifications count
                notificationsBtn.setStyle("-fx-font-weight: bold; -fx-text-fill: #60519b;");
                break;
                
            case BAGGAGE:
                if (currentController instanceof UserBaggageController) {
                    ((UserBaggageController) currentController).refreshData();
                }
                break;
                
            case ALL:
                // Refresh all data in current view
                if (currentController instanceof UserFlightInfoController) {
                    ((UserFlightInfoController) currentController).refreshData();
                } else if (currentController instanceof UserBaggageController) {
                    ((UserBaggageController) currentController).refreshData();
                }
                
                // Show notification indicator
                notificationsBtn.setStyle("-fx-font-weight: bold; -fx-text-fill: #60519b;");
                break;
        }
    }
    
    public void setUserData(LoginController.UserData userData) {
        this.userData = userData;
        
        if (userData != null && userNameLabel != null) {
            userNameLabel.setText("Welcome, " + userData.getFullName());
        }
    }
    
    @FXML
    private void showUserDashboard() {
        loadView("user-dashboard-view.fxml");
        setActiveButton(dashboardBtn);
    }
    
    @FXML
    private void showFlightInfo() {
        loadView("user-flight-info-view.fxml");
        setActiveButton(flightInfoBtn);
    }
    
    @FXML
    private void showBooking() {
        loadView("user-booking-view.fxml");
        setActiveButton(bookingBtn);
    }
    
    public void showCheckIn() {
        loadView("user-checkin-view.fxml");
        setActiveButton(checkInBtn);
    }
    
    public void showBaggage() {
        loadView("user-baggage-view.fxml");
        setActiveButton(baggageBtn);
    }
    
    public void showWaitingRoom() {
        loadView("user-waiting-room-view.fxml");
        setActiveButton(waitingRoomBtn);
    }
    
    @FXML
    private void showNotifications() {
        loadView("user-notifications-view.fxml");
        setActiveButton(notificationsBtn);
        // Reset notification style after viewing
        notificationsBtn.setStyle("");
    }
    
    @FXML
    private void showServices() {
        loadView("user-services-view.fxml");
        setActiveButton(servicesBtn);
    }
    
    @FXML
    private void logout() {
        try {
            // Unregister from data service
            dataService.removeDataChangeListener(this);
            
            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("styles/main.css").toExternalForm());
            
            // Get current stage and show login
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setTitle("Airport Management System - Login");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            
            // Store current controller reference
            currentController = loader.getController();
            
            // Pass user data to the controller if it needs it
            if (currentController instanceof UserBaseController) {
                ((UserBaseController) currentController).setUserData(userData);
            }
            
            // If dashboard, set parent controller
            if (currentController instanceof UserDashboardController) {
                ((UserDashboardController) currentController).setParentController(this);
            }
            
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
            
            // Show error message in content area
            Label errorLabel = new Label("Failed to load content: " + e.getMessage());
            errorLabel.getStyleClass().add("error-message");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);
        }
    }
    
    private void setActiveButton(Button button) {
        // Reset previous active button
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active-nav-button");
        }
        
        // Set new active button
        button.getStyleClass().add("active-nav-button");
        currentActiveButton = button;
    }
} 
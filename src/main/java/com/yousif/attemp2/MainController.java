package com.yousif.attemp2;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainContainer;
    
    @FXML
    private VBox sidebarMenu;
    
    @FXML
    private Button dashboardBtn;
    
    @FXML
    private Button flightOpsBtn;
    
    @FXML
    private Button passengerBtn;
    
    @FXML
    private Button baggageBtn;
    
    @FXML
    private Button flowAnalysisBtn;
    
    @FXML
    private Button securityBtn;
    
    @FXML
    private Button boardingBtn;
    
    @FXML
    private Button simulationBtn;
    
    @FXML
    private Button logoutBtn;
    
    @FXML
    private Button adminServicesBtn;
    
    private Button currentActiveButton;

    @FXML
    public void initialize() {
        // Set dashboard as default view
        loadModule("dashboard-view.fxml");
        setActiveButton(dashboardBtn);
    }
    
    @FXML
    private void showDashboard() {
        loadModule("dashboard-view.fxml");
        setActiveButton(dashboardBtn);
    }
    
    @FXML
    private void showFlightOperations() {
        loadModule("flight-operations-view.fxml");
        setActiveButton(flightOpsBtn);
    }
    
    @FXML
    private void showPassengerCheckin() {
        loadModule("passenger-checkin-view.fxml");
        setActiveButton(passengerBtn);
    }
    
    @FXML
    private void showBaggageTracking() {
        loadModule("baggage-tracking-view.fxml");
        setActiveButton(baggageBtn);
    }
    
    @FXML
    private void showPassengerFlowAnalysis() {
        loadModule("passenger-flow-analysis-view.fxml");
        setActiveButton(flowAnalysisBtn);
    }
    
    @FXML
    private void showSecurityCheckpoint() {
        loadModule("security-checkpoint-view.fxml");
        setActiveButton(securityBtn);
    }
    
    @FXML
    private void showBoardingStatus() {
        loadModule("boarding-status-view.fxml");
        setActiveButton(boardingBtn);
    }
    
    @FXML
    private void showSimulation() {
        loadModule("simulation-view.fxml");
        setActiveButton(simulationBtn);
    }
    
    @FXML
    private void showAdminServices() {
        loadModule("admin-services-view.fxml");
        setActiveButton(adminServicesBtn);
    }
    
    @FXML
    private void logout() {
        try {
            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            scene.getStylesheets().add(getClass().getResource("styles/main.css").toExternalForm());
            
            // Get current stage and show login
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setTitle("ELkaror International Airport - Login");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadModule(String fxmlFile) {
        try {
            System.out.println("Attempting to load: " + fxmlFile);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            mainContainer.setCenter(loader.load());
            System.out.println("Successfully loaded: " + fxmlFile);
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
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
package com.yousif.attemp2;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class UserDashboardController implements UserBaseController {

    @FXML
    private VBox dashboardContainer;
    @FXML
    private VBox boardingPassCard;
    @FXML
    private VBox trackBaggageCard;
    @FXML
    private VBox waitingRoomCard;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label upcomingFlightsLabel;
    @FXML
    private Label baggageCountLabel;
    
    private UserPortalController parentController;
    private LoginController.UserData userData;

    public void setParentController(UserPortalController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        setupDashboardCards();
    }
    
    private void setupDashboardCards() {
        if (boardingPassCard != null) {
            boardingPassCard.setOnMouseClicked(e -> {
                if (parentController != null) {
                    parentController.showCheckIn();
                }
            });
        }
        
        if (trackBaggageCard != null) {
            trackBaggageCard.setOnMouseClicked(e -> {
                if (parentController != null) {
                    parentController.showBaggage();
                }
            });
        }
        
        if (waitingRoomCard != null) {
            waitingRoomCard.setOnMouseClicked(e -> {
                if (parentController != null) {
                    parentController.showWaitingRoom();
                }
            });
        }
    }
    
    private void updateDashboardInfo() {
        if (userData == null) return;
        
        // Update welcome message
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + userData.getFullName());
        }
        
        try {
            // Count upcoming flights for the user
            DatabaseConnection db = DatabaseConnection.getInstance();
            String[] nameParts = userData.getFullName().split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            
            String flightQuery = "SELECT COUNT(*) as flight_count FROM bookings b " +
                               "JOIN flights f ON b.flight_id = f.flight_id " +
                               "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                               "WHERE p.first_name = ? AND p.last_name = ? " +
                               "AND f.departure_time > NOW()";
            
            java.sql.ResultSet rs = db.executeQuery(flightQuery, firstName, lastName);
            if (rs.next() && upcomingFlightsLabel != null) {
                int flightCount = rs.getInt("flight_count");
                upcomingFlightsLabel.setText("You have " + flightCount + " upcoming flight" + 
                    (flightCount != 1 ? "s" : ""));
            }
            
            // Count baggage items for the user
            String baggageQuery = "SELECT COUNT(*) as baggage_count FROM baggage b " +
                                "JOIN bookings bk ON b.booking_id = bk.booking_id " +
                                "JOIN passengers p ON bk.passenger_id = p.passenger_id " +
                                "WHERE p.first_name = ? AND p.last_name = ?";
            
            rs = db.executeQuery(baggageQuery, firstName, lastName);
            if (rs.next() && baggageCountLabel != null) {
                int baggageCount = rs.getInt("baggage_count");
                baggageCountLabel.setText("You have " + baggageCount + " registered baggage item" + 
                    (baggageCount != 1 ? "s" : ""));
            }
            
        } catch (Exception e) {
            showError("Error updating dashboard: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void setUserData(LoginController.UserData userData) {
        this.userData = userData;
        updateDashboardInfo();
    }
} 
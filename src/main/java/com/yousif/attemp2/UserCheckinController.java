package com.yousif.attemp2;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class UserCheckinController implements UserBaseController {
    
    @FXML
    private TextField flightNumberField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private TextField bookingRefField;
    
    @FXML
    private TableView<CheckinFlight> upcomingFlightsTable;
    
    @FXML
    private TableColumn<CheckinFlight, String> flightNumberCol;
    
    @FXML
    private TableColumn<CheckinFlight, String> originCol;
    
    @FXML
    private TableColumn<CheckinFlight, String> destinationCol;
    
    @FXML
    private TableColumn<CheckinFlight, LocalDateTime> departureCol;
    
    @FXML
    private TableColumn<CheckinFlight, String> statusCol;
    
    @FXML
    private TableColumn<CheckinFlight, String> checkinStatusCol;
    
    @FXML
    private TableColumn<CheckinFlight, Void> actionsCol;
    
    private ObservableList<CheckinFlight> upcomingFlights = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private LoginController.UserData userData;
    
    @FXML
    public void initialize() {
        // Initialize table
        setupTable();
    }
    
    private void setupTable() {
        flightNumberCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        originCol.setCellValueFactory(new PropertyValueFactory<>("origin"));
        destinationCol.setCellValueFactory(new PropertyValueFactory<>("destination"));
        departureCol.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        departureCol.setCellFactory(column -> new TableCell<CheckinFlight, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<CheckinFlight, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    getStyleClass().removeAll("status-ontime", "status-delayed", "status-boarding", 
                                             "status-departed", "status-arrived", "status-cancelled");
                    getStyleClass().add("status-" + item.toLowerCase().replace(" ", ""));
                }
            }
        });
        
        checkinStatusCol.setCellValueFactory(new PropertyValueFactory<>("checkinStatus"));
        checkinStatusCol.setCellFactory(column -> new TableCell<CheckinFlight, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().removeAll("status-completed", "status-available", "status-unavailable");
                    if ("Checked In".equals(item)) {
                        getStyleClass().add("status-completed");
                    } else if ("Available".equals(item)) {
                        getStyleClass().add("status-available");
                    } else {
                        getStyleClass().add("status-unavailable");
                    }
                }
            }
        });
        
        // Action column with buttons
        actionsCol.setCellFactory(column -> new TableCell<CheckinFlight, Void>() {
            private final Button checkInButton = new Button("Check In");
            private final Button boardingPassButton = new Button("Boarding Pass");
            
            {
                checkInButton.getStyleClass().add("small-action-button");
                checkInButton.setGraphic(new FontIcon("fas-check-circle"));
                
                boardingPassButton.getStyleClass().add("small-action-button");
                boardingPassButton.setGraphic(new FontIcon("fas-ticket-alt"));
                
                checkInButton.setOnAction(event -> {
                    CheckinFlight flight = getTableView().getItems().get(getIndex());
                    performCheckin(flight);
                });
                
                boardingPassButton.setOnAction(event -> {
                    CheckinFlight flight = getTableView().getItems().get(getIndex());
                    showBoardingPass(flight);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CheckinFlight flight = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);
                    
                    if ("Available".equals(flight.getCheckinStatus())) {
                        buttons.getChildren().add(checkInButton);
                    } else if ("Checked In".equals(flight.getCheckinStatus())) {
                        buttons.getChildren().add(boardingPassButton);
                    }
                    
                    setGraphic(buttons);
                }
            }
        });
        
        // Set the items after setting up all columns
        upcomingFlightsTable.setItems(upcomingFlights);
        
        // Add debug logging
        System.out.println("Debug: Table setup completed");
        System.out.println("Debug: Number of columns: " + upcomingFlightsTable.getColumns().size());
    }
    
    private void loadSampleData() {
        // Revert to hardcoded sample data
        upcomingFlights.clear();
        upcomingFlights.addAll(
            new CheckinFlight("AA1234", "American Airlines", "New York (JFK)", "Los Angeles (LAX)",
                java.time.LocalDateTime.now().plusDays(1), java.time.LocalDateTime.now().plusDays(1).plusHours(5), "On Time", "Available", "BK123456"),
            new CheckinFlight("DL2345", "Delta Airlines", "Atlanta (ATL)", "Chicago (ORD)",
                java.time.LocalDateTime.now().plusDays(2), java.time.LocalDateTime.now().plusDays(2).plusHours(2), "Delayed", "Available", "BK234567"),
            new CheckinFlight("UA3456", "United Airlines", "Chicago (ORD)", "Denver (DEN)",
                java.time.LocalDateTime.now().plusDays(3), java.time.LocalDateTime.now().plusDays(3).plusHours(3), "Boarding", "Checked In", "BK345678"),
            new CheckinFlight("BA4567", "British Airways", "London (LHR)", "New York (JFK)",
                java.time.LocalDateTime.now().plusDays(4), java.time.LocalDateTime.now().plusDays(4).plusHours(8), "On Time", "Available", "BK456789")
        );
        upcomingFlightsTable.setItems(upcomingFlights);
    }
    
    @FXML
    private void findFlight() {
        String flightNumber = flightNumberField.getText();
        
        if (flightNumber == null || flightNumber.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a flight number");
            return;
        }
        
        // In a real application, this would query a database
        // For demo, just find the flight in our sample data
        Optional<CheckinFlight> foundFlight = upcomingFlights.stream()
            .filter(f -> f.getFlightNumber().equals(flightNumber))
            .findFirst();
            
        if (foundFlight.isPresent()) {
            // Pre-fill the form with the flight details
            lastNameField.requestFocus();
        } else {
            showAlert(Alert.AlertType.ERROR, "Flight Not Found", 
                     "No flight found with number: " + flightNumber);
        }
    }
    
    @FXML
    private void checkIn() {
        String flightNumber = flightNumberField.getText();
        String lastName = lastNameField.getText();
        String bookingRef = bookingRefField.getText();
        
        if (flightNumber.isEmpty() || lastName.isEmpty() || bookingRef.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields");
            return;
        }
        
        // In a real application, this would validate against a database
        // For demo, just find the flight in our sample data
        Optional<CheckinFlight> foundFlight = upcomingFlights.stream()
            .filter(f -> f.getFlightNumber().equals(flightNumber) && 
                        f.getBookingReference().equals(bookingRef))
            .findFirst();
            
        if (foundFlight.isPresent()) {
            CheckinFlight flight = foundFlight.get();
            
            if ("Available".equals(flight.getCheckinStatus())) {
                performCheckin(flight);
            } else if ("Checked In".equals(flight.getCheckinStatus())) {
                showAlert(Alert.AlertType.INFORMATION, "Already Checked In", 
                         "You are already checked in for this flight.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Check-in Not Available", 
                         "Online check-in is not currently available for this flight.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Invalid Details", 
                     "No matching flight found with the provided details.");
        }
    }
    
    @FXML
    private void resetForm() {
        flightNumberField.clear();
        lastNameField.clear();
        bookingRefField.clear();
        flightNumberField.requestFocus();
    }
    
    private void performCheckin(CheckinFlight flight) {
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String query = "UPDATE bookings SET booking_status = 'Checked In' " +
                          "WHERE booking_reference = ?";
            
            db.executeUpdate(query, flight.getBookingReference());
            
            // Update the UI
            flight.setCheckinStatus("Checked In");
            upcomingFlightsTable.refresh();
            
            // Show confirmation
            showAlert(Alert.AlertType.INFORMATION, "Check-in Successful", 
                     "You have successfully checked in for flight " + flight.getFlightNumber() + 
                     ".\nYou can now view or print your boarding pass.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Failed to check in: " + e.getMessage());
        }
    }
    
    private void showBoardingPass(CheckinFlight flight) {
        // In a real application, this would open a boarding pass view or generate a PDF
        showAlert(Alert.AlertType.INFORMATION, "Boarding Pass", 
                 "Boarding pass for flight " + flight.getFlightNumber() + 
                 " from " + flight.getOrigin() + " to " + flight.getDestination() + 
                 "\nDeparture: " + formatter.format(flight.getDepartureTime()) + 
                 "\n\nThis would typically show a QR code or barcode for scanning at the gate.");
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void setUserData(LoginController.UserData userData) {
        System.out.println("Debug: setUserData called with user: " + (userData != null ? userData.getFullName() : "null"));
        this.userData = userData;
        if (userData != null) {
            loadSampleData(); // Load the actual data when user data is set
        } else {
            System.out.println("Debug: userData is null, not loading flights");
        }
    }
    
    // Inner class to represent a flight with check-in status
    public static class CheckinFlight extends Flight {
        private final StringProperty checkinStatus;
        private final StringProperty bookingReference;
        
        public CheckinFlight(String flightNumber, String airline, String origin, String destination,
                            LocalDateTime departureTime, LocalDateTime arrivalTime, String status,
                            String checkinStatus, String bookingReference) {
            super(flightNumber, airline, origin, destination, departureTime, arrivalTime, status);
            this.checkinStatus = new SimpleStringProperty(checkinStatus);
            this.bookingReference = new SimpleStringProperty(bookingReference);
        }
        
        public String getCheckinStatus() {
            return checkinStatus.get();
        }
        
        public void setCheckinStatus(String value) {
            checkinStatus.set(value);
        }
        
        public StringProperty checkinStatusProperty() {
            return checkinStatus;
        }
        
        public String getBookingReference() {
            return bookingReference.get();
        }
        
        public void setBookingReference(String value) {
            bookingReference.set(value);
        }
        
        public StringProperty bookingReferenceProperty() {
            return bookingReference;
        }
        
        // Override toString for debugging
        @Override
        public String toString() {
            return String.format("Flight[number=%s, origin=%s, destination=%s, status=%s, checkinStatus=%s]",
                getFlightNumber(), getOrigin(), getDestination(), getStatus(), getCheckinStatus());
        }
    }
} 
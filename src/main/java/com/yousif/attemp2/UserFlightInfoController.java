package com.yousif.attemp2;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserFlightInfoController implements UserBaseController {
    
    @FXML
    private VBox flightStatusContainer;
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private TabPane flightTabPane;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private VBox noFlightsMessage;
    
    @FXML
    private ComboBox<String> originAirportCombo;
    
    @FXML
    private ComboBox<String> destAirportCombo;
    
    @FXML
    private TextField statusFlightField;
    
    @FXML
    private TableView<Flight> myFlightsTable;
    @FXML
    private TableColumn<Flight, String> myFlightNumberCol;
    @FXML
    private TableColumn<Flight, String> myOriginCol;
    @FXML
    private TableColumn<Flight, String> myDestinationCol;
    @FXML
    private TableColumn<Flight, String> myDepartureCol;
    @FXML
    private TableColumn<Flight, String> myArrivalCol;
    @FXML
    private TableColumn<Flight, String> myStatusCol;
    @FXML
    private TableColumn<Flight, Void> myActionsCol;

    @FXML
    private TableView<Flight> allFlightsTable;
    @FXML
    private TableColumn<Flight, String> allFlightNumberCol;
    @FXML
    private TableColumn<Flight, String> allAirlineCol;
    @FXML
    private TableColumn<Flight, String> allOriginCol;
    @FXML
    private TableColumn<Flight, String> allDestinationCol;
    @FXML
    private TableColumn<Flight, String> allDepartureCol;
    @FXML
    private TableColumn<Flight, String> allArrivalCol;
    @FXML
    private TableColumn<Flight, String> allStatusCol;
    @FXML
    private TableColumn<Flight, Void> allReserveCol;
    
    private ObservableList<Flight> flights = FXCollections.observableArrayList();
    private ObservableList<Flight> myFlights = FXCollections.observableArrayList();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
    
    // Reference to the data service
    private final DataService dataService = DataService.getInstance();
    
    private LoginController.UserData userData;
    
    @FXML
    public void initialize() {
        loadFlightData();
        
        // Setup airport dropdown lists
        setupAirportComboBoxes();
        setupTableColumns();
        setupReserveSeatButton();
        // Set table items
        allFlightsTable.setItems(flights);
        myFlightsTable.setItems(myFlights);
    }
    
    /**
     * Search for flights based on the criteria entered
     */
    @FXML
    public void searchFlights() {
        String origin = originAirportCombo.getValue();
        String destination = destAirportCombo.getValue();
        
        if (origin == null || destination == null || origin.isEmpty() || destination.isEmpty()) {
            showError("Please select both origin and destination airports");
            return;
        }
        
        // Perform search and update the table
        ObservableList<Flight> searchResults = FXCollections.observableArrayList();
        
        for (Flight flight : flights) {
            boolean matchesOrigin = flight.getOrigin().equals(origin);
            boolean matchesDestination = flight.getDestination().equals(destination);
            
            if (matchesOrigin && matchesDestination) {
                searchResults.add(flight);
            }
        }
        
        // Update the all flights table with search results
        allFlightsTable.setItems(searchResults);
        
        // Show message if no results
        if (searchResults.isEmpty()) {
            showNoFlightsMessage(true);
            showInfo("No flights found for the selected route");
        } else {
            showNoFlightsMessage(false);
        }
    }
    
    /**
     * Check status of a specific flight number
     */
    @FXML
    public void checkFlightStatus() {
        String flightNumber = statusFlightField.getText().trim();
        
        if (flightNumber.isEmpty()) {
            showError("Please enter a flight number");
            return;
        }
        
        // Find the flight in the data service
        Flight foundFlight = null;
        for (Flight flight : dataService.getFlights()) {
            if (flight.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                foundFlight = flight;
                break;
            }
        }
        
        // Update the flight status container
        flightStatusContainer.getChildren().clear();
        
        if (foundFlight != null) {
            // Create a detailed view of the flight status
            Label flightInfoLabel = new Label("Flight: " + foundFlight.getFlightNumber() + " - " + 
                foundFlight.getAirline());
            flightInfoLabel.getStyleClass().add("flight-info-title");
            
            Label routeLabel = new Label(foundFlight.getOrigin() + " → " + foundFlight.getDestination());
            
            Label departureLabel = new Label("Departure: " + formatDateTime(foundFlight.getDepartureTime()));
            Label statusLabel = new Label("Status: " + foundFlight.getStatus());
            statusLabel.getStyleClass().add("flight-status");
            
            Label gateLabel = new Label("Gate: " + foundFlight.getGate());
            Label terminalLabel = new Label("Terminal: " + foundFlight.getTerminal());
            
            flightStatusContainer.getChildren().addAll(
                flightInfoLabel, routeLabel, departureLabel, statusLabel, gateLabel, terminalLabel
            );
        } else {
            Label notFoundLabel = new Label("Flight not found: " + flightNumber);
            notFoundLabel.getStyleClass().add("error-message");
            flightStatusContainer.getChildren().add(notFoundLabel);
        }
    }
    
    /**
     * Refresh the flight data from the data service
     */
    public void refreshData() {
        loadFlightData();
    }
    
    @Override
    public void setUserData(LoginController.UserData userData) {
        this.userData = userData;
        if (userNameLabel != null && userData != null) {
            userNameLabel.setText(userData.getFullName());
        }
        // Reload myFlights for the new user
        loadFlightData();
        if (myFlightsTable != null) myFlightsTable.setItems(myFlights);
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(formatter);
    }
    
    private void loadFlightData() {
        flights.clear();
        myFlights.clear();

        ObservableList<Flight> serviceFlights = dataService.getFlights();
        flights.addAll(serviceFlights);

        if (userData != null) {
            try {
                DatabaseConnection db = DatabaseConnection.getInstance();
                // Split full name into first and last name
                String[] nameParts = userData.getFullName().split(" ", 2);
                String firstName = nameParts.length > 0 ? nameParts[0] : "";
                String lastName = nameParts.length > 1 ? nameParts[1] : "";
                // Get all flight numbers for this user
                String query = "SELECT f.flight_number FROM bookings b " +
                               "JOIN flights f ON b.flight_id = f.flight_id " +
                               "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                               "WHERE p.first_name = ? AND p.last_name = ?";
                java.sql.ResultSet rs = db.executeQuery(query, firstName, lastName);
                java.util.List<String> userFlightNumbers = new java.util.ArrayList<>();
                while (rs.next()) {
                    userFlightNumbers.add(rs.getString("flight_number"));
                }
                for (Flight flight : serviceFlights) {
                    if (userFlightNumbers.contains(flight.getFlightNumber())) {
                        myFlights.add(flight);
                    }
                }
            } catch (Exception e) {
                showError("Failed to load user's flights: " + e.getMessage());
            }
        }
        myFlightsTable.setItems(myFlights);
    }
    
    private void setupAirportComboBoxes() {
        // Add sample airport options
        ObservableList<String> airports = FXCollections.observableArrayList(
            "New York (JFK)",
            "Los Angeles (LAX)",
            "Chicago (ORD)",
            "Atlanta (ATL)",
            "Dallas (DFW)",
            "Denver (DEN)",
            "San Francisco (SFO)",
            "Seattle (SEA)",
            "Miami (MIA)",
            "London (LHR)",
            "Paris (CDG)",
            "Tokyo (HND)"
        );
        
        originAirportCombo.setItems(airports);
        destAirportCombo.setItems(airports);
    }
    
    private void showNoFlightsMessage(boolean show) {
        if (noFlightsMessage != null) {
            noFlightsMessage.setVisible(show);
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void setupTableColumns() {
        // All Flights columns
        allFlightNumberCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFlightNumber()));
        allAirlineCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAirline()));
        allOriginCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrigin()));
        allDestinationCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDestination()));
        allDepartureCol.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().getDepartureTime())));
        allArrivalCol.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().getArrivalTime())));
        allStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        // My Flights columns
        myFlightNumberCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFlightNumber()));
        myOriginCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrigin()));
        myDestinationCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDestination()));
        myDepartureCol.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().getDepartureTime())));
        myArrivalCol.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().getArrivalTime())));
        myStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        // Actions column for myFlightsTable (optional, can add buttons if needed)
    }
    
    private void setupReserveSeatButton() {
        allReserveCol.setCellFactory(col -> new TableCell<Flight, Void>() {
            private final Button reserveBtn = new Button("Reserve Seat");
            {
                reserveBtn.getStyleClass().add("primary-button");
                reserveBtn.setOnAction(event -> {
                    Flight flight = getTableView().getItems().get(getIndex());
                    showReserveSeatDialog(flight);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Flight flight = getTableView().getItems().get(getIndex());
                    if ("Available for Reservation".equals(flight.getStatus())) {
                        setGraphic(reserveBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }
    
    private void showReserveSeatDialog(Flight flight) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reserve a Seat");
        dialog.setHeaderText("Reserve a seat for flight " + flight.getFlightNumber());
        ButtonType reserveButtonType = new ButtonType("Reserve", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reserveButtonType, ButtonType.CANCEL);
        // Seat selection UI
        ComboBox<String> seatCombo = new ComboBox<>();
        // Generate seat labels (A1-F26)
        java.util.List<String> allSeats = new java.util.ArrayList<>();
        for (char row = 'A'; row <= 'F'; row++) {
            for (int col = 1; col <= 26; col++) {
                allSeats.add(row + String.valueOf(col));
            }
        }
        // Remove reserved seats
        java.util.List<String> reserved = DataService.getInstance().getReservedSeatsForFlight(flight.getFlightNumber());
        allSeats.removeAll(reserved);
        seatCombo.getItems().addAll(allSeats);
        seatCombo.setPromptText("Select Seat");
        dialog.getDialogPane().setContent(seatCombo);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reserveButtonType) {
                return seatCombo.getValue();
            }
            return null;
        });
        dialog.showAndWait().ifPresent(seat -> {
            if (seat != null && !seat.isEmpty()) {
                reserveSeatForUser(flight, seat);
            }
        });
    }
    
    private void reserveSeatForUser(Flight flight, String seat) {
        try {
            // Get user/passenger info (for demo, use userData)
            if (userData == null) {
                showError("User not logged in");
                return;
            }
            DatabaseConnection db = DatabaseConnection.getInstance();
            // Find passenger_id for this user
            String[] nameParts = userData.getFullName().split(" ", 2);
            String firstName = nameParts.length > 0 ? nameParts[0] : "";
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            String findPassenger = "SELECT passenger_id FROM passengers WHERE first_name = ? AND last_name = ?";
            java.sql.ResultSet rs = db.executeQuery(findPassenger, firstName, lastName);
            int passengerId = -1;
            if (rs.next()) {
                passengerId = rs.getInt("passenger_id");
            }
            if (passengerId == -1) {
                showError("Passenger record not found for user");
                return;
            }
            // Find flight_id
            String findFlight = "SELECT flight_id FROM flights WHERE flight_number = ?";
            rs = db.executeQuery(findFlight, flight.getFlightNumber());
            int flightId = -1;
            if (rs.next()) {
                flightId = rs.getInt("flight_id");
            }
            if (flightId == -1) {
                showError("Flight not found");
                return;
            }
            // Insert booking
            String insertBooking = "INSERT INTO bookings (booking_reference, passenger_id, flight_id, seat_number, booking_status) VALUES (?, ?, ?, ?, 'Confirmed')";
            String bookingRef = "BR" + System.currentTimeMillis();
            db.executeUpdate(insertBooking, bookingRef, passengerId, flightId, seat);
            showInfo("Seat reserved successfully!\nSeat: " + seat + "\nBooking Reference: " + bookingRef);
        } catch (Exception e) {
            showError("Failed to reserve seat: " + e.getMessage());
        }
    }
} 
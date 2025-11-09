package com.yousif.attemp2;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class UserBookingController implements UserBaseController {
    
    @FXML private ComboBox<String> originAirportCombo;
    @FXML private ComboBox<String> destAirportCombo;
    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, String> flightNumberCol;
    @FXML private TableColumn<Flight, String> airlineCol;
    @FXML private TableColumn<Flight, String> originCol;
    @FXML private TableColumn<Flight, String> destinationCol;
    @FXML private TableColumn<Flight, String> departureCol;
    @FXML private TableColumn<Flight, String> arrivalCol;
    @FXML private TableColumn<Flight, String> priceCol;
    @FXML private TableColumn<Flight, Void> actionsCol;
    
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, String> bookingRefCol;
    @FXML private TableColumn<Booking, String> bookedFlightCol;
    @FXML private TableColumn<Booking, String> bookingDateCol;
    @FXML private TableColumn<Booking, String> seatCol;
    @FXML private TableColumn<Booking, String> statusCol;
    @FXML private TableColumn<Booking, Void> bookingActionsCol;
    
    private ObservableList<Flight> flights = FXCollections.observableArrayList();
    private ObservableList<Booking> bookings = FXCollections.observableArrayList();
    private final DataService dataService = DataService.getInstance();
    private LoginController.UserData userData;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    @FXML
    public void initialize() {
        setupAirportComboBoxes();
        setupFlightsTable();
        setupBookingsTable();
        
        // Load initial flight data
        loadFlightData();
        flightsTable.setItems(flights);
    }
    
    private void setupAirportComboBoxes() {
        ObservableList<String> airports = FXCollections.observableArrayList(
            "New York (JFK)", "Los Angeles (LAX)", "Chicago (ORD)", "Atlanta (ATL)",
            "Dallas (DFW)", "Denver (DEN)", "San Francisco (SFO)", "Seattle (SEA)",
            "Miami (MIA)", "London (LHR)", "Paris (CDG)", "Tokyo (HND)"
        );
        originAirportCombo.setItems(airports);
        destAirportCombo.setItems(airports);
    }
    
    private void setupFlightsTable() {
        flightNumberCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFlightNumber()));
        airlineCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAirline()));
        originCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrigin()));
        destinationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDestination()));
        departureCol.setCellValueFactory(data -> new SimpleStringProperty(formatter.format(data.getValue().getDepartureTime())));
        arrivalCol.setCellValueFactory(data -> new SimpleStringProperty(formatter.format(data.getValue().getArrivalTime())));
        priceCol.setCellValueFactory(data -> new SimpleStringProperty("$" + data.getValue().getPrice()));
        
        actionsCol.setCellFactory(col -> new TableCell<Flight, Void>() {
            private final Button bookButton = new Button("Book");
            {
                bookButton.getStyleClass().add("primary-button");
                bookButton.setGraphic(new FontIcon("fas-ticket-alt"));
                bookButton.setOnAction(e -> {
                    Flight flight = getTableView().getItems().get(getIndex());
                    bookFlight(flight);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(bookButton);
                }
            }
        });
        
        flightsTable.setItems(flights);
    }
    
    private void setupBookingsTable() {
        bookingRefCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookingRef()));
        bookedFlightCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFlightNumber()));
        bookingDateCol.setCellValueFactory(data -> new SimpleStringProperty(formatter.format(data.getValue().getBookingDate())));
        seatCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSeat()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        
        bookingActionsCol.setCellFactory(col -> new TableCell<Booking, Void>() {
            private final Button downloadButton = new Button("Download Ticket");
            {
                downloadButton.getStyleClass().add("secondary-button");
                downloadButton.setGraphic(new FontIcon("fas-download"));
                downloadButton.setOnAction(e -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    downloadTicket(booking);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Booking booking = getTableView().getItems().get(getIndex());
                    if ("Confirmed".equals(booking.getStatus())) {
                        setGraphic(downloadButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        bookingsTable.setItems(bookings);
    }
    
    @FXML
    private void searchFlights() {
        String origin = originAirportCombo.getValue();
        String destination = destAirportCombo.getValue();
        
        if (origin == null || destination == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select both origin and destination airports");
            return;
        }
        
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String query = "SELECT * FROM flights WHERE origin = ? AND destination = ? AND status IN ('On Time', 'Available for Reservation', 'Boarding')";
            java.sql.ResultSet rs = db.executeQuery(query, origin, destination);
            
            flights.clear();
            while (rs.next()) {
                Flight flight = new Flight(
                    rs.getString("flight_number"),
                    rs.getString("airline"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getTimestamp("departure_time").toLocalDateTime(),
                    rs.getTimestamp("arrival_time").toLocalDateTime(),
                    rs.getString("status")
                );
                flight.setGate(rs.getString("gate"));
                flight.setTerminal(rs.getString("terminal"));
                flights.add(flight);
            }
            
            flightsTable.setItems(flights);
            
            if (flights.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Flights", 
                         "No flights found for the selected route");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Failed to search flights: " + e.getMessage());
        }
    }
    
    private void bookFlight(Flight flight) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Seat");
        dialog.setHeaderText("Book Flight " + flight.getFlightNumber());
        
        ButtonType bookButtonType = new ButtonType("Book", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bookButtonType, ButtonType.CANCEL);
        
        ComboBox<String> seatCombo = new ComboBox<>();
        java.util.List<String> reservedSeats = getReservedSeatsForFlight(flight.getFlightNumber());
        java.util.List<String> availableSeats = new java.util.ArrayList<>();
        
        // Generate available seats (A1-F26)
        for (char row = 'A'; row <= 'F'; row++) {
            for (int col = 1; col <= 26; col++) {
                String seat = row + String.valueOf(col);
                if (!reservedSeats.contains(seat)) {
                    availableSeats.add(seat);
                }
            }
        }
        
        seatCombo.getItems().addAll(availableSeats);
        seatCombo.setPromptText("Select Seat");
        
        dialog.getDialogPane().setContent(seatCombo);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == bookButtonType) {
                return seatCombo.getValue();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(seat -> {
            try {
                String bookingRef = createBooking(flight, seat);
                if (bookingRef != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                             "Flight booked successfully!\nBooking Reference: " + bookingRef);
                    // Refresh both tables
                    loadBookings();
                    loadFlightData();
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "Failed to book flight: " + e.getMessage());
            }
        });
    }
    
    private java.util.List<String> getReservedSeatsForFlight(String flightNumber) {
        java.util.List<String> reservedSeats = new java.util.ArrayList<>();
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String query = "SELECT b.seat_number FROM bookings b " +
                         "JOIN flights f ON b.flight_id = f.flight_id " +
                         "WHERE f.flight_number = ? AND b.booking_status != 'Cancelled'";
            java.sql.ResultSet rs = db.executeQuery(query, flightNumber);
            while (rs.next()) {
                reservedSeats.add(rs.getString("seat_number"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reservedSeats;
    }
    
    private String createBooking(Flight flight, String seat) {
        if (userData == null) return null;
        
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String[] nameParts = userData.getFullName().split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            
            // First, ensure we have a passenger record
            String findPassenger = "SELECT passenger_id FROM passengers WHERE user_id = ?";
            java.sql.ResultSet rs = db.executeQuery(findPassenger, userData.getUserId());
            int passengerId = -1;
            
            if (rs.next()) {
                passengerId = rs.getInt("passenger_id");
            } else {
                // Create passenger record if it doesn't exist
                String insertPassenger = "INSERT INTO passengers (user_id, first_name, last_name) VALUES (?, ?, ?)";
                db.executeUpdate(insertPassenger, userData.getUserId(), firstName, lastName);
                
                // Get the new passenger_id
                rs = db.executeQuery(findPassenger, userData.getUserId());
                if (rs.next()) {
                    passengerId = rs.getInt("passenger_id");
                }
            }
            
            if (passengerId == -1) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create or find passenger record");
                return null;
            }
            
            // Get flight_id
            String findFlight = "SELECT flight_id FROM flights WHERE flight_number = ?";
            rs = db.executeQuery(findFlight, flight.getFlightNumber());
            int flightId = -1;
            if (rs.next()) {
                flightId = rs.getInt("flight_id");
            }
            if (flightId == -1) {
                showAlert(Alert.AlertType.ERROR, "Error", "Flight not found");
                return null;
            }
            
            // Create booking reference (format: BK + 6 digits)
            String bookingRef = String.format("BK%06d", (int)(Math.random() * 1000000));
            
            // Insert booking
            String insertBooking = "INSERT INTO bookings (booking_reference, passenger_id, flight_id, seat_number, booking_status) VALUES (?, ?, ?, ?, 'Confirmed')";
            db.executeUpdate(insertBooking, bookingRef, passengerId, flightId, seat);
            
            // Refresh the bookings table
            loadBookings();
            
            return bookingRef;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create booking: " + e.getMessage());
            return null;
        }
    }
    
    private void downloadTicket(Booking booking) {
        try {
            String fileName = "ticket_" + booking.getBookingRef() + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(fileName));
            
            // Write ticket details
            writer.println("=== Flight Ticket ===");
            writer.println();
            writer.println("Booking Reference: " + booking.getBookingRef());
            writer.println("Flight: " + booking.getFlightNumber());
            writer.println("Passenger: " + userData.getFullName());
            writer.println("Seat: " + booking.getSeat());
            writer.println("Status: " + booking.getStatus());
            writer.println("Booking Date: " + formatter.format(booking.getBookingDate()));
            
            writer.close();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                     "Ticket downloaded successfully!\nFile: " + fileName);
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Failed to generate ticket: " + e.getMessage());
        }
    }
    
    private void loadBookings() {
        bookings.clear();
        if (userData == null) return;
        
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String[] nameParts = userData.getFullName().split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            
            String query = "SELECT b.*, f.flight_number FROM bookings b " +
                         "JOIN flights f ON b.flight_id = f.flight_id " +
                         "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                         "WHERE p.first_name = ? AND p.last_name = ? " +
                         "ORDER BY b.created_at DESC";
            
            java.sql.ResultSet rs = db.executeQuery(query, firstName, lastName);
            while (rs.next()) {
                Booking booking = new Booking(
                    rs.getString("booking_reference"),
                    rs.getString("flight_number"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getString("seat_number"),
                    rs.getString("booking_status")
                );
                bookings.add(booking);
            }
            
            bookingsTable.setItems(bookings);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Failed to load bookings: " + e.getMessage());
        }
    }
    
    private void loadFlightData() {
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String query = "SELECT * FROM flights WHERE status IN ('On Time', 'Available for Reservation', 'Boarding')";
            java.sql.ResultSet rs = db.executeQuery(query);
            
            flights.clear();
            while (rs.next()) {
                Flight flight = new Flight(
                    rs.getString("flight_number"),
                    rs.getString("airline"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getTimestamp("departure_time").toLocalDateTime(),
                    rs.getTimestamp("arrival_time").toLocalDateTime(),
                    rs.getString("status")
                );
                flight.setGate(rs.getString("gate"));
                flight.setTerminal(rs.getString("terminal"));
                flights.add(flight);
            }
            
            flightsTable.setItems(flights);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Failed to load flights: " + e.getMessage());
        }
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
        this.userData = userData;
        loadBookings();
    }
    
    // Inner class to represent a booking
    public static class Booking {
        private final String bookingRef;
        private final String flightNumber;
        private final LocalDateTime bookingDate;
        private final String seat;
        private final String status;
        
        public Booking(String bookingRef, String flightNumber, LocalDateTime bookingDate,
                      String seat, String status) {
            this.bookingRef = bookingRef;
            this.flightNumber = flightNumber;
            this.bookingDate = bookingDate;
            this.seat = seat;
            this.status = status;
        }
        
        public String getBookingRef() { return bookingRef; }
        public String getFlightNumber() { return flightNumber; }
        public LocalDateTime getBookingDate() { return bookingDate; }
        public String getSeat() { return seat; }
        public String getStatus() { return status; }
    }
} 
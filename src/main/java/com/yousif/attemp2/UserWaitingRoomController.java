package com.yousif.attemp2;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class UserWaitingRoomController implements UserBaseController {
    
    @FXML
    private TextField flightNumberField;
    
    @FXML
    private ComboBox<String> terminalCombo;
    
    @FXML
    private ComboBox<String> roomTypeCombo;
    
    @FXML
    private ComboBox<Integer> guestsCombo;
    
    @FXML
    private TableView<WaitingRoom> availableRoomsTable;
    
    @FXML
    private TableColumn<WaitingRoom, String> roomTypeCol;
    
    @FXML
    private TableColumn<WaitingRoom, String> terminalCol;
    
    @FXML
    private TableColumn<WaitingRoom, Integer> capacityCol;
    
    @FXML
    private TableColumn<WaitingRoom, String> availabilityCol;
    
    @FXML
    private TableColumn<WaitingRoom, String> priceCol;
    
    @FXML
    private TableColumn<WaitingRoom, Void> roomActionsCol;
    
    @FXML
    private TableView<Reservation> myReservationsTable;
    
    @FXML
    private TableColumn<Reservation, String> reservationIdCol;
    
    @FXML
    private TableColumn<Reservation, String> resRoomTypeCol;
    
    @FXML
    private TableColumn<Reservation, String> resTerminalCol;
    
    @FXML
    private TableColumn<Reservation, String> flightCol;
    
    @FXML
    private TableColumn<Reservation, LocalDate> dateCol;
    
    @FXML
    private TableColumn<Reservation, Integer> guestsCol;
    
    @FXML
    private TableColumn<Reservation, String> statusCol;
    
    @FXML
    private TableColumn<Reservation, Void> resActionsCol;
    
    private ObservableList<WaitingRoom> availableRooms = FXCollections.observableArrayList();
    private ObservableList<Reservation> myReservations = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    @FXML
    public void initialize() {
        // Initialize dropdowns
        terminalCombo.setItems(FXCollections.observableArrayList(
            "Terminal 1", "Terminal 2", "Terminal 3", "Terminal 4", "Terminal 5"
        ));
        
        roomTypeCombo.setItems(FXCollections.observableArrayList(
            "Standard Lounge", "Business Lounge", "Premium Lounge", "Family Room", "Quiet Zone"
        ));
        
        guestsCombo.setItems(FXCollections.observableArrayList(
            1, 2, 3, 4, 5, 6
        ));
        
        // Initialize tables
        setupAvailableRoomsTable();
        setupMyReservationsTable();
        
        // Load sample data
        loadSampleData();
    }
    
    private void setupAvailableRoomsTable() {
        roomTypeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        terminalCol.setCellValueFactory(new PropertyValueFactory<>("terminal"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        availabilityCol.setCellValueFactory(new PropertyValueFactory<>("availability"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        // Action column with buttons
        roomActionsCol.setCellFactory(column -> new TableCell<WaitingRoom, Void>() {
            private final Button reserveButton = new Button("Reserve");
            
            {
                reserveButton.getStyleClass().add("small-action-button");
                reserveButton.setGraphic(new FontIcon("fas-calendar-plus"));
                
                reserveButton.setOnAction(event -> {
                    WaitingRoom room = getTableView().getItems().get(getIndex());
                    reserveRoom(room);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    WaitingRoom room = getTableView().getItems().get(getIndex());
                    if ("Available".equals(room.getAvailability())) {
                        setGraphic(reserveButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        availableRoomsTable.setItems(availableRooms);
    }
    
    private void setupMyReservationsTable() {
        reservationIdCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        resRoomTypeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        resTerminalCol.setCellValueFactory(new PropertyValueFactory<>("terminal"));
        flightCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(column -> new TableCell<Reservation, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        guestsCol.setCellValueFactory(new PropertyValueFactory<>("guests"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Action column with buttons
        resActionsCol.setCellFactory(column -> new TableCell<Reservation, Void>() {
            private final Button viewButton = new Button("View");
            private final Button cancelButton = new Button("Cancel");
            
            {
                viewButton.getStyleClass().add("small-action-button");
                viewButton.setGraphic(new FontIcon("fas-eye"));
                
                cancelButton.getStyleClass().add("small-action-button");
                cancelButton.setGraphic(new FontIcon("fas-times"));
                
                viewButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    viewReservation(reservation);
                });
                
                cancelButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    cancelReservation(reservation);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);
                    buttons.getChildren().add(viewButton);
                    
                    if (!"Completed".equals(reservation.getStatus())) {
                        buttons.getChildren().add(cancelButton);
                    }
                    
                    setGraphic(buttons);
                }
            }
        });
        
        myReservationsTable.setItems(myReservations);
    }
    
    private void loadSampleData() {
        // Sample data for available rooms
        availableRooms.add(new WaitingRoom("Standard Lounge", "Terminal 1", 20, "Available", "$25"));
        availableRooms.add(new WaitingRoom("Business Lounge", "Terminal 2", 15, "Available", "$45"));
        availableRooms.add(new WaitingRoom("Premium Lounge", "Terminal 3", 10, "Limited", "$75"));
        availableRooms.add(new WaitingRoom("Family Room", "Terminal 1", 6, "Available", "$35"));
        availableRooms.add(new WaitingRoom("Quiet Zone", "Terminal 4", 8, "Full", "$40"));
        
        // Sample data for my reservations
        myReservations.add(new Reservation("RES123", "Business Lounge", "Terminal 2", "BA123", 
                           LocalDate.now().plusDays(2), 2, "Confirmed"));
        myReservations.add(new Reservation("RES456", "Family Room", "Terminal 1", "EK432", 
                           LocalDate.now().minusDays(5), 4, "Completed"));
    }
    
    @FXML
    private void checkAvailability() {
        String terminal = terminalCombo.getValue();
        String roomType = roomTypeCombo.getValue();
        
        if (terminal == null && roomType == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select at least one search criteria");
            return;
        }
        
        // In a real application, this would query a database
        // For demo, just filter the available rooms list
        ObservableList<WaitingRoom> filteredRooms = FXCollections.observableArrayList();
        
        for (WaitingRoom room : availableRooms) {
            boolean matches = true;
            
            if (terminal != null && !room.getTerminal().equals(terminal)) {
                matches = false;
            }
            
            if (roomType != null && !room.getRoomType().equals(roomType)) {
                matches = false;
            }
            
            if (matches) {
                filteredRooms.add(room);
            }
        }
        
        availableRoomsTable.setItems(filteredRooms);
    }
    
    @FXML
    private void makeReservation() {
        String flightNumber = flightNumberField.getText();
        String terminal = terminalCombo.getValue();
        String roomType = roomTypeCombo.getValue();
        Integer guests = guestsCombo.getValue();
        
        if (flightNumber == null || flightNumber.isEmpty() || terminal == null || 
            roomType == null || guests == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields");
            return;
        }
        
        // In a real application, this would create a reservation in the database
        // For demo, just find the room in our sample data
        Optional<WaitingRoom> foundRoom = availableRooms.stream()
            .filter(r -> r.getTerminal().equals(terminal) && 
                        r.getRoomType().equals(roomType) &&
                        "Available".equals(r.getAvailability()))
            .findFirst();
            
        if (foundRoom.isPresent()) {
            WaitingRoom room = foundRoom.get();
            
            // Check if the room can accommodate the guests
            if (room.getCapacity() < guests) {
                showAlert(Alert.AlertType.ERROR, "Capacity Exceeded", 
                         "This room cannot accommodate " + guests + " guests. Maximum capacity is " + 
                         room.getCapacity() + ".");
                return;
            }
            
            // Create a new reservation
            String reservationId = "RES" + (1000 + myReservations.size());
            Reservation newReservation = new Reservation(reservationId, roomType, terminal, 
                                                       flightNumber, LocalDate.now().plusDays(1), 
                                                       guests, "Confirmed");
            myReservations.add(newReservation);
            
            // Show confirmation
            showAlert(Alert.AlertType.INFORMATION, "Reservation Confirmed", 
                     "Your waiting room has been reserved.\nReservation ID: " + reservationId);
            
            // Reset form
            flightNumberField.clear();
            terminalCombo.setValue(null);
            roomTypeCombo.setValue(null);
            guestsCombo.setValue(null);
        } else {
            showAlert(Alert.AlertType.ERROR, "Room Not Available", 
                     "No available room matches your criteria.");
        }
    }
    
    private void reserveRoom(WaitingRoom room) {
        // Pre-fill the form with the selected room details
        terminalCombo.setValue(room.getTerminal());
        roomTypeCombo.setValue(room.getRoomType());
        
        // Focus on the flight number field
        flightNumberField.requestFocus();
        
        showAlert(Alert.AlertType.INFORMATION, "Room Selected", 
                 "Please complete the reservation form with your flight details.");
    }
    
    private void viewReservation(Reservation reservation) {
        // In a real application, this would show detailed reservation information
        showAlert(Alert.AlertType.INFORMATION, "Reservation Details", 
                 "Reservation ID: " + reservation.getReservationId() + 
                 "\nRoom Type: " + reservation.getRoomType() + 
                 "\nTerminal: " + reservation.getTerminal() + 
                 "\nFlight: " + reservation.getFlightNumber() + 
                 "\nDate: " + formatter.format(reservation.getDate()) + 
                 "\nGuests: " + reservation.getGuests() + 
                 "\nStatus: " + reservation.getStatus());
    }
    
    private void cancelReservation(Reservation reservation) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Reservation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to cancel this reservation?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // In a real application, this would update the database
            myReservations.remove(reservation);
            
            showAlert(Alert.AlertType.INFORMATION, "Reservation Cancelled", 
                     "Your reservation has been cancelled successfully.");
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
        // userData parameter is currently unused
        // Update UI based on user data if needed
    }
    
    // Inner class to represent a waiting room
    public static class WaitingRoom {
        private final StringProperty roomType;
        private final StringProperty terminal;
        private final ObjectProperty<Integer> capacity;
        private final StringProperty availability;
        private final StringProperty price;
        
        public WaitingRoom(String roomType, String terminal, int capacity, String availability, String price) {
            this.roomType = new SimpleStringProperty(roomType);
            this.terminal = new SimpleStringProperty(terminal);
            this.capacity = new SimpleObjectProperty<>(capacity);
            this.availability = new SimpleStringProperty(availability);
            this.price = new SimpleStringProperty(price);
        }
        
        public String getRoomType() {
            return roomType.get();
        }
        
        public StringProperty roomTypeProperty() {
            return roomType;
        }
        
        public String getTerminal() {
            return terminal.get();
        }
        
        public StringProperty terminalProperty() {
            return terminal;
        }
        
        public int getCapacity() {
            return capacity.get();
        }
        
        public ObjectProperty<Integer> capacityProperty() {
            return capacity;
        }
        
        public String getAvailability() {
            return availability.get();
        }
        
        public StringProperty availabilityProperty() {
            return availability;
        }
        
        public String getPrice() {
            return price.get();
        }
        
        public StringProperty priceProperty() {
            return price;
        }
    }
    
    // Inner class to represent a reservation
    public static class Reservation {
        private final StringProperty reservationId;
        private final StringProperty roomType;
        private final StringProperty terminal;
        private final StringProperty flightNumber;
        private final ObjectProperty<LocalDate> date;
        private final ObjectProperty<Integer> guests;
        private final StringProperty status;
        
        public Reservation(String reservationId, String roomType, String terminal, String flightNumber,
                          LocalDate date, int guests, String status) {
            this.reservationId = new SimpleStringProperty(reservationId);
            this.roomType = new SimpleStringProperty(roomType);
            this.terminal = new SimpleStringProperty(terminal);
            this.flightNumber = new SimpleStringProperty(flightNumber);
            this.date = new SimpleObjectProperty<>(date);
            this.guests = new SimpleObjectProperty<>(guests);
            this.status = new SimpleStringProperty(status);
        }
        
        public String getReservationId() {
            return reservationId.get();
        }
        
        public StringProperty reservationIdProperty() {
            return reservationId;
        }
        
        public String getRoomType() {
            return roomType.get();
        }
        
        public StringProperty roomTypeProperty() {
            return roomType;
        }
        
        public String getTerminal() {
            return terminal.get();
        }
        
        public StringProperty terminalProperty() {
            return terminal;
        }
        
        public String getFlightNumber() {
            return flightNumber.get();
        }
        
        public StringProperty flightNumberProperty() {
            return flightNumber;
        }
        
        public LocalDate getDate() {
            return date.get();
        }
        
        public ObjectProperty<LocalDate> dateProperty() {
            return date;
        }
        
        public int getGuests() {
            return guests.get();
        }
        
        public ObjectProperty<Integer> guestsProperty() {
            return guests;
        }
        
        public String getStatus() {
            return status.get();
        }
        
        public StringProperty statusProperty() {
            return status;
        }
    }
} 
package com.yousif.attemp2;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;

public class FlightOperationsController {

    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private ComboBox<String> terminalFilter;
    
    @FXML
    private Label totalFlightsLabel;
    
    @FXML
    private TableView<Flight> flightTable;
    
    @FXML
    private TableColumn<Flight, String> flightNumberColumn;
    
    @FXML
    private TableColumn<Flight, String> airlineColumn;
    
    @FXML
    private TableColumn<Flight, String> destinationColumn;
    
    @FXML
    private TableColumn<Flight, String> departureColumn;
    
    @FXML
    private TableColumn<Flight, String> statusColumn;
    
    @FXML
    private TableColumn<Flight, String> terminalColumn;
    
    @FXML
    private TableColumn<Flight, String> gateColumn;
    
    @FXML
    private TableColumn<Flight, Flight> actionsColumn;
    
    @FXML
    private Label totalGatesValue;
    
    @FXML
    private Label availableGatesValue;
    
    @FXML
    private Label occupiedGatesValue;
    
    @FXML
    private Label onTimeCountLabel;
    
    @FXML
    private Label delayedCountLabel;
    
    @FXML
    private Label boardingCountLabel;
    
    @FXML
    private Label departedCountLabel;
    
    @FXML
    private Label cancelledCountLabel;
    
    @FXML
    private Label onTimeLabel;
    
    @FXML
    private Label delayedLabel;
    
    @FXML
    private Label boardingLabel;
    
    @FXML
    private Label departedLabel;
    
    @FXML
    private Label cancelledLabel;
    
    // Reference to the shared data service
    private final DataService dataService = DataService.getInstance();
    
    // Add real-time status updates
    private Timeline statusTimeline;
    
    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        loadFlightData();
        updateGateStatistics();
        startRealTimeStatusUpdates();
    }
    
    private void setupFilters() {
        // Setup status filter options
        statusFilter.getItems().addAll("All", "Available for Reservation", "On Time", "Delayed", "Boarding", "Departed", "Cancelled");
        statusFilter.setValue("All");
        
        // Setup terminal filter options
        terminalFilter.getItems().addAll("All", "Terminal A", "Terminal B", "Terminal C", "Terminal D");
        terminalFilter.setValue("All");
    }
    
    private void setupTableColumns() {
        flightNumberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFlightNumber()));
        airlineColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAirline()));
        destinationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDestination()));
        departureColumn.setCellValueFactory(cellData -> new SimpleStringProperty(getDeparture(cellData.getValue())));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        terminalColumn.setCellValueFactory(cellData -> new SimpleStringProperty(getTerminal(cellData.getValue())));
        gateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGate()));
        
        // Setup actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button detailsBtn = new Button();
            private final Button editBtn = new Button();
            private final HBox buttonsBox = new HBox(5, detailsBtn, editBtn);
            
            {
                detailsBtn.setGraphic(new FontIcon("fas-info-circle"));
                editBtn.setGraphic(new FontIcon("fas-edit"));
                
                detailsBtn.getStyleClass().add("table-button");
                editBtn.getStyleClass().add("table-button");
                
                buttonsBox.setAlignment(Pos.CENTER);
                
                detailsBtn.setOnAction(event -> {
                    Flight flight = getTableView().getItems().get(getIndex());
                    showFlightDetailsDialog(flight);
                });
                
                editBtn.setOnAction(event -> {
                    Flight flight = getTableView().getItems().get(getIndex());
                    showEditDialog(flight);
                });
            }
            
            @Override
            protected void updateItem(Flight flight, boolean empty) {
                super.updateItem(flight, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }
    
    private void loadFlightData() {
        // Use the shared data service instead of local data
        flightTable.setItems(dataService.getFlights());
        
        // If data service is empty, add sample data
        if (dataService.getFlights().isEmpty()) {
            addSampleFlights();
        }
        
        totalFlightsLabel.setText("Total: " + flightTable.getItems().size() + " flights");
    }
    
    private void addSampleFlights() {
        // Removed unused variable: DateTimeFormatter timeFormatter;
        
        // Create sample flights using the current time + offsets
        LocalDateTime now = LocalDateTime.now();
        
        dataService.addFlight(new Flight("AA1234", "American Airlines", "New York (JFK)", "Los Angeles (LAX)", 
            now.plusMinutes(30), now.plusHours(6), "On Time"));
        
        dataService.addFlight(new Flight("DL2345", "Delta Airlines", "Atlanta (ATL)", "Chicago (ORD)", 
            now.plusMinutes(45), now.plusHours(2), "Delayed"));
            
        dataService.addFlight(new Flight("UA3456", "United Airlines", "Chicago (ORD)", "Denver (DEN)", 
            now.plusHours(1), now.plusHours(3), "Boarding"));
            
        dataService.addFlight(new Flight("BA4567", "British Airways", "London (LHR)", "New York (JFK)", 
            now.plusHours(2), now.plusHours(10), "On Time"));
    }
    
    private void updateGateStatistics() {
        int total = 45;
        int occupied = dataService.getFlights().size();
        int available = total - occupied;
        
        totalGatesValue.setText(String.valueOf(total));
        occupiedGatesValue.setText(String.valueOf(occupied));
        availableGatesValue.setText(String.valueOf(available));
    }
    
    @FXML
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();
        String terminal = terminalFilter.getValue();
        
        ObservableList<Flight> filteredData = FXCollections.observableArrayList();
        
        for (Flight flight : dataService.getFlights()) {
            boolean matchesSearch = searchText.isEmpty() || 
                flight.getFlightNumber().toLowerCase().contains(searchText) || 
                flight.getDestination().toLowerCase().contains(searchText) ||
                flight.getAirline().toLowerCase().contains(searchText);
                
            boolean matchesStatus = "All".equals(status) || flight.getStatus().equals(status);
            boolean matchesTerminal = "All".equals(terminal) || flight.getTerminal().equals(terminal.substring(terminal.length() - 1));
            
            if (matchesSearch && matchesStatus && matchesTerminal) {
                filteredData.add(flight);
            }
        }
        
        flightTable.setItems(filteredData);
        totalFlightsLabel.setText("Total: " + filteredData.size() + " flights");
    }
    
    private void showFlightDetailsDialog(Flight flight) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Flight Details");
        alert.setHeaderText("Flight " + flight.getFlightNumber() + " to " + flight.getDestination());
        
        // Create content for the dialog
        StringBuilder content = new StringBuilder();
        content.append("Airline: ").append(flight.getAirline()).append("\n");
        content.append("Departure Time: ").append(flight.getDeparture()).append("\n");
        content.append("Status: ").append(flight.getStatus()).append("\n");
        content.append("Terminal: ").append(flight.getTerminal()).append("\n");
        content.append("Gate: ").append(flight.getGate()).append("\n");
        content.append("Aircraft: Boeing 787-9").append("\n");
        content.append("Capacity: 290 passengers").append("\n");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    @FXML
    public void addNewFlight() {
        try {
            Dialog<Flight> dialog = new Dialog<>();
            dialog.setTitle("Add New Flight");
            dialog.setHeaderText("Enter flight details");
            
            // Create the custom dialog
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            TextField flightNumber = new TextField();
            TextField airline = new TextField();
            TextField origin = new TextField();
            TextField destination = new TextField();
            DatePicker departureDate = new DatePicker();
            TextField departureTime = new TextField();
            TextField gate = new TextField();
            ComboBox<String> terminal = new ComboBox<>();
            terminal.getItems().addAll("A", "B", "C", "D");
            TextField priceField = new TextField();
            priceField.setPromptText("Enter price (e.g. 299.99)");
            
            grid.add(new Label("Flight Number:"), 0, 0);
            grid.add(flightNumber, 1, 0);
            grid.add(new Label("Airline:"), 0, 1);
            grid.add(airline, 1, 1);
            grid.add(new Label("Origin:"), 0, 2);
            grid.add(origin, 1, 2);
            grid.add(new Label("Destination:"), 0, 3);
            grid.add(destination, 1, 3);
            grid.add(new Label("Departure Date:"), 0, 4);
            grid.add(departureDate, 1, 4);
            grid.add(new Label("Departure Time (HH:mm):"), 0, 5);
            grid.add(departureTime, 1, 5);
            grid.add(new Label("Gate:"), 0, 6);
            grid.add(gate, 1, 6);
            grid.add(new Label("Terminal:"), 0, 7);
            grid.add(terminal, 1, 7);
            grid.add(new Label("Price:"), 0, 8);
            grid.add(priceField, 1, 8);
            
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    try {
                        // Parse departure time
                        LocalDateTime departureDateTime = LocalDateTime.of(
                            departureDate.getValue(),
                            LocalTime.parse(departureTime.getText())
                        );
                        
                        // Calculate arrival time (example: add 2 hours)
                        LocalDateTime arrivalDateTime = departureDateTime.plusHours(2);
                        
                        // Create new flight
                        Flight newFlight = new Flight(
                            flightNumber.getText(),
                            airline.getText(),
                            origin.getText(),
                            destination.getText(),
                            departureDateTime,
                            arrivalDateTime,
                            "Available for Reservation"  // Set status as available for reservation
                        );
                        newFlight.setGate(gate.getText());
                        newFlight.setTerminal(terminal.getValue());
                        
                        // Set price if provided
                        if (!priceField.getText().isEmpty()) {
                            try {
                                double price = Double.parseDouble(priceField.getText());
                                newFlight.setPrice(price);
                            } catch (NumberFormatException e) {
                                showAlert("Invalid price format. Using default price.");
                            }
                        }
                        
                        // Add to database
                        DatabaseConnection db = DatabaseConnection.getInstance();
                        String query = "INSERT INTO flights " +
                            "(flight_number, airline, origin, destination, departure_time, arrival_time, status, gate, terminal, price) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                            
                        db.executeUpdate(query,
                            newFlight.getFlightNumber(),
                            newFlight.getAirline(),
                            newFlight.getOrigin(),
                            newFlight.getDestination(),
                            java.sql.Timestamp.valueOf(newFlight.getDepartureTime()),
                            java.sql.Timestamp.valueOf(newFlight.getArrivalTime()),
                            newFlight.getStatus(),
                            newFlight.getGate(),
                            newFlight.getTerminal(),
                            newFlight.getPrice()
                        );
                        
                        return newFlight;
                    } catch (Exception e) {
                        showAlert("Invalid input: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });
            
            Optional<Flight> result = dialog.showAndWait();
            result.ifPresent(flight -> {
                dataService.addFlight(flight);
                loadFlightData(); // Refresh the table
                showInfoAlert("Flight added successfully and is now available for booking in the user portal.");
            });
            
        } catch (Exception e) {
            showAlert("Error adding flight: " + e.getMessage());
        }
    }
    
    @FXML
    private void deleteFlight() {
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if (selectedFlight != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Flight " + selectedFlight.getFlightNumber());
            confirmAlert.setContentText("Are you sure you want to delete this flight from the schedule?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                dataService.removeFlight(selectedFlight);
                totalFlightsLabel.setText("Total: " + dataService.getFlights().size() + " flights");
                updateGateStatistics();
                
                // Notify users about the deleted flight
                dataService.addNotification("Flight cancelled: " + selectedFlight.getFlightNumber() + " to " + selectedFlight.getDestination());
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Flight Selected");
            alert.setContentText("Please select a flight to delete.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void showGateAssignment() {
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if (selectedFlight != null) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Gate Assignment");
            dialog.setHeaderText("Assign Gate for Flight " + selectedFlight.getFlightNumber());
            
            // Set the button types
            ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);
            
            // Create content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Gate ComboBox
            ComboBox<String> gateComboBox = new ComboBox<>();
            List<String> availableGates = new ArrayList<>();
            for (char terminal : new char[]{'A', 'B', 'C', 'D'}) {
                for (int i = 1; i <= 20; i++) {
                    String gate = terminal + String.valueOf(i);
                    boolean isOccupied = false;
                    
                    // Check if gate is already occupied
                    for (Flight flight : dataService.getFlights()) {
                        if (flight != selectedFlight && flight.getGate().equals(gate)) {
                            isOccupied = true;
                            break;
                        }
                    }
                    
                    if (!isOccupied) {
                        availableGates.add(gate);
                    }
                }
            }
            
            gateComboBox.getItems().addAll(availableGates);
            gateComboBox.setValue(selectedFlight.getGate());
            
            grid.add(new Label("Current Gate:"), 0, 0);
            grid.add(new Label(selectedFlight.getGate()), 1, 0);
            grid.add(new Label("New Gate:"), 0, 1);
            grid.add(gateComboBox, 1, 1);
            
            dialog.getDialogPane().setContent(grid);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == assignButtonType) {
                    return gateComboBox.getValue();
                }
                return null;
            });
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newGate -> {
                selectedFlight.setGate(newGate);
                flightTable.refresh();
                updateGateStatistics();
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Flight Selected");
            alert.setContentText("Please select a flight to assign a gate.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void showDelayManagement() {
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if (selectedFlight != null) {
            Dialog<Integer> dialog = new Dialog<>();
            dialog.setTitle("Delay Management");
            dialog.setHeaderText("Manage Delay for Flight " + selectedFlight.getFlightNumber());
            
            // Set the button types
            ButtonType delayButtonType = new ButtonType("Apply Delay", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(delayButtonType, ButtonType.CANCEL);
            
            // Create content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Delay options
            ComboBox<Integer> delayComboBox = new ComboBox<>();
            delayComboBox.getItems().addAll(0, 15, 30, 45, 60, 90, 120, 180, 240);
            delayComboBox.setValue(0);
            
            grid.add(new Label("Current Status:"), 0, 0);
            grid.add(new Label(selectedFlight.getStatus()), 1, 0);
            grid.add(new Label("Delay (minutes):"), 0, 1);
            grid.add(delayComboBox, 1, 1);
            
            dialog.getDialogPane().setContent(grid);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == delayButtonType) {
                    return delayComboBox.getValue();
                }
                return null;
            });
            
            Optional<Integer> result = dialog.showAndWait();
            result.ifPresent(delayMinutes -> {
                if (delayMinutes > 0) {
                    selectedFlight.setStatus("Delayed");
                    // You could add departure time adjustment here
                    flightTable.refresh();
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Flight Selected");
            alert.setContentText("Please select a flight to manage delays.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void showFlightDetails() {
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if (selectedFlight != null) {
            showFlightDetailsDialog(selectedFlight);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Flight Selected");
            alert.setContentText("Please select a flight from the table.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void showScheduleChanges() {
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();
        if (selectedFlight != null) {
            Dialog<Flight> dialog = new Dialog<>();
            dialog.setTitle("Schedule Changes");
            dialog.setHeaderText("Modify Schedule for Flight " + selectedFlight.getFlightNumber());
            
            // Set the button types
            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
            
            // Create content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Fields for editing
            TextField departureField = new TextField(selectedFlight.getDeparture());
            
            // Status ComboBox
            ComboBox<String> statusComboBox = new ComboBox<>();
            statusComboBox.getItems().addAll("Available for Reservation", "On Time", "Delayed", "Boarding", "Departed", "Cancelled");
            statusComboBox.setValue(selectedFlight.getStatus());
            
            grid.add(new Label("Flight Number:"), 0, 0);
            grid.add(new Label(selectedFlight.getFlightNumber()), 1, 0);
            grid.add(new Label("Airline:"), 0, 1);
            grid.add(new Label(selectedFlight.getAirline()), 1, 1);
            grid.add(new Label("Destination:"), 0, 2);
            grid.add(new Label(selectedFlight.getDestination()), 1, 2);
            grid.add(new Label("Departure Time:"), 0, 3);
            grid.add(departureField, 1, 3);
            grid.add(new Label("Status:"), 0, 4);
            grid.add(statusComboBox, 1, 4);
            
            dialog.getDialogPane().setContent(grid);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    return new Flight(
                        selectedFlight.getFlightNumber(),
                        selectedFlight.getAirline(),
                        selectedFlight.getDestination(),
                        departureField.getText(),
                        statusComboBox.getValue(),
                        selectedFlight.getTerminal(),
                        selectedFlight.getGate()
                    );
                }
                return null;
            });
            
            Optional<Flight> result = dialog.showAndWait();
            result.ifPresent(updatedFlight -> {
                int index = dataService.getFlights().indexOf(selectedFlight);
                if (index >= 0) {
                    // Since Flight is immutable for some properties, we need to replace it
                    dataService.getFlights().set(index, updatedFlight);
                    flightTable.refresh();
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Flight Selected");
            alert.setContentText("Please select a flight to modify schedule.");
            alert.showAndWait();
        }
    }
    
    // Add real-time status updates
    private void startRealTimeStatusUpdates() {
        statusTimeline = new Timeline(new KeyFrame(Duration.minutes(2), event -> {
            for (Flight flight : dataService.getFlights()) {
                switch (flight.getStatus()) {
                    case "Available for Reservation":
                        flight.setStatus("On Time");
                        break;
                    case "On Time":
                        flight.setStatus("Boarding");
                        break;
                    case "Boarding":
                        flight.setStatus("Departed");
                        break;
                    default:
                        // Do nothing for Departed, Delayed, Cancelled
                        break;
                }
            }
            flightTable.refresh();
        }));
        statusTimeline.setCycleCount(Timeline.INDEFINITE);
        statusTimeline.play();
    }
    
    // Helper methods to convert between time formats and provide compatibility
    private String getDeparture(Flight flight) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return flight.getDepartureTime().format(formatter);
    }
    
    private String getTerminal(Flight flight) {
        // For now, assign terminal based on airline name - first letter
        if (flight.getGate() == null || flight.getGate().isEmpty()) {
            return "A";
        }
        return flight.getGate().substring(0, 1);
    }
    
    private void showEditDialog(Flight flight) {
        Dialog<Flight> dialog = new Dialog<>();
        dialog.setTitle("Edit Flight");
        dialog.setHeaderText("Edit Flight Details for " + flight.getFlightNumber());
        
        ButtonType updateButton = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButton, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Status ComboBox
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Available for Reservation", "On Time", "Delayed", "Boarding", "Departed", "Cancelled");
        statusComboBox.setValue(flight.getStatus());
        
        // Gate ComboBox
        ComboBox<String> gateComboBox = new ComboBox<>();
        
        // Generate gates for terminal A, B, C
        char[] terminals = {'A', 'B', 'C'};
        for (char terminal : terminals) {
            for (int i = 1; i <= 15; i++) {
                gateComboBox.getItems().add(terminal + String.valueOf(i));
            }
        }
        gateComboBox.setValue(flight.getGate());
        
        // Fields for times
        TextField departureField = new TextField(flight.getDeparture());
        
        grid.add(new Label("Flight Number:"), 0, 0);
        grid.add(new Label(flight.getFlightNumber()), 1, 0);
        grid.add(new Label("Airline:"), 0, 1);
        grid.add(new Label(flight.getAirline()), 1, 1);
        grid.add(new Label("Destination:"), 0, 2);
        grid.add(new Label(flight.getDestination()), 1, 2);
        grid.add(new Label("Departure Time:"), 0, 3);
        grid.add(departureField, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusComboBox, 1, 4);
        grid.add(new Label("Terminal:"), 0, 5);
        grid.add(new Label(flight.getTerminal()), 1, 5);
        grid.add(new Label("Gate:"), 0, 6);
        grid.add(gateComboBox, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButton) {
                try {
                    // Parse departure time
                    String[] timeParts = departureField.getText().split(":");
                    if (timeParts.length != 2) {
                        throw new IllegalArgumentException("Invalid time format");
                    }
                    
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    
                    // Get the original date but update the time
                    LocalDateTime currentDeparture = flight.getDepartureTime();
                    LocalDateTime newDeparture = currentDeparture
                        .withHour(hour)
                        .withMinute(minute)
                        .withSecond(0)
                        .withNano(0);
                    
                    // Update flight with new values
                    flight.setDepartureTime(newDeparture);
                    flight.setStatus(statusComboBox.getValue());
                    flight.setGate(gateComboBox.getValue());
                    
                    return flight;
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Input");
                    alert.setHeaderText("Invalid Time Format");
                    alert.setContentText("Please enter time in HH:MM format.");
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Overloaded method for information alerts
    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void addFlight() {
        addNewFlight();
    }
} 
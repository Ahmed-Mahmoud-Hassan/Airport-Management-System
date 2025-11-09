package com.yousif.attemp2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class UserBaggageController implements UserBaseController {
    
    @FXML
    private VBox noBaggageMessage;
    
    @FXML
    private TableView<BaggageItem> baggageTable;
    
    @FXML
    private TextField baggageIdField;
    
    @FXML
    private TableColumn<BaggageItem, String> baggageIdCol;
    
    @FXML
    private TableColumn<BaggageItem, String> flightCol;
    
    @FXML
    private TableColumn<BaggageItem, String> statusCol;
    
    @FXML
    private TableColumn<BaggageItem, String> locationCol;
    
    @FXML
    private TableColumn<BaggageItem, String> lastUpdatedCol;
    
    @FXML
    private TableColumn<BaggageItem, BaggageItem> actionsCol;
    
    private ObservableList<BaggageItem> baggageItems = FXCollections.observableArrayList();
    
    // Reference to the data service
    private final DataService dataService = DataService.getInstance();
    
    private LoginController.UserData userData;
    
    @FXML
    public void initialize() {
        setupTable();
        loadBaggageData();
    }
    
    /**
     * Refresh the baggage data from the data service
     */
    public void refreshData() {
        loadBaggageData();
    }
    
    @Override
    public void setUserData(LoginController.UserData userData) {
        this.userData = userData;
        loadBaggageData(); // Reload data specific to this user
    }
    
    private void setupTable() {
        baggageIdCol.setCellValueFactory(data -> data.getValue().baggageIdProperty());
        flightCol.setCellValueFactory(data -> data.getValue().flightProperty());
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());
        locationCol.setCellValueFactory(data -> data.getValue().locationProperty());
        lastUpdatedCol.setCellValueFactory(data -> data.getValue().handlingTimeProperty());
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button detailsBtn = new Button();
            {
                FontIcon icon = new FontIcon("fas-info-circle");
                detailsBtn.setGraphic(icon);
                detailsBtn.getStyleClass().add("action-button");
                detailsBtn.setOnAction(event -> {
                    BaggageItem baggage = getTableView().getItems().get(getIndex());
                    showBaggageDetails(baggage);
                });
            }
            @Override
            protected void updateItem(BaggageItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsBtn);
                }
            }
        });
    }
    
    private void loadBaggageData() {
        // Clear existing data
        baggageItems.clear();
        
        if (userData == null) return;
        
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String query = "SELECT b.*, p.first_name, p.last_name, f.flight_number " +
                         "FROM baggage b " +
                         "JOIN bookings bk ON b.booking_id = bk.booking_id " +
                         "JOIN passengers p ON bk.passenger_id = p.passenger_id " +
                         "JOIN flights f ON bk.flight_id = f.flight_id " +
                         "WHERE p.user_id = ? " +
                         "ORDER BY b.handling_time DESC";
            
            java.sql.ResultSet rs = db.executeQuery(query, userData.getUserId());
            
            while (rs.next()) {
                String baggageTag = rs.getString("baggage_tag");
                String passengerName = rs.getString("first_name") + " " + rs.getString("last_name");
                String flightNumber = rs.getString("flight_number");
                String location = rs.getString("current_location");
                String status = rs.getString("status");
                String handlingTime = rs.getString("handling_time");
                
                BaggageItem item = new BaggageItem(baggageTag, passengerName, flightNumber, 
                                               location, status, handlingTime);
                baggageItems.add(item);
            }
            
            // Apply the data to table
            baggageTable.setItems(baggageItems);
            
            // Show/hide no baggage message
            if (noBaggageMessage != null) {
                noBaggageMessage.setVisible(baggageItems.isEmpty());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load baggage data: " + e.getMessage());
        }
    }
    
    /**
     * Track baggage by ID
     */
    @FXML
    public void trackBaggage() {
        String baggageId = baggageIdField.getText().trim();
        
        if (baggageId.isEmpty()) {
            showError("Please enter a baggage ID");
            return;
        }
        
        // Search for baggage in data service
        boolean found = false;
        for (BaggageItem item : dataService.getBaggageItems()) {
            if (item.getBaggageId().equals(baggageId)) {
                // Display details for the found baggage
                showBaggageDetails(item);
                found = true;
                break;
            }
        }
        
        if (!found) {
            showError("Baggage ID not found: " + baggageId);
        }
    }
    
    private void showBaggageDetails(BaggageItem baggage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Baggage Details");
        alert.setHeaderText("Baggage ID: " + baggage.getBaggageId());
        
        // Create content for the dialog
        StringBuilder content = new StringBuilder();
        content.append("Passenger: ").append(baggage.getPassengerName()).append("\n\n");
        content.append("Flight: ").append(baggage.getFlight()).append("\n\n");
        content.append("Current Location: ").append(baggage.getLocation()).append("\n\n");
        content.append("Status: ").append(baggage.getStatus()).append("\n\n");
        content.append("Handling Time: ").append(baggage.getHandlingTime()).append("\n\n");
        
        // Add tracking history
        content.append("Tracking History:\n");
        content.append("- Check-in: Completed\n");
        content.append("- Screening: Completed\n");
        content.append("- Sorting: In Progress\n");
        content.append("- Loading: Pending\n");
        content.append("- Transport: Pending\n");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
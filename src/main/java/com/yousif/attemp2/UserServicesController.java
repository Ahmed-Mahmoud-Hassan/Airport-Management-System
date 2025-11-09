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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class UserServicesController implements UserBaseController {
    
    @FXML
    private ComboBox<String> serviceTypeCombo;
    
    @FXML
    private ComboBox<String> locationCombo;
    
    @FXML
    private TextField flightNumberField;
    
    @FXML
    private ComboBox<String> urgencyCombo;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private TableView<ServiceRequest> requestsTable;
    
    @FXML
    private TableColumn<ServiceRequest, String> requestIdCol;
    
    @FXML
    private TableColumn<ServiceRequest, String> serviceTypeCol;
    
    @FXML
    private TableColumn<ServiceRequest, LocalDateTime> requestDateCol;
    
    @FXML
    private TableColumn<ServiceRequest, String> locationCol;
    
    @FXML
    private TableColumn<ServiceRequest, String> urgencyCol;
    
    @FXML
    private TableColumn<ServiceRequest, String> statusCol;
    
    @FXML
    private TableColumn<ServiceRequest, Void> actionsCol;
    
    @FXML
    private TabPane servicesTabPane;
    
    private ObservableList<ServiceRequest> serviceRequests = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private Integer userId = null;
    private final DataService dataService = DataService.getInstance();
    
    @FXML
    public void initialize() {
        // Initialize service type dropdown
        serviceTypeCombo.setItems(FXCollections.observableArrayList(
            "Special Assistance", "Baggage Assistance", "Language Assistance",
            "Family Services", "VIP Services", "Information Assistance", "Other"
        ));
        
        // Initialize location dropdown
        locationCombo.setItems(FXCollections.observableArrayList(
            "Terminal 1", "Terminal 2", "Terminal 3", "Terminal 4", "Terminal 5",
            "Baggage Claim", "Check-in Area", "Security Checkpoint", "Gate Area"
        ));
        
        // Initialize urgency dropdown
        urgencyCombo.setItems(FXCollections.observableArrayList(
            "Low", "Medium", "High", "Urgent"
        ));
        
        // Initialize table
        setupTable();
        
        // Load requests from DB if userId is set
        if (userId != null) {
            loadRequestsFromDb();
        }
    }
    
    private void setupTable() {
        requestIdCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        serviceTypeCol.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        requestDateCol.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        requestDateCol.setCellFactory(column -> new TableCell<ServiceRequest, LocalDateTime>() {
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
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        urgencyCol.setCellValueFactory(new PropertyValueFactory<>("urgency"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Action column with buttons
        actionsCol.setCellFactory(column -> new TableCell<ServiceRequest, Void>() {
            private final Button viewButton = new Button("View");
            private final Button cancelButton = new Button("Cancel");
            
            {
                viewButton.getStyleClass().add("small-action-button");
                viewButton.setGraphic(new FontIcon("fas-eye"));
                
                cancelButton.getStyleClass().add("small-action-button");
                cancelButton.setGraphic(new FontIcon("fas-times"));
                
                viewButton.setOnAction(event -> {
                    ServiceRequest request = getTableView().getItems().get(getIndex());
                    viewServiceRequest(request);
                });
                
                cancelButton.setOnAction(event -> {
                    ServiceRequest request = getTableView().getItems().get(getIndex());
                    cancelServiceRequest(request);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ServiceRequest request = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);
                    buttons.getChildren().add(viewButton);
                    
                    if ("Pending".equals(request.getStatus()) || "In Progress".equals(request.getStatus())) {
                        buttons.getChildren().add(cancelButton);
                    }
                    
                    setGraphic(buttons);
                }
            }
        });
        
        requestsTable.setItems(serviceRequests);
    }
    
    private void loadRequestsFromDb() {
        serviceRequests.clear();
        if (userId == null) return;
        for (DataService.ServiceRequestRow row : dataService.getServiceRequestsForUser(userId)) {
            serviceRequests.add(new ServiceRequest(
                String.valueOf(row.requestId),
                row.serviceType,
                row.createdAt,
                row.location,
                row.urgency,
                row.status,
                row.description
            ));
        }
    }
    
    @FXML
    private void submitRequest() {
        String serviceType = serviceTypeCombo.getValue();
        String location = locationCombo.getValue();
        String urgency = urgencyCombo.getValue();
        String description = descriptionArea.getText();
        
        if (serviceType == null || location == null || urgency == null || 
            description == null || description.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields");
            return;
        }
        
        if (userId == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User not set. Please log in again.");
            return;
        }
        
        boolean success = dataService.addServiceRequest(userId, serviceType, location, urgency, description);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Request Submitted", "Your service request has been submitted successfully.");
            clearForm();
            loadRequestsFromDb();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit service request. Please try again.");
        }
    }
    
    @FXML
    private void clearForm() {
        serviceTypeCombo.setValue(null);
        locationCombo.setValue(null);
        flightNumberField.clear();
        urgencyCombo.setValue(null);
        descriptionArea.clear();
    }
    
    private void viewServiceRequest(ServiceRequest request) {
        // In a real application, this would show detailed request information
        showAlert(Alert.AlertType.INFORMATION, "Service Request Details", 
                 "Request ID: " + request.getRequestId() + 
                 "\nService Type: " + request.getServiceType() + 
                 "\nDate: " + formatter.format(request.getRequestDate()) + 
                 "\nLocation: " + request.getLocation() + 
                 "\nUrgency: " + request.getUrgency() + 
                 "\nStatus: " + request.getStatus() + 
                 "\n\nDescription: " + request.getDescription());
    }
    
    private void cancelServiceRequest(ServiceRequest request) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Service Request");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to cancel this service request?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // In a real application, this would update the database
            request.setStatus("Cancelled");
            requestsTable.refresh();
            
            showAlert(Alert.AlertType.INFORMATION, "Request Cancelled", 
                     "Your service request has been cancelled successfully.");
        }
    }
    
    @FXML
    private void requestSpecialAssistance() {
        serviceTypeCombo.setValue("Special Assistance");
        servicesTabPane.getSelectionModel().select(0);
        descriptionArea.requestFocus();
    }
    
    @FXML
    private void requestBaggageAssistance() {
        serviceTypeCombo.setValue("Baggage Assistance");
        servicesTabPane.getSelectionModel().select(0);
        descriptionArea.requestFocus();
    }
    
    @FXML
    private void requestLanguageAssistance() {
        serviceTypeCombo.setValue("Language Assistance");
        servicesTabPane.getSelectionModel().select(0);
        descriptionArea.requestFocus();
    }
    
    @FXML
    private void requestFamilyServices() {
        serviceTypeCombo.setValue("Family Services");
        servicesTabPane.getSelectionModel().select(0);
        descriptionArea.requestFocus();
    }
    
    @FXML
    private void requestVIPServices() {
        serviceTypeCombo.setValue("VIP Services");
        servicesTabPane.getSelectionModel().select(0);
        descriptionArea.requestFocus();
    }
    
    @FXML
    private void requestInformationAssistance() {
        serviceTypeCombo.setValue("Information Assistance");
        servicesTabPane.getSelectionModel().select(0);
        descriptionArea.requestFocus();
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
        // Store userId for DB operations
        if (userData != null) {
            try {
                // Fetch user_id from DB using username
                DatabaseConnection dbConn = DatabaseConnection.getInstance();
                try (java.sql.Connection conn = dbConn.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users WHERE username = ?")) {
                    stmt.setString(1, userData.getUsername());
                    java.sql.ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        userId = rs.getInt("user_id");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // After setting userId, load requests from DB
        if (userId != null) {
            loadRequestsFromDb();
        }
    }
    
    // Inner class to represent a service request
    public static class ServiceRequest {
        private final StringProperty requestId;
        private final StringProperty serviceType;
        private final ObjectProperty<LocalDateTime> requestDate;
        private final StringProperty location;
        private final StringProperty urgency;
        private final StringProperty status;
        private final StringProperty description;
        
        public ServiceRequest(String requestId, String serviceType, LocalDateTime requestDate,
                             String location, String urgency, String status, String description) {
            this.requestId = new SimpleStringProperty(requestId);
            this.serviceType = new SimpleStringProperty(serviceType);
            this.requestDate = new SimpleObjectProperty<>(requestDate);
            this.location = new SimpleStringProperty(location);
            this.urgency = new SimpleStringProperty(urgency);
            this.status = new SimpleStringProperty(status);
            this.description = new SimpleStringProperty(description);
        }
        
        public String getRequestId() {
            return requestId.get();
        }
        
        public StringProperty requestIdProperty() {
            return requestId;
        }
        
        public String getServiceType() {
            return serviceType.get();
        }
        
        public StringProperty serviceTypeProperty() {
            return serviceType;
        }
        
        public LocalDateTime getRequestDate() {
            return requestDate.get();
        }
        
        public ObjectProperty<LocalDateTime> requestDateProperty() {
            return requestDate;
        }
        
        public String getLocation() {
            return location.get();
        }
        
        public StringProperty locationProperty() {
            return location;
        }
        
        public String getUrgency() {
            return urgency.get();
        }
        
        public StringProperty urgencyProperty() {
            return urgency;
        }
        
        public String getStatus() {
            return status.get();
        }
        
        public void setStatus(String value) {
            status.set(value);
        }
        
        public StringProperty statusProperty() {
            return status;
        }
        
        public String getDescription() {
            return description.get();
        }
        
        public StringProperty descriptionProperty() {
            return description;
        }
    }
} 
package com.yousif.attemp2;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminServicesController {
    @FXML private TableView<DataService.ServiceRequestRow> requestsTable;
    @FXML private TableColumn<DataService.ServiceRequestRow, String> requestIdCol;
    @FXML private TableColumn<DataService.ServiceRequestRow, String> userIdCol;
    @FXML private TableColumn<DataService.ServiceRequestRow, String> serviceTypeCol;
    @FXML private TableColumn<DataService.ServiceRequestRow, String> locationCol;
    @FXML private TableColumn<DataService.ServiceRequestRow, String> urgencyCol;
    @FXML private TableColumn<DataService.ServiceRequestRow, String> statusCol;
    @FXML private TableColumn<DataService.ServiceRequestRow, LocalDateTime> createdAtCol;
    @FXML private TableColumn<DataService.ServiceRequestRow, String> descriptionCol;
    @FXML private TableColumn<DataService.ServiceRequestRow, Void> actionsCol;
    @FXML private Button refreshBtn;

    private final ObservableList<DataService.ServiceRequestRow> serviceRequests = FXCollections.observableArrayList();
    private final DataService dataService = DataService.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTable();
        loadRequestsFromDb();
    }

    private void setupTable() {
        requestIdCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().requestId)));
        userIdCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().userId)));
        serviceTypeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().serviceType));
        locationCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().location));
        urgencyCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().urgency));
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().status));
        createdAtCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().createdAt));
        createdAtCol.setCellFactory(column -> new TableCell<DataService.ServiceRequestRow, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        });
        descriptionCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().description));
        actionsCol.setCellFactory(column -> new TableCell<DataService.ServiceRequestRow, Void>() {
            private final Button updateStatusBtn = new Button("Update Status");
            private final Button viewBtn = new Button("View");
            {
                updateStatusBtn.getStyleClass().add("small-action-button");
                updateStatusBtn.setGraphic(new FontIcon("fas-edit"));
                updateStatusBtn.setOnAction(e -> {
                    DataService.ServiceRequestRow row = getTableView().getItems().get(getIndex());
                    showStatusUpdateDialog(row);
                });
                viewBtn.getStyleClass().add("small-action-button");
                viewBtn.setGraphic(new FontIcon("fas-eye"));
                viewBtn.setOnAction(e -> {
                    DataService.ServiceRequestRow row = getTableView().getItems().get(getIndex());
                    showRequestDetails(row);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, viewBtn, updateStatusBtn);
                    setGraphic(box);
                }
            }
        });
        requestsTable.setItems(serviceRequests);
    }

    private void loadRequestsFromDb() {
        serviceRequests.clear();
        serviceRequests.addAll(dataService.getAllServiceRequests());
    }

    @FXML
    private void refreshRequests() {
        loadRequestsFromDb();
    }

    private void showStatusUpdateDialog(DataService.ServiceRequestRow row) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(row.status, "Pending", "In Progress", "Completed", "Cancelled");
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Update Status for Request ID: " + row.requestId);
        dialog.setContentText("Select new status:");
        dialog.showAndWait().ifPresent(newStatus -> {
            if (!newStatus.equals(row.status)) {
                // Update in DB
                if (dataService.updateServiceRequestStatus(row.requestId, newStatus)) {
                    row.status = newStatus;
                    requestsTable.refresh();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status.");
                }
            }
        });
    }

    private void showRequestDetails(DataService.ServiceRequestRow row) {
        showAlert(Alert.AlertType.INFORMATION, "Service Request Details",
            "Request ID: " + row.requestId +
            "\nUser ID: " + row.userId +
            "\nService Type: " + row.serviceType +
            "\nLocation: " + row.location +
            "\nUrgency: " + row.urgency +
            "\nStatus: " + row.status +
            "\nCreated At: " + formatter.format(row.createdAt) +
            "\n\nDescription: " + row.description);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Use DataService.ServiceRequestRow for table rows
}
// Note: Add updateServiceRequestStatus(int requestId, String newStatus) to DataService. 
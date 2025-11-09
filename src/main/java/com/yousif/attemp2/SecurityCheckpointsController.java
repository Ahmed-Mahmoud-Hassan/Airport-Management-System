package com.yousif.attemp2;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class SecurityCheckpointsController implements Initializable {

    @FXML
    private Label currentPassengersLabel;

    @FXML
    private Label avgWaitTimeLabel;

    @FXML
    private Label activeCheckpointsLabel;

    @FXML
    private TableView<CheckpointStatus> checkpointTable;

    @FXML
    private TableColumn<CheckpointStatus, String> checkpointColumn;

    @FXML
    private TableColumn<CheckpointStatus, String> waitTimeColumn;

    @FXML
    private TableColumn<CheckpointStatus, String> throughputColumn;

    @FXML
    private TableColumn<CheckpointStatus, String> staffColumn;

    @FXML
    private TableColumn<CheckpointStatus, String> statusColumn;

    private ObservableList<CheckpointStatus> checkpointData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up the table columns
        checkpointColumn.setCellValueFactory(data -> data.getValue().checkpointProperty());
        waitTimeColumn.setCellValueFactory(data -> data.getValue().waitTimeProperty());
        throughputColumn.setCellValueFactory(data -> data.getValue().throughputProperty());
        staffColumn.setCellValueFactory(data -> data.getValue().staffingProperty());
        
        // Custom cell factory for status column
        statusColumn.setCellFactory(unused -> new TableCell<CheckpointStatus, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    StackPane statusPane = new StackPane();
                    Text statusText = new Text(item);
                    
                    statusPane.getStyleClass().add("status-pill");
                    
                    switch (item) {
                        case "NORMAL":
                            statusPane.getStyleClass().add("status-normal-pill");
                            break;
                        case "WARNING":
                            statusPane.getStyleClass().add("status-warning-pill");
                            break;
                        case "CRITICAL":
                            statusPane.getStyleClass().add("status-critical-pill");
                            break;
                    }
                    
                    statusPane.getChildren().add(statusText);
                    setGraphic(statusPane);
                    setText(null);
                }
            }
        });
        
        // Set up wait time column with color styling
        waitTimeColumn.setCellFactory(unused -> new TableCell<CheckpointStatus, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    // Parse wait time to determine severity
                    String timeValue = item.split(" ")[0];
                    int minutes = Integer.parseInt(timeValue);
                    
                    if (minutes <= 8) {
                        setStyle("-fx-text-fill: #4caf50;"); // Green for short wait
                    } else if (minutes <= 15) {
                        setStyle("-fx-text-fill: #8c9eff;"); // Purple for medium wait
                    } else {
                        setStyle("-fx-text-fill: #f44336;"); // Red for long wait
                    }
                }
            }
        });

        // Load sample data
        loadCheckpointData();
        
        // Set the table data
        checkpointTable.setItems(checkpointData);
    }

    private void loadCheckpointData() {
        // Sample checkpoint data matching the prototype image
        checkpointData.add(new CheckpointStatus("North Terminal A", "8 min", "320/hr", "6/8", "NORMAL"));
        checkpointData.add(new CheckpointStatus("Main Terminal B", "24 min", "180/hr", "4/6", "WARNING"));
        checkpointData.add(new CheckpointStatus("South Terminal A", "5 min", "280/hr", "5/6", "NORMAL"));
        checkpointData.add(new CheckpointStatus("International C", "14 min", "210/hr", "7/8", "NORMAL"));
        // Add more checkpoints as needed
    }

    // Model class for checkpoint status
    public static class CheckpointStatus {
        private final SimpleStringProperty checkpoint;
        private final SimpleStringProperty waitTime;
        private final SimpleStringProperty throughput;
        private final SimpleStringProperty staffing;
        private final SimpleStringProperty status;

        public CheckpointStatus(String checkpoint, String waitTime, String throughput, String staffing, String status) {
            this.checkpoint = new SimpleStringProperty(checkpoint);
            this.waitTime = new SimpleStringProperty(waitTime);
            this.throughput = new SimpleStringProperty(throughput);
            this.staffing = new SimpleStringProperty(staffing);
            this.status = new SimpleStringProperty(status);
        }

        public String getCheckpoint() {
            return checkpoint.get();
        }

        public SimpleStringProperty checkpointProperty() {
            return checkpoint;
        }

        public String getWaitTime() {
            return waitTime.get();
        }

        public SimpleStringProperty waitTimeProperty() {
            return waitTime;
        }

        public String getThroughput() {
            return throughput.get();
        }

        public SimpleStringProperty throughputProperty() {
            return throughput;
        }

        public String getStaffing() {
            return staffing.get();
        }

        public SimpleStringProperty staffingProperty() {
            return staffing;
        }

        public String getStatus() {
            return status.get();
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }
    }
} 
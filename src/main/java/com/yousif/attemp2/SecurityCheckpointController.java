package com.yousif.attemp2;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.Map;

public class SecurityCheckpointController {

    @FXML
    private Label activeCheckpointsValue;
    
    @FXML
    private Label avgWaitTimeValue;
    
    @FXML
    private Label passengersScreenedValue;
    
    @FXML
    private Label securityAlertsValue;
    
    @FXML
    private AreaChart<String, Number> waitTimeChart;
    
    @FXML
    private PieChart checkpointDistributionChart;
    
    @FXML
    private ComboBox<String> terminalSelector;
    
    @FXML
    private TableView<SecurityCheckpoint> checkpointTable;
    
    @FXML
    private TableColumn<SecurityCheckpoint, String> terminalColumn;
    
    @FXML
    private TableColumn<SecurityCheckpoint, String> checkpointColumn;
    
    @FXML
    private TableColumn<SecurityCheckpoint, String> statusColumn;
    
    @FXML
    private TableColumn<SecurityCheckpoint, Integer> currentWaitColumn;
    
    @FXML
    private TableColumn<SecurityCheckpoint, Integer> throughputColumn;
    
    @FXML
    private TableColumn<SecurityCheckpoint, String> staffingColumn;
    
    @FXML
    private TableColumn<SecurityCheckpoint, SecurityCheckpoint> actionsColumn;
    
    @FXML
    private Label terminalAStaffingLabel;
    
    @FXML
    private ProgressBar terminalAStaffingBar;
    
    @FXML
    private Label terminalBStaffingLabel;
    
    @FXML
    private ProgressBar terminalBStaffingBar;
    
    @FXML
    private Label terminalCStaffingLabel;
    
    @FXML
    private ProgressBar terminalCStaffingBar;
    
    @FXML
    private Label recommendedStaffingLabel;
    
    @FXML
    private VBox alertsContainer;
    
    private ObservableList<SecurityCheckpoint> checkpointData = FXCollections.observableArrayList();
    private Map<String, Integer> terminalStaffing = new HashMap<>();
    
    @FXML
    public void initialize() {
        setupStatistics();
        setupTerminalSelector();
        setupWaitTimeChart();
        setupDistributionChart();
        setupCheckpointTable();
        loadCheckpointData();
        setupStaffingLevels();
        loadSecurityAlerts();
    }
    
    private void setupStatistics() {
        activeCheckpointsValue.setText("12/15");
        avgWaitTimeValue.setText("14 min");
        passengersScreenedValue.setText("2,845");
        securityAlertsValue.setText("3");
    }
    
    private void setupTerminalSelector() {
        terminalSelector.getItems().addAll("All Terminals", "Terminal A", "Terminal B", "Terminal C");
        terminalSelector.setValue("All Terminals");
        terminalSelector.setOnAction(unused -> refreshCheckpoints());
    }
    
    private void setupWaitTimeChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Wait Time");
        
        // Add data for each hour
        String[] hours = {"00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", 
                          "14:00", "16:00", "18:00", "20:00", "22:00"};
        
        // Sample data for wait times
        int[] waitTimes = {6, 4, 3, 8, 16, 24, 20, 18, 14, 10, 8, 7};
        
        for (int i = 0; i < hours.length; i++) {
            series.getData().add(new XYChart.Data<>(hours[i], waitTimes[i]));
        }
        
        waitTimeChart.getData().add(series);
    }
    
    private void setupDistributionChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Terminal A", 45),
                new PieChart.Data("Terminal B", 35),
                new PieChart.Data("Terminal C", 20)
        );
        
        checkpointDistributionChart.setData(pieChartData);
        checkpointDistributionChart.setLabelsVisible(true);
    }
    
    private void setupCheckpointTable() {
        terminalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTerminal()));
        checkpointColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCheckpoint()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        currentWaitColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCurrentWait()).asObject());
        throughputColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getThroughput()).asObject());
        staffingColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStaffing()));
        
        // Actions column with buttons
        actionsColumn.setCellFactory(unused -> new TableCell<SecurityCheckpoint, SecurityCheckpoint>() {
            private final Button detailsButton = new Button();
            private final Button alertButton = new Button();
            
            {
                detailsButton.getStyleClass().add("table-button");
                FontIcon detailsIcon = new FontIcon("fas-info-circle");
                detailsButton.setGraphic(detailsIcon);
                detailsButton.setOnAction(unused -> {
                    SecurityCheckpoint checkpoint = getTableView().getItems().get(getIndex());
                    showCheckpointDetails(checkpoint);
                });
                
                alertButton.getStyleClass().add("table-button");
                FontIcon alertIcon = new FontIcon("fas-exclamation-triangle");
                alertButton.setGraphic(alertIcon);
                alertButton.setOnAction(unused -> {
                    SecurityCheckpoint checkpoint = getTableView().getItems().get(getIndex());
                    showAlertDialog(checkpoint);
                });
            }
            
            @Override
            protected void updateItem(SecurityCheckpoint item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(detailsButton, alertButton);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void loadCheckpointData() {
        checkpointData.addAll(
            new SecurityCheckpoint("A", "1", "Open", 12, 180, "4/5"),
            new SecurityCheckpoint("A", "2", "Open", 15, 160, "4/5"),
            new SecurityCheckpoint("A", "3", "Open", 8, 200, "4/5"),
            new SecurityCheckpoint("B", "1", "Open", 18, 150, "3/5"),
            new SecurityCheckpoint("B", "2", "Open", 22, 140, "3/5"),
            new SecurityCheckpoint("B", "3", "Closed", 0, 0, "0/5"),
            new SecurityCheckpoint("B", "4", "Open", 14, 165, "4/5"),
            new SecurityCheckpoint("C", "1", "Open", 25, 130, "3/5"),
            new SecurityCheckpoint("C", "2", "Open", 16, 145, "3/5"),
            new SecurityCheckpoint("C", "3", "Open", 10, 175, "2/5")
        );
        
        checkpointTable.setItems(checkpointData);
    }
    
    @FXML
    public void refreshCheckpoints() {
        String selectedTerminal = terminalSelector.getValue();
        ObservableList<SecurityCheckpoint> filteredData = FXCollections.observableArrayList();
        
        for (SecurityCheckpoint checkpoint : checkpointData) {
            if (selectedTerminal.equals("All Terminals") || 
                selectedTerminal.equals("Terminal " + checkpoint.getTerminal())) {
                filteredData.add(checkpoint);
            }
        }
        
        checkpointTable.setItems(filteredData);
    }
    
    private void setupStaffingLevels() {
        terminalStaffing.put("A", 12);
        terminalStaffing.put("B", 10);
        terminalStaffing.put("C", 8);
        
        updateStaffingDisplay();
    }
    
    private void updateStaffingDisplay() {
        terminalAStaffingLabel.setText(terminalStaffing.get("A") + "/15");
        terminalAStaffingBar.setProgress(terminalStaffing.get("A") / 15.0);
        terminalBStaffingLabel.setText(terminalStaffing.get("B") + "/15");
        terminalBStaffingBar.setProgress(terminalStaffing.get("B") / 15.0);
        terminalCStaffingLabel.setText(terminalStaffing.get("C") + "/15");
        terminalCStaffingBar.setProgress(terminalStaffing.get("C") / 15.0);
        updateRecommendations();
    }
    
    private void updateRecommendations() {
        // Remove all children from the recommendedStaffingLabel's parent VBox
        VBox parent = (VBox) recommendedStaffingLabel.getParent();
        parent.getChildren().remove(recommendedStaffingLabel);
        // Check Terminal B (which is Terminal 2)
        int terminalBStaff = terminalStaffing.getOrDefault("B", 0);
        if (terminalBStaff < 2) {
            recommendedStaffingLabel.setText("Increase Terminal 2 by 2 staff members");
            if (!parent.getChildren().contains(recommendedStaffingLabel)) {
                parent.getChildren().add(1, recommendedStaffingLabel);
            }
        }
    }
    
    @FXML
    public void increaseStaffing(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String terminal = "";
        
        if (sourceButton.getId().contains("terminalA")) {
            terminal = "A";
        } else if (sourceButton.getId().contains("terminalB")) {
            terminal = "B";
        } else if (sourceButton.getId().contains("terminalC")) {
            terminal = "C";
        }
        
        if (!terminal.isEmpty() && terminalStaffing.get(terminal) < 15) {
            terminalStaffing.put(terminal, terminalStaffing.get(terminal) + 1);
            updateStaffingDisplay();
            updateWaitTimes();
        }
    }
    
    @FXML
    public void decreaseStaffing(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String terminal = "";
        
        if (sourceButton.getId().contains("terminalA")) {
            terminal = "A";
        } else if (sourceButton.getId().contains("terminalB")) {
            terminal = "B";
        } else if (sourceButton.getId().contains("terminalC")) {
            terminal = "C";
        }
        
        if (!terminal.isEmpty() && terminalStaffing.get(terminal) > 0) {
            terminalStaffing.put(terminal, terminalStaffing.get(terminal) - 1);
            updateStaffingDisplay();
            updateWaitTimes();
        }
    }
    
    private void updateWaitTimes() {
        // Simulate wait time changes based on staffing levels
        for (SecurityCheckpoint checkpoint : checkpointData) {
            String terminal = checkpoint.getTerminal();
            int staffing = terminalStaffing.get(terminal);
            
            // Simple formula: more staff = less wait time
            if (checkpoint.getStatus().equals("Open")) {
                int baseWait = 30;
                int newWait = baseWait - (staffing * 2);
                if (newWait < 5) newWait = 5;
                checkpoint.setCurrentWait(newWait);
            }
        }
        
        checkpointTable.refresh();
        
        // Update average wait time
        int totalWait = 0;
        int count = 0;
        for (SecurityCheckpoint checkpoint : checkpointData) {
            if (checkpoint.getStatus().equals("Open")) {
                totalWait += checkpoint.getCurrentWait();
                count++;
            }
        }
        
        int avgWait = count > 0 ? totalWait / count : 0;
        avgWaitTimeValue.setText(avgWait + " min");
    }
    
    @FXML
    public void applyRecommendedStaffing() {
        // Apply the recommended staffing changes
        terminalStaffing.put("C", terminalStaffing.get("C") + 2);
        
        if (terminalStaffing.get("C") > 15) {
            terminalStaffing.put("C", 15);
        }
        
        updateStaffingDisplay();
        updateWaitTimes();
        
        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Staffing Updated");
        alert.setHeaderText("Recommended Staffing Applied");
        alert.setContentText("Terminal C staffing has been increased by 2 members. Wait times have been updated accordingly.");
        alert.showAndWait();
    }
    
    private void loadSecurityAlerts() {
        addAlert("High wait time at Terminal C Checkpoint 1", "critical", "15 minutes ago");
        addAlert("Unattended baggage reported at Terminal A", "warning", "32 minutes ago");
        addAlert("Equipment malfunction at Terminal B Checkpoint 2", "warning", "1 hour ago");
    }
    
    private void addAlert(String message, String severity, String time) {
        HBox alertBox = new HBox(10);
        alertBox.setPadding(new Insets(10));
        alertBox.getStyleClass().add("alert-box");
        alertBox.getStyleClass().add("alert-" + severity);
        
        FontIcon icon = new FontIcon("fas-exclamation-triangle");
        icon.getStyleClass().add("status-" + (severity.equals("critical") ? "critical" : "warning"));
        
        VBox textContainer = new VBox(5);
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("alert-message");
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("alert-time");
        
        textContainer.getChildren().addAll(messageLabel, timeLabel);
        alertBox.getChildren().addAll(icon, textContainer);
        
        alertsContainer.getChildren().add(alertBox);
    }
    
    private void showCheckpointDetails(SecurityCheckpoint checkpoint) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Checkpoint Details");
        alert.setHeaderText("Terminal " + checkpoint.getTerminal() + " Checkpoint " + checkpoint.getCheckpoint());
        
        StringBuilder content = new StringBuilder();
        content.append("Status: ").append(checkpoint.getStatus()).append("\n");
        content.append("Current Wait Time: ").append(checkpoint.getCurrentWait()).append(" minutes\n");
        content.append("Throughput: ").append(checkpoint.getThroughput()).append(" passengers/hour\n");
        content.append("Staffing: ").append(checkpoint.getStaffing()).append("\n");
        content.append("Equipment Status: Operational\n");
        content.append("Last Maintenance: 3 days ago\n");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    private void showAlertDialog(SecurityCheckpoint checkpoint) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Create Security Alert");
        alert.setHeaderText("Create Alert for Terminal " + checkpoint.getTerminal() + " Checkpoint " + checkpoint.getCheckpoint());
        
        // Create a ComboBox for alert type selection
        ComboBox<String> alertTypeCombo = new ComboBox<>();
        alertTypeCombo.getItems().addAll(
            "Equipment Malfunction", 
            "Staffing Shortage", 
            "Excessive Wait Time", 
            "Security Incident",
            "Unattended Baggage"
        );
        alertTypeCombo.setValue("Equipment Malfunction");
        
        // Create a TextArea for additional details
        TextArea detailsArea = new TextArea();
        detailsArea.setPromptText("Enter alert details here...");
        detailsArea.setPrefRowCount(5);
        
        // Create layout for dialog
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Alert Type:"), 
            alertTypeCombo,
            new Label("Details:"),
            detailsArea
        );
        
        alert.getDialogPane().setContent(content);
        
        ButtonType createButtonType = new ButtonType("Create Alert", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(createButtonType, ButtonType.CANCEL);
        
        alert.showAndWait().ifPresent(type -> {
            if (type == createButtonType) {
                // Simulate creating a new alert
                String alertType = alertTypeCombo.getValue();
                addAlert(alertType + " at Terminal " + checkpoint.getTerminal() + " Checkpoint " + checkpoint.getCheckpoint(), 
                         "warning", "Just now");
                
                // Update alert count
                int currentAlerts = Integer.parseInt(securityAlertsValue.getText());
                securityAlertsValue.setText(String.valueOf(currentAlerts + 1));
            }
        });
    }
    
    // Model class for security checkpoint
    public static class SecurityCheckpoint {
        private final String terminal;
        private final String checkpoint;
        private String status;
        private int currentWait;
        private final int throughput;
        private final String staffing;
        
        public SecurityCheckpoint(String terminal, String checkpoint, String status, int currentWait, int throughput, String staffing) {
            this.terminal = terminal;
            this.checkpoint = checkpoint;
            this.status = status;
            this.currentWait = currentWait;
            this.throughput = throughput;
            this.staffing = staffing;
        }
        
        public String getTerminal() { return terminal; }
        public String getCheckpoint() { return checkpoint; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getCurrentWait() { return currentWait; }
        public void setCurrentWait(int currentWait) { this.currentWait = currentWait; }
        public int getThroughput() { return throughput; }
        public String getStaffing() { return staffing; }
    }
} 
package com.yousif.attemp2;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class PassengerCheckinController {

    @FXML
    private Label activeCountersValue;
    
    @FXML
    private Label avgWaitTimeValue;
    
    @FXML
    private Label passengersProcessedValue;
    
    @FXML
    private Label staffOnDutyValue;
    
    @FXML
    private ComboBox<String> terminalSelector;
    
    @FXML
    private GridPane heatmapGrid;
    
    @FXML
    private BarChart<String, Number> waitTimeChart;
    
    @FXML
    private TableView<CounterStaff> staffingTable;
    
    @FXML
    private TableColumn<CounterStaff, String> terminalColumn;
    
    @FXML
    private TableColumn<CounterStaff, String> counterColumn;
    
    @FXML
    private TableColumn<CounterStaff, String> statusColumn;
    
    @FXML
    private TableColumn<CounterStaff, String> staffNameColumn;
    
    @FXML
    private TableColumn<CounterStaff, Integer> queueLengthColumn;
    
    @FXML
    private TableColumn<CounterStaff, Integer> waitTimeColumn;
    
    @FXML
    private TableColumn<CounterStaff, CounterStaff> actionsColumn;
    
    @FXML
    private VBox recommendationsContainer;
    
    private ObservableList<CounterStaff> staffData = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupStatistics();
        setupTerminalSelector();
        generateHeatmap("All");
        setupWaitTimeChart();
        setupStaffingTable();
        loadStaffData();
        generateRecommendations();
    }
    
    private void setupStatistics() {
        activeCountersValue.setText("18/24");
        avgWaitTimeValue.setText("12 min");
        passengersProcessedValue.setText("3,245");
        staffOnDutyValue.setText("32");
    }
    
    private void setupTerminalSelector() {
        terminalSelector.getItems().addAll("All Terminals", "Terminal A", "Terminal B", "Terminal C");
        terminalSelector.setValue("All Terminals");
        terminalSelector.setOnAction(unused -> refreshHeatmap());
    }
    
    @FXML
    private void refreshHeatmap() {
        String selectedTerminal = terminalSelector.getValue();
        if (selectedTerminal.equals("All Terminals")) {
            generateHeatmap("All");
        } else {
            generateHeatmap(selectedTerminal.substring(selectedTerminal.length() - 1));
        }
    }
    
    private void generateHeatmap(String terminal) {
        // Clear existing heatmap
        heatmapGrid.getChildren().clear();
        
        // Reset column and row constraints
        heatmapGrid.getColumnConstraints().clear();
        heatmapGrid.getRowConstraints().clear();
        
        int rowIndex = 0;
        
        if (terminal.equals("All") || terminal.equals("A")) {
            createHeatmapRow("Terminal A", rowIndex++, getRandomUtilization(8));
        }
        
        if (terminal.equals("All") || terminal.equals("B")) {
            createHeatmapRow("Terminal B", rowIndex++, getRandomUtilization(8));
        }
        
        if (terminal.equals("All") || terminal.equals("C")) {
            createHeatmapRow("Terminal C", rowIndex++, getRandomUtilization(8));
        }
    }
    
    private void createHeatmapRow(String terminalName, int rowIndex, String[] utilization) {
        // Add terminal label
        Label terminalLabel = new Label(terminalName);
        terminalLabel.getStyleClass().add("terminal-label");
        heatmapGrid.add(terminalLabel, 0, rowIndex);
        
        // Add counter rectangles
        for (int i = 0; i < utilization.length; i++) {
            Rectangle rect = new Rectangle(40, 40);
            rect.getStyleClass().add("counter-rect");
            
            switch (utilization[i]) {
                case "high":
                    rect.getStyleClass().add("high-utilization");
                    break;
                case "medium":
                    rect.getStyleClass().add("medium-utilization");
                    break;
                case "low":
                    rect.getStyleClass().add("low-utilization");
                    break;
                case "closed":
                    rect.getStyleClass().add("closed");
                    break;
            }
            
            StackPane counterCell = new StackPane();
            Label counterLabel = new Label(String.valueOf(i + 1));
            counterLabel.setTextFill(Color.WHITE);
            counterCell.getChildren().addAll(rect, counterLabel);
            
            heatmapGrid.add(counterCell, i + 1, rowIndex);
        }
    }
    
    private String[] getRandomUtilization(int count) {
        String[] levels = {"high", "medium", "low", "closed"};
        String[] result = new String[count];
        
        for (int i = 0; i < count; i++) {
            int randomIndex = (int) (Math.random() * 10);
            if (randomIndex < 3) {
                result[i] = levels[0]; // high
            } else if (randomIndex < 6) {
                result[i] = levels[1]; // medium
            } else if (randomIndex < 9) {
                result[i] = levels[2]; // low
            } else {
                result[i] = levels[3]; // closed
            }
        }
        
        return result;
    }
    
    private void setupWaitTimeChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Wait Time");
        
        // Add data for each hour
        String[] hours = {"00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", 
                          "14:00", "16:00", "18:00", "20:00", "22:00"};
        
        // Sample data for wait times
        int[] waitTimes = {5, 3, 2, 8, 15, 22, 18, 20, 16, 12, 10, 7};
        
        for (int i = 0; i < hours.length; i++) {
            series.getData().add(new XYChart.Data<>(hours[i], waitTimes[i]));
        }
        
        waitTimeChart.getData().add(series);
    }
    
    private void setupStaffingTable() {
        terminalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTerminal()));
        counterColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCounter()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        staffNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStaffName()));
        queueLengthColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQueueLength()).asObject());
        waitTimeColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getWaitTime()).asObject());
        
        // Actions column with buttons
        actionsColumn.setCellFactory(unused -> new TableCell<CounterStaff, CounterStaff>() {
            private final Button reassignButton = new Button();
            
            {
                reassignButton.getStyleClass().add("table-button");
                FontIcon icon = new FontIcon("fas-exchange-alt");
                reassignButton.setGraphic(icon);
                reassignButton.setOnAction(unused -> {
                    CounterStaff staff = getTableView().getItems().get(getIndex());
                    showReassignDialog(staff);
                });
            }
            
            @Override
            protected void updateItem(CounterStaff item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    setGraphic(reassignButton);
                }
            }
        });
    }
    
    private void loadStaffData() {
        staffData.addAll(
            new CounterStaff("A", "1", "Open", "John Smith", 8, 15),
            new CounterStaff("A", "2", "Open", "Sarah Johnson", 6, 12),
            new CounterStaff("A", "3", "Open", "Michael Brown", 4, 8),
            new CounterStaff("A", "4", "Closed", "N/A", 0, 0),
            new CounterStaff("B", "1", "Open", "Emma Wilson", 10, 18),
            new CounterStaff("B", "2", "Open", "James Taylor", 7, 14),
            new CounterStaff("B", "3", "Open", "Patricia Davis", 5, 10),
            new CounterStaff("C", "1", "Open", "Robert Miller", 3, 6),
            new CounterStaff("C", "2", "Open", "Jennifer Garcia", 9, 16),
            new CounterStaff("C", "3", "Closed", "N/A", 0, 0)
        );
        
        staffingTable.setItems(staffData);
    }
    
    private void showReassignDialog(CounterStaff staff) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reassign Staff");
        dialog.setHeaderText("Reassign " + staff.getStaffName() + " from Terminal " + staff.getTerminal() + " Counter " + staff.getCounter());
        
        ButtonType reassignButtonType = new ButtonType("Reassign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reassignButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        ComboBox<String> terminalCombo = new ComboBox<>();
        terminalCombo.getItems().addAll("A", "B", "C");
        terminalCombo.setValue(staff.getTerminal()); // Set current terminal as default
        
        ComboBox<String> counterCombo = new ComboBox<>();
        counterCombo.getItems().addAll("1", "2", "3", "4", "5");
        counterCombo.setValue(staff.getCounter()); // Set current counter as default
        
        ComboBox<String> shiftCombo = new ComboBox<>();
        shiftCombo.getItems().addAll("Morning (6:00-14:00)", "Afternoon (14:00-22:00)", "Night (22:00-6:00)");
        shiftCombo.setValue("Morning (6:00-14:00)");
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Regular Check-in", "Premium/Business", "Special Assistance", "Supervisor");
        roleCombo.setValue("Regular Check-in");
        
        grid.add(new Label("Terminal:"), 0, 0);
        grid.add(terminalCombo, 1, 0);
        grid.add(new Label("Counter:"), 0, 1);
        grid.add(counterCombo, 1, 1);
        grid.add(new Label("Shift:"), 0, 2);
        grid.add(shiftCombo, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleCombo, 1, 3);
        
        // Add expected impact information
        Label impactLabel = new Label("Expected Impact:");
        impactLabel.setStyle("-fx-font-weight: bold;");
        grid.add(impactLabel, 0, 4, 2, 1);
        
        Label waitTimeImpactLabel = new Label("• Wait Time: Reduced by ~3 minutes");
        grid.add(waitTimeImpactLabel, 0, 5, 2, 1);
        
        Label queueImpactLabel = new Label("• Queue Length: Reduced by ~4 passengers");
        grid.add(queueImpactLabel, 0, 6, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reassignButtonType) {
                return terminalCombo.getValue() + ":" + counterCombo.getValue() + ":" + 
                       shiftCombo.getValue() + ":" + roleCombo.getValue();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (result != null && result.contains(":")) {
                String[] parts = result.split(":");
                staff.setTerminal(parts[0]);
                staff.setCounter(parts[1]);
                
                // Update queue length and wait time to simulate improvement
                Random random = new Random();
                staff.setQueueLength(Math.max(0, staff.getQueueLength() - random.nextInt(4)));
                staff.setWaitTime(Math.max(0, staff.getWaitTime() - random.nextInt(6)));
                
                staffingTable.refresh();
                
                // Show success message
                showSuccessMessage("Staff reassigned successfully!");
                
                // Refresh heatmap to show the changes
                refreshHeatmap();
            }
        });
    }
    
    private void generateRecommendations() {
        addRecommendation("Open additional counters in Terminal B to reduce wait times", "fas-plus-circle");
        addRecommendation("Reassign 2 staff members from Terminal C to Terminal A during peak hours (10:00-14:00)", "fas-exchange-alt");
        addRecommendation("Consider closing underutilized counters in Terminal C to optimize resource allocation", "fas-minus-circle");
    }
    
    private void addRecommendation(String text, String iconLiteral) {
        HBox recommendationBox = new HBox(10);
        recommendationBox.setPadding(new Insets(5, 0, 5, 0));
        
        FontIcon icon = new FontIcon(iconLiteral);
        icon.getStyleClass().add("recommendation-icon");
        
        Label recommendationLabel = new Label(text);
        recommendationLabel.setWrapText(true);
        
        recommendationBox.getChildren().addAll(icon, recommendationLabel);
        recommendationsContainer.getChildren().add(recommendationBox);
    }
    
    @FXML
    private void optimizeStaffing() {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Optimize Staff Allocation");
        alert.setHeaderText("Apply Automated Staff Optimization");
        alert.setContentText("The system has calculated the optimal staff allocation based on current passenger flow and wait times. Would you like to apply these changes?");
        
        // Add detailed information
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(
            "Optimization details:\n\n" +
            "• Move 2 staff from Terminal C to Terminal A (peak hours: 10:00-14:00)\n" +
            "• Open 3 additional counters in Terminal B\n" +
            "• Close 2 underutilized counters in Terminal C\n" +
            "• Reassign specialized staff to handle passengers with special needs\n\n" +
            "Expected results:\n" +
            "• Reduce average wait time from 12 min to 8 min\n" +
            "• Improve passenger throughput by 23%\n" +
            "• Optimize staff utilization by 15%"
        );
        textArea.setPrefHeight(200);
        
        alert.getDialogPane().setExpandableContent(textArea);
        alert.getDialogPane().setExpanded(true);
        
        // Show options for optimization strategy
        ButtonType applyAllButton = new ButtonType("Apply All Changes");
        ButtonType applyPartialButton = new ButtonType("Apply Selected Changes");
        ButtonType cancelButton = ButtonType.CANCEL;
        
        alert.getButtonTypes().setAll(applyAllButton, applyPartialButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == applyAllButton) {
                // Apply all optimization changes
                updateStaffAllocation(true);
                
                // Show success message
                showSuccessMessage("Staff allocation optimized successfully!");
            } else if (result.get() == applyPartialButton) {
                // Show detailed selection dialog
                showDetailedOptimizationDialog();
            }
        }
    }
    
    private void updateStaffAllocation(boolean applyAll) {
        // Update staff allocation in the table
        Random random = new Random();
        
        // Simulate staff optimization changes
        for (CounterStaff staff : staffData) {
            // Only modify some entries to simulate optimization
            if (random.nextBoolean() && applyAll) {
                // Randomly adjust queue length and wait time to simulate improvement
                staff.setQueueLength(Math.max(0, staff.getQueueLength() - random.nextInt(4)));
                staff.setWaitTime(Math.max(0, staff.getWaitTime() - random.nextInt(6)));
                
                // Occasionally change status
                if (random.nextDouble() < 0.3) {
                    if ("Closed".equals(staff.getStatus())) {
                        staff.setStatus("Open");
                        staff.setStaffName(getRandomStaffName());
                        staff.setQueueLength(random.nextInt(5));
                        staff.setWaitTime(random.nextInt(10));
                    }
                }
            }
        }
        
        // Add some new staff entries
        if (applyAll) {
            staffData.add(new CounterStaff("B", "4", "Open", "David Wilson", 4, 7));
            staffData.add(new CounterStaff("B", "5", "Open", "Lisa Anderson", 3, 6));
        }
        
        // Update statistics
        avgWaitTimeValue.setText("8 min");
        activeCountersValue.setText("20/24");
        staffOnDutyValue.setText("34");
        
        // Refresh the table
        staffingTable.refresh();
        
        // Refresh heatmap to show the changes
        refreshHeatmap();
    }
    
    private void showDetailedOptimizationDialog() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Select Optimization Changes");
        dialog.setHeaderText("Choose which optimization changes to apply");
        
        ButtonType applyButton = new ButtonType("Apply Selected", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButton, ButtonType.CANCEL);
        
        // Create checkboxes for each optimization option
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 10, 10, 10));
        
        CheckBox option1 = new CheckBox("Move 2 staff from Terminal C to Terminal A");
        CheckBox option2 = new CheckBox("Open 3 additional counters in Terminal B");
        CheckBox option3 = new CheckBox("Close 2 underutilized counters in Terminal C");
        CheckBox option4 = new CheckBox("Reassign specialized staff for special needs");
        
        // Select all by default
        option1.setSelected(true);
        option2.setSelected(true);
        option3.setSelected(true);
        option4.setSelected(true);
        
        content.getChildren().addAll(
            new Label("Select changes to apply:"),
            option1, option2, option3, option4
        );
        
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == applyButton) {
                List<String> selectedOptions = new ArrayList<>();
                if (option1.isSelected()) selectedOptions.add("option1");
                if (option2.isSelected()) selectedOptions.add("option2");
                if (option3.isSelected()) selectedOptions.add("option3");
                if (option4.isSelected()) selectedOptions.add("option4");
                return selectedOptions;
            }
            return null;
        });
        
        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(selectedOptions -> {
            // Apply partial optimization based on selected options
            boolean applyMajorChanges = selectedOptions.size() > 2;
            updateStaffAllocation(applyMajorChanges);
            
            // Show success message
            showSuccessMessage("Selected staff allocation changes applied successfully!");
        });
    }
    
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String getRandomStaffName() {
        String[] firstNames = {"John", "Mary", "James", "Patricia", "Robert", "Jennifer", "Michael", "Linda", "William", "Elizabeth"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor"};
        
        Random random = new Random();
        return firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
    }
    
    // Model class for counter staff
    public static class CounterStaff {
        private String terminal;
        private String counter;
        private String status;
        private String staffName;
        private int queueLength;
        private int waitTime;
        
        public CounterStaff(String terminal, String counter, String status, String staffName, int queueLength, int waitTime) {
            this.terminal = terminal;
            this.counter = counter;
            this.status = status;
            this.staffName = staffName;
            this.queueLength = queueLength;
            this.waitTime = waitTime;
        }
        
        public String getTerminal() { return terminal; }
        public void setTerminal(String terminal) { this.terminal = terminal; }
        
        public String getCounter() { return counter; }
        public void setCounter(String counter) { this.counter = counter; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getStaffName() { return staffName; }
        public void setStaffName(String staffName) { this.staffName = staffName; }
        
        public int getQueueLength() { return queueLength; }
        public void setQueueLength(int queueLength) { this.queueLength = queueLength; }
        
        public int getWaitTime() { return waitTime; }
        public void setWaitTime(int waitTime) { this.waitTime = waitTime; }
    }
} 
package com.yousif.attemp2;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.List;

public class PassengerFlowAnalysisController {

    @FXML
    private Label currentPassengersValue;
    
    @FXML
    private Label peakCongestionValue;
    
    @FXML
    private Label avgProcessingTimeValue;
    
    @FXML
    private Label efficiencyRatingValue;
    
    @FXML
    private ComboBox<String> terminalSelector;
    
    @FXML
    private Pane heatmapPane;
    
    @FXML
    private LineChart<String, Number> hourlyTrafficChart;
    
    @FXML
    private PieChart bottleneckChart;
    
    @FXML
    private TableView<CongestionPoint> congestionTable;
    
    @FXML
    private TableColumn<CongestionPoint, String> locationColumn;
    
    @FXML
    private TableColumn<CongestionPoint, String> congestionLevelColumn;
    
    @FXML
    private TableColumn<CongestionPoint, String> waitTimeColumn;
    
    @FXML
    private TableColumn<CongestionPoint, String> peakTimeColumn;
    
    @FXML
    private TableColumn<CongestionPoint, Integer> recommendedStaffColumn;
    
    @FXML
    private TableColumn<CongestionPoint, CongestionPoint> actionsColumn;
    
    @FXML
    private VBox recommendationsContainer;
    
    private ObservableList<CongestionPoint> congestionData = FXCollections.observableArrayList();
    
    // Terminal layouts stored as: terminalId -> Map of (locationId -> Point2D)
    private Map<String, Map<String, Point2D>> terminalLayouts = new HashMap<>();
    
    // Data collection variables for real-time updates
    private Timer dataCollectionTimer;
    private LocalDateTime lastUpdated;
    
    // Report template configurations
    private List<ReportTemplate> reportTemplates = new ArrayList<>();
    
    @FXML
    public void initialize() {
        setupStatistics();
        setupTerminalSelector();
        setupCongestionTable();
        loadCongestionData();
        createHeatmap("T1");
        setupHourlyTrafficChart();
        setupBottleneckChart();
        generateRecommendations();
        initializeReportTemplates();
        
        // Start real-time data collection
        startDataCollection();
    }
    
    private void setupStatistics() {
        currentPassengersValue.setText("3,452");
        peakCongestionValue.setText("Security (T1)");
        avgProcessingTimeValue.setText("14 min");
        efficiencyRatingValue.setText("84%");
    }
    
    private void setupTerminalSelector() {
        terminalSelector.getItems().addAll("Terminal 1", "Terminal 2", "Terminal 3", "All Terminals");
        terminalSelector.setValue("Terminal 1");
        
        // Create terminal layout data
        initializeTerminalLayouts();
    }
    
    private void initializeTerminalLayouts() {
        // Terminal 1 layout
        Map<String, Point2D> t1Layout = new HashMap<>();
        t1Layout.put("Check-in", new Point2D(100, 80));
        t1Layout.put("Security", new Point2D(250, 150));
        t1Layout.put("Immigration", new Point2D(400, 80));
        t1Layout.put("Retail Area", new Point2D(550, 150));
        t1Layout.put("Gates", new Point2D(700, 80));
        terminalLayouts.put("T1", t1Layout);
        
        // Terminal 2 layout
        Map<String, Point2D> t2Layout = new HashMap<>();
        t2Layout.put("Check-in", new Point2D(80, 100));
        t2Layout.put("Security", new Point2D(200, 200));
        t2Layout.put("Immigration", new Point2D(350, 100));
        t2Layout.put("Retail Area", new Point2D(500, 200));
        t2Layout.put("Gates", new Point2D(650, 100));
        terminalLayouts.put("T2", t2Layout);
        
        // Terminal 3 layout
        Map<String, Point2D> t3Layout = new HashMap<>();
        t3Layout.put("Check-in", new Point2D(120, 120));
        t3Layout.put("Security", new Point2D(280, 180));
        t3Layout.put("Immigration", new Point2D(450, 120));
        t3Layout.put("Retail Area", new Point2D(580, 180));
        t3Layout.put("Gates", new Point2D(720, 120));
        terminalLayouts.put("T3", t3Layout);
    }
    
    private void setupCongestionTable() {
        locationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));
        congestionLevelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCongestionLevel()));
        waitTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWaitTime()));
        peakTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPeakTime()));
        recommendedStaffColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRecommendedStaff()).asObject());
        
        // Color coding for congestion level
        congestionLevelColumn.setCellFactory(unused -> new TableCell<CongestionPoint, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    switch (item) {
                        case "Low":
                            setStyle("-fx-text-fill: #4caf50;"); // Green
                            break;
                        case "Medium":
                            setStyle("-fx-text-fill: #ff9800;"); // Orange
                            break;
                        case "High":
                            setStyle("-fx-text-fill: #f44336;"); // Red
                            break;
                        case "Critical":
                            setStyle("-fx-text-fill: #9c27b0;"); // Purple
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Actions column with buttons
        actionsColumn.setCellFactory(unused -> new TableCell<CongestionPoint, CongestionPoint>() {
            private final Button optimizeButton = new Button("Optimize");
            private final Button viewButton = new Button();
            
            {
                optimizeButton.getStyleClass().add("table-button");
                viewButton.getStyleClass().add("table-button");
                
                FontIcon viewIcon = new FontIcon("fas-eye");
                viewButton.setGraphic(viewIcon);
                
                optimizeButton.setOnAction(unused -> {
                    CongestionPoint point = getTableView().getItems().get(getIndex());
                    showOptimizationDialog(point);
                });
                
                viewButton.setOnAction(unused -> {
                    CongestionPoint point = getTableView().getItems().get(getIndex());
                    showCongestionDetails(point);
                });
            }
            
            @Override
            protected void updateItem(CongestionPoint item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.setAlignment(Pos.CENTER);
                    buttons.getChildren().addAll(viewButton, optimizeButton);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void loadCongestionData() {
        congestionData.addAll(
            new CongestionPoint("Security (T1)", "Critical", "25 min", "08:00-10:00", 12),
            new CongestionPoint("Check-in (T1)", "Medium", "12 min", "07:00-09:00", 8),
            new CongestionPoint("Immigration (T1)", "High", "18 min", "15:00-17:00", 10),
            new CongestionPoint("Security (T2)", "High", "20 min", "12:00-14:00", 9),
            new CongestionPoint("Check-in (T2)", "Low", "8 min", "10:00-12:00", 6),
            new CongestionPoint("Gates (T1)", "Medium", "10 min", "18:00-20:00", 7),
            new CongestionPoint("Retail Area (T1)", "Low", "5 min", "14:00-16:00", 4),
            new CongestionPoint("Immigration (T3)", "Medium", "15 min", "16:00-18:00", 8),
            new CongestionPoint("Security (T3)", "Medium", "15 min", "09:00-11:00", 8),
            new CongestionPoint("Gates (T2)", "Low", "8 min", "19:00-21:00", 6)
        );
        
        congestionTable.setItems(congestionData);
    }
    
    @FXML
    private void refreshHeatmap() {
        String terminalValue = terminalSelector.getValue();
        String terminalId;
        
        if (terminalValue.equals("All Terminals")) {
            terminalId = "All";
        } else {
            // Extract terminal number (e.g., "Terminal 1" -> "T1")
            terminalId = "T" + terminalValue.substring(terminalValue.length() - 1);
        }
        
        createHeatmap(terminalId);
    }
    
    private void createHeatmap(String terminalId) {
        heatmapPane.getChildren().clear();
        
        // If showing all terminals, just show a bar chart comparison instead
        if (terminalId.equals("All")) {
            createTerminalComparisonChart();
            return;
        }
        
        Map<String, Point2D> layout = terminalLayouts.get(terminalId);
        if (layout == null) return;
        
        // Background rectangle representing the terminal
        Rectangle background = new Rectangle(10, 10, 780, 280);
        background.setFill(Color.web("#1c202c"));
        background.setArcHeight(20);
        background.setArcWidth(20);
        background.setStroke(Color.web("#60519b"));
        background.setStrokeWidth(1);
        heatmapPane.getChildren().add(background);
        
        // Add a terminal label
        Text terminalLabel = new Text("Terminal " + terminalId.substring(1));
        terminalLabel.setFill(Color.WHITE);
        terminalLabel.setX(20);
        terminalLabel.setY(30);
        heatmapPane.getChildren().add(terminalLabel);
        
        // Add congestion points
        for (Map.Entry<String, Point2D> entry : layout.entrySet()) {
            String locationName = entry.getKey();
            Point2D position = entry.getValue();
            
            // Find congestion level for this location
            String congestionLevel = getCongestionLevelForLocation(locationName + " (" + terminalId + ")");
            
            // Add congestion point
            addCongestionPoint(locationName, position, congestionLevel);
        }
        
        // Add connection lines between areas
        String[] areas = {"Check-in", "Security", "Immigration", "Retail Area", "Gates"};
        for (int i = 0; i < areas.length - 1; i++) {
            Point2D start = layout.get(areas[i]);
            Point2D end = layout.get(areas[i + 1]);
            
            if (start != null && end != null) {
                javafx.scene.shape.Line connection = new javafx.scene.shape.Line(
                    start.getX(), start.getY(), end.getX(), end.getY());
                connection.setStroke(Color.GRAY);
                connection.setStrokeWidth(2);
                heatmapPane.getChildren().add(connection);
            }
        }
    }
    
    private void createTerminalComparisonChart() {
        // Create a bar chart showing congestion across terminals
        double barWidth = 150;
        double barHeight = 30;
        double spacing = 40;
        double startX = 50;
        double startY = 50;
        
        String[] terminals = {"T1", "T2", "T3"};
        String[] areas = {"Check-in", "Security", "Immigration", "Retail Area", "Gates"};
        
        // Add labels for areas
        for (int i = 0; i < areas.length; i++) {
            Text areaLabel = new Text(areas[i]);
            areaLabel.setFill(Color.WHITE);
            areaLabel.setX(startX - 30 - areaLabel.getLayoutBounds().getWidth());
            areaLabel.setY(startY + i * (barHeight + spacing) + barHeight / 2 + 5);
            heatmapPane.getChildren().add(areaLabel);
        }
        
        // Add labels for terminals
        for (int i = 0; i < terminals.length; i++) {
            Text terminalLabel = new Text("Terminal " + terminals[i].substring(1));
            terminalLabel.setFill(Color.WHITE);
            terminalLabel.setX(startX + i * (barWidth + 10) + barWidth / 2 - 30);
            terminalLabel.setY(startY - 10);
            heatmapPane.getChildren().add(terminalLabel);
        }
        
        // Create bars for each area/terminal combination
        for (int areaIndex = 0; areaIndex < areas.length; areaIndex++) {
            for (int terminalIndex = 0; terminalIndex < terminals.length; terminalIndex++) {
                String location = areas[areaIndex] + " (" + terminals[terminalIndex] + ")";
                String congestionLevel = getCongestionLevelForLocation(location);
                
                Rectangle bar = new Rectangle(
                    startX + terminalIndex * (barWidth + 10),
                    startY + areaIndex * (barHeight + spacing),
                    barWidth,
                    barHeight
                );
                
                // Set color based on congestion level
                switch (congestionLevel) {
                    case "Low":
                        bar.setFill(Color.web("#4caf50", 0.7)); // Green
                        break;
                    case "Medium":
                        bar.setFill(Color.web("#ff9800", 0.7)); // Orange
                        break;
                    case "High":
                        bar.setFill(Color.web("#f44336", 0.7)); // Red
                        break;
                    case "Critical":
                        bar.setFill(Color.web("#9c27b0", 0.7)); // Purple
                        break;
                    default:
                        bar.setFill(Color.web("#bfc0d1", 0.7)); // Light gray
                        break;
                }
                
                bar.setArcHeight(5);
                bar.setArcWidth(5);
                bar.setStroke(Color.WHITE);
                bar.setStrokeWidth(0.5);
                
                // Tooltip for the bar
                String waitTime = getWaitTimeForLocation(location);
                Tooltip tooltip = new Tooltip(location + "\nCongestion: " + congestionLevel + "\nWait Time: " + waitTime);
                Tooltip.install(bar, tooltip);
                
                heatmapPane.getChildren().add(bar);
                
                // Add text on bar
                Text congestionText = new Text(congestionLevel);
                congestionText.setFill(Color.WHITE);
                congestionText.setX(bar.getX() + barWidth / 2 - 20);
                congestionText.setY(bar.getY() + barHeight / 2 + 5);
                heatmapPane.getChildren().add(congestionText);
            }
        }
    }
    
    private void addCongestionPoint(String locationName, Point2D position, String congestionLevel) {
        double circleRadius = 30;
        
        Circle circle = new Circle(position.getX(), position.getY(), circleRadius);
        
        // Set color based on congestion level
        switch (congestionLevel) {
            case "Low":
                circle.setFill(Color.web("#4caf50", 0.7)); // Green
                break;
            case "Medium":
                circle.setFill(Color.web("#ff9800", 0.7)); // Orange
                break;
            case "High":
                circle.setFill(Color.web("#f44336", 0.7)); // Red
                break;
            case "Critical":
                circle.setFill(Color.web("#9c27b0", 0.7)); // Purple
                break;
            default:
                circle.setFill(Color.web("#bfc0d1", 0.7)); // Light gray
                break;
        }
        
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(1);
        
        Text nameText = new Text(locationName);
        nameText.setFill(Color.WHITE);
        nameText.setX(position.getX() - nameText.getLayoutBounds().getWidth() / 2);
        nameText.setY(position.getY() + 5);
        
        // Get wait time for this location
        String waitTime = "N/A";
        for (CongestionPoint point : congestionData) {
            if (point.getLocation().startsWith(locationName)) {
                waitTime = point.getWaitTime();
                break;
            }
        }
        
        Text waitTimeText = new Text(waitTime);
        waitTimeText.setFill(Color.WHITE);
        waitTimeText.setX(position.getX() - waitTimeText.getLayoutBounds().getWidth() / 2);
        waitTimeText.setY(position.getY() + 20);
        
        heatmapPane.getChildren().addAll(circle, nameText, waitTimeText);
    }
    
    private String getCongestionLevelForLocation(String location) {
        for (CongestionPoint point : congestionData) {
            if (point.getLocation().equals(location)) {
                return point.getCongestionLevel();
            }
        }
        return "Low"; // Default
    }
    
    private String getWaitTimeForLocation(String location) {
        for (CongestionPoint point : congestionData) {
            if (point.getLocation().equals(location)) {
                return point.getWaitTime();
            }
        }
        return "N/A"; // Default
    }
    
    private void setupHourlyTrafficChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Passenger Traffic");
        
        // Add data points for each hour
        series.getData().add(new XYChart.Data<>("06:00", 1200));
        series.getData().add(new XYChart.Data<>("08:00", 2500));
        series.getData().add(new XYChart.Data<>("10:00", 1800));
        series.getData().add(new XYChart.Data<>("12:00", 2100));
        series.getData().add(new XYChart.Data<>("14:00", 1500));
        series.getData().add(new XYChart.Data<>("16:00", 2300));
        series.getData().add(new XYChart.Data<>("18:00", 2700));
        series.getData().add(new XYChart.Data<>("20:00", 2000));
        series.getData().add(new XYChart.Data<>("22:00", 1200));
        series.getData().add(new XYChart.Data<>("00:00", 500));
        
        hourlyTrafficChart.getData().add(series);
    }
    
    private void setupBottleneckChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Security", 35),
            new PieChart.Data("Check-in", 25),
            new PieChart.Data("Immigration", 20),
            new PieChart.Data("Gates", 15),
            new PieChart.Data("Retail Area", 5)
        );
        bottleneckChart.setData(pieChartData);
    }
    
    private void generateRecommendations() {
        recommendationsContainer.getChildren().clear();
        
        addRecommendation(
            "Increase staff at Terminal 1 Security from 8 to 12 during peak hours (08:00-10:00)",
            "fas-user-plus",
            "High"
        );
        
        addRecommendation(
            "Redirect passengers from Terminal 1 to Terminal 2 Check-in counters to balance load",
            "fas-random",
            "Medium"
        );
        
        addRecommendation(
            "Open additional immigration counters at Terminal 3 during afternoon peak (15:00-17:00)",
            "fas-door-open",
            "Medium"
        );
        
        addRecommendation(
            "Deploy mobile check-in staff with tablets in Terminal 1 queues",
            "fas-tablet-alt",
            "Medium"
        );
        
        addRecommendation(
            "Implement express security lane at Terminal 2 for passengers with no checked baggage",
            "fas-fast-forward",
            "Low"
        );
    }
    
    private void addRecommendation(String text, String iconLiteral, String priority) {
        HBox recommendationBox = new HBox(10);
        recommendationBox.setAlignment(Pos.CENTER_LEFT);
        recommendationBox.getStyleClass().add("recommendation-box");
        
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconColor(Color.WHITE);
        
        Label priorityLabel = new Label(priority);
        priorityLabel.setPrefWidth(70);
        
        switch (priority) {
            case "High":
                priorityLabel.getStyleClass().add("priority-high");
                break;
            case "Medium":
                priorityLabel.getStyleClass().add("priority-medium");
                break;
            case "Low":
                priorityLabel.getStyleClass().add("priority-low");
                break;
        }
        
        Label textLabel = new Label(text);
        textLabel.setWrapText(true);
        
        recommendationBox.getChildren().addAll(icon, priorityLabel, textLabel);
        recommendationsContainer.getChildren().add(recommendationBox);
    }
    
    private void showCongestionDetails(CongestionPoint point) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congestion Point Details");
        alert.setHeaderText("Location: " + point.getLocation());
        
        // Create content for the dialog
        StringBuilder content = new StringBuilder();
        content.append("Congestion Level: ").append(point.getCongestionLevel()).append("\n");
        content.append("Current Wait Time: ").append(point.getWaitTime()).append("\n");
        content.append("Peak Hours: ").append(point.getPeakTime()).append("\n");
        content.append("Recommended Staff: ").append(point.getRecommendedStaff()).append("\n\n");
        content.append("Hourly Breakdown:").append("\n");
        content.append("- 06:00-08:00: Low congestion (5 min)\n");
        content.append("- 08:00-10:00: ").append(point.getCongestionLevel().equals("Critical") ? "Critical" : "High").append(" congestion (").append(point.getWaitTime()).append(")\n");
        content.append("- 10:00-12:00: Medium congestion (12 min)\n");
        content.append("- 12:00-14:00: Medium congestion (10 min)\n");
        content.append("- 14:00-16:00: Low congestion (8 min)\n");
        
        // Add historical data
        content.append("\nHistorical Data:\n");
        content.append("- Yesterday: ").append(getHistoricalCongestion(point.getLocation(), -1)).append("\n");
        content.append("- Last week: ").append(getHistoricalCongestion(point.getLocation(), -7)).append("\n");
        content.append("- Last month: ").append(getHistoricalCongestion(point.getLocation(), -30)).append("\n");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    private String getHistoricalCongestion(String location, int daysOffset) {
        // Simulate historical data
        if (location.contains("Security")) {
            return daysOffset == -1 ? "High (22 min)" : "Medium (15 min)";
        } else if (location.contains("Check-in")) {
            return daysOffset == -1 ? "Medium (12 min)" : "Low (8 min)";
        } else {
            return "Medium (10-15 min)";
        }
    }
    
    private void showOptimizationDialog(CongestionPoint point) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Optimize Congestion Point");
        alert.setHeaderText("Optimization for " + point.getLocation());
        
        // Create a custom layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label currentStaffLabel = new Label("Current Staff: " + (point.getRecommendedStaff() - 4));
        Label recommendedStaffLabel = new Label("Recommended Staff: " + point.getRecommendedStaff());
        
        Slider staffSlider = new Slider(point.getRecommendedStaff() - 4, point.getRecommendedStaff() + 4, point.getRecommendedStaff());
        staffSlider.setShowTickLabels(true);
        staffSlider.setShowTickMarks(true);
        staffSlider.setMajorTickUnit(1);
        staffSlider.setMinorTickCount(0);
        staffSlider.setBlockIncrement(1);
        staffSlider.setSnapToTicks(true);
        
        Label selectedStaffLabel = new Label("Selected Staff: " + (int) staffSlider.getValue());
        staffSlider.valueProperty().addListener((unused, __, newVal) -> 
            selectedStaffLabel.setText("Selected Staff: " + newVal.intValue()));
        
        Label impactLabel = new Label("Estimated Wait Time Impact:");
        ProgressBar impactBar = new ProgressBar(0.7);
        Label impactValueLabel = new Label("Expected Wait Time: ~" + 
            calculateExpectedWaitTime(point.getWaitTime(), point.getRecommendedStaff(), (int) staffSlider.getValue()));
        
        staffSlider.valueProperty().addListener((unused, __, newVal) -> {
            double ratio = (double) newVal.intValue() / point.getRecommendedStaff();
            impactBar.setProgress(Math.min(1.0, 2.0 - ratio));
            
            impactValueLabel.setText("Expected Wait Time: ~" + 
                calculateExpectedWaitTime(point.getWaitTime(), point.getRecommendedStaff(), newVal.intValue()));
        });
        
        content.getChildren().addAll(
            currentStaffLabel, recommendedStaffLabel, 
            new Separator(), 
            selectedStaffLabel, staffSlider,
            new Separator(),
            impactLabel, impactBar, impactValueLabel
        );
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
    
    private String calculateExpectedWaitTime(String currentWaitTime, int recommendedStaff, int selectedStaff) {
        // Extract the numeric part of the wait time
        int minutes = Integer.parseInt(currentWaitTime.split(" ")[0]);
        
        double ratio = (double) recommendedStaff / selectedStaff;
        int expectedMinutes = (int) Math.max(5, Math.min(40, minutes * ratio));
        
        return expectedMinutes + " min";
    }
    
    /**
     * Starts real-time data collection from system modules
     */
    private void startDataCollection() {
        if (dataCollectionTimer != null) {
            dataCollectionTimer.cancel();
        }
        
        dataCollectionTimer = new Timer(true);
        lastUpdated = LocalDateTime.now();
        
        dataCollectionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // This runs on a background thread, so we need to use Platform.runLater
                javafx.application.Platform.runLater(() -> {
                    updateRealTimeData();
                });
            }
        }, 30000, 30000);
        
        System.out.println("Real-time data collection started");
    }
    
    /**
     * Stops real-time data collection
     */
    public void stopDataCollection() {
        if (dataCollectionTimer != null) {
            dataCollectionTimer.cancel();
            dataCollectionTimer = null;
            System.out.println("Real-time data collection stopped");
        }
    }
    
    /**
     * Updates UI with real-time data from various airport systems
     */
    private void updateRealTimeData() {
        // In a real system, this would pull data from other modules via API calls or messaging
        
        // Simulate data updates
        int currentPassengers = 3000 + (int)(Math.random() * 1000);
        currentPassengersValue.setText(String.format("%,d", currentPassengers));
        
        // Update congestion levels randomly for demo purposes
        for (CongestionPoint point : congestionData) {
            if (Math.random() < 0.3) { // 30% chance to update a point
                updateCongestionPoint(point);
            }
        }
        
        // Refresh UI components
        congestionTable.refresh();
        createHeatmap(terminalSelector.getValue().equals("All Terminals") ? "All" : "T" + terminalSelector.getValue().substring(terminalSelector.getValue().length() - 1));
        
        // Update last updated timestamp
        lastUpdated = LocalDateTime.now();
        System.out.println("Data refreshed at: " + lastUpdated.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    /**
     * Updates a single congestion point with new data
     */
    private void updateCongestionPoint(CongestionPoint point) {
        String[] levels = {"Low", "Medium", "High", "Critical"};
        int currentLevel = 0;
        
        // Find current level
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].equals(point.getCongestionLevel())) {
                currentLevel = i;
                break;
            }
        }
        
        // Randomly shift up or down one level
        int shift = Math.random() < 0.5 ? -1 : 1;
        int newLevel = Math.max(0, Math.min(levels.length - 1, currentLevel + shift));
        
        // Update congestion level
        point.setCongestionLevel(levels[newLevel]);
        
        // Update wait time based on level
        switch (levels[newLevel]) {
            case "Low":
                point.setWaitTime((3 + (int)(Math.random() * 5)) + " min");
                break;
            case "Medium":
                point.setWaitTime((8 + (int)(Math.random() * 7)) + " min");
                break;
            case "High":
                point.setWaitTime((15 + (int)(Math.random() * 10)) + " min");
                break;
            case "Critical":
                point.setWaitTime((25 + (int)(Math.random() * 15)) + " min");
                break;
        }
    }
    
    /**
     * Initializes available report templates
     */
    private void initializeReportTemplates() {
        reportTemplates.clear();
        
        // Add standard templates
        reportTemplates.add(new ReportTemplate("Daily Flow Summary", 
                "Basic daily summary with passenger counts and bottlenecks", 
                new String[]{"passengerCounts", "waitTimes", "peakHours"}, "daily"));
                
        reportTemplates.add(new ReportTemplate("Comprehensive Analysis", 
                "Detailed analysis with all metrics and recommendations", 
                new String[]{"passengerCounts", "waitTimes", "peakHours", "congestionLevels", 
                        "staffingEfficiency", "recommendations"}, "detailed"));
                
        reportTemplates.add(new ReportTemplate("Executive Dashboard", 
                "High-level overview for management", 
                new String[]{"keySummary", "congestionHighlights", "efficiencyMetrics"}, "executive"));
                
        reportTemplates.add(new ReportTemplate("Staff Planning Report", 
                "Focused on optimizing staff allocation", 
                new String[]{"waitTimes", "peakHours", "recommendedStaffing", "efficiencyMetrics"}, "staffing"));
    }
    
    /**
     * Shows dialog for selecting report template and export options
     */
    @FXML
    private void generateReport() {
        // Create dialog
        Dialog<ReportSettings> dialog = new Dialog<>();
        dialog.setTitle("Generate Flow Analysis Report");
        dialog.setHeaderText("Configure Report Settings");
        
        // Set buttons
        ButtonType generateButtonType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);
        
        // Create content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        // Template selector
        Label templateLabel = new Label("Select Report Template:");
        ComboBox<ReportTemplate> templateCombo = new ComboBox<>();
        templateCombo.setItems(FXCollections.observableArrayList(reportTemplates));
        templateCombo.setCellFactory(unused -> new ListCell<ReportTemplate>() {
            @Override
            protected void updateItem(ReportTemplate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        templateCombo.setButtonCell(new ListCell<ReportTemplate>() {
            @Override
            protected void updateItem(ReportTemplate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        templateCombo.getSelectionModel().select(0);
        
        // Template description
        Label descriptionLabel = new Label("Template Description:");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefHeight(60);
        descriptionArea.setText(reportTemplates.get(0).getDescription());
        
        // Update description when template changes
        templateCombo.getSelectionModel().selectedItemProperty().addListener((unused, __, newVal) -> {
            if (newVal != null) {
                descriptionArea.setText(newVal.getDescription());
            }
        });
        
        // Format selector
        Label formatLabel = new Label("Export Format:");
        ComboBox<String> formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll("PDF", "Excel", "HTML", "CSV");
        formatCombo.setValue("PDF");
        
        // Date range
        Label dateRangeLabel = new Label("Data Range:");
        ComboBox<String> rangeCombo = new ComboBox<>();
        rangeCombo.getItems().addAll("Today", "Last 7 Days", "Last 30 Days", "Custom...");
        rangeCombo.setValue("Today");
        
        // Terminal selector
        Label terminalLabel = new Label("Terminal:");
        ComboBox<String> reportTerminalCombo = new ComboBox<>();
        reportTerminalCombo.getItems().addAll("All Terminals", "Terminal 1", "Terminal 2", "Terminal 3");
        reportTerminalCombo.setValue(terminalSelector.getValue());
        
        // Chart inclusion
        CheckBox includeChartsCheck = new CheckBox("Include Charts and Graphs");
        includeChartsCheck.setSelected(true);
        
        // Table inclusion
        CheckBox includeTablesCheck = new CheckBox("Include Data Tables");
        includeTablesCheck.setSelected(true);
        
        // Add all to content
        content.getChildren().addAll(
            templateLabel, templateCombo,
            descriptionLabel, descriptionArea,
            new Separator(),
            formatLabel, formatCombo,
            dateRangeLabel, rangeCombo,
            terminalLabel, reportTerminalCombo,
            new Separator(),
            includeChartsCheck, includeTablesCheck
        );
        
        dialog.getDialogPane().setContent(content);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generateButtonType) {
                return new ReportSettings(
                    templateCombo.getValue(),
                    formatCombo.getValue(),
                    rangeCombo.getValue(),
                    reportTerminalCombo.getValue(),
                    includeChartsCheck.isSelected(),
                    includeTablesCheck.isSelected()
                );
            }
            return null;
        });
        
        // Show dialog and process result
        dialog.showAndWait().ifPresent(settings -> {
            if (settings.getFormat().equals("PDF")) {
                exportToPdf(settings);
            } else if (settings.getFormat().equals("Excel")) {
                exportToExcel(settings);
            } else if (settings.getFormat().equals("HTML")) {
                exportToHtml(settings);
            } else if (settings.getFormat().equals("CSV")) {
                exportToCsv(settings);
            }
        });
    }
    
    /**
     * Exports report to PDF format (creates a text file with .pdf extension for demo)
     */
    private void exportToPdf(ReportSettings settings) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("passenger_flow_" + settings.getTemplate().getFilePrefix() + "_report.pdf");
        
        File file = fileChooser.showSaveDialog(null);
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Create report content
                StringBuilder reportContent = new StringBuilder();
                
                // Header
                reportContent.append("==========================================\n");
                reportContent.append("      PASSENGER FLOW ANALYSIS REPORT      \n");
                reportContent.append("==========================================\n\n");
                
                // Report metadata
                reportContent.append("Report Template: ").append(settings.getTemplate().getName()).append("\n");
                reportContent.append("Date Range: ").append(settings.getDateRange()).append("\n");
                reportContent.append("Terminal: ").append(settings.getTerminal()).append("\n");
                reportContent.append("Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
                
                reportContent.append("------------------------------------------\n");
                
                // Executive Summary
                reportContent.append("EXECUTIVE SUMMARY\n");
                reportContent.append("------------------------------------------\n");
                reportContent.append("Current Passengers: ").append(currentPassengersValue.getText()).append("\n");
                reportContent.append("Peak Congestion Location: ").append(peakCongestionValue.getText()).append("\n");
                reportContent.append("Average Processing Time: ").append(avgProcessingTimeValue.getText()).append("\n");
                reportContent.append("Overall Efficiency Rating: ").append(efficiencyRatingValue.getText()).append("\n\n");
                
                // Congestion data table if requested
                if (settings.isIncludeTables()) {
                    reportContent.append("CONGESTION POINTS\n");
                    reportContent.append("------------------------------------------\n");
                    
                    // Table header
                    reportContent.append(String.format("%-20s %-15s %-12s %-15s %-10s\n", 
                            "LOCATION", "CONGESTION", "WAIT TIME", "PEAK TIME", "REC. STAFF"));
                    reportContent.append("------------------------------------------\n");
                    
                    // Table data
                    for (CongestionPoint point : congestionData) {
                        reportContent.append(String.format("%-20s %-15s %-12s %-15s %-10s\n", 
                                truncateString(point.getLocation(), 20),
                                point.getCongestionLevel(),
                                point.getWaitTime(),
                                point.getPeakTime(),
                                point.getRecommendedStaff()));
                    }
                    reportContent.append("\n");
                }
                
                // Add recommendations section
                if (settings.getTemplate().hasSection("recommendations")) {
                    reportContent.append("OPTIMIZATION RECOMMENDATIONS\n");
                    reportContent.append("------------------------------------------\n");
                    
                    List<String> recommendations = new ArrayList<>();
                    recommendations.add("Increase staff at Terminal 1 Security from 8 to 12 during peak hours (08:00-10:00)");
                    recommendations.add("Redirect passengers from Terminal 1 to Terminal 2 Check-in counters to balance load");
                    recommendations.add("Open additional immigration counters at Terminal 3 during afternoon peak (15:00-17:00)");
                    recommendations.add("Deploy mobile check-in staff with tablets in Terminal 1 queues");
                    
                    for (int i = 0; i < recommendations.size(); i++) {
                        reportContent.append((i+1) + ". " + recommendations.get(i)).append("\n");
                    }
                    reportContent.append("\n");
                }
                
                // Additional sections based on template
                if (settings.getTemplate().hasSection("staffingEfficiency")) {
                    reportContent.append("STAFFING EFFICIENCY ANALYSIS\n");
                    reportContent.append("------------------------------------------\n");
                    reportContent.append("Based on current passenger flow metrics, staffing efficiency is at ")
                               .append(efficiencyRatingValue.getText())
                               .append(". Optimal staff distribution would reduce waiting times by approximately 18% during peak hours.\n\n");
                }
                
                // Footer
                reportContent.append("------------------------------------------\n");
                reportContent.append("ELkaror International Airport - Passenger Flow Analysis\n");
                
                // Write to file
                writer.write(reportContent.toString());
                
                // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Report Generation");
                alert.setHeaderText("Report Generated");
                alert.setContentText("Report saved to: " + file.getAbsolutePath() + "\n\n" + 
                                    "Template: " + settings.getTemplate().getName() + "\n" +
                                    "Date Range: " + settings.getDateRange() + "\n" +
                                    "Terminal: " + settings.getTerminal());
                alert.showAndWait();
            } catch (IOException e) {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to generate report");
                alert.setContentText("Error: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Helper method to truncate strings if they're too long
     */
    private String truncateString(String input, int maxLength) {
        if (input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Exports report to Excel format (creates a CSV file for demo)
     */
    private void exportToExcel(ReportSettings settings) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.csv"));
        fileChooser.setInitialFileName("passenger_flow_" + settings.getTemplate().getFilePrefix() + "_report.csv");
        
        File file = fileChooser.showSaveDialog(null);
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                StringBuilder csvContent = new StringBuilder();
                
                // Header row for summary
                csvContent.append("Report Information\n");
                csvContent.append("Template,").append(settings.getTemplate().getName()).append("\n");
                csvContent.append("Date Range,").append(settings.getDateRange()).append("\n");
                csvContent.append("Terminal,").append(settings.getTerminal()).append("\n");
                csvContent.append("Generated On,").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
                
                // Key metrics
                csvContent.append("Key Metrics\n");
                csvContent.append("Current Passengers,").append(currentPassengersValue.getText()).append("\n");
                csvContent.append("Peak Congestion,").append(peakCongestionValue.getText()).append("\n");
                csvContent.append("Average Processing Time,").append(avgProcessingTimeValue.getText()).append("\n");
                csvContent.append("Efficiency Rating,").append(efficiencyRatingValue.getText()).append("\n\n");
                
                // Congestion data
                if (settings.isIncludeTables()) {
                    csvContent.append("Congestion Points\n");
                    csvContent.append("Location,Congestion Level,Wait Time,Peak Time,Recommended Staff\n");
                    
                    for (CongestionPoint point : congestionData) {
                        csvContent.append(point.getLocation()).append(",")
                                 .append(point.getCongestionLevel()).append(",")
                                 .append(point.getWaitTime()).append(",")
                                 .append(point.getPeakTime()).append(",")
                                 .append(point.getRecommendedStaff()).append("\n");
                    }
                }
                
                // Write to file
                writer.write(csvContent.toString());
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Report Generation");
                alert.setHeaderText("CSV Report Generated");
            alert.setContentText("Report saved to: " + file.getAbsolutePath());
            alert.showAndWait();
            } catch (IOException e) {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to generate CSV report");
                alert.setContentText("Error: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Exports report to HTML format
     */
    private void exportToHtml(ReportSettings settings) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save HTML Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
        fileChooser.setInitialFileName("passenger_flow_" + settings.getTemplate().getFilePrefix() + "_report.html");
        
        File file = fileChooser.showSaveDialog(null);
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                StringBuilder htmlContent = new StringBuilder();
                
                // HTML Header
                htmlContent.append("<!DOCTYPE html>\n");
                htmlContent.append("<html lang=\"en\">\n");
                htmlContent.append("<head>\n");
                htmlContent.append("  <meta charset=\"UTF-8\">\n");
                htmlContent.append("  <title>Passenger Flow Analysis Report</title>\n");
                htmlContent.append("  <style>\n");
                htmlContent.append("    body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }\n");
                htmlContent.append("    h1 { color: #333366; text-align: center; }\n");
                htmlContent.append("    h2 { color: #333366; margin-top: 30px; }\n");
                htmlContent.append("    .metadata { background-color: #f5f5f5; padding: 15px; border-radius: 5px; }\n");
                htmlContent.append("    table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
                htmlContent.append("    th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }\n");
                htmlContent.append("    th { background-color: #333366; color: white; }\n");
                htmlContent.append("    tr:nth-child(even) { background-color: #f5f5f5; }\n");
                htmlContent.append("    .low { color: green; }\n");
                htmlContent.append("    .medium { color: orange; }\n");
                htmlContent.append("    .high { color: red; }\n");
                htmlContent.append("    .critical { color: purple; }\n");
                htmlContent.append("    .footer { margin-top: 50px; text-align: center; font-size: 12px; color: #666; }\n");
                htmlContent.append("  </style>\n");
                htmlContent.append("</head>\n");
                htmlContent.append("<body>\n");
                
                // Header content
                htmlContent.append("  <h1>Passenger Flow Analysis Report</h1>\n");
                
                // Metadata
                htmlContent.append("  <div class=\"metadata\">\n");
                htmlContent.append("    <p><strong>Report Template:</strong> ").append(settings.getTemplate().getName()).append("</p>\n");
                htmlContent.append("    <p><strong>Date Range:</strong> ").append(settings.getDateRange()).append("</p>\n");
                htmlContent.append("    <p><strong>Terminal:</strong> ").append(settings.getTerminal()).append("</p>\n");
                htmlContent.append("    <p><strong>Generated on:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>\n");
                htmlContent.append("  </div>\n");
                
                // Executive Summary
                htmlContent.append("  <h2>Executive Summary</h2>\n");
                htmlContent.append("  <table>\n");
                htmlContent.append("    <tr><th>Metric</th><th>Value</th></tr>\n");
                htmlContent.append("    <tr><td>Current Passengers</td><td>").append(currentPassengersValue.getText()).append("</td></tr>\n");
                htmlContent.append("    <tr><td>Peak Congestion Location</td><td>").append(peakCongestionValue.getText()).append("</td></tr>\n");
                htmlContent.append("    <tr><td>Average Processing Time</td><td>").append(avgProcessingTimeValue.getText()).append("</td></tr>\n");
                htmlContent.append("    <tr><td>Overall Efficiency Rating</td><td>").append(efficiencyRatingValue.getText()).append("</td></tr>\n");
                htmlContent.append("  </table>\n");
                
                // Congestion Points
                if (settings.isIncludeTables()) {
                    htmlContent.append("  <h2>Congestion Points</h2>\n");
                    htmlContent.append("  <table>\n");
                    htmlContent.append("    <tr><th>Location</th><th>Congestion Level</th><th>Wait Time</th><th>Peak Time</th><th>Recommended Staff</th></tr>\n");
                    
                    for (CongestionPoint point : congestionData) {
                        String cssClass;
                        switch (point.getCongestionLevel()) {
                            case "Low": cssClass = "low"; break;
                            case "Medium": cssClass = "medium"; break;
                            case "High": cssClass = "high"; break;
                            case "Critical": cssClass = "critical"; break;
                            default: cssClass = ""; break;
                        }
                        
                        htmlContent.append("    <tr>\n");
                        htmlContent.append("      <td>").append(point.getLocation()).append("</td>\n");
                        htmlContent.append("      <td class=\"").append(cssClass).append("\">").append(point.getCongestionLevel()).append("</td>\n");
                        htmlContent.append("      <td>").append(point.getWaitTime()).append("</td>\n");
                        htmlContent.append("      <td>").append(point.getPeakTime()).append("</td>\n");
                        htmlContent.append("      <td>").append(point.getRecommendedStaff()).append("</td>\n");
                        htmlContent.append("    </tr>\n");
                    }
                    htmlContent.append("  </table>\n");
                }
                
                // Recommendations
                if (settings.getTemplate().hasSection("recommendations")) {
                    htmlContent.append("  <h2>Optimization Recommendations</h2>\n");
                    htmlContent.append("  <ol>\n");
                    
                    List<String> recommendations = new ArrayList<>();
                    recommendations.add("Increase staff at Terminal 1 Security from 8 to 12 during peak hours (08:00-10:00)");
                    recommendations.add("Redirect passengers from Terminal 1 to Terminal 2 Check-in counters to balance load");
                    recommendations.add("Open additional immigration counters at Terminal 3 during afternoon peak (15:00-17:00)");
                    recommendations.add("Deploy mobile check-in staff with tablets in Terminal 1 queues");
                    
                    for (String recommendation : recommendations) {
                        htmlContent.append("    <li>").append(recommendation).append("</li>\n");
                    }
                    htmlContent.append("  </ol>\n");
                }
                
                // Additional sections based on template
                if (settings.getTemplate().hasSection("staffingEfficiency")) {
                    htmlContent.append("  <h2>Staffing Efficiency Analysis</h2>\n");
                    htmlContent.append("  <p>Based on current passenger flow metrics, staffing efficiency is at ")
                             .append(efficiencyRatingValue.getText())
                             .append(". Optimal staff distribution would reduce waiting times by approximately 18% during peak hours.</p>\n");
                }
                
                // Footer
                htmlContent.append("  <div class=\"footer\">\n");
                htmlContent.append("    <p>ELkaror International Airport - Passenger Flow Analysis</p>\n");
                htmlContent.append("  </div>\n");
                
                // Close HTML tags
                htmlContent.append("</body>\n");
                htmlContent.append("</html>\n");
                
                // Write to file
                writer.write(htmlContent.toString());
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Report Generation");
                alert.setHeaderText("HTML Report Generated");
                alert.setContentText("Report saved to: " + file.getAbsolutePath());
                alert.showAndWait();
            } catch (IOException e) {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to generate HTML report");
                alert.setContentText("Error: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Exports report to CSV format
     */
    private void exportToCsv(ReportSettings settings) {
        // Reuse the Excel export method since it creates CSV
        exportToExcel(settings);
    }
    
    @FXML
    private void applyRecommendations() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Apply Recommendations");
        alert.setHeaderText("Apply All Optimization Recommendations?");
        alert.setContentText("This will automatically adjust staffing levels and passenger routing based on the system recommendations. Do you want to proceed?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Simulate applying recommendations
                for (CongestionPoint point : congestionData) {
                    if (point.getCongestionLevel().equals("Critical")) {
                        point.setCongestionLevel("High");
                        point.setWaitTime("18 min");
                    } else if (point.getCongestionLevel().equals("High")) {
                        point.setCongestionLevel("Medium");
                        point.setWaitTime("12 min");
                    }
                }
                
                congestionTable.refresh();
                createHeatmap(terminalSelector.getValue().equals("All Terminals") ? "All" : "T" + terminalSelector.getValue().substring(terminalSelector.getValue().length() - 1));
                
                // Update statistics
                peakCongestionValue.setText("Immigration (T1)");
                avgProcessingTimeValue.setText("11 min");
                efficiencyRatingValue.setText("91%");
                
                // Show success alert
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Recommendations Applied");
                successAlert.setHeaderText("Optimization Successful");
                successAlert.setContentText("All recommendations have been applied. Staff schedules and routing have been updated.");
                successAlert.showAndWait();
            }
        });
    }
    
    public static class CongestionPoint {
        private final String location;
        private String congestionLevel;
        private String waitTime;
        private final String peakTime;
        private final int recommendedStaff;
        
        public CongestionPoint(String location, String congestionLevel, String waitTime, String peakTime, int recommendedStaff) {
            this.location = location;
            this.congestionLevel = congestionLevel;
            this.waitTime = waitTime;
            this.peakTime = peakTime;
            this.recommendedStaff = recommendedStaff;
        }
        
        public String getLocation() { return location; }
        public String getCongestionLevel() { return congestionLevel; }
        public void setCongestionLevel(String congestionLevel) { this.congestionLevel = congestionLevel; }
        public String getWaitTime() { return waitTime; }
        public void setWaitTime(String waitTime) { this.waitTime = waitTime; }
        public String getPeakTime() { return peakTime; }
        public int getRecommendedStaff() { return recommendedStaff; }
    }
    
    /**
     * Class to represent a report template
     */
    public static class ReportTemplate {
        private final String name;
        private final String description;
        private final String[] includedSections;
        private final String filePrefix;
        
        public ReportTemplate(String name, String description, String[] includedSections, String filePrefix) {
            this.name = name;
            this.description = description;
            this.includedSections = includedSections;
            this.filePrefix = filePrefix;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String[] getIncludedSections() { return includedSections; }
        public String getFilePrefix() { return filePrefix; }
        
        public boolean hasSection(String sectionName) {
            for (String section : includedSections) {
                if (section.equals(sectionName)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * Class to hold report generation settings
     */
    public static class ReportSettings {
        private final ReportTemplate template;
        private final String format;
        private final String dateRange;
        private final String terminal;
        private final boolean includeCharts;
        private final boolean includeTables;
        
        public ReportSettings(ReportTemplate template, String format, String dateRange, 
                             String terminal, boolean includeCharts, boolean includeTables) {
            this.template = template;
            this.format = format;
            this.dateRange = dateRange;
            this.terminal = terminal;
            this.includeCharts = includeCharts;
            this.includeTables = includeTables;
        }
        
        public ReportTemplate getTemplate() { return template; }
        public String getFormat() { return format; }
        public String getDateRange() { return dateRange; }
        public String getTerminal() { return terminal; }
        public boolean isIncludeCharts() { return includeCharts; }
        public boolean isIncludeTables() { return includeTables; }
    }
} 
package com.yousif.attemp2;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class DashboardController {

    @FXML
    private Label activeFlightsValue;
    
    @FXML
    private Label passengersValue;
    
    @FXML
    private Label gateUtilizationValue;
    
    @FXML
    private Label onTimePerformanceValue;
    
    @FXML
    private AreaChart<String, Number> passengerTrafficChart;
    
    @FXML
    private PieChart flightStatusChart;
    
    @FXML
    private Label serverStatusLabel;
    
    @FXML
    private ProgressBar serverStatusBar;
    
    @FXML
    private Label databaseStatusLabel;
    
    @FXML
    private ProgressBar databaseStatusBar;
    
    @FXML
    private Label networkStatusLabel;
    
    @FXML
    private ProgressBar networkStatusBar;
    
    @FXML
    private Label securityStatusLabel;
    
    @FXML
    private ProgressBar securityStatusBar;
    
    @FXML
    private VBox activityFeedContainer;
    
    private final Random random = new Random();
    
    @FXML
    public void initialize() {
        loadStatistics();
        setupCharts();
        setupSystemHealth();
        loadActivityFeed();
        
        // Simulate real-time updates
        startDataUpdates();
    }
    
    private void loadStatistics() {
        activeFlightsValue.setText("123");
        passengersValue.setText("45,682");
        gateUtilizationValue.setText("86%");
        onTimePerformanceValue.setText("92%");
    }
    
    private void setupCharts() {
        // Passenger Traffic Chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("00:00", 1200));
        series.getData().add(new XYChart.Data<>("02:00", 850));
        series.getData().add(new XYChart.Data<>("04:00", 600));
        series.getData().add(new XYChart.Data<>("06:00", 1500));
        series.getData().add(new XYChart.Data<>("08:00", 3200));
        series.getData().add(new XYChart.Data<>("10:00", 4100));
        series.getData().add(new XYChart.Data<>("12:00", 3800));
        series.getData().add(new XYChart.Data<>("14:00", 4300));
        series.getData().add(new XYChart.Data<>("16:00", 3900));
        series.getData().add(new XYChart.Data<>("18:00", 3500));
        series.getData().add(new XYChart.Data<>("20:00", 2800));
        series.getData().add(new XYChart.Data<>("22:00", 1900));
        
        passengerTrafficChart.getData().add(series);
        
        // Flight Status Chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("On Time", 75),
                new PieChart.Data("Delayed", 15),
                new PieChart.Data("Cancelled", 5),
                new PieChart.Data("Diverted", 5)
        );
        
        flightStatusChart.setData(pieChartData);
        flightStatusChart.setLabelsVisible(true);
    }
    
    private void setupSystemHealth() {
        // Set progress bars and labels
        serverStatusBar.setProgress(0.98);
        databaseStatusBar.setProgress(0.95);
        networkStatusBar.setProgress(0.99);
        securityStatusBar.setProgress(1.0);
        
        // Set status labels
        serverStatusLabel.setText("Normal");
        databaseStatusLabel.setText("Normal");
        networkStatusLabel.setText("Normal");
        securityStatusLabel.setText("Normal");
    }
    
    private void loadActivityFeed() {
        addActivityItem("Flight UA237 arrived at Gate B8", "5 minutes ago", "fas-plane-arrival", "status-normal");
        addActivityItem("Security checkpoint 3 at capacity", "12 minutes ago", "fas-exclamation-triangle", "status-warning");
        addActivityItem("Gate A12 requires maintenance", "25 minutes ago", "fas-tools", "status-warning");
        addActivityItem("Baggage carousel 4 operational again", "32 minutes ago", "fas-suitcase", "status-normal");
        addActivityItem("Flight BA145 delayed by 45 minutes", "45 minutes ago", "fas-clock", "status-warning");
    }
    
    private void addActivityItem(String message, String time, String icon, String statusClass) {
        HBox activityItem = new HBox();
        activityItem.setSpacing(10);
        activityItem.setPadding(new Insets(5, 0, 5, 0));
        
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.getStyleClass().add(statusClass);
        
        VBox textContainer = new VBox();
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("activity-message");
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("activity-time");
        
        textContainer.getChildren().addAll(messageLabel, timeLabel);
        activityItem.getChildren().addAll(fontIcon, textContainer);
        
        activityFeedContainer.getChildren().add(activityItem);
    }
    
    private void startDataUpdates() {
        // Simulate real-time data updates
        Thread updateThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(5000); // Update every 5 seconds
                    Platform.runLater(this::updateRandomData);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        updateThread.setDaemon(true);
        updateThread.start();
    }
    
    private void updateRandomData() {
        // Update active flights (random fluctuation)
        int flights = Integer.parseInt(activeFlightsValue.getText()) + random.nextInt(5) - 2;
        activeFlightsValue.setText(String.valueOf(Math.max(flights, 100)));
        
        // Add new activity occasionally
        if (random.nextInt(3) == 0) {
            String[] messages = {
                "Flight DL482 boarding at Gate C15",
                "Baggage claim area B congestion reported",
                "Weather alert: Light rain expected",
                "Flight AA219 arrived 10 minutes early",
                "Security checkpoint 2 wait time: 8 minutes"
            };
            
            String[] icons = {
                "fas-plane-departure",
                "fas-suitcase",
                "fas-cloud-rain",
                "fas-plane-arrival",
                "fas-user-shield"
            };
            
            String[] statuses = {
                "status-normal",
                "status-warning",
                "status-warning",
                "status-normal",
                "status-normal"
            };
            
            int index = random.nextInt(messages.length);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String time = "Just now (" + LocalDateTime.now().format(formatter) + ")";
            
            // Add at the top of the feed
            addActivityItemAtTop(messages[index], time, icons[index], statuses[index]);
            
            // Remove oldest item if there are more than 8
            if (activityFeedContainer.getChildren().size() > 8) {
                activityFeedContainer.getChildren().remove(activityFeedContainer.getChildren().size() - 1);
            }
        }
    }
    
    private void addActivityItemAtTop(String message, String time, String icon, String statusClass) {
        HBox activityItem = new HBox();
        activityItem.setSpacing(10);
        activityItem.setPadding(new Insets(5, 0, 5, 0));
        
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.getStyleClass().add(statusClass);
        
        VBox textContainer = new VBox();
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("activity-message");
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("activity-time");
        
        textContainer.getChildren().addAll(messageLabel, timeLabel);
        activityItem.getChildren().addAll(fontIcon, textContainer);
        
        activityFeedContainer.getChildren().add(0, activityItem);
    }
} 
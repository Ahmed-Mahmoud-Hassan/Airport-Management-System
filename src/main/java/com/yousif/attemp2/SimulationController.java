package com.yousif.attemp2;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.*;

public class SimulationController {

    @FXML
    private Canvas simulationCanvas;
    
    @FXML
    private Slider passengerRateSlider;
    
    @FXML
    private ComboBox<String> simulationTypeComboBox;
    
    @FXML
    private Button startSimulationBtn;
    
    @FXML
    private Button stopSimulationBtn;
    
    @FXML
    private Button resetSimulationBtn;
    
    @FXML
    private TableView<SimulationMetric> metricsTable;
    
    @FXML
    private TableColumn<SimulationMetric, String> metricNameColumn;
    
    @FXML
    private TableColumn<SimulationMetric, String> metricValueColumn;
    
    @FXML
    private LineChart<Number, Number> flowRateChart;
    
    @FXML
    private NumberAxis xAxis;
    
    @FXML
    private NumberAxis yAxis;
    
    private GraphicsContext gc;
    private AnimationTimer animationTimer;
    private Timeline passengerGeneratorTimeline;
    private List<Passenger> passengers = new ArrayList<>();
    private Map<String, Integer> checkpointCounts = new HashMap<>();
    private int totalPassengersProcessed = 0;
    private int totalPassengersCreated = 0;
    private int simulationTimeSeconds = 0;
    private XYChart.Series<Number, Number> flowRateSeries;
    private ObservableList<SimulationMetric> metrics = FXCollections.observableArrayList();
    
    // Airport zones (x, y, width, height)
    private final int[] checkInZone = {30, 100, 100, 150};
    private final int[] securityZone = {160, 100, 100, 150};
    private final int[] gateZone = {290, 100, 100, 150};
    private final int[] boardingZone = {420, 100, 100, 150};
    
    @FXML
    public void initialize() {
        setupUI();
        setupCanvas();
        setupMetricsTable();
        setupFlowRateChart();
        
        stopSimulationBtn.setDisable(true);
    }
    
    private void setupUI() {
        simulationTypeComboBox.setItems(FXCollections.observableArrayList(
            "Normal Day Flow",
            "Peak Hour Rush",
            "Low Traffic Period"
        ));
        simulationTypeComboBox.getSelectionModel().selectFirst();
        
        passengerRateSlider.setMin(1);
        passengerRateSlider.setMax(10);
        passengerRateSlider.setValue(3);
        
        startSimulationBtn.setOnAction(e -> startSimulation());
        stopSimulationBtn.setOnAction(e -> stopSimulation());
        resetSimulationBtn.setOnAction(e -> resetSimulation());
    }
    
    private void setupCanvas() {
        gc = simulationCanvas.getGraphicsContext2D();
        drawAirportLayout();
    }
    
    private void setupMetricsTable() {
        metricNameColumn.setCellValueFactory(cellData -> 
            cellData.getValue().nameProperty());
        
        metricValueColumn.setCellValueFactory(cellData -> 
            cellData.getValue().valueProperty());
        
        metrics.add(new SimulationMetric("Total Passengers", "0"));
        metrics.add(new SimulationMetric("Check-in Queue", "0"));
        metrics.add(new SimulationMetric("Security Queue", "0"));
        metrics.add(new SimulationMetric("Gate Queue", "0"));
        metrics.add(new SimulationMetric("Boarded", "0"));
        metrics.add(new SimulationMetric("Processing Rate", "0 /min"));
        metrics.add(new SimulationMetric("Simulation Time", "00:00"));
        
        metricsTable.setItems(metrics);
    }
    
    private void setupFlowRateChart() {
        xAxis.setLabel("Time (seconds)");
        yAxis.setLabel("Passengers");
        
        flowRateSeries = new XYChart.Series<>();
        flowRateSeries.setName("Passenger Flow");
        
        flowRateChart.getData().add(flowRateSeries);
        flowRateChart.setAnimated(false);
        flowRateChart.setCreateSymbols(false);
        flowRateChart.setLegendVisible(true);
    }
    
    private void drawAirportLayout() {
        gc.clearRect(0, 0, simulationCanvas.getWidth(), simulationCanvas.getHeight());
        
        // Draw background
        gc.setFill(Color.rgb(245, 245, 250));
        gc.fillRect(0, 0, simulationCanvas.getWidth(), simulationCanvas.getHeight());
        
        // Draw zones with gradient
        drawZone(checkInZone, "Check-in", Color.rgb(173, 216, 230));
        drawZone(securityZone, "Security", Color.rgb(255, 228, 196));
        drawZone(gateZone, "Gate", Color.rgb(152, 251, 152));
        drawZone(boardingZone, "Boarding", Color.rgb(216, 191, 216));
        
        // Draw connecting paths with arrows
        drawPath(checkInZone, securityZone);
        drawPath(securityZone, gateZone);
        drawPath(gateZone, boardingZone);
    }
    
    private void drawZone(int[] zone, String label, Color color) {
        // Draw zone with border
        gc.setFill(color);
        gc.fillRoundRect(zone[0], zone[1], zone[2], zone[3], 10, 10);
        
        gc.setStroke(color.darker());
        gc.setLineWidth(2);
        gc.strokeRoundRect(zone[0], zone[1], zone[2], zone[3], 10, 10);
        
        // Draw label
        gc.setFill(Color.rgb(50, 50, 50));
        gc.setFont(new Font("Arial", 14));
        gc.fillText(label, zone[0] + (zone[2] / 2) - 25, zone[1] + 25);
    }
    
    private void drawPath(int[] fromZone, int[] toZone) {
        double startX = fromZone[0] + fromZone[2];
        double startY = fromZone[1] + (fromZone[3] / 2);
        double endX = toZone[0];
        double endY = toZone[1] + (toZone[3] / 2);
        
        // Draw main path
        gc.setStroke(Color.rgb(100, 100, 100));
        gc.setLineWidth(2);
        gc.strokeLine(startX, startY, endX, endY);
        
        // Draw arrow
        double arrowLength = 10;
        double dx = endX - startX;
        double dy = endY - startY;
        double angle = Math.atan2(dy, dx);
        double arrowX1 = endX - arrowLength * Math.cos(angle - Math.PI/6);
        double arrowY1 = endY - arrowLength * Math.sin(angle - Math.PI/6);
        double arrowX2 = endX - arrowLength * Math.cos(angle + Math.PI/6);
        double arrowY2 = endY - arrowLength * Math.sin(angle + Math.PI/6);
        
        gc.setFill(Color.rgb(100, 100, 100));
        gc.beginPath();
        gc.moveTo(endX, endY);
        gc.lineTo(arrowX1, arrowY1);
        gc.lineTo(arrowX2, arrowY2);
        gc.closePath();
        gc.fill();
    }
    
    private void startSimulation() {
        resetSimulation();
        
        // Initialize checkpoint counts
        checkpointCounts.put("checkin", 0);
        checkpointCounts.put("security", 0);
        checkpointCounts.put("gate", 0);
        checkpointCounts.put("boarding", 0);
        
        // Start passenger generator
        int passengerRate = (int) passengerRateSlider.getValue();
        passengerGeneratorTimeline = new Timeline(
            new KeyFrame(Duration.seconds(60.0 / passengerRate), e -> generatePassenger())
        );
        passengerGeneratorTimeline.setCycleCount(Timeline.INDEFINITE);
        passengerGeneratorTimeline.play();
        
        // Start animation timer
        simulationTimeSeconds = 0;
        animationTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                
                if (now - lastUpdate > 250_000_000) { // Update every 0.25 seconds
                    updateSimulation();
                    lastUpdate = now;
                    simulationTimeSeconds++;
                    
                    // Update time metric
                    int minutes = simulationTimeSeconds / 60;
                    int seconds = simulationTimeSeconds % 60;
                    updateMetric("Simulation Time", String.format("%02d:%02d", minutes, seconds));
                    
                    // Add data point to chart every 5 seconds
                    if (simulationTimeSeconds % 5 == 0) {
                        flowRateSeries.getData().add(new XYChart.Data<>(
                            simulationTimeSeconds, totalPassengersProcessed));
                    }
                }
                
                drawSimulation();
            }
        };
        animationTimer.start();
        
        // Update UI
        startSimulationBtn.setDisable(true);
        stopSimulationBtn.setDisable(false);
        simulationTypeComboBox.setDisable(true);
        passengerRateSlider.setDisable(true);
    }
    
    private void stopSimulation() {
        if (passengerGeneratorTimeline != null) {
            passengerGeneratorTimeline.stop();
        }
        
        if (animationTimer != null) {
            animationTimer.stop();
        }
        
        // Update UI
        startSimulationBtn.setDisable(false);
        stopSimulationBtn.setDisable(true);
        simulationTypeComboBox.setDisable(false);
        passengerRateSlider.setDisable(false);
    }
    
    private void resetSimulation() {
        stopSimulation();
        
        // Reset data
        passengers.clear();
        checkpointCounts.clear();
        totalPassengersProcessed = 0;
        totalPassengersCreated = 0;
        simulationTimeSeconds = 0;
        
        // Reset UI
        drawAirportLayout();
        flowRateSeries.getData().clear();
        
        // Reset metrics
        updateMetric("Total Passengers", "0");
        updateMetric("Check-in Queue", "0");
        updateMetric("Security Queue", "0");
        updateMetric("Gate Queue", "0");
        updateMetric("Boarded", "0");
        updateMetric("Processing Rate", "0 /min");
        updateMetric("Simulation Time", "00:00");
    }
    
    private void generatePassenger() {
        // Create new passenger at entrance
        Passenger passenger = new Passenger(
            10, // Start at entrance
            simulationCanvas.getHeight() / 2,
            "checkin" // First destination
        );
        passengers.add(passenger);
        totalPassengersCreated++;
        
        // Update metric
        updateMetric("Total Passengers", String.valueOf(totalPassengersCreated));
    }
    
    private void updateSimulation() {
        // Reset counts for this frame
        checkpointCounts.put("checkin", 0);
        checkpointCounts.put("security", 0);
        checkpointCounts.put("gate", 0);
        checkpointCounts.put("boarding", 0);
        
        // Update passenger positions and statuses
        Iterator<Passenger> iterator = passengers.iterator();
        while (iterator.hasNext()) {
            Passenger passenger = iterator.next();
            
            // Update passenger position based on destination
            switch (passenger.getDestination()) {
                case "checkin":
                    moveTowards(passenger, checkInZone[0] + checkInZone[2]/2, checkInZone[1] + checkInZone[3]/2);
                    checkpointCounts.put("checkin", checkpointCounts.get("checkin") + 1);
                    break;
                case "security":
                    moveTowards(passenger, securityZone[0] + securityZone[2]/2, securityZone[1] + securityZone[3]/2);
                    checkpointCounts.put("security", checkpointCounts.get("security") + 1);
                    break;
                case "gate":
                    moveTowards(passenger, gateZone[0] + gateZone[2]/2, gateZone[1] + gateZone[3]/2);
                    checkpointCounts.put("gate", checkpointCounts.get("gate") + 1);
                    break;
                case "boarding":
                    moveTowards(passenger, boardingZone[0] + boardingZone[2]/2, boardingZone[1] + boardingZone[3]/2);
                    checkpointCounts.put("boarding", checkpointCounts.get("boarding") + 1);
                    break;
                case "complete":
                    // Remove passenger who completed boarding
                    iterator.remove();
                    totalPassengersProcessed++;
                    break;
            }
            
            // Process passenger at destination
            if (passenger.isAtDestination()) {
                processPassengerAtDestination(passenger);
            }
        }
        
        // Update metrics
        updateMetric("Check-in Queue", String.valueOf(checkpointCounts.get("checkin")));
        updateMetric("Security Queue", String.valueOf(checkpointCounts.get("security")));
        updateMetric("Gate Queue", String.valueOf(checkpointCounts.get("gate")));
        updateMetric("Boarded", String.valueOf(checkpointCounts.get("boarding")));
        
        // Calculate processing rate (passengers per minute)
        if (simulationTimeSeconds > 0) {
            double rate = (double) totalPassengersProcessed / (simulationTimeSeconds / 60.0);
            updateMetric("Processing Rate", String.format("%.1f /min", rate));
        }
    }
    
    private void moveTowards(Passenger passenger, double targetX, double targetY) {
        double dx = targetX - passenger.getX();
        double dy = targetY - passenger.getY();
        double distance = Math.sqrt(dx*dx + dy*dy);
        
        if (distance < 5) {
            // Passenger has reached destination
            passenger.setAtDestination(true);
            return;
        }
        
        // Move towards destination
        double speed = 5.0;
        passenger.setX(passenger.getX() + (dx / distance) * speed);
        passenger.setY(passenger.getY() + (dy / distance) * speed);
    }
    
    private void processPassengerAtDestination(Passenger passenger) {
        // Process time at each checkpoint
        passenger.incrementProcessingTime();
        
        // Different processing times for different checkpoints
        int processingTimeRequired = 0;
        switch (passenger.getDestination()) {
            case "checkin":
                processingTimeRequired = 30; // 30 frames (~3 seconds)
                break;
            case "security":
                processingTimeRequired = 50; // 50 frames (~5 seconds)
                break;
            case "gate":
                processingTimeRequired = 20; // 20 frames (~2 seconds)
                break;
            case "boarding":
                processingTimeRequired = 10; // 10 frames (~1 second)
                break;
        }
        
        // Move to next checkpoint when processing is complete
        if (passenger.getProcessingTime() >= processingTimeRequired) {
            passenger.setProcessingTime(0);
            passenger.setAtDestination(false);
            
            // Set next destination
            switch (passenger.getDestination()) {
                case "checkin":
                    passenger.setDestination("security");
                    break;
                case "security":
                    passenger.setDestination("gate");
                    break;
                case "gate":
                    passenger.setDestination("boarding");
                    break;
                case "boarding":
                    passenger.setDestination("complete");
                    break;
            }
        }
    }
    
    private void drawSimulation() {
        // Redraw layout
        drawAirportLayout();
        
        // Draw passengers
        for (Passenger passenger : passengers) {
            // Different colors based on destination
            Color passengerColor;
            switch (passenger.getDestination()) {
                case "checkin":
                    passengerColor = Color.BLUE;
                    break;
                case "security":
                    passengerColor = Color.rgb(220, 100, 50);
                    break;
                case "gate":
                    passengerColor = Color.rgb(30, 150, 30);
                    break;
                case "boarding":
                    passengerColor = Color.PURPLE;
                    break;
                default:
                    passengerColor = Color.BLACK;
            }
            
            // Draw passenger as a circle with shadow
            double size = passenger.isAtDestination() ? 7.0 : 6.0;
            
            // Shadow
            gc.setFill(Color.rgb(0, 0, 0, 0.2));
            gc.fillOval(passenger.getX() - size + 1, passenger.getY() - size + 1, size*2, size*2);
            
            // Passenger
            gc.setFill(passengerColor);
            gc.fillOval(passenger.getX() - size, passenger.getY() - size, size*2, size*2);
            
            // Highlight
            gc.setFill(Color.rgb(255, 255, 255, 0.7));
            gc.fillOval(passenger.getX() - size/3, passenger.getY() - size/3, size/2, size/2);
            
            // Processing indicator
            if (passenger.isAtDestination() && passenger.getProcessingTime() > 0) {
                drawProcessingIndicator(passenger);
            }
        }
    }
    
    private void drawProcessingIndicator(Passenger passenger) {
        int maxProcessingTime;
        switch(passenger.getDestination()) {
            case "checkin": maxProcessingTime = 30; break;
            case "security": maxProcessingTime = 50; break;
            case "gate": maxProcessingTime = 20; break;
            case "boarding": maxProcessingTime = 10; break;
            default: maxProcessingTime = 10;
        }
        
        double progress = (double) passenger.getProcessingTime() / maxProcessingTime;
        double arcSize = 8.0;
        
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(passenger.getX() - arcSize, passenger.getY() - arcSize, arcSize*2, arcSize*2);
        
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(2);
        gc.strokeArc(
            passenger.getX() - arcSize, 
            passenger.getY() - arcSize, 
            arcSize*2, 
            arcSize*2, 
            90, 
            -progress * 360, 
            javafx.scene.shape.ArcType.OPEN
        );
    }
    
    private void updateMetric(String name, String value) {
        for (SimulationMetric metric : metrics) {
            if (metric.getName().equals(name)) {
                metric.setValue(value);
                break;
            }
        }
    }
    
    // Inner class for passengers
    private static class Passenger {
        private double x;
        private double y;
        private String destination;
        private boolean atDestination;
        private int processingTime;
        
        public Passenger(double x, double y, String destination) {
            this.x = x;
            this.y = y;
            this.destination = destination;
            this.atDestination = false;
            this.processingTime = 0;
        }
        
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        
        public boolean isAtDestination() { return atDestination; }
        public void setAtDestination(boolean atDestination) { this.atDestination = atDestination; }
        
        public int getProcessingTime() { return processingTime; }
        public void setProcessingTime(int processingTime) { this.processingTime = processingTime; }
        public void incrementProcessingTime() { this.processingTime++; }
    }
    
    // Inner class for metrics table
    public static class SimulationMetric {
        private final SimpleStringProperty name;
        private final SimpleStringProperty value;
        
        public SimulationMetric(String name, String value) {
            this.name = new SimpleStringProperty(name);
            this.value = new SimpleStringProperty(value);
        }
        
        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }
        public SimpleStringProperty nameProperty() { return name; }
        
        public String getValue() { return value.get(); }
        public void setValue(String value) { this.value.set(value); }
        public SimpleStringProperty valueProperty() { return value; }
    }
}

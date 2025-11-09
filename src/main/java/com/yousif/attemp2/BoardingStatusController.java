package com.yousif.attemp2;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;

public class BoardingStatusController {

    @FXML
    private Label flightsBoardingValue;
    
    @FXML
    private Label passengersBoardingValue;
    
    @FXML
    private Label onTimeDeparturesValue;
    
    @FXML
    private Label avgBoardingTimeValue;
    
    @FXML
    private ComboBox<String> terminalSelector;
    
    @FXML
    private TableView<BoardingFlight> boardingTable;
    
    @FXML
    private TableColumn<BoardingFlight, String> flightColumn;
    
    @FXML
    private TableColumn<BoardingFlight, String> destinationColumn;
    
    @FXML
    private TableColumn<BoardingFlight, String> gateColumn;
    
    @FXML
    private TableColumn<BoardingFlight, String> departureColumn;
    
    @FXML
    private TableColumn<BoardingFlight, String> statusColumn;
    
    @FXML
    private TableColumn<BoardingFlight, Double> progressColumn;
    
    @FXML
    private TableColumn<BoardingFlight, BoardingFlight> actionsColumn;
    
    @FXML
    private ComboBox<String> flightSelector;
    
    @FXML
    private Pane seatMapContainer;
    
    @FXML
    private Label selectedFlightStatus;
    
    @FXML
    private ProgressBar overallProgressBar;
    
    @FXML
    private Label overallProgressLabel;
    
    @FXML
    private PieChart boardingDistributionChart;
    
    private ObservableList<BoardingFlight> flightData = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupStatistics();
        setupTerminalSelector();
        setupFlightSelector();
        setupBoardingTable();
        loadFlightData();
        setupBoardingDistributionChart();
        generateSeatMap("AA1234");
    }
    
    private void setupStatistics() {
        flightsBoardingValue.setText("8");
        passengersBoardingValue.setText("1,245");
        onTimeDeparturesValue.setText("92%");
        avgBoardingTimeValue.setText("22 min");
    }
    
    private void setupTerminalSelector() {
        terminalSelector.getItems().addAll("All Terminals", "Terminal A", "Terminal B", "Terminal C");
        terminalSelector.setValue("All Terminals");
        terminalSelector.setOnAction(unused -> refreshBoardingStatus());
    }
    
    private void setupFlightSelector() {
        flightSelector.getItems().addAll("AA1234 - New York (JFK)", "DL5678 - London (LHR)", "UA9012 - Chicago (ORD)", "BA7890 - Paris (CDG)");
        flightSelector.setValue("AA1234 - New York (JFK)");
        flightSelector.setOnAction(unused -> {
            String flight = flightSelector.getValue().split(" - ")[0];
            generateSeatMap(flight);
            updateBoardingProgress(flight);
        });
    }
    
    private void setupBoardingTable() {
        flightColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFlight()));
        destinationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDestination()));
        gateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGate()));
        departureColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDeparture()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        
        // Progress column with progress bar
        progressColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getProgress()).asObject());
        progressColumn.setCellFactory(unused -> new TableCell<BoardingFlight, Double>() {
            private final ProgressBar progressBar = new ProgressBar();
            private final Label percentLabel = new Label();
            private final HBox container = new HBox(5);
            
            {
                progressBar.setPrefWidth(100);
                container.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().addAll(progressBar, percentLabel);
            }
            
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    progressBar.setProgress(item);
                    percentLabel.setText(String.format("%.0f%%", item * 100));
                    setGraphic(container);
                }
            }
        });
        
        // Actions column with buttons
        actionsColumn.setCellFactory(unused -> new TableCell<BoardingFlight, BoardingFlight>() {
            private final Button detailsButton = new Button();
            
            {
                detailsButton.getStyleClass().add("table-button");
                FontIcon icon = new FontIcon("fas-info-circle");
                detailsButton.setGraphic(icon);
                detailsButton.setOnAction(unused -> {
                    BoardingFlight flight = getTableView().getItems().get(getIndex());
                    showFlightDetails(flight);
                });
            }
            
            @Override
            protected void updateItem(BoardingFlight item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });
    }
    
    private void loadFlightData() {
        flightData.addAll(
            new BoardingFlight("AA1234", "New York (JFK)", "A12", "08:45", "Boarding", 0.65),
            new BoardingFlight("DL5678", "London (LHR)", "B05", "09:15", "Pending", 0.0),
            new BoardingFlight("UA9012", "Chicago (ORD)", "C22", "09:30", "Boarding", 0.35),
            new BoardingFlight("BA7890", "Paris (CDG)", "A22", "10:00", "Complete", 1.0),
            new BoardingFlight("LH4567", "Frankfurt (FRA)", "B14", "10:30", "Pending", 0.0),
            new BoardingFlight("EK8901", "Dubai (DXB)", "C10", "11:15", "Boarding", 0.15),
            new BoardingFlight("SQ2345", "Singapore (SIN)", "D04", "12:00", "Pending", 0.0),
            new BoardingFlight("QF6789", "Sydney (SYD)", "D12", "13:30", "Pending", 0.0)
        );
        
        boardingTable.setItems(flightData);
    }
    
    private void setupBoardingDistributionChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Boarded", 65),
            new PieChart.Data("Waiting", 25),
            new PieChart.Data("Not Checked In", 10)
        );
        boardingDistributionChart.setData(pieChartData);
        boardingDistributionChart.setLabelsVisible(true);
    }
    
    @FXML
    public void refreshBoardingStatus() {
        // In a real app, this would fetch updated data from the server
        // For demo purposes, we'll just update the progress of one flight
        for (BoardingFlight flight : flightData) {
            if ("AA1234".equals(flight.getFlight())) {
                double newProgress = Math.min(1.0, flight.getProgress() + 0.1);
                flightData.set(flightData.indexOf(flight), 
                    new BoardingFlight(flight.getFlight(), flight.getDestination(), 
                        flight.getGate(), flight.getDeparture(), 
                        newProgress >= 1.0 ? "Complete" : "Boarding", newProgress));
                break;
            }
        }
        
        // Update boarding distribution chart
        boardingDistributionChart.getData().get(0).setPieValue(Math.min(100, 65 + 5));
        boardingDistributionChart.getData().get(1).setPieValue(Math.max(0, 25 - 5));
    }
    
    private void generateSeatMap(String flightNumber) {
        seatMapContainer.getChildren().clear();
        
        // This is a simplified seat map visualization
        // In a real app, this would be based on actual seat data from the database
        
        int rows = 14;
        int cols = 6;
        double seatSize = 20;
        double spacing = 5;
        double aisleWidth = 15;
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // Add aisle between cols 2 and 3
                double xOffset = col < 3 ? 0 : aisleWidth;
                
                Rectangle seat = new Rectangle(
                    col * (seatSize + spacing) + xOffset,
                    row * (seatSize + spacing),
                    seatSize,
                    seatSize
                );
                
                // Randomly assign seat status for demonstration
                double random = Math.random();
                if (random < 0.6) {
                    // Boarded seat
                    seat.setFill(Color.web("#60519b"));
                } else if (random < 0.8) {
                    // Checked In seat
                    seat.setFill(Color.web("#8c9eff"));
                } else {
                    // Available seat
                    seat.setFill(Color.web("#5d5e69"));
                }
                
                seat.setStroke(Color.BLACK);
                seat.setStrokeWidth(1);
                
                // Add seat label
                String seatLabel = String.valueOf((char)('A' + col)) + (row + 1);
                Text text = new Text(
                    col * (seatSize + spacing) + xOffset + 5,
                    row * (seatSize + spacing) + 15,
                    seatLabel
                );
                text.setFill(Color.WHITE);
                text.setStyle("-fx-font-size: 8px;");
                
                seatMapContainer.getChildren().addAll(seat, text);
            }
        }
    }
    
    private void updateBoardingProgress(String flightNumber) {
        for (BoardingFlight flight : flightData) {
            if (flight.getFlight().equals(flightNumber)) {
                selectedFlightStatus.setText(flight.getStatus() + " - " + 
                    String.format("%.0f%%", flight.getProgress() * 100) + " complete");
                overallProgressBar.setProgress(flight.getProgress());
                overallProgressLabel.setText(String.format("%.0f%%", flight.getProgress() * 100));
                break;
            }
        }
    }
    
    @FXML
    private void showFlightDetails(BoardingFlight flight) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Flight Boarding Details");
        dialog.setHeaderText("Flight " + flight.getFlight() + " to " + flight.getDestination());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        grid.add(new Label("Gate:"), 0, 0);
        grid.add(new Label(flight.getGate()), 1, 0);
        
        grid.add(new Label("Departure:"), 0, 1);
        grid.add(new Label(flight.getDeparture()), 1, 1);
        
        grid.add(new Label("Status:"), 0, 2);
        grid.add(new Label(flight.getStatus()), 1, 2);
        
        grid.add(new Label("Boarding Progress:"), 0, 3);
        ProgressBar pb = new ProgressBar(flight.getProgress());
        pb.setPrefWidth(200);
        grid.add(pb, 1, 3);
        
        grid.add(new Label("Total Passengers:"), 0, 4);
        grid.add(new Label("180"), 1, 4);
        
        grid.add(new Label("Boarded:"), 0, 5);
        grid.add(new Label(String.valueOf((int)(180 * flight.getProgress()))), 1, 5);
        
        Button generatePassesBtn = new Button("Generate Boarding Passes");
        generatePassesBtn.setOnAction(e -> generateBoardingPasses(flight));
        grid.add(generatePassesBtn, 1, 7);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    private void generateBoardingPasses(BoardingFlight flight) {
        try {
            // This would generate boarding passes in a real application
            // For demo purposes, we'll just show a success message
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Boarding Passes Generated");
            alert.setHeaderText(null);
            alert.setContentText("Boarding passes for flight " + flight.getFlight() + 
                " have been generated and sent to the printing queue.");
            alert.showAndWait();
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to generate boarding passes: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    public static class BoardingFlight {
        private final String flight;
        private final String destination;
        private final String gate;
        private final String departure;
        private final String status;
        private final double progress;
        
        public BoardingFlight(String flight, String destination, String gate, String departure, String status, double progress) {
            this.flight = flight;
            this.destination = destination;
            this.gate = gate;
            this.departure = departure;
            this.status = status;
            this.progress = progress;
        }
        
        public String getFlight() { return flight; }
        public String getDestination() { return destination; }
        public String getGate() { return gate; }
        public String getDeparture() { return departure; }
        public String getStatus() { return status; }
        public double getProgress() { return progress; }
    }
} 
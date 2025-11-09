package com.yousif.attemp2;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.Optional;

public class BaggageTrackingController {

    @FXML
    private Label bagsProcessedValue;
    
    @FXML
    private Label avgProcessingTimeValue;
    
    @FXML
    private Label misroutedBagsValue;
    
    @FXML
    private Label systemEfficiencyValue;
    
    @FXML
    private TextField baggageIdField;
    
    @FXML
    private ComboBox<String> flightFilter;
    
    @FXML
    private TableView<BaggageItem> baggageTable;
    
    @FXML
    private TableColumn<BaggageItem, String> baggageIdColumn;
    
    @FXML
    private TableColumn<BaggageItem, String> passengerNameColumn;
    
    @FXML
    private TableColumn<BaggageItem, String> flightColumn;
    
    @FXML
    private TableColumn<BaggageItem, String> locationColumn;
    
    @FXML
    private TableColumn<BaggageItem, String> statusColumn;
    
    @FXML
    private TableColumn<BaggageItem, String> handlingTimeColumn;
    
    @FXML
    private TableColumn<BaggageItem, BaggageItem> actionsColumn;
    
    @FXML
    private Pane baggageFlowPane;
    
    @FXML
    private BarChart<String, Number> processingTimeChart;
    
    private ObservableList<BaggageItem> baggageData = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupStatistics();
        setupFilters();
        setupBaggageTable();
        loadBaggageData();
        createBaggageFlowVisualization();
        setupProcessingTimeChart();
    }
    
    private void setupStatistics() {
        bagsProcessedValue.setText("2,478");
        avgProcessingTimeValue.setText("65 sec");
        misroutedBagsValue.setText("15");
        systemEfficiencyValue.setText("97.5%");
    }
    
    private void setupFilters() {
        flightFilter.getItems().addAll("All Flights", "AA1234 - New York", "DL5678 - London", "UA9012 - Chicago", "BA7890 - Paris");
        flightFilter.setValue("All Flights");
    }
    
    private void setupBaggageTable() {
        baggageIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBaggageId()));
        passengerNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPassengerName()));
        flightColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFlight()));
        locationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        handlingTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHandlingTime()));
        
        // Status column with colored indicators
        statusColumn.setCellFactory(unused -> new TableCell<BaggageItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    // Apply style based on status
                    switch (item) {
                        case "Check-in":
                            setStyle("-fx-text-fill: #2196f3;");
                            break;
                        case "Screening":
                            setStyle("-fx-text-fill: #ff9800;");
                            break;
                        case "Sorting":
                            setStyle("-fx-text-fill: #673ab7;");
                            break;
                        case "Loading":
                            setStyle("-fx-text-fill: #60519b;");
                            break;
                        case "Delivered":
                            setStyle("-fx-text-fill: #4caf50;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Actions column with buttons
        actionsColumn.setCellFactory(unused -> new TableCell<BaggageItem, BaggageItem>() {
            private final Button trackButton = new Button();
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            
            {
                trackButton.getStyleClass().add("table-button");
                FontIcon trackIcon = new FontIcon("fas-search-location");
                trackButton.setGraphic(trackIcon);
                trackButton.setTooltip(new Tooltip("Track Baggage"));
                trackButton.setOnAction(unused -> {
                    BaggageItem baggage = getTableView().getItems().get(getIndex());
                    showBaggageDetails(baggage);
                });
                
                editButton.getStyleClass().add("table-button");
                FontIcon editIcon = new FontIcon("fas-edit");
                editButton.setGraphic(editIcon);
                editButton.setTooltip(new Tooltip("Edit Baggage"));
                editButton.setOnAction(unused -> {
                    BaggageItem baggage = getTableView().getItems().get(getIndex());
                    editBaggageItem(baggage);
                });
                
                deleteButton.getStyleClass().add("table-button");
                FontIcon deleteIcon = new FontIcon("fas-trash-alt");
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setTooltip(new Tooltip("Delete Baggage"));
                deleteButton.setOnAction(unused -> {
                    BaggageItem baggage = getTableView().getItems().get(getIndex());
                    deleteBaggageItem(baggage);
                });
            }
            
            @Override
            protected void updateItem(BaggageItem item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.setAlignment(Pos.CENTER);
                    buttons.getChildren().addAll(trackButton, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void loadBaggageData() {
        baggageData.clear();
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String query = "SELECT * FROM baggage_tracking_view";
            java.sql.ResultSet rs = db.executeQuery(query);
            while (rs.next()) {
                baggageData.add(new BaggageItem(
                    rs.getString("baggage_tag"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("flight_number"),
                    rs.getString("current_location"),
                    rs.getString("baggage_status"),
                    rs.getString("handling_time")
                ));
            }
            baggageTable.setItems(baggageData);
        } catch (Exception e) {
            showAlert("Failed to load baggage data: " + e.getMessage());
        }
    }
    
    private void updateBaggageInDatabase(BaggageItem baggage, int newBookingId) {
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String query = "UPDATE baggage SET booking_id=?, status=?, current_location=?, handling_time=?, weight=? WHERE baggage_tag=?";
            db.executeUpdate(query, newBookingId, baggage.getStatus(), baggage.getLocation(), baggage.getHandlingTime(), baggage.getWeight(), baggage.getBaggageId());
            loadBaggageData();
        } catch (Exception e) {
            showAlert("Failed to update baggage: " + e.getMessage());
        }
    }
    
    private void addBaggageToDatabase(BaggageItem baggage, int bookingId) {
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String query = "INSERT INTO baggage (baggage_tag, booking_id, weight, status, current_location, handling_time) VALUES (?, ?, ?, ?, ?, ?)";
            db.executeUpdate(query, baggage.getBaggageId(), bookingId, baggage.getWeight(), baggage.getStatus(), baggage.getLocation(), baggage.getHandlingTime());
            loadBaggageData();
        } catch (Exception e) {
            showAlert("Failed to add baggage: " + e.getMessage());
        }
    }
    
    private void deleteBaggageFromDatabase(String baggageTag) {
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String query = "DELETE FROM baggage WHERE baggage_tag=?";
            db.executeUpdate(query, baggageTag);
            loadBaggageData();
        } catch (Exception e) {
            showAlert("Failed to delete baggage: " + e.getMessage());
        }
    }
    
    private void createBaggageFlowVisualization() {
        baggageFlowPane.getChildren().clear();
        
        // Create the stages of baggage handling
        double stageWidth = 100;
        double stageHeight = 60;
        double spacing = 50;
        double startX = 30;
        double startY = 80;
        
        // Draw the 5 stages: Check-in, Screening, Sorting, Loading, Delivered
        String[] stages = {"Check-in", "Screening", "Sorting", "Loading", "Delivered"};
        Color[] stageColors = {Color.web("#2196f3"), Color.web("#ff9800"), Color.web("#673ab7"), 
                              Color.web("#60519b"), Color.web("#4caf50")};
        
        Rectangle[] stageBoxes = new Rectangle[stages.length];
        
        // Create stage boxes
        for (int i = 0; i < stages.length; i++) {
            double x = startX + i * (stageWidth + spacing);
            
            Rectangle stageBox = new Rectangle(x, startY, stageWidth, stageHeight);
            stageBox.setFill(stageColors[i].deriveColor(0, 0.8, 1, 0.7));
            stageBox.setStroke(stageColors[i]);
            stageBox.setStrokeWidth(1);
            stageBox.setArcHeight(10);
            stageBox.setArcWidth(10);
            
            Text stageText = new Text(stages[i]);
            stageText.setFill(Color.WHITE);
            stageText.setX(x + (stageWidth/2) - (stageText.getLayoutBounds().getWidth()/2));
            stageText.setY(startY + (stageHeight/2) + 5);
            
            baggageFlowPane.getChildren().addAll(stageBox, stageText);
            stageBoxes[i] = stageBox;
        }
        
        // Connect stages with arrows
        for (int i = 0; i < stages.length - 1; i++) {
            double startLineX = startX + ((i + 1) * stageWidth) + (i * spacing);
            double endLineX = startX + ((i + 1) * stageWidth) + (i * spacing) + spacing;
            double lineY = startY + (stageHeight / 2);
            
            Line line = new Line(startLineX, lineY, endLineX, lineY);
            line.setStroke(Color.GRAY);
            line.setStrokeWidth(1.5);
            
            // Add arrowhead
            double arrowWidth = 8;
            double arrowHeight = 5;
            
            Line arrow1 = new Line(
                endLineX - arrowWidth, lineY - arrowHeight,
                endLineX, lineY
            );
            arrow1.setStroke(Color.GRAY);
            arrow1.setStrokeWidth(1.5);
            
            Line arrow2 = new Line(
                endLineX - arrowWidth, lineY + arrowHeight,
                endLineX, lineY
            );
            arrow2.setStroke(Color.GRAY);
            arrow2.setStrokeWidth(1.5);
            
            baggageFlowPane.getChildren().addAll(line, arrow1, arrow2);
        }
        
        // Add animated baggage items at different stages
        addAnimatedBaggage("BAG001245", stages, stageBoxes, 2, stageColors[2]); // Sorting
        addAnimatedBaggage("BAG002367", stages, stageBoxes, 1, stageColors[1]); // Screening
        addAnimatedBaggage("BAG005634", stages, stageBoxes, 4, stageColors[4]); // Delivered
    }
    
    private void addAnimatedBaggage(String id, String[] stages, Rectangle[] stageBoxes, 
                                    int currentStage, Color color) {
        double stageX = stageBoxes[currentStage].getX() + stageBoxes[currentStage].getWidth() / 2;
        double stageY = stageBoxes[currentStage].getY() - 20;
        
        Circle baggage = new Circle(stageX, stageY, 10, color);
        baggage.setStroke(Color.WHITE);
        baggage.setStrokeWidth(1.5);
        
        Text baggageId = new Text(id);
        baggageId.setX(stageX - 25);
        baggageId.setY(stageY - 15);
        baggageId.setFill(color);
        
        baggageFlowPane.getChildren().addAll(baggage, baggageId);
    }
    
    private void setupProcessingTimeChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Processing Time");
        
        // Add data for each stage with times in seconds (20 sec to 2 min range)
        series.getData().add(new XYChart.Data<>("Check-in", 20)); // 20 seconds
        series.getData().add(new XYChart.Data<>("Screening", 45)); // 45 seconds
        series.getData().add(new XYChart.Data<>("Sorting", 75)); // 1 min 15 sec
        series.getData().add(new XYChart.Data<>("Loading", 90)); // 1 min 30 sec
        series.getData().add(new XYChart.Data<>("Transport", 120)); // 2 minutes
        
        processingTimeChart.getData().add(series);
        
        // Update the y-axis label to reflect seconds
        processingTimeChart.getYAxis().setLabel("Processing Time (seconds)");
        
        // Apply custom styling to the chart
        for (XYChart.Data<String, Number> data : series.getData()) {
            // Add hover effect to show exact time
            Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue() + " seconds");
            Tooltip.install(data.getNode(), tooltip);
            
            // Add visual indicator for different time ranges
            if ((int)data.getYValue() <= 30) {
                data.getNode().setStyle("-fx-background-color: #4caf50;"); // Green for fast processing
            } else if ((int)data.getYValue() <= 90) {
                data.getNode().setStyle("-fx-background-color: #ffc107;"); // Yellow for medium processing
            } else {
                data.getNode().setStyle("-fx-background-color: #f44336;"); // Red for slower processing
            }
        }
    }
    
    @FXML
    private void searchBaggage() {
        String searchId = baggageIdField.getText().trim();
        if (searchId.isEmpty()) {
            showAlert("Please enter a baggage ID to search.");
            return;
        }
        
        // Filter the table to show only the matching baggage
        ObservableList<BaggageItem> filteredBaggage = FXCollections.observableArrayList();
        for (BaggageItem item : baggageData) {
            if (item.getBaggageId().contains(searchId)) {
                filteredBaggage.add(item);
            }
        }
        
        if (filteredBaggage.isEmpty()) {
            showAlert("No baggage found with ID: " + searchId);
            return;
        }
        
        baggageTable.setItems(filteredBaggage);
    }
    
    @FXML
    private void applyFilters() {
        String selectedFlight = flightFilter.getValue();
        
        // Reset to all data if "All Flights" is selected
        if (selectedFlight.equals("All Flights")) {
            baggageTable.setItems(baggageData);
            return;
        }
        
        // Extract flight number from the selection
        String flightNumber = selectedFlight.split(" - ")[0];
        
        // Filter the table to show only bags for the selected flight
        ObservableList<BaggageItem> filteredBaggage = FXCollections.observableArrayList();
        for (BaggageItem item : baggageData) {
            if (item.getFlight().equals(flightNumber)) {
                filteredBaggage.add(item);
            }
        }
        
        baggageTable.setItems(filteredBaggage);
    }
    
    private void showBaggageDetails(BaggageItem baggage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Baggage Details");
        alert.setHeaderText("Baggage ID: " + baggage.getBaggageId());
        
        // Create content for the dialog
        StringBuilder content = new StringBuilder();
        content.append("Passenger: ").append(baggage.getPassengerName()).append("\n");
        content.append("Flight: ").append(baggage.getFlight()).append("\n");
        content.append("Current Location: ").append(baggage.getLocation()).append("\n");
        content.append("Status: ").append(baggage.getStatus()).append("\n");
        content.append("Handling Time: ").append(baggage.getHandlingTime()).append("\n\n");
        content.append("Tracking History:").append("\n");
        content.append("- Check-in: Processed in 20 seconds at Counter 3").append("\n");
        content.append("- Screening: Processed in 45 seconds at Security Point 2").append("\n");
        
        if (baggage.getStatus().equals("Sorting") || 
            baggage.getStatus().equals("Loading") || 
            baggage.getStatus().equals("Delivered")) {
            content.append("- Sorting: Processed in 75 seconds at Sorting Area 2").append("\n");
        }
        
        if (baggage.getStatus().equals("Loading") || 
            baggage.getStatus().equals("Delivered")) {
            content.append("- Loading: Processed in 90 seconds at Loading Zone A").append("\n");
        }
        
        if (baggage.getStatus().equals("Delivered")) {
            content.append("- Delivered: Processed in 120 seconds to Aircraft Hold").append("\n");
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Helper class for booking selection
    public static class BookingItem {
        private final int bookingId;
        private final String displayText;
        public BookingItem(int bookingId, String displayText) {
            this.bookingId = bookingId;
            this.displayText = displayText;
        }
        public int getBookingId() { return bookingId; }
        @Override
        public String toString() { return displayText; }
    }

    @FXML
    private void addBaggage() {
        // Fetch valid bookings for selection
        ObservableList<BookingItem> bookingOptions = FXCollections.observableArrayList();
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String query = "SELECT b.booking_id, b.booking_reference, p.first_name, p.last_name, f.flight_number " +
                           "FROM bookings b " +
                           "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                           "JOIN flights f ON b.flight_id = f.flight_id";
            java.sql.ResultSet rs = db.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("booking_id");
                String display = rs.getString("booking_reference") + " - " +
                                 rs.getString("first_name") + " " + rs.getString("last_name") + " - " +
                                 rs.getString("flight_number");
                bookingOptions.add(new BookingItem(id, display));
            }
        } catch (Exception e) {
            showAlert("Failed to load bookings: " + e.getMessage());
            return;
        }

        // Create a custom dialog
        Dialog<BaggageItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Baggage");
        dialog.setHeaderText("Enter Baggage Details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField baggageTagField = new TextField();
        baggageTagField.setPromptText("Baggage Tag");
        ComboBox<BookingItem> bookingComboBox = new ComboBox<>(bookingOptions);
        if (!bookingOptions.isEmpty()) {
            bookingComboBox.getSelectionModel().selectFirst();
        }
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Checked In", "In Transit", "Loaded", "Delivered");
        statusComboBox.setValue("Checked In");
        TextField locationField = new TextField();
        locationField.setPromptText("Current Location");
        TextField handlingTimeField = new TextField();
        handlingTimeField.setPromptText("Handling Time");
        TextField weightField = new TextField();
        weightField.setPromptText("Weight (kg)");

        grid.add(new Label("Baggage Tag:"), 0, 0);
        grid.add(baggageTagField, 1, 0);
        grid.add(new Label("Booking:"), 0, 1);
        grid.add(bookingComboBox, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusComboBox, 1, 2);
        grid.add(new Label("Current Location:"), 0, 3);
        grid.add(locationField, 1, 3);
        grid.add(new Label("Handling Time:"), 0, 4);
        grid.add(handlingTimeField, 1, 4);
        grid.add(new Label("Weight (kg):"), 0, 5);
        grid.add(weightField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (baggageTagField.getText().isEmpty() ||
                    bookingComboBox.getValue() == null ||
                    locationField.getText().isEmpty() ||
                    handlingTimeField.getText().isEmpty() ||
                    weightField.getText().isEmpty()) {
                    showAlert("All fields are required. Please fill in all information.");
                    return null;
                }
                try {
                    Double.parseDouble(weightField.getText());
                } catch (NumberFormatException e) {
                    showAlert("Weight must be a valid number.");
                    return null;
                }
                return new BaggageItem(
                    baggageTagField.getText(),
                    "", // passengerName will be filled by the view
                    "", // flight will be filled by the view
                    locationField.getText(),
                    statusComboBox.getValue(),
                    handlingTimeField.getText(),
                    weightField.getText()
                );
            }
            return null;
        });

        Optional<BaggageItem> result = dialog.showAndWait();
        if (result.isPresent()) {
            BaggageItem baggageItem = result.get();
            BookingItem selectedBooking = bookingComboBox.getValue();
            if (selectedBooking != null) {
                addBaggageToDatabase(baggageItem, selectedBooking.getBookingId());
            }
        }
    }
    
    @FXML
    private void editBaggage() {
        BaggageItem selectedBaggage = baggageTable.getSelectionModel().getSelectedItem();
        if (selectedBaggage == null) {
            showAlert("Please select a baggage item to edit.");
            return;
        }

        // Fetch valid bookings for selection
        ObservableList<BookingItem> bookingOptions = FXCollections.observableArrayList();
        int currentBookingIndex = -1;
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            String query = "SELECT b.booking_id, b.booking_reference, p.first_name, p.last_name, f.flight_number " +
                           "FROM bookings b " +
                           "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                           "JOIN flights f ON b.flight_id = f.flight_id";
            java.sql.ResultSet rs = db.executeQuery(query);
            int idx = 0;
            while (rs.next()) {
                int id = rs.getInt("booking_id");
                String display = rs.getString("booking_reference") + " - " +
                                 rs.getString("first_name") + " " + rs.getString("last_name") + " - " +
                                 rs.getString("flight_number");
                bookingOptions.add(new BookingItem(id, display));
                // Try to match the current booking by flight and name
                if (selectedBaggage.getFlight().equals(rs.getString("flight_number")) &&
                    display.contains(selectedBaggage.getPassengerName().split(" ")[0])) {
                    currentBookingIndex = idx;
                }
                idx++;
            }
        } catch (Exception e) {
            showAlert("Failed to load bookings: " + e.getMessage());
            return;
        }

        // Create a custom dialog
        Dialog<BaggageItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Baggage");
        dialog.setHeaderText("Edit Baggage Details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField baggageTagField = new TextField(selectedBaggage.getBaggageId());
        baggageTagField.setDisable(true); // Don't allow changing the tag
        ComboBox<BookingItem> bookingComboBox = new ComboBox<>(bookingOptions);
        bookingComboBox.setPromptText("Select Booking");
        if (currentBookingIndex >= 0) bookingComboBox.getSelectionModel().select(currentBookingIndex);
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Checked In", "In Transit", "Loaded", "Delivered");
        statusComboBox.setValue(selectedBaggage.getStatus());
        TextField locationField = new TextField(selectedBaggage.getLocation());
        TextField handlingTimeField = new TextField(selectedBaggage.getHandlingTime());
        TextField weightField = new TextField(selectedBaggage.getWeight());

        grid.add(new Label("Baggage Tag:"), 0, 0);
        grid.add(baggageTagField, 1, 0);
        grid.add(new Label("Booking:"), 0, 1);
        grid.add(bookingComboBox, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusComboBox, 1, 2);
        grid.add(new Label("Current Location:"), 0, 3);
        grid.add(locationField, 1, 3);
        grid.add(new Label("Handling Time:"), 0, 4);
        grid.add(handlingTimeField, 1, 4);
        grid.add(new Label("Weight (kg):"), 0, 5);
        grid.add(weightField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (statusComboBox.getValue() == null ||
                    locationField.getText().isEmpty() ||
                    handlingTimeField.getText().isEmpty() ||
                    weightField.getText().isEmpty() ||
                    bookingComboBox.getValue() == null) {
                    showAlert("All fields are required. Please fill in all information.");
                    return null;
                }
                try {
                    Double.parseDouble(weightField.getText());
                } catch (NumberFormatException e) {
                    showAlert("Weight must be a valid number.");
                    return null;
                }
                selectedBaggage.setStatus(statusComboBox.getValue());
                selectedBaggage.setLocation(locationField.getText());
                selectedBaggage.setHandlingTime(handlingTimeField.getText());
                selectedBaggage.setWeight(weightField.getText());
                // Store the new booking id in the BaggageItem for update
                selectedBaggage.setFlight(bookingComboBox.getValue().toString()); // for display only
                selectedBaggage.setPassengerName(bookingComboBox.getValue().toString()); // for display only
                selectedBaggage.bookingIdForUpdate = bookingComboBox.getValue().getBookingId();
                return selectedBaggage;
            }
            return null;
        });

        Optional<BaggageItem> result = dialog.showAndWait();
        if (result.isPresent()) {
            updateBaggageInDatabase(result.get(), result.get().bookingIdForUpdate);
        }
    }
    
    @FXML
    private void deleteBaggage() {
        BaggageItem selectedBaggage = baggageTable.getSelectionModel().getSelectedItem();
        if (selectedBaggage == null) {
            showAlert("Please select a baggage item to delete.");
            return;
        }
        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Baggage Item");
        confirmAlert.setContentText("Are you sure you want to delete baggage item " + selectedBaggage.getBaggageId() + "?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteBaggageFromDatabase(selectedBaggage.getBaggageId());
        }
    }
    
    private void updateBaggageStatistics() {
        int total = baggageData.size();
        
        // Count misrouted bags (those in screening or sorting for this example)
        int misrouted = 0;
        for (BaggageItem item : baggageData) {
            if (item.getStatus().equals("Screening") || item.getStatus().equals("Sorting")) {
                misrouted++;
            }
        }
        
        // Update the statistics display
        bagsProcessedValue.setText(String.valueOf(total));
        misroutedBagsValue.setText(String.valueOf(misrouted));
        
        // Calculate and update efficiency
        double efficiency = (total - misrouted) / (double) total * 100;
        systemEfficiencyValue.setText(String.format("%.1f%%", efficiency));
    }

    // Helper methods for row-level actions
    private void editBaggageItem(BaggageItem baggage) {
        // Reuse the edit dialog logic with the specific baggage item
        // Create a custom dialog
        Dialog<BaggageItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Baggage");
        dialog.setHeaderText("Edit Baggage Details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create a grid for the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create form fields with existing values
        TextField baggageIdField = new TextField(baggage.getBaggageId());
        baggageIdField.setDisable(true); // Don't allow changing the ID
        
        TextField passengerNameField = new TextField(baggage.getPassengerName());
        
        ComboBox<String> flightComboBox = new ComboBox<>();
        flightComboBox.getItems().addAll("AA1234", "DL5678", "UA9012", "BA7890");
        flightComboBox.setValue(baggage.getFlight());
        
        TextField locationField = new TextField(baggage.getLocation());
        
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Check-in", "Screening", "Sorting", "Loading", "Delivered");
        statusComboBox.setValue(baggage.getStatus());
        
        TextField handlingTimeField = new TextField(baggage.getHandlingTime());

        // Add fields to the grid
        grid.add(new Label("Baggage ID:"), 0, 0);
        grid.add(baggageIdField, 1, 0);
        grid.add(new Label("Passenger Name:"), 0, 1);
        grid.add(passengerNameField, 1, 1);
        grid.add(new Label("Flight:"), 0, 2);
        grid.add(flightComboBox, 1, 2);
        grid.add(new Label("Current Location:"), 0, 3);
        grid.add(locationField, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusComboBox, 1, 4);
        grid.add(new Label("Handling Time:"), 0, 5);
        grid.add(handlingTimeField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate form data
                if (passengerNameField.getText().isEmpty() || 
                    locationField.getText().isEmpty() || 
                    handlingTimeField.getText().isEmpty()) {
                    showAlert("All fields are required. Please fill in all information.");
                    return null;
                }
                
                // Update the baggage item values
                baggage.setPassengerName(passengerNameField.getText());
                baggage.setFlight(flightComboBox.getValue());
                baggage.setLocation(locationField.getText());
                baggage.setStatus(statusComboBox.getValue());
                baggage.setHandlingTime(handlingTimeField.getText());
                
                return baggage;
            }
            return null;
        });

        // Show dialog and handle result
        Optional<BaggageItem> result = dialog.showAndWait();
        if (result.isPresent()) {
            // Refresh the table to show updated values
            baggageTable.refresh();
            // Update the baggage flow visualization to reflect any changes
            createBaggageFlowVisualization();
            // Update statistics
            updateBaggageStatistics();
        }
    }
    
    private void deleteBaggageItem(BaggageItem baggage) {
        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Baggage Item");
        confirmAlert.setContentText("Are you sure you want to delete baggage item " + baggage.getBaggageId() + "?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteBaggageFromDatabase(baggage.getBaggageId());
        }
    }

    public static class BaggageItem {
        private final String baggageTag;
        private String passengerName;
        private String flight;
        private String location;
        private String status;
        private String handlingTime;
        private String weight;
        public int bookingIdForUpdate = -1;
        
        public BaggageItem(String baggageTag, String passengerName, String flight, 
                           String location, String status, String handlingTime) {
            this.baggageTag = baggageTag;
            this.passengerName = passengerName;
            this.flight = flight;
            this.location = location;
            this.status = status;
            this.handlingTime = handlingTime;
        }
        
        public BaggageItem(String baggageTag, String passengerName, String flight, String location, String status, String handlingTime, String weight) {
            this.baggageTag = baggageTag;
            this.passengerName = passengerName;
            this.flight = flight;
            this.location = location;
            this.status = status;
            this.handlingTime = handlingTime;
            this.weight = weight;
        }
        
        public String getBaggageId() { return baggageTag; }
        public String getPassengerName() { return passengerName; }
        public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
        public String getFlight() { return flight; }
        public void setFlight(String flight) { this.flight = flight; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getHandlingTime() { return handlingTime; }
        public void setHandlingTime(String handlingTime) { this.handlingTime = handlingTime; }
        public String getWeight() { return weight; }
        public void setWeight(String weight) { this.weight = weight; }
    }
} 
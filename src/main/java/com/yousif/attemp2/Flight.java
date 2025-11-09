package com.yousif.attemp2;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Flight {
    private final StringProperty flightNumber;
    private final StringProperty airline;
    private final StringProperty origin;
    private final StringProperty destination;
    private final ObjectProperty<LocalDateTime> departureTime;
    private final ObjectProperty<LocalDateTime> arrivalTime;
    private final StringProperty gate;
    private final StringProperty status;
    private StringProperty terminal;
    private double price;
    
    // Formatter for time strings
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    // Original constructor
    public Flight(String flightNumber, String destination, String time, String gate, String status) {
        this.flightNumber = new SimpleStringProperty(flightNumber);
        this.airline = new SimpleStringProperty("");
        this.origin = new SimpleStringProperty("");
        this.destination = new SimpleStringProperty(destination);
        this.departureTime = new SimpleObjectProperty<>(LocalDateTime.now());
        this.arrivalTime = new SimpleObjectProperty<>(LocalDateTime.now());
        this.gate = new SimpleStringProperty(gate);
        this.status = new SimpleStringProperty(status);
        this.terminal = new SimpleStringProperty(gate.length() > 0 ? gate.substring(0, 1) : "A");
    }
    
    // Extended constructor for user portal
    public Flight(String flightNumber, String airline, String origin, String destination, 
                 LocalDateTime departureTime, LocalDateTime arrivalTime, String status) {
        this.flightNumber = new SimpleStringProperty(flightNumber);
        this.airline = new SimpleStringProperty(airline);
        this.origin = new SimpleStringProperty(origin);
        this.destination = new SimpleStringProperty(destination);
        this.departureTime = new SimpleObjectProperty<>(departureTime);
        this.arrivalTime = new SimpleObjectProperty<>(arrivalTime);
        this.gate = new SimpleStringProperty("");
        this.status = new SimpleStringProperty(status);
        this.terminal = new SimpleStringProperty("A");
        this.price = 199.99; // Default price
    }
    
    // Constructor for flight schedule changes (used in FlightOperationsController)
    public Flight(String flightNumber, String airline, String destination, String departureText, 
                  String status, String terminal, String gate) {
        this.flightNumber = new SimpleStringProperty(flightNumber);
        this.airline = new SimpleStringProperty(airline);
        this.origin = new SimpleStringProperty("");
        this.destination = new SimpleStringProperty(destination);
        
        // Parse the departure time from string format HH:mm
        LocalDateTime parsedTime;
        try {
            String[] parts = departureText.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            parsedTime = LocalDateTime.now()
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0);
        } catch (Exception e) {
            parsedTime = LocalDateTime.now();
        }
        
        this.departureTime = new SimpleObjectProperty<>(parsedTime);
        this.arrivalTime = new SimpleObjectProperty<>(parsedTime.plusHours(2)); // Default 2-hour flight
        this.gate = new SimpleStringProperty(gate);
        this.status = new SimpleStringProperty(status);
        this.terminal = new SimpleStringProperty(terminal);
    }
    
    // Flight Number
    public String getFlightNumber() {
        return flightNumber.get();
    }
    
    public void setFlightNumber(String value) {
        flightNumber.set(value);
    }
    
    public StringProperty flightNumberProperty() {
        return flightNumber;
    }
    
    // Airline
    public String getAirline() {
        return airline.get();
    }
    
    public void setAirline(String value) {
        airline.set(value);
    }
    
    public StringProperty airlineProperty() {
        return airline;
    }
    
    // Origin
    public String getOrigin() {
        return origin.get();
    }
    
    public void setOrigin(String value) {
        origin.set(value);
    }
    
    public StringProperty originProperty() {
        return origin;
    }
    
    // Destination
    public String getDestination() {
        return destination.get();
    }
    
    public void setDestination(String value) {
        destination.set(value);
    }
    
    public StringProperty destinationProperty() {
        return destination;
    }
    
    // Departure Time
    public LocalDateTime getDepartureTime() {
        return departureTime.get();
    }
    
    public void setDepartureTime(LocalDateTime value) {
        departureTime.set(value);
    }
    
    public ObjectProperty<LocalDateTime> departureTimeProperty() {
        return departureTime;
    }
    
    // Arrival Time
    public LocalDateTime getArrivalTime() {
        return arrivalTime.get();
    }
    
    public void setArrivalTime(LocalDateTime value) {
        arrivalTime.set(value);
    }
    
    public ObjectProperty<LocalDateTime> arrivalTimeProperty() {
        return arrivalTime;
    }
    
    // Time (legacy support)
    /**
     * Get the departure time formatted as HH:mm
     */
    public String getTime() {
        return departureTime.get().format(TIME_FORMATTER);
    }
    
    public void setTime(String value) {
        // This is kept for backward compatibility
    }
    
    public StringProperty timeProperty() {
        return new SimpleStringProperty(departureTime.get().toString());
    }
    
    // Gate
    public String getGate() {
        return gate.get();
    }
    
    public void setGate(String value) {
        gate.set(value);
        // Also update terminal
        if (value != null && !value.isEmpty()) {
            setTerminal(value.substring(0, 1));
        }
    }
    
    public StringProperty gateProperty() {
        return gate;
    }
    
    // Terminal
    public String getTerminal() {
        return terminal.get();
    }
    
    public void setTerminal(String value) {
        if (terminal == null) {
            terminal = new SimpleStringProperty(value);
        } else {
            terminal.set(value);
        }
    }
    
    public StringProperty terminalProperty() {
        if (terminal == null) {
            terminal = new SimpleStringProperty(getGate().length() > 0 ? getGate().substring(0, 1) : "A");
        }
        return terminal;
    }
    
    // Status
    public String getStatus() {
        return status.get();
    }
    
    public void setStatus(String value) {
        status.set(value);
    }
    
    public StringProperty statusProperty() {
        return status;
    }
    
    // Convenience method for getting formatted departure time
    public String getDeparture() {
        return departureTime.get().format(TIME_FORMATTER);
    }

    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
} 
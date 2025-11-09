package com.yousif.attemp2;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * BaggageItem - Represents a baggage item in the airport system
 */
public class BaggageItem {
    private final StringProperty baggageId;
    private final StringProperty passengerName;
    private final StringProperty flight;
    private final StringProperty location;
    private final StringProperty status;
    private final StringProperty handlingTime;
    
    /**
     * Create a new baggage item
     * 
     * @param baggageId The baggage ID
     * @param passengerName The passenger name
     * @param flight The flight number
     * @param location The current location
     * @param status The current status
     * @param handlingTime The handling time
     */
    public BaggageItem(String baggageId, String passengerName, String flight, String location, String status, String handlingTime) {
        this.baggageId = new SimpleStringProperty(baggageId);
        this.passengerName = new SimpleStringProperty(passengerName);
        this.flight = new SimpleStringProperty(flight);
        this.location = new SimpleStringProperty(location);
        this.status = new SimpleStringProperty(status);
        this.handlingTime = new SimpleStringProperty(handlingTime);
    }
    
    // Getters and setters
    public String getBaggageId() {
        return baggageId.get();
    }
    
    public void setBaggageId(String value) {
        baggageId.set(value);
    }
    
    public StringProperty baggageIdProperty() {
        return baggageId;
    }
    
    public String getPassengerName() {
        return passengerName.get();
    }
    
    public void setPassengerName(String value) {
        passengerName.set(value);
    }
    
    public StringProperty passengerNameProperty() {
        return passengerName;
    }
    
    public String getFlight() {
        return flight.get();
    }
    
    public void setFlight(String value) {
        flight.set(value);
    }
    
    public StringProperty flightProperty() {
        return flight;
    }
    
    public String getLocation() {
        return location.get();
    }
    
    public void setLocation(String value) {
        location.set(value);
    }
    
    public StringProperty locationProperty() {
        return location;
    }
    
    public String getStatus() {
        return status.get();
    }
    
    public void setStatus(String value) {
        status.set(value);
    }
    
    public StringProperty statusProperty() {
        return status;
    }
    
    public String getHandlingTime() {
        return handlingTime.get();
    }
    
    public void setHandlingTime(String value) {
        handlingTime.set(value);
    }
    
    public StringProperty handlingTimeProperty() {
        return handlingTime;
    }
} 
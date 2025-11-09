package com.yousif.attemp2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DataService - A singleton service class to manage shared data between admin and user portals
 */
public class DataService {
    // Singleton instance
    private static DataService instance;
    
    // Observable lists of data that will be shared between admin and user portals
    private final ObservableList<Flight> flights = FXCollections.observableArrayList();
    private final ObservableList<String> notifications = FXCollections.observableArrayList();
    private final ObservableList<BaggageItem> baggageItems = FXCollections.observableArrayList();
    
    // List of data change listeners
    private final List<DataChangeListener> listeners = new ArrayList<>();
    
    // Database connection manager
    private final DatabaseConnection dbConnection;
    
    // Private constructor for singleton pattern
    private DataService() {
        dbConnection = DatabaseConnection.getInstance();
        loadAllData(); // Load data from database
    }
    
    /**
     * Get the singleton instance of DataService
     * @return The DataService instance
     */
    public static synchronized DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }
    
    /**
     * Register a listener for data changes
     * @param listener The listener to register
     */
    public void addDataChangeListener(DataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a registered listener
     * @param listener The listener to remove
     */
    public void removeDataChangeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners that data has changed
     * @param dataType The type of data that changed
     */
    public void notifyDataChanged(DataType dataType) {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged(dataType);
        }
    }
    
    /**
     * Initialize the database and load initial data
     * @return true if initialization is successful
     */
    public boolean initializeDatabase() {
        return dbConnection.initializeDatabase();
    }
    
    /**
     * Reload all data from the database
     */
    public void refreshData() {
        loadAllData();
        notifyDataChanged(DataType.ALL);
    }
    
    /**
     * Load all data from database tables
     */
    private void loadAllData() {
        loadFlightsFromDb();
        loadBaggageFromDb();
        loadNotificationsFromDb();
    }
    
    /**
     * Get all flights
     * @return Observable list of flights
     */
    public ObservableList<Flight> getFlights() {
        return flights;
    }
    
    /**
     * Get all baggage items
     * @return Observable list of baggage items
     */
    public ObservableList<BaggageItem> getBaggageItems() {
        return baggageItems;
    }
    
    /**
     * Get all notifications
     * @return Observable list of notifications
     */
    public ObservableList<String> getNotifications() {
        return notifications;
    }
    
    /**
     * Add a new flight
     * @param flight The flight to add
     */
    public void addFlight(Flight flight) {
        // Add to database first
        if (addFlightToDb(flight)) {
            // If successful, add to in-memory list
            flights.add(flight);
            notifyDataChanged(DataType.FLIGHTS);
        }
    }
    
    /**
     * Remove a flight
     * @param flight The flight to remove
     */
    public void removeFlight(Flight flight) {
        // Remove from database first
        if (removeFlightFromDb(flight)) {
            // If successful, remove from in-memory list
            flights.remove(flight);
            notifyDataChanged(DataType.FLIGHTS);
        }
    }
    
    /**
     * Add a new notification
     * @param message The notification message
     */
    public void addNotification(String message) {
        // Add to database first
        if (addNotificationToDb(message)) {
            // If successful, add to in-memory list
            notifications.add(message);
            notifyDataChanged(DataType.NOTIFICATIONS);
        }
    }
    
    /**
     * Add a new baggage item
     * @param item The baggage item to add
     */
    public void addBaggageItem(BaggageItem item) {
        // Add to database first
        if (addBaggageToDb(item)) {
            // If successful, add to in-memory list
            baggageItems.add(item);
            notifyDataChanged(DataType.BAGGAGE);
        }
    }
    
    /**
     * Remove a baggage item
     * @param item The baggage item to remove
     */
    public void removeBaggageItem(BaggageItem item) {
        // Remove from database first
        if (removeBaggageFromDb(item)) {
            // If successful, remove from in-memory list
            baggageItems.remove(item);
            notifyDataChanged(DataType.BAGGAGE);
        }
    }
    
    /**
     * Load flights from database
     */
    private void loadFlightsFromDb() {
        flights.clear();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT flight_id, flight_number, airline, origin, destination, " +
                "departure_time, arrival_time, status, gate, terminal FROM flights")) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String flightNumber = rs.getString("flight_number");
                String airline = rs.getString("airline");
                String origin = rs.getString("origin");
                String destination = rs.getString("destination");
                LocalDateTime departureTime = rs.getTimestamp("departure_time").toLocalDateTime();
                LocalDateTime arrivalTime = rs.getTimestamp("arrival_time").toLocalDateTime();
                String status = rs.getString("status");
                String gate = rs.getString("gate");
                String terminal = rs.getString("terminal");
                
                Flight flight = new Flight(flightNumber, airline, origin, destination, 
                                         departureTime, arrivalTime, status);
                flight.setGate(gate);
                flight.setTerminal(terminal);
                
                flights.add(flight);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading flights from database: " + e.getMessage());
            e.printStackTrace();
            
            // If database load fails, add some sample data
            if (flights.isEmpty()) {
                addSampleFlights();
            }
        }
    }
    
    /**
     * Load baggage items from database
     */
    private void loadBaggageFromDb() {
        baggageItems.clear();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT b.baggage_tag, p.first_name, p.last_name, f.flight_number, " +
                "b.current_location, b.status, b.handling_time " +
                "FROM baggage b " +
                "JOIN bookings bk ON b.booking_id = bk.booking_id " +
                "JOIN passengers p ON bk.passenger_id = p.passenger_id " +
                "JOIN flights f ON bk.flight_id = f.flight_id")) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String baggageTag = rs.getString("baggage_tag");
                String passengerName = rs.getString("first_name") + " " + rs.getString("last_name");
                String flightNumber = rs.getString("flight_number");
                String location = rs.getString("current_location");
                String status = rs.getString("status");
                String handlingTime = rs.getString("handling_time");
                
                BaggageItem item = new BaggageItem(baggageTag, passengerName, flightNumber, 
                                               location, status, handlingTime);
                
                baggageItems.add(item);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading baggage from database: " + e.getMessage());
            e.printStackTrace();
            
            // If database load fails, add some sample data
            if (baggageItems.isEmpty()) {
                addSampleBaggage();
            }
        }
    }
    
    /**
     * Load notifications from database
     */
    private void loadNotificationsFromDb() {
        notifications.clear();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT message FROM notifications ORDER BY created_at DESC LIMIT 20")) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(rs.getString("message"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading notifications from database: " + e.getMessage());
            e.printStackTrace();
            
            // If database load fails, add some sample data
            if (notifications.isEmpty()) {
                addSampleNotifications();
            }
        }
    }
    
    /**
     * Add a flight to the database
     * @param flight The flight to add
     * @return true if successful
     */
    private boolean addFlightToDb(Flight flight) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO flights (flight_number, airline, origin, destination, " +
                "departure_time, arrival_time, status, gate, terminal) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, flight.getFlightNumber());
            stmt.setString(2, flight.getAirline());
            stmt.setString(3, flight.getOrigin());
            stmt.setString(4, flight.getDestination());
            stmt.setTimestamp(5, Timestamp.valueOf(flight.getDepartureTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(flight.getArrivalTime()));
            stmt.setString(7, flight.getStatus());
            stmt.setString(8, flight.getGate());
            stmt.setString(9, flight.getTerminal());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding flight to database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Remove a flight from the database
     * @param flight The flight to remove
     * @return true if successful
     */
    private boolean removeFlightFromDb(Flight flight) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM flights WHERE flight_number = ?")) {
            
            stmt.setString(1, flight.getFlightNumber());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error removing flight from database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Add a notification to the database
     * @param message The notification message
     * @return true if successful
     */
    private boolean addNotificationToDb(String message) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO notifications (message) VALUES (?)")) {
            
            stmt.setString(1, message);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding notification to database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Add a baggage item to the database
     * @param item The baggage item to add
     * @return true if successful
     */
    private boolean addBaggageToDb(BaggageItem item) {
        // This is a simplified implementation
        // In a real app, you'd need to handle the relationships properly
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO baggage (baggage_tag, booking_id, status, current_location, handling_time) " +
                "SELECT ?, b.booking_id, ?, ?, ? FROM bookings b " +
                "JOIN flights f ON b.flight_id = f.flight_id " +
                "WHERE f.flight_number = ? LIMIT 1")) {
            
            stmt.setString(1, item.getBaggageId());
            stmt.setString(2, item.getStatus());
            stmt.setString(3, item.getLocation());
            stmt.setString(4, item.getHandlingTime());
            stmt.setString(5, item.getFlight());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding baggage to database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Remove a baggage item from the database
     * @param item The baggage item to remove
     * @return true if successful
     */
    private boolean removeBaggageFromDb(BaggageItem item) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM baggage WHERE baggage_tag = ?")) {
            
            stmt.setString(1, item.getBaggageId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error removing baggage from database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Add sample flights (used as fallback when database is unavailable)
     */
    private void addSampleFlights() {
        LocalDateTime now = LocalDateTime.now();
        
        flights.add(new Flight("AA1234", "American Airlines", "New York (JFK)", "Los Angeles (LAX)", 
            now.plusMinutes(30), now.plusHours(6), "On Time"));
        
        flights.add(new Flight("DL2345", "Delta Airlines", "Atlanta (ATL)", "Chicago (ORD)", 
            now.plusMinutes(45), now.plusHours(2), "Delayed"));
            
        flights.add(new Flight("UA3456", "United Airlines", "Chicago (ORD)", "Denver (DEN)", 
            now.plusHours(1), now.plusHours(3), "Boarding"));
            
        flights.add(new Flight("BA4567", "British Airways", "London (LHR)", "New York (JFK)", 
            now.plusHours(2), now.plusHours(10), "On Time"));
    }
    
    /**
     * Add sample baggage items (used as fallback when database is unavailable)
     */
    private void addSampleBaggage() {
        baggageItems.add(new BaggageItem("BAG001245", "John Smith", "AA1234", 
                                       "Sorting Area 2", "In Transit", "75 sec"));
                                       
        baggageItems.add(new BaggageItem("BAG002367", "Jane Doe", "DL2345", 
                                       "Security Screening", "Checked In", "45 sec"));
                                       
        baggageItems.add(new BaggageItem("BAG003890", "David Brown", "UA3456", 
                                       "Loading Area", "Loading", "60 sec"));
    }
    
    /**
     * Add sample notifications (used as fallback when database is unavailable)
     */
    private void addSampleNotifications() {
        notifications.add("Flight AA1234 is now boarding at Gate A12");
        notifications.add("Flight DL2345 has been delayed by 30 minutes");
        notifications.add("Baggage claim for BA4567 is at carousel 3");
    }
    
    /**
     * Data types for change notifications
     */
    public enum DataType {
        FLIGHTS, BAGGAGE, NOTIFICATIONS, ALL
    }
    
    /**
     * Interface for data change listeners
     */
    public interface DataChangeListener {
        void onDataChanged(DataType dataType);
    }

    // --- Service Requests ---
    /**
     * Add a new service request to the database
     * @param userId The user ID submitting the request
     * @param serviceType The type of service
     * @param location The location
     * @param urgency The urgency
     * @param description The description
     * @return true if successful
     */
    public boolean addServiceRequest(int userId, String serviceType, String location, String urgency, String description) {
        String sql = "INSERT INTO service_requests (user_id, service_type, location, urgency, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, serviceType);
            stmt.setString(3, location);
            stmt.setString(4, urgency);
            stmt.setString(5, description);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding service request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetch all service requests for a specific user
     * @param userId The user ID
     * @return List of ServiceRequestRow
     */
    public List<ServiceRequestRow> getServiceRequestsForUser(int userId) {
        List<ServiceRequestRow> requests = new ArrayList<>();
        String sql = "SELECT * FROM service_requests WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                requests.add(ServiceRequestRow.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching service requests: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Fetch all service requests (for admin)
     * @return List of ServiceRequestRow
     */
    public List<ServiceRequestRow> getAllServiceRequests() {
        List<ServiceRequestRow> requests = new ArrayList<>();
        String sql = "SELECT * FROM service_requests ORDER BY created_at DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                requests.add(ServiceRequestRow.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all service requests: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

    // Helper class for service requests (row mapping)
    public static class ServiceRequestRow {
        public int requestId;
        public int userId;
        public String serviceType;
        public String location;
        public String urgency;
        public String description;
        public String status;
        public LocalDateTime createdAt;

        public static ServiceRequestRow fromResultSet(ResultSet rs) throws SQLException {
            ServiceRequestRow row = new ServiceRequestRow();
            row.requestId = rs.getInt("request_id");
            row.userId = rs.getInt("user_id");
            row.serviceType = rs.getString("service_type");
            row.location = rs.getString("location");
            row.urgency = rs.getString("urgency");
            row.description = rs.getString("description");
            row.status = rs.getString("status");
            row.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
            return row;
        }
    }

    /**
     * Update the status of a service request
     * @param requestId The request ID
     * @param newStatus The new status
     * @return true if successful
     */
    public boolean updateServiceRequestStatus(int requestId, String newStatus) {
        String sql = "UPDATE service_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, requestId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating service request status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all reserved seats for a given flight number
     * @param flightNumber The flight number
     * @return List of reserved seat numbers (e.g., "12A", "14C")
     */
    public List<String> getReservedSeatsForFlight(String flightNumber) {
        List<String> reservedSeats = new ArrayList<>();
        String sql = "SELECT b.seat_number FROM bookings b JOIN flights f ON b.flight_id = f.flight_id WHERE f.flight_number = ? AND b.seat_number IS NOT NULL AND b.booking_status IN ('Confirmed', 'Checked In')";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, flightNumber);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reservedSeats.add(rs.getString("seat_number"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reserved seats: " + e.getMessage());
        }
        return reservedSeats;
    }
} 
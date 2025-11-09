-- Airport Management System Database Schema
-- MySQL Database

-- Drop database if it exists (for clean installs)
DROP DATABASE IF EXISTS airport_management;

-- Create database
CREATE DATABASE airport_management;
USE airport_management;

-- Create Users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- Store hashed passwords in production
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Create Flights table
CREATE TABLE flights (
    flight_id INT AUTO_INCREMENT PRIMARY KEY,
    flight_number VARCHAR(10) NOT NULL,
    airline VARCHAR(50) NOT NULL,
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'On Time',  -- On Time, Delayed, Boarding, Departed, Cancelled
    gate VARCHAR(5),
    terminal CHAR(1),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Passengers table
CREATE TABLE passengers (
    passenger_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    passport_number VARCHAR(20),
    nationality VARCHAR(50),
    date_of_birth DATE,
    contact_number VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Create Bookings table
CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(10) NOT NULL UNIQUE,
    passenger_id INT NOT NULL,
    flight_id INT NOT NULL,
    seat_number VARCHAR(5),
    booking_status VARCHAR(20) DEFAULT 'Confirmed',  -- Confirmed, Checked In, Cancelled
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id) ON DELETE CASCADE,
    FOREIGN KEY (flight_id) REFERENCES flights(flight_id) ON DELETE CASCADE
);

-- Create Baggage table
CREATE TABLE baggage (
    baggage_id INT AUTO_INCREMENT PRIMARY KEY,
    baggage_tag VARCHAR(15) NOT NULL UNIQUE,
    booking_id INT NOT NULL,
    weight DECIMAL(5,2),  -- Weight in kg
    status VARCHAR(20) DEFAULT 'Checked In',  -- Checked In, In Transit, Loaded, Delivered
    current_location VARCHAR(50),
    handling_time VARCHAR(20),  -- e.g., "75 sec"
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- Create Baggage Tracking table
CREATE TABLE baggage_tracking (
    tracking_id INT AUTO_INCREMENT PRIMARY KEY,
    baggage_id INT NOT NULL,
    location VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (baggage_id) REFERENCES baggage(baggage_id) ON DELETE CASCADE
);

-- Create Counters table (for check-in counters)
CREATE TABLE counters (
    counter_id INT AUTO_INCREMENT PRIMARY KEY,
    counter_number VARCHAR(5) NOT NULL,
    terminal CHAR(1) NOT NULL,
    status VARCHAR(20) DEFAULT 'Closed',  -- Open, Closed, Maintenance
    assigned_staff INT NULL
);

-- Create Staff table
CREATE TABLE staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    position VARCHAR(50) NOT NULL,  -- Check-in Agent, Security, Gate Agent
    shift VARCHAR(20) DEFAULT 'Day',  -- Day, Night, Morning
    terminal CHAR(1),
    contact_number VARCHAR(20),
    email VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Create Security Checkpoint table
CREATE TABLE security_checkpoints (
    checkpoint_id INT AUTO_INCREMENT PRIMARY KEY,
    checkpoint_number VARCHAR(5) NOT NULL,
    terminal CHAR(1) NOT NULL,
    status VARCHAR(20) DEFAULT 'Open',  -- Open, Closed, Maintenance
    current_queue_length INT DEFAULT 0,
    average_wait_time INT DEFAULT 0  -- In minutes
);

-- Create Gates table
CREATE TABLE gates (
    gate_id INT AUTO_INCREMENT PRIMARY KEY,
    gate_number VARCHAR(5) NOT NULL,
    terminal CHAR(1) NOT NULL,
    status VARCHAR(20) DEFAULT 'Available',  -- Available, Occupied, Maintenance
    current_flight_id INT NULL,
    FOREIGN KEY (current_flight_id) REFERENCES flights(flight_id) ON DELETE SET NULL
);

-- Create Notifications table
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,  -- NULL for system-wide notifications
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Insert sample admin user
INSERT INTO users (username, password, full_name, email, is_admin)
VALUES ('admin', 'admin123', 'System Administrator', 'admin@airport.com', TRUE);

-- Insert sample regular user
INSERT INTO users (username, password, full_name, email, is_admin)
VALUES ('user', 'user123', 'John Smith', 'john@example.com', FALSE);

-- Insert sample flights
INSERT INTO flights (flight_number, airline, origin, destination, departure_time, arrival_time, status, gate, terminal)
VALUES
('AA1234', 'American Airlines', 'New York (JFK)', 'Los Angeles (LAX)', 
 NOW() + INTERVAL 2 HOUR, NOW() + INTERVAL 7 HOUR, 'On Time', 'A12', 'A'),
('DL2345', 'Delta Airlines', 'Atlanta (ATL)', 'Chicago (ORD)', 
 NOW() + INTERVAL 3 HOUR, NOW() + INTERVAL 5 HOUR, 'Delayed', 'B5', 'B'),
('UA3456', 'United Airlines', 'Chicago (ORD)', 'Denver (DEN)', 
 NOW() + INTERVAL 1 HOUR, NOW() + INTERVAL 3 HOUR, 'Boarding', 'C8', 'C'),
('BA4567', 'British Airways', 'London (LHR)', 'New York (JFK)', 
 NOW() + INTERVAL 2 HOUR, NOW() + INTERVAL 10 HOUR, 'On Time', 'A7', 'A');

-- Insert sample passengers
INSERT INTO passengers (user_id, first_name, last_name, passport_number, nationality, date_of_birth)
VALUES 
(2, 'John', 'Smith', 'P123456', 'USA', '1985-03-15'),
(NULL, 'Jane', 'Doe', 'P234567', 'Canada', '1990-07-22');

-- Insert sample bookings
INSERT INTO bookings (booking_reference, passenger_id, flight_id, seat_number, booking_status)
VALUES 
('ABC123', 1, 1, '12A', 'Confirmed'),
('DEF456', 2, 2, '15B', 'Checked In');

-- Insert sample baggage
INSERT INTO baggage (baggage_tag, booking_id, weight, status, current_location, handling_time)
VALUES 
('BAG001245', 1, 23.5, 'In Transit', 'Sorting Area 2', '75 sec'),
('BAG002367', 2, 18.2, 'Checked In', 'Security Screening', '45 sec');

-- Insert sample staff
INSERT INTO staff (user_id, first_name, last_name, position, shift, terminal, email)
VALUES 
(1, 'Admin', 'User', 'Supervisor', 'Day', 'A', 'admin@airport.com'),
(NULL, 'Sarah', 'Johnson', 'Check-in Agent', 'Morning', 'B', 'sarah@airport.com'),
(NULL, 'Michael', 'Brown', 'Security Officer', 'Night', 'C', 'michael@airport.com');

-- Insert sample notifications
INSERT INTO notifications (user_id, message)
VALUES 
(2, 'Your flight AA1234 is now boarding at Gate A12'),
(2, 'Your baggage BAG001245 has been loaded onto flight AA1234'),
(NULL, 'All flights to Chicago are experiencing delays due to weather conditions');

-- Create view for passenger information with flight details
CREATE VIEW passenger_flight_view AS
SELECT 
    p.passenger_id,
    p.first_name,
    p.last_name,
    b.booking_reference,
    b.seat_number,
    b.booking_status,
    f.flight_number,
    f.airline,
    f.origin,
    f.destination,
    f.departure_time,
    f.status AS flight_status,
    f.gate,
    f.terminal
FROM 
    passengers p
JOIN 
    bookings b ON p.passenger_id = b.passenger_id
JOIN 
    flights f ON b.flight_id = f.flight_id;

-- Create view for baggage tracking with passenger and flight information
CREATE VIEW baggage_tracking_view AS
SELECT 
    bg.baggage_tag,
    bg.status AS baggage_status,
    bg.current_location,
    bg.handling_time,
    p.first_name,
    p.last_name,
    f.flight_number,
    f.airline,
    f.destination,
    f.departure_time,
    f.gate,
    f.terminal
FROM 
    baggage bg
JOIN 
    bookings b ON bg.booking_id = b.booking_id
JOIN 
    passengers p ON b.passenger_id = p.passenger_id
JOIN 
    flights f ON b.flight_id = f.flight_id; 

-- Service Requests table
CREATE TABLE service_requests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    location VARCHAR(100) NOT NULL,
    urgency VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
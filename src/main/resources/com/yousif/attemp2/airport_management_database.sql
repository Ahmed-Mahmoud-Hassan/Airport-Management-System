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
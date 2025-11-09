package com.yousif.attemp2;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to generate PDF boarding passes for passengers
 */
public class BoardingPassPDFGenerator {
    
    /**
     * Generates a boarding pass PDF for a passenger
     * 
     * @param passengerName The name of the passenger
     * @param flightNumber The flight number
     * @param destination The flight destination
     * @param gate The boarding gate
     * @param seat The passenger's seat assignment
     * @param boardingTime The boarding time
     * @param departureTime The departure time
     * @return The generated PDF file, or null if generation failed
     */
    public static File generateBoardingPass(
            Stage ownerStage,
            String passengerName,
            String flightNumber,
            String destination,
            String gate,
            String seat,
            String boardingTime,
            String departureTime
    ) {
        try {
            // Create file chooser for user to select save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Boarding Pass");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            
            String fileName = "BoardingPass_" + flightNumber + "_" + passengerName.replace(" ", "_") + ".pdf";
            fileChooser.setInitialFileName(fileName);
            
            File file = fileChooser.showSaveDialog(ownerStage);
            if (file == null) {
                return null; // User cancelled
            }
            
            // Generate PDF content (simplified simulation in this version)
            try (FileOutputStream fos = new FileOutputStream(file)) {
                // In a real implementation, we would use a PDF library like iText, Apache PDFBox, etc.
                // For this example, we'll just create a simple text file with .pdf extension
                StringBuilder content = new StringBuilder();
                content.append("BOARDING PASS\n\n");
                content.append("Passenger: ").append(passengerName).append("\n");
                content.append("Flight: ").append(flightNumber).append("\n");
                content.append("Destination: ").append(destination).append("\n");
                content.append("Gate: ").append(gate).append("\n");
                content.append("Seat: ").append(seat).append("\n");
                content.append("Boarding Time: ").append(boardingTime).append("\n");
                content.append("Departure Time: ").append(departureTime).append("\n\n");
                content.append("Generated: ").append(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                fos.write(content.toString().getBytes());
            }
            
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generates batch boarding passes for multiple passengers on the same flight
     * 
     * @param ownerStage The owner stage for file dialogs
     * @param flightNumber The flight number
     * @param destination The flight destination
     * @param gate The boarding gate
     * @param boardingTime The boarding time
     * @param departureTime The departure time
     * @param passengers Array of passenger info objects with name and seat
     * @return The generated PDF file, or null if generation failed
     */
    public static File generateBatchBoardingPasses(
            Stage ownerStage,
            String flightNumber,
            String destination,
            String gate,
            String boardingTime,
            String departureTime,
            PassengerInfo[] passengers
    ) {
        try {
            // Create file chooser for user to select save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Batch Boarding Passes");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            
            String fileName = "BoardingPasses_" + flightNumber + ".pdf";
            fileChooser.setInitialFileName(fileName);
            
            File file = fileChooser.showSaveDialog(ownerStage);
            if (file == null) {
                return null; // User cancelled
            }
            
            // Generate PDF content for all passengers
            try (FileOutputStream fos = new FileOutputStream(file)) {
                StringBuilder content = new StringBuilder();
                content.append("BATCH BOARDING PASSES - FLIGHT ").append(flightNumber).append("\n\n");
                content.append("Destination: ").append(destination).append("\n");
                content.append("Gate: ").append(gate).append("\n");
                content.append("Boarding Time: ").append(boardingTime).append("\n");
                content.append("Departure Time: ").append(departureTime).append("\n\n");
                
                for (PassengerInfo passenger : passengers) {
                    content.append("-----------------------------------\n");
                    content.append("Passenger: ").append(passenger.getName()).append("\n");
                    content.append("Seat: ").append(passenger.getSeat()).append("\n");
                    content.append("-----------------------------------\n\n");
                }
                
                content.append("Generated: ").append(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                fos.write(content.toString().getBytes());
            }
            
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Simple passenger information container class
     */
    public static class PassengerInfo {
        private final String name;
        private final String seat;
        
        public PassengerInfo(String name, String seat) {
            this.name = name;
            this.seat = seat;
        }
        
        public String getName() {
            return name;
        }
        
        public String getSeat() {
            return seat;
        }
    }
} 
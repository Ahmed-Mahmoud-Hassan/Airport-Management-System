package com.yousif.attemp2;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserNotificationsController implements UserBaseController {
    
    @FXML
    private VBox notificationsContainer;
    
    @FXML
    private CheckBox flightUpdatesCheck;
    
    @FXML
    private CheckBox boardingAlertsCheck;
    
    @FXML
    private CheckBox baggageAlertsCheck;
    
    @FXML
    private CheckBox securityAlertsCheck;
    
    @FXML
    private CheckBox specialOffersCheck;
    
    @FXML
    private ComboBox<String> notificationMethodCombo;
    
    @FXML
    private ComboBox<String> notificationTimingCombo;
    
    private List<Notification> notifications = new ArrayList<>();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private LoginController.UserData userData;
    
    @FXML
    public void initialize() {
        // Initialize notification method dropdown
        notificationMethodCombo.setItems(FXCollections.observableArrayList(
            "In-App", "Email", "SMS", "Push Notification"
        ));
        notificationMethodCombo.setValue("In-App");
        
        // Initialize notification timing dropdown
        notificationTimingCombo.setItems(FXCollections.observableArrayList(
            "30 minutes before", "1 hour before", "2 hours before", "3 hours before", "1 day before"
        ));
        notificationTimingCombo.setValue("1 hour before");
        
        // Load notifications for the user if set, otherwise clear
        if (userData != null) {
            loadUserNotificationsFromDb();
        } else {
            notifications.clear();
        }
        
        // Display notifications
        displayNotifications();
    }
    
    private void loadUserNotificationsFromDb() {
        notifications.clear();
        if (userData == null) return;
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            // Get user_id from users table using username
            int userId = -1;
            java.sql.ResultSet rsUser = db.executeQuery("SELECT user_id FROM users WHERE username = ?", userData.getUsername());
            if (rsUser.next()) {
                userId = rsUser.getInt("user_id");
            }
            if (userId == -1) return;
            String query = "SELECT notification_id, message, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
            java.sql.ResultSet rs = db.executeQuery(query, userId);
            while (rs.next()) {
                notifications.add(new Notification(
                    "Notification", // You can add a title column if you want
                    rs.getString("message"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    NotificationType.FLIGHT_UPDATE, // Or parse type if you store it
                    rs.getBoolean("is_read")
                ));
            }
        } catch (Exception e) {
            // handle error
        }
    }
    
    private void displayNotifications() {
        notificationsContainer.getChildren().clear();
        
        if (notifications.isEmpty()) {
            Label emptyLabel = new Label("No notifications to display");
            emptyLabel.getStyleClass().add("placeholder-text");
            notificationsContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Notification notification : notifications) {
            VBox notificationBox = createNotificationBox(notification);
            notificationsContainer.getChildren().add(notificationBox);
        }
    }
    
    private VBox createNotificationBox(Notification notification) {
        VBox box = new VBox(5);
        box.getStyleClass().add("notification-box");
        if (!notification.isRead()) {
            box.getStyleClass().add("unread-notification");
        }
        box.setPadding(new Insets(10));
        
        // Header with title and time
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Icon based on notification type
        FontIcon icon = new FontIcon();
        switch (notification.getType()) {
            case FLIGHT_UPDATE:
                icon.setIconLiteral("fas-plane");
                break;
            case BOARDING_ALERT:
                icon.setIconLiteral("fas-door-open");
                break;
            case BAGGAGE_ALERT:
                icon.setIconLiteral("fas-suitcase");
                break;
            case SECURITY_ALERT:
                icon.setIconLiteral("fas-shield-alt");
                break;
            case SPECIAL_OFFER:
                icon.setIconLiteral("fas-tag");
                break;
            default:
                icon.setIconLiteral("fas-bell");
        }
        
        Label titleLabel = new Label(notification.getTitle());
        titleLabel.getStyleClass().add("notification-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label timeLabel = new Label(formatter.format(notification.getTimestamp()));
        timeLabel.getStyleClass().add("notification-time");
        
        header.getChildren().addAll(icon, titleLabel, spacer, timeLabel);
        
        // Message
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.getStyleClass().add("notification-message");
        messageLabel.setWrapText(true);
        
        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button markReadBtn = new Button(notification.isRead() ? "Mark as Unread" : "Mark as Read");
        markReadBtn.getStyleClass().add("small-action-button");
        markReadBtn.setGraphic(new FontIcon(notification.isRead() ? "fas-envelope" : "fas-envelope-open"));
        
        markReadBtn.setOnAction(event -> {
            notification.setRead(!notification.isRead());
            box.getStyleClass().removeAll("unread-notification");
            if (!notification.isRead()) {
                box.getStyleClass().add("unread-notification");
            }
            markReadBtn.setText(notification.isRead() ? "Mark as Unread" : "Mark as Read");
            markReadBtn.setGraphic(new FontIcon(notification.isRead() ? "fas-envelope" : "fas-envelope-open"));
        });
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("small-action-button");
        deleteBtn.setGraphic(new FontIcon("fas-trash"));
        
        deleteBtn.setOnAction(event -> {
            notifications.remove(notification);
            displayNotifications();
        });
        
        actions.getChildren().addAll(markReadBtn, deleteBtn);
        
        box.getChildren().addAll(header, messageLabel, actions);
        return box;
    }
    
    @FXML
    private void markAllAsRead() {
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        displayNotifications();
        
        showAlert(Alert.AlertType.INFORMATION, "Notifications", "All notifications marked as read");
    }
    
    @FXML
    private void saveSettings() {
        // In a real application, this would save settings to a database or preferences file
        showAlert(Alert.AlertType.INFORMATION, "Settings Saved", 
                 "Your notification preferences have been saved.");
    }
    
    @FXML
    private void resetSettings() {
        // Reset checkboxes to default values
        flightUpdatesCheck.setSelected(true);
        boardingAlertsCheck.setSelected(true);
        baggageAlertsCheck.setSelected(true);
        securityAlertsCheck.setSelected(true);
        specialOffersCheck.setSelected(false);
        
        // Reset dropdowns to default values
        notificationMethodCombo.setValue("In-App");
        notificationTimingCombo.setValue("1 hour before");
        
        showAlert(Alert.AlertType.INFORMATION, "Settings Reset", 
                 "Your notification preferences have been reset to default values.");
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void setUserData(LoginController.UserData userData) {
        this.userData = userData;
        if (userData != null) {
            loadUserNotificationsFromDb();
            displayNotifications();
        }
    }
    
    // Notification type enum - changed from private to public
    public enum NotificationType {
        FLIGHT_UPDATE,
        BOARDING_ALERT,
        BAGGAGE_ALERT,
        SECURITY_ALERT,
        SPECIAL_OFFER
    }
    
    // Inner class to represent a notification
    public static class Notification {
        private final StringProperty title;
        private final StringProperty message;
        private final ObjectProperty<LocalDateTime> timestamp;
        private final NotificationType type;
        private boolean read;
        
        public Notification(String title, String message, LocalDateTime timestamp, 
                           NotificationType type, boolean read) {
            this.title = new SimpleStringProperty(title);
            this.message = new SimpleStringProperty(message);
            this.timestamp = new SimpleObjectProperty<>(timestamp);
            this.type = type;
            this.read = read;
        }
        
        public String getTitle() {
            return title.get();
        }
        
        /**
         * @deprecated This method is not used in the current implementation
         */
        @Deprecated
        public StringProperty titleProperty() {
            return title;
        }
        
        public String getMessage() {
            return message.get();
        }
        
        /**
         * @deprecated This method is not used in the current implementation
         */
        @Deprecated
        public StringProperty messageProperty() {
            return message;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp.get();
        }
        
        /**
         * @deprecated This method is not used in the current implementation
         */
        @Deprecated
        public ObjectProperty<LocalDateTime> timestampProperty() {
            return timestamp;
        }
        
        public NotificationType getType() {
            return type;
        }
        
        public boolean isRead() {
            return read;
        }
        
        public void setRead(boolean read) {
            this.read = read;
        }
    }
} 
module com.yousif.attemp2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires transitive javafx.base;
    requires transitive javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    
    // Add required modules for database connectivity
    requires transitive java.sql;
    requires java.base;
    requires java.desktop;

    // Combined exports and opens
    exports com.yousif.attemp2;
    opens com.yousif.attemp2 to javafx.fxml;
}
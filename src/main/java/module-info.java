module com.f216.sportsmanager {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.f216.sportsmanager to javafx.fxml;
    exports com.f216.sportsmanager;
    exports com.f216.sportsmanager.models;
    opens com.f216.sportsmanager.models to javafx.fxml;
    exports com.f216.sportsmanager.interfaces;
    opens com.f216.sportsmanager.interfaces to javafx.fxml;
}
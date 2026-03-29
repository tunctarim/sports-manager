module com.f216.sportsmanager {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.f216.sportsmanager to javafx.fxml;
    exports com.f216.sportsmanager;
}
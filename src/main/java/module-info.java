module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    exports org.example;
    opens org.example.controllers to javafx.fxml;

}
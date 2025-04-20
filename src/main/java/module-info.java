module com.example.pcimg {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires commons.math3;

    opens com.example.pcimg to javafx.fxml;
    exports com.example.pcimg;
}

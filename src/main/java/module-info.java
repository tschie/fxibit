module com.tylerschiewe.fxibit {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    opens com.tylerschiewe.fxibit to javafx.fxml;

    exports com.tylerschiewe.fxibit;
}

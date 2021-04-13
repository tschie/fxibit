module com.tylerschiewe.fxibit {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.web;
    requires java.logging;
    requires org.apache.commons.io;

    opens com.tylerschiewe.fxibit to javafx.fxml;

    exports com.tylerschiewe.fxibit;
}

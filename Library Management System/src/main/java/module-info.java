module com.lawpavilion.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.logging;

    opens com.lawpavilion.frontend.controller to javafx.fxml;
    exports com.lawpavilion.frontend.controller;
    exports com.lawpavilion.frontend.pojo;
    exports com.lawpavilion.frontend.utils;
    exports com.lawpavilion.frontend;
}
module com.example.project3 {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.naming;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires java.sql;
    requires mysql.connector.java;

    opens com.example.project3 to javafx.fxml;
    exports com.example.project3;
}
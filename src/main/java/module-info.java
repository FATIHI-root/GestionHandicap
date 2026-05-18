module ma.ac.uir.gestionhandicap {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires org.apache.pdfbox.io;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens ma.ac.uir.gestionhandicap to javafx.fxml;
    opens ma.ac.uir.gestionhandicap.controller.auth to javafx.fxml;
    opens ma.ac.uir.gestionhandicap.controller.admin to javafx.fxml;
    opens ma.ac.uir.gestionhandicap.controller.user to javafx.fxml;

    exports ma.ac.uir.gestionhandicap;
    exports ma.ac.uir.gestionhandicap.model;
    exports ma.ac.uir.gestionhandicap.model.enums;
}
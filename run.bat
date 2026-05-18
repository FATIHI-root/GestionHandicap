@echo off
java --module-path "target\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp "target\GestionHandicap-1.0-SNAPSHOT.jar;target\lib\*" ma.ac.uir.gestionhandicap.MainApp
pause

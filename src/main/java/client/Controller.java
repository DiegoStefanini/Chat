package client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

public class Controller {
    private Controller() {

    }

    public static int showAlert(String tipo, String title, String message) {
        Alert alert;
        if ("errore".equals(tipo)) {
            alert = new Alert(Alert.AlertType.ERROR);
        } else {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if ("conferma".equals(tipo)) {
            return (alert.showAndWait().get() == ButtonType.OK) ? 1 : 0 ;
        }
        alert.showAndWait();
        return 0;
    }
}
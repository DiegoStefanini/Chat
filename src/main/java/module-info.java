module org.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.base;
    requires com.google.gson;  // Aggiungi il modulo Gson
    exports data;  // Esporta il pacchetto 'data' per l'accesso ad altre librerie
    opens data to com.google.gson;

    opens client to javafx.fxml;
    exports client;
}
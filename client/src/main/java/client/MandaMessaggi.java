package client;

import data.Packet;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;

public class MandaMessaggi extends Application {

    private final Gson gson;
    private final PrintWriter MandaAlServer;
    private final String NomeClient;
    private final ChatPage Precedente;
    private final String Target;
    private BufferPacchetti buffer;
    ListView<String> chatListView;
    public MandaMessaggi(PrintWriter manda,  String nome, String tar, ChatPage prec, BufferPacchetti b) {
        MandaAlServer = manda;
        gson = new Gson();
        NomeClient = nome;
        Target = tar;
        Precedente = prec;
        buffer = b;
    }

    public void aggiungiMessaggio(Packet pacchetto) {
        if (!pacchetto.getMittente().equals(NomeClient)) {
            Platform.runLater(() -> {
                chatListView.getItems().add(pacchetto.getMittente() + ": " + pacchetto.getContenuto());
            });
        }
    }
    @Override
    public void start(Stage stage) throws IOException {
        // Creazione dei componenti per la schermata della chat
        chatListView = new ListView<>();

        TextField inputField = new TextField();
        Button indietroButton = new Button("Indietro");
        Label titleLabel = new Label(Target);

        // Impostazioni dello stile
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Layout superiore con pulsante e titolo
        HBox topBar = new HBox(indietroButton, titleLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(20);
        topBar.setStyle("-fx-padding: 10px; -fx-background-color: #d3d3d3;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setAlignment(Pos.CENTER);
        HBox.setHgrow(topBar, Priority.ALWAYS);

        // Layout principale
        BorderPane layout = new BorderPane();
        layout.setTop(topBar);
        layout.setCenter(chatListView);
        layout.setBottom(inputField);
        // Azioni sul pulsante "Indietro"
        indietroButton.setOnAction(e -> {
            ChatPage chatPage = Precedente;
            try {
                chatPage.start(new Stage());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            stage.close(); // chiudi la finestra
        });

        // Aggiungere messaggi
        inputField.setPromptText("Scrivi un messaggio...");
        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String message = inputField.getText().trim();
                // DA FARE: UN MESSAGGIO NON PUO CONTENERE "/ù"
                if (!message.isEmpty() && !message.contains("/ù")) {
                    // Invio del messaggio al server
                    Packet pacchetto = new Packet("MESSAGGIO", Target, NomeClient, message, false);
                    String json = gson.toJson(pacchetto);
                    MandaAlServer.println(json);

                    // Mostrare il messaggio nella chat
                    chatListView.getItems().add("Tu: " + message);
                    inputField.clear();
                } else {
                    Controller.showAlert("errore", "messaggio non valido", "non puoi mandare un messaggio vuoto !");
                }
            }
        });
        Packet pacch = new Packet("CARICAMESSAGGI", Target, NomeClient, "", false);
        String tosend = gson.toJson(pacch);
        MandaAlServer.println(tosend);
        chatListView.getItems().clear(); // Ripulisce la lista dei messaggi
        pacch = null;
        while (pacch == null) {
            if (buffer.getLast() != null && buffer.getLast().getHeader().equals("CARICAMESSAGGI")) {
                pacch = buffer.consuma();
            }
        }
        String[] Messaggi = pacch.getContenuto().split("/ù\\s*");
        if (Messaggi.length > 0 && !"".equals(Messaggi[0])) {

            for (String messaggio : Messaggi) {
                chatListView.getItems().add(messaggio);
            }
        }

        // Impostazioni della scena
        Scene scene = new Scene(layout, 400, 600);
        stage.setScene(scene);
        stage.setTitle("Chat - " + Target);
        stage.show();
    }
}

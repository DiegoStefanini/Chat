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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;

public class MandaMessaggi extends Application {

    private final Gson gson;
    private final PrintWriter MandaAlServer;
    private final BufferedReader RiceviDalServer;
    private final String NomeClient;
    private final Stage Precedente;
    private final String Target;
    private boolean ricezione = true;
    public MandaMessaggi(PrintWriter manda, BufferedReader leggi, Gson g, String nome, String tar, Stage prec) {
        MandaAlServer = manda;
        RiceviDalServer = leggi;
        gson = g;
        NomeClient = nome;
        Target = tar;
        Precedente = prec;
    }


    @Override
    public void start(Stage stage) throws IOException {
        // Creazione dei componenti per la schermata della chat
        ListView<String> chatListView = new ListView<>();

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
            Packet pacchetto = new Packet("INTERROMPI", Target, NomeClient, "", false);
            String json = gson.toJson(pacchetto);
            MandaAlServer.println(json);
            ricezione = false;
            Precedente.show();
            stage.close(); // Esempio: chiudi la finestra
        });

        // Aggiungere messaggi
        inputField.setPromptText("Scrivi un messaggio...");
        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String message = inputField.getText().trim();
                // DA FARE: UN MESSAGGIO NON PUO CONTENERE "/ù"
                if (!message.isEmpty()) {
                    // Invio del messaggio al server
                    Packet pacchetto = new Packet("MESSAGGIO", Target, NomeClient, message, false);
                    String json = gson.toJson(pacchetto);
                    MandaAlServer.println(json);

                    // Mostrare il messaggio nella chat
                    chatListView.getItems().add("Tu: " + message);
                    inputField.clear();


                }
            }
        });
        Packet pacch = new Packet("CARICAMESSAGGI", Target, NomeClient, "", false);
        String tosend = gson.toJson(pacch);
        MandaAlServer.println(tosend);
        chatListView.getItems().clear(); // Ripulisce la lista dei messaggi

        new Thread(() -> {
            try {
                while (ricezione) {
                    String json = RiceviDalServer.readLine();
                    // DA FARE: CONTROLLARE ERRORE Connection reset (QUANDO IL SERVER SI STOPPA, DA ERRORE, TROVIAMO UN MODO PER CONTROLLARE STA COSA E NON FAR CRASHARE L'APP CHE è BRUTTO <3)
                    if (json != null) {
                        Packet pacchetto = gson.fromJson(json, Packet.class);
                        if ("MESSAGGIO".equals(pacchetto.getHeader()) && (pacchetto.getMittente().equals(Target) || pacchetto.getDestinatario().equals(Target)) ) {
                            Platform.runLater(() -> {
                                chatListView.getItems().add(pacchetto.getMittente() + ": " + pacchetto.getContenuto());
                            });
                        } else if ("CARICAMESSAGGI".equals(pacchetto.getHeader())) {
                                String[] Messaggi = pacchetto.getContenuto().split("/ù\\s*");

                                if (Messaggi.length > 0 && !"".equals(Messaggi[0])) {
                                    for (String messaggio : Messaggi) {
                                        Platform.runLater(() -> {
                                            chatListView.getItems().add(messaggio);
                                        });
                                    }
                                }
                        } else if ("INTERROMPI".equals(pacchetto.getHeader())) {
                            ricezione = false;
                        }
                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();


        // Impostazioni della scena
        Scene scene = new Scene(layout, 400, 600);
        stage.setScene(scene);
        stage.setTitle("Chat - " + Target);
        stage.show();
    }
}

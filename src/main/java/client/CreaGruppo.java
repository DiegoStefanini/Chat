package client;

import data.Packet;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.google.gson.Gson;

public class CreaGruppo extends Application {

    private final Gson gson;
    private final PrintWriter MandaAlServer;
    private final String NomeClient;
    private final ChatPage Precedente;
    private BufferPacchetti buffer;
    private Ricezione ricezione;

    public CreaGruppo(PrintWriter manda, Gson g, String nome, ChatPage prec, BufferPacchetti b, Ricezione r) {
        MandaAlServer = manda;
        gson = g;
        NomeClient = nome;
        Precedente = prec;
        buffer = b;
        ricezione = r;
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Creazione dei componenti per la schermata della chat
        ListView<String> usersListView = new ListView<>();

        // Abilita la selezione multipla
        usersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        ArrayList<String> selezionati = new ArrayList<>(); // mi memorizzo quelli che ho selezionato

        usersListView.setOnMouseClicked(event -> {  // ho fatto così perchè senno devi mantenere shift per selezionare utenti
            if (event.getButton() == MouseButton.PRIMARY) { // clic sinistro
                String selectedItem = usersListView.getSelectionModel().getSelectedItem();

                if (selezionati.contains(selectedItem)) {  // Deseleziona
                    selezionati.remove(selectedItem);
                } else { // Seleziona
                    selezionati.add(selectedItem);
                }

                Platform.runLater(() -> {
                    usersListView.getSelectionModel().clearSelection(); // deseleziono tutti
                    for (String item : selezionati) {
                        usersListView.getSelectionModel().select(item); // li riseleziono tutti da codice
                    }
                });
            }
        });

        // Pulsanti per azioni
        Button indietroButton = new Button("Indietro");
        Button confermaButton = new Button("Conferma selezione");
        Label titleLabel = new Label("Crea Gruppo");
        TextField nomeGruppoField = new TextField();
        nomeGruppoField.setPromptText("Inserisci il nome del gruppo");
        nomeGruppoField.setPrefHeight(40);
        // Stile del titolo
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Layout superiore con pulsante e titolo
        HBox topBar = new HBox(indietroButton, titleLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(20);
        topBar.setStyle("-fx-padding: 10px; -fx-background-color: #d3d3d3;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setAlignment(Pos.CENTER);

        // Layout inferiore con pulsante di conferma
        HBox bottomBar = new HBox(confermaButton);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setSpacing(20);
        bottomBar.setStyle("-fx-padding: 10px;");

        // Layout principale
        BorderPane layout = new BorderPane();
        layout.setTop(topBar);
        layout.setCenter(usersListView);
        layout.setBottom(bottomBar);

        // Aggiungi il campo di testo per il nome del gruppo nel layout sopra la lista
        VBox topLayout = new VBox();
        topLayout.setAlignment(Pos.CENTER);
        topLayout.getChildren().addAll(topBar, nomeGruppoField);
        layout.setTop(topLayout); // Aggiungiamo il layout modificato con nomeGruppoField sopra

        // Azione del pulsante "Indietro"
        indietroButton.setOnAction(e -> {
            ChatPage chatPage = Precedente;
            try {
                chatPage.start(new Stage());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            stage.close(); // Torna alla finestra precedente
        });

        // Azione sul pulsante "Conferma selezione"
        confermaButton.setOnAction(e -> {
            String nomeGruppo = nomeGruppoField.getText().trim();
            StringBuilder partecipanti = new StringBuilder();

            if (nomeGruppo.isEmpty()) {
                Controller.showAlert("errore", "Nome vuoto", "Devi selezionare prima un nome del gruppo");
            } else {
                if (Controller.showAlert("conferma", "Crea il gruppo", "Sei sicuro di voler proseguire ?") == 1) {

                    for (String sele : selezionati) {
                        partecipanti.append(sele).append(",");
                    }
                    partecipanti.append(NomeClient);

                    Packet pacch = new Packet("CREAGRUPPO", partecipanti.toString(), "", nomeGruppo, false);
                    String tosend = gson.toJson(pacch);
                    MandaAlServer.println(tosend);
                    ChatPage chatPage = new ChatPage(MandaAlServer, NomeClient, buffer, ricezione);
                    try {
                        chatPage.start(new Stage());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    stage.close();
                }
            }
        });

        // Popola la lista con i dati ricevuti dal server
        Packet pacch = new Packet("GETALLUSERS", "", NomeClient, "", false);
        String tosend = gson.toJson(pacch);
        MandaAlServer.println(tosend);
        pacch = null;
        while (pacch == null) {
            if (buffer.getLast() != null && buffer.getLast().getHeader().equals("GETALLUSERS")) {
                pacch = buffer.consuma();
            }
        }
        String[] AllUsers = pacch.getContenuto().split(",\\s*");
        for (String utente : AllUsers) {
            if (!utente.equals(NomeClient)) {
                Platform.runLater(() -> usersListView.getItems().add(utente));
            }
        }

        // Impostazioni della scena
        Scene scene = new Scene(layout, 400, 600);
        stage.setScene(scene);
        stage.setTitle("Crea un gruppo");
        stage.show();
    }
}


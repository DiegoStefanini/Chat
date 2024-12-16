package client;

import data.Packet;
import javafx.application.Application;
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

public class ChatPage extends Application {

    private Gson gson;
    private PrintWriter MandaAlServer;
    private BufferedReader RiceviDalServer;
    private String NomeClient;
    private Stage Chatsstage;
    private boolean ricezione;
    public ChatPage(PrintWriter manda, BufferedReader leggi, Gson g, String nome) {
        MandaAlServer = manda;
        RiceviDalServer = leggi;
        gson = g;
        NomeClient = nome;
    }

    public void start(Stage stage) throws IOException {
        Packet pacchetto;
        Chatsstage = stage;
        // Creazione dei componenti per la schermata della chat
        ListView<String> chatListView = new ListView<>();


        pacchetto = new Packet("CHAT", "", "", "", false);
        String json = gson.toJson(pacchetto); // converto il pacchetto in JSON
        MandaAlServer.println(json);
        json = RiceviDalServer.readLine();
        pacchetto = gson.fromJson(json, Packet.class);

        String[] Chats = pacchetto.getContenuto().split(",\\s*");
        if (Chats.length > 0) {
            for (String chat : Chats) { // per ogni elemento di Chats identificato come chat
                chatListView.getItems().add(chat);

            }
        }

        // Campo di testo per la ricerca dell'utente
        TextField searchField = new TextField();
        searchField.setPromptText("Cerca utente");

        // Bottone con la scritta "Cerca"
        Button searchButton = new Button("Cerca");

        // Azione del bottone di ricerca
        searchButton.setOnAction(e -> {
            try {
                cercaUser(searchField.getText(), chatListView);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        // Layout per la ricerca, centrato orizzontalmente
        HBox searchLayout = new HBox(10, searchField, searchButton);
        searchLayout.setAlignment(Pos.CENTER); // Allinea gli elementi al centro

        // Bottone per creare un gruppo
        Button createGroupButton = new Button("Crea Gruppo");
        createGroupButton.setOnAction(e -> {
            try {
                creaGruppo();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        // Layout della schermata della chat
        VBox chatLayout = new VBox(10);
        chatLayout.setAlignment(Pos.CENTER); // Centra tutti gli elementi della VBox

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS); // mette uno spazio tra Bottone cerca gruppo e la fine

        chatLayout.getChildren().addAll( // mette in ordine tutti gli elementi della pagina
                new Label("Cerca un utente:"),
                searchLayout, // Campo di ricerca
                chatListView,
                createGroupButton,
                spacer
        );

        // Gestore per doppio click
        chatListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedChat = chatListView.getSelectionModel().getSelectedItem();
                if (selectedChat != null) {
                    try {
                        openChatWindow(selectedChat); // Seleziona la chat e apri la finestra della chat
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    Controller.showAlert("errore", "Errore", "Seleziona una chat!");
                }
            }
        });

        // Gestore per il tasto invio
        chatListView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String selectedChat = chatListView.getSelectionModel().getSelectedItem();
                if (selectedChat != null) {
                    try {
                        openChatWindow(selectedChat); // Seleziona la chat e apri la finestra della chat
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    Controller.showAlert("errore", "Errore", "Seleziona una chat!");
                }
            }
        });

        // Impostazione della scena e visualizzazione
        Scene scene = new Scene(chatLayout, 400, 300);
        stage.setTitle("SRPgram");
        stage.setScene(scene);
        stage.show();
    }

    // Metodo per la ricerca dell'utente
    private void cercaUser(String target, ListView<String> lista) throws IOException {
        // Puoi implementare la logica per la ricerca dell'utente
        for (String chat : lista.getItems()) {
            if (target.equals(chat)) {
                Controller.showAlert("errore", "ERRORE","Hai già una chat avviata con questo utente");
                return;
            }
        }
        Packet pacchetto = new Packet("AVVIACHAT", target, NomeClient, "", false);
        String json = gson.toJson(pacchetto); // converto il pacchetto in JSON
        MandaAlServer.println(json);
        json = RiceviDalServer.readLine();
        pacchetto = gson.fromJson(json, Packet.class);
        if (pacchetto.getError()) {
            Controller.showAlert("errore", "Errore", pacchetto.getContenuto());
        } else {
                lista.getItems().add(target);
        }
    }

    // Metodo per creare un gruppo
    private void creaGruppo() throws IOException {
        // Puoi implementare la logica per la creazione di un gruppo
        CreaGruppo crea = new CreaGruppo(MandaAlServer, RiceviDalServer, gson, NomeClient, Chatsstage);
        crea.start(new Stage());
        Chatsstage.close();
    }

    // Metodo per aprire la finestra della chat specifica
    private void openChatWindow(String target) throws IOException {
        MandaMessaggi mandamessaggi = new MandaMessaggi(MandaAlServer, RiceviDalServer, gson, NomeClient, target, Chatsstage);
        mandamessaggi.start(new Stage());
        Chatsstage.close();
    }

}

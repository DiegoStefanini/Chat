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
import java.util.Map;

import com.google.gson.Gson;

public class ChatPage extends Application {

    private Gson gson;
    private PrintWriter MandaAlServer;
    private String NomeClient;
    private Stage Chatsstage;
    private Ricezione ricezione;
    private BufferPacchetti buffer;
    Map<String, Object> Chats;
    private ListView<String> chatListView;
    public ChatPage(PrintWriter manda, String nome, BufferPacchetti b, Ricezione r) {
        MandaAlServer = manda;
        gson = new Gson();
        NomeClient = nome;
        buffer = b;
        ricezione = r;
    }

    public void updateNewMessage(Packet pacchetto) {
        String mittente = pacchetto.getMittente(); // Recuperiamo il mittente dal pacchetto

        // Controlliamo se il mittente esiste già nella mappa
        if (Chats.containsKey(mittente)) {
            // Recuperiamo il valore associato alla chiave del mittente
            Object value = Chats.get(mittente);

            // Verifica se il valore è un Double, lo converte in Integer
            if (value instanceof Double) {
                value = ((Double) value).intValue();  // Converte il Double in Integer
            }

            if (value instanceof Integer) {
                // Incrementiamo il valore
                Chats.put(mittente, (Integer) value + 1);  // Aggiorniamo la mappa
            }
        } else {
            // Se il mittente non esiste nella mappa, possiamo aggiungerlo con un valore di partenza (ad esempio, 1)
            Chats.put(mittente, 1);
        }
        updateChatListView();

        // Ora possiamo aggiornare la vista della chat con i nuovi valori
    }


    private void updateChatListView() {
        // Aggiorniamo la ListView con i nuovi dati della mappa
        Platform.runLater(() -> {
            chatListView.getItems().clear(); // Pulisce la ListView
        });
        if (!Chats.isEmpty()) {
            for (Map.Entry<String, Object> nome : Chats.entrySet()) {
                Object value = nome.getValue();

                Platform.runLater(() -> {
                    double doubleValue = (value instanceof Integer) ? ((Integer) value).doubleValue() : (Double) value;
                    chatListView.getItems().add(nome.getKey() + " [" + ((int) doubleValue) + "]");
                });

            }
        }
    }

    public void start(Stage stage) throws IOException {
        Packet pacchetto;
        Chatsstage = stage;
        ricezione.setChatPage(this);
        ricezione.setTarget("");

        // Creazione dei componenti per la schermata della chat
         chatListView = new ListView<>();


        pacchetto = new Packet("CHAT", "", "", "", false);
        String json = gson.toJson(pacchetto); // converto il pacchetto in JSON
        MandaAlServer.println(json);
        pacchetto = null;
        while (pacchetto == null) {
            if (buffer.getLast() != null && buffer.getLast().getHeader().equals("CHAT")) {
                pacchetto = buffer.consuma();
            }
        }

        json = pacchetto.getContenuto();

        Chats = gson.fromJson(json, Map.class);

        updateChatListView();
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
                new Label("Aggiungi un utente:"),
                searchLayout, // Campo di ricerca
                chatListView,
                createGroupButton,
                spacer
        );

        // Gestore per doppio click
        chatListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedChat = chatListView.getSelectionModel().getSelectedItem();
                selectedChat = selectedChat.split(" ")[0];
                if (selectedChat != null) {
                    try {
                        openChatWindow(selectedChat.trim()); // Seleziona la chat e apri la finestra della chat
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
        pacchetto = null;
        while (pacchetto == null) {
            if (buffer.getLast() != null && buffer.getLast().getHeader().equals("AVVIACHAT")) {
                pacchetto = buffer.consuma();
            }
        }

        if (pacchetto.getError()) {
            Controller.showAlert("errore", "Errore", pacchetto.getContenuto());
        } else {
            lista.getItems().add(target);
        }
    }

    // Metodo per creare un gruppo
    private void creaGruppo() throws IOException {
        // Puoi implementare la logica per la creazione di un gruppo
        System.out.println("nuovo ! ");
        CreaGruppo crea = new CreaGruppo(MandaAlServer, gson, NomeClient, this, buffer, ricezione);
        crea.start(new Stage());
        Chatsstage.close();
    }

    // Metodo per aprire la finestra della chat specifica
    private void openChatWindow(String target) throws IOException {
        ricezione.setTarget(target);
        MandaMessaggi mandamessaggi = new MandaMessaggi(MandaAlServer, NomeClient, target, this, buffer);
        ricezione.setMandaMessaggi(mandamessaggi);
        mandamessaggi.start(new Stage());
        Chatsstage.close();
    }

}

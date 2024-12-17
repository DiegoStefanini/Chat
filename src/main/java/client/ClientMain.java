package client;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import data.Packet;
public class ClientMain extends Application {
    private String serverAddress = "127.0.0.1"; // IP del server
    private int port = 12345;
    private Stage primaryStage;
    private Gson gson;
    private PrintWriter MandaAlServer;
    private BufferedReader RiceviDalServer;
    private Socket link;
    private String NomeClient;
    private Label errorLabel; // Label per il messaggio di errore

    public void stop() throws Exception {
        if (link != null) {
            Packet DaInviareAlServer = new Packet("LOGOUT", "", "", "", false);
            String json = gson.toJson(DaInviareAlServer);
            MandaAlServer.println(json);
        }
    }

    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        gson = new Gson();

        // Creazione dei componenti della schermata di login
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Registrazione");

        // Creazione della Label per errori
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;"); // Colore rosso per l'errore
        errorLabel.setVisible(false); // Nascondi la label inizialmente

        // Layout della schermata di login
        VBox loginLayout = new VBox(15);
        loginLayout.setAlignment(Pos.CENTER); // Centra gli elementi
        loginLayout.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20;"); // Aggiunge uno sfondo chiaro e un po' di padding
        loginLayout.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton, registerButton, errorLabel);

        // Azione del login
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (!username.isEmpty() && !password.isEmpty()) {
                try {
                    // mando credenziali al server
                    Packet DaInviareAlServer = new Packet("LOGIN", "", username, password, false);
                    String json = gson.toJson(DaInviareAlServer);
                    MandaAlServer.println(json);

                    json = RiceviDalServer.readLine();
                    Packet PacketRicevuto = gson.fromJson(json, Packet.class);
                    NomeClient = username;
                    if (!PacketRicevuto.getError()) {
                        openChatPage(); // Se le credenziali sono valide, apri la pagina della chat
                    } else {
                        showAlert("Le credenziali non sono valide !");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                showAlert("Inserisci username e password!");
            }
        });

        // Azione della registrazione
        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (!username.isEmpty() && !password.isEmpty()) {
                // DA FARE: UN NOME NON PUO CONTENERE "," E "/ù" e caratteri speciali in generale
                try {
                    // mando credenziali al server
                    Packet DaInviareAlServer = new Packet("REGISTRA", "", username, password, false);
                    String json = gson.toJson(DaInviareAlServer);
                    MandaAlServer.println(json);

                    json = RiceviDalServer.readLine();
                    Packet PacketRicevuto = gson.fromJson(json, Packet.class);
                    NomeClient = username;
                    if (!PacketRicevuto.getError()) {
                        openChatPage(); // Se le credenziali sono valide, apri la pagina della chat
                    } else {
                        showAlert(PacketRicevuto.getContenuto());
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                showAlert("Inserisci username e password!");
            }
        });

        // Creazione della scena e configurazione della finestra
        Scene scene = new Scene(loginLayout, 400, 300);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();

        try {
            // Crea una connessione al server
            link = new Socket(serverAddress, port);
        } catch (IOException _) {
        }


        if (link == null) {
            showAlert("Impossibile collegarsi al server");
            return;
        }

        MandaAlServer = new PrintWriter(link.getOutputStream(), true);
        RiceviDalServer = new BufferedReader(new InputStreamReader(link.getInputStream()));


    }

    // Metodo per aprire la schermata della chat
    private void openChatPage() throws IOException {
        BufferPacchetti buffer = new BufferPacchetti();
        Ricezione r = new Ricezione(buffer, RiceviDalServer);
        Thread ThreadRicezione = new Thread(r);
        ThreadRicezione.start();
        ChatPage chatPage = new ChatPage(MandaAlServer, NomeClient, buffer, r);
        chatPage.start(new Stage()); // Crea e avvia la schermata della chat
        primaryStage.close(); // Chiudi la schermata di login
    }

    // Metodo per mostrare un messaggio di errore nella schermata sotto il bottone
    private void showAlert(String message) {
        errorLabel.setText(message); // Imposta il testo dell'errore
        errorLabel.setVisible(true); // Mostra il messaggio
    }

    public static void main(String[] args) {
        launch(args);
    }
}

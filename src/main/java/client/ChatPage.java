package client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ChatPage extends Application {

    @Override
    public void start(Stage stage) {
        // Creazione dei componenti per la schermata della chat
        ListView<String> chatListView = new ListView<>();
        chatListView.getItems().addAll("Chat 1", "Chat 2", "Chat 3");

        Button selectChatButton = new Button("Seleziona Chat");

        // Layout della schermata della chat
        VBox chatLayout = new VBox(10);
        chatLayout.getChildren().addAll(new Label("Seleziona una chat:"), chatListView, selectChatButton);

        // Azione del bottone per selezionare la chat
        selectChatButton.setOnAction(e -> {
            String selectedChat = chatListView.getSelectionModel().getSelectedItem();
            if (selectedChat != null) {
                openChatWindow(selectedChat); // Seleziona la chat e apri la finestra della chat
            } else {
                showAlert("Errore", "Seleziona una chat!");
            }
        });

        Scene scene = new Scene(chatLayout, 400, 300);
        stage.setTitle("Seleziona una Chat");
        stage.setScene(scene);
        stage.show();
    }

    // Metodo per aprire la finestra della chat specifica
    private void openChatWindow(String chatName) {
        // Puoi implementare una nuova finestra di chat in base al nome della chat
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chat Selezionata");
        alert.setHeaderText(null);
        alert.setContentText("Sei entrato nella chat: " + chatName);
        alert.showAndWait();
    }

    // Metodo per mostrare un alert in caso di errore
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

//    public static void main(String[] args) {
//        launch(args);
//    }
}

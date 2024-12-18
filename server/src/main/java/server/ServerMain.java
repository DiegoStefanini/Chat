package server;
import java.net.Socket;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ServerMain {

    private static final String URL = "jdbc:mysql://localhost:3306/chatmultiutente"; // Sostituisci con il tuo URL
    private static final String USER = "root"; // Sostituisci con il tuo username
    private static final String PASSWORD = "";
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Carica il driver JDBC (opzionale con le versioni moderne di Java)
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Crea la connessione
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connessione al database riuscita!");
        } catch (SQLException e) {
            System.out.println("Errore di connessione al database!");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Classe non trovata!");
            throw new RuntimeException(e);
        }
        return connection;
    }


    public static void main(String[] args) {
        int port = 12345; // Porta su cui il server ascolta
        GestoreClients gestore = new GestoreClients();
        Connection connessione = getConnection();

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server avviato. In attesa di connessioni...");

            while (true) {
                // Accetta e aspetta la connessione di un client una nuova connessione dal client
                Socket client = serverSocket.accept();
                System.out.println("Nuovo client connesso, in attesa che faccia il login...");
                Thread handler = new Thread(new ClientHandler(client,gestore,connessione));
                handler.start();
            }

        } catch (Exception e) {
            System.err.println("Errore del server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
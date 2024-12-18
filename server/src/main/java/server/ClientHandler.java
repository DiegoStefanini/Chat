package server;
import data.Packet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import com.google.gson.Gson;

class ClientHandler implements Runnable {
    //attributi
    private Socket link;
    private GestoreClients UtentiOnline;
    private String MioNome;
    private Connection connessione;
    private int UserID;
    public ClientHandler(Socket s, GestoreClients v, Connection connessione) {
        this.link = s;
        this.UtentiOnline = v;
        this.connessione = connessione;
    }

    public void cleanup() {
        try {
            link.close();
        } catch (IOException e) {
        }
        UtentiOnline.Logout(MioNome);
    }

    public void attendi(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException _) {
        }
    }

    private int checkCredenziali(String username, String password) {
        String query = "SELECT id FROM user WHERE Password = ? AND Nome = ?";
        try (PreparedStatement preparedStatement = connessione.prepareStatement(query)) {
            preparedStatement.setString(1, password);  // Imposta la password come primo parametro
            preparedStatement.setString(2, username);  // Imposta il nome come secondo parametro

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Se c'è un risultato, restituisci l'ID dell'utente
                return resultSet.getInt("id");  // Restituisce l'ID
            } else {
                // Se non ci sono risultati, le credenziali non sono corrette
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Errore durante il login!");
            e.printStackTrace();
            return -1;
        }
    }

    private int getUserID(String username) {
        String query = "SELECT id FROM user WHERE Nome = ?";
        try (PreparedStatement preparedStatement = connessione.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Controlla se ci sono righe nel ResultSet
            if (resultSet.next()) {
                // Recupera l'ID dalla prima colonna del ResultSet
                return resultSet.getInt("id");
            } else {
                // Ritorna un valore speciale se l'utente non esiste
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Errore durante il check se l'utente esiste!");
            throw new RuntimeException(e);
        }
    }
    private String getUsers() {
        String query = "SELECT nome FROM user";
        try (PreparedStatement preparedStatement = connessione.prepareStatement(query)) {
            ResultSet result = preparedStatement.executeQuery();
            boolean first = true;
            StringBuilder restituisci = new StringBuilder();
            while(result.next()){
                if (!first) {
                    restituisci.append(","); // Separatore
                }
                restituisci.append(result.getString("nome"));
                first = false;
            }
            // Controlla se ci sono righe nel ResultSet
            return restituisci.toString();
        } catch (SQLException e) {
            System.out.println("Errore durante ricerca utenti!");
            throw new RuntimeException(e);
        }
    }
    private String getChat() {
        Map<String, Integer> associativo = new HashMap<>();
        String query = "SELECT id_gruppo, da_leggere FROM relazione_utenti WHERE id_utente = ? ORDER BY id";
        Gson gson = new Gson();
        try (PreparedStatement preparedStatement = connessione.prepareStatement(query)) {
            preparedStatement.setInt(1, UserID);
            // Esegui la query per recuperare gli id_gruppo
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                boolean first = true; // Indica se siamo al primo elemento, per aggiungere la virgola
                while (resultSet.next()) {

                    int groupId = resultSet.getInt("id_gruppo");
                    int daLeggere = resultSet.getInt("da_leggere");
                    // Conta il numero di partecipanti nel gruppo
                    int partecipanti = countGroupParticipants(groupId);
                    String groupName;
                    // Se ci sono solo due partecipanti, recupera il nome dell'altro utente
                    if (partecipanti == 2) {
                        groupName = getOtherUserName(UserID, groupId);
                    } else {
                        groupName = getGroupNameById(groupId); // Nome del gruppo
                    }
                    associativo.put(groupName, daLeggere);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return gson.toJson(associativo);
    }

    // Funzione per contare il numero di partecipanti in un gruppo
    private int countGroupParticipants(int groupId) {
        String query = "SELECT COUNT(id_utente) AS partecipanti FROM relazione_utenti WHERE id_gruppo = ?";
        try (PreparedStatement preparedStatement = connessione.prepareStatement(query)) {
            preparedStatement.setInt(1, groupId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("partecipanti");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0; // Se non trovi alcun risultato, restituire 0
    }

    // Funzione per ottenere il nome dell'altro utente nel gruppo
    private String getOtherUserName(int userID, int groupId) {
        String query = "SELECT nome FROM user " +
                "JOIN relazione_utenti ON user.id = relazione_utenti.id_utente " +
                "WHERE relazione_utenti.id_gruppo = ? AND user.id != ?";
        try (PreparedStatement preparedStatement = connessione.prepareStatement(query)) {
            preparedStatement.setInt(1, groupId);
            preparedStatement.setInt(2, userID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("nome");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    // Funzione per ottenere il nome del gruppo per id_gruppo
    private String getGroupNameById(int groupId) {
        String query = "SELECT nome FROM gruppi WHERE id = ?";
        try (PreparedStatement preparedStatement = connessione.prepareStatement(query)) {
            preparedStatement.setInt(1, groupId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("nome");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }


    private int addUser( String username, String password) {
        if (getUserID(username) != -1) {
            return -1; // L'utente esiste già
        }

        String query = "INSERT INTO user (Nome, Password) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connessione.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, username);  // Primo parametro: username
            preparedStatement.setString(2, password); // Secondo parametro: password
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                // Recupera l'ID generato
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1); // L'ID generato
                        System.out.println("Utente aggiunto con successo con ID: " + userId);
                        return userId;
                    } else {
                        System.out.println("ID non generato.");
                        return -1;
                    }
                }
            } else {
                System.out.println("Nessuna riga inserita.");
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Errore durante l'inserimento dell'utente!");
            e.printStackTrace();
            return -1;
        }
    }

    private int startChat(String username) {
        int target = getUserID(username);
        if (target == -1) {
            return -1; // L'utente specificato non esiste
        }

        String queryGruppi = "INSERT INTO gruppi (NOME) VALUES (?)";
        try (PreparedStatement preparedStatementGruppi = connessione.prepareStatement(queryGruppi, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatementGruppi.setString(1, (username+":"+MioNome));
            int rowsInsertedGruppi = preparedStatementGruppi.executeUpdate();

            if (rowsInsertedGruppi > 0) {
                // Recupera l'ID del gruppo appena generato
                try (ResultSet generatedKeys = preparedStatementGruppi.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int groupId = generatedKeys.getInt(1); // L'ID generato del gruppo

                        // Inserisci i record nella tabella relazione_utenti
                        String queryRelazione = "INSERT INTO relazione_utenti (id_gruppo, id_utente) VALUES (?, ?)";
                        try (PreparedStatement preparedStatementRelazione = connessione.prepareStatement(queryRelazione)) {
                            // Primo record: UserID corrente
                            preparedStatementRelazione.setInt(1, groupId);
                            preparedStatementRelazione.setInt(2, UserID);
                            preparedStatementRelazione.executeUpdate();

                            // Secondo record: Target (utente con cui si sta avviando la chat)
                            preparedStatementRelazione.setInt(2, target);
                            preparedStatementRelazione.executeUpdate();
                        }

                        System.out.println("Chat creata con successo con ID: " + groupId + " tra " + UserID + " e " + target);
                        return groupId;
                    } else {
                        System.out.println("Errore: ID del gruppo non generato.");
                        return -1;
                    }
                }
            } else {
                System.out.println("Errore: Nessuna riga inserita nella tabella gruppi.");
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Errore durante l'avvio della chat!");
            e.printStackTrace();
            return -1;
        }
    }
    private int getGruppoId(String nome) {
        String query = "SELECT id FROM gruppi WHERE nome = ?";
        try (PreparedStatement stmt = connessione.prepareStatement(query)) {
            stmt.setString(1, nome);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");  // Restituisce l'ID
            } else {
                return -1; // non esiste il gruppo
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void InserisciMessaggio(String mittente, String destinatario, String contenuto) {
        String query = "INSERT INTO messaggi (mittente, destinatario, contenuto) VALUES (?, ?, ?)";
        String query2 = "UPDATE relazione_utenti SET da_leggere = da_leggere + 1 WHERE id_gruppo = (SELECT id FROM gruppi WHERE nome = ?) AND id_utente = ?";
        String target;
        if (getGruppoId(destinatario) == -1) {
            target = (destinatario + ":" + MioNome);
        } else {
            target = destinatario;
        }
        try (PreparedStatement stmt = connessione.prepareStatement(query2)) {
            stmt.setString(1, target);
            stmt.setInt(2, getUserID(destinatario));
            stmt.executeUpdate();
        }catch (SQLException e) {
            System.out.println("Errore durante l'inserimento del messaggio!");
            e.printStackTrace();
        }
        try (PreparedStatement preparedStatement = connessione.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            int idDestinatario = (getUserID(destinatario) != -1) ? getUserID(destinatario) :  getGruppoId(destinatario);
            preparedStatement.setInt(1, getUserID(mittente));
            preparedStatement.setInt(2, idDestinatario);
            preparedStatement.setString(3, contenuto);
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Messaggio tra " +mittente+" e "+destinatario+" inserito");
            } else {
                System.out.println("Nessuna riga inserita.");
            }
        } catch (SQLException e) {
            System.out.println("Errore durante l'inserimento del messaggio!");
            e.printStackTrace();
        }
    }
    private void resetDaLeggere(String target) {
        String query = "UPDATE relazione_utenti SET da_leggere = 0 WHERE id_gruppo = (SELECT id FROM gruppi WHERE nome = ?) AND id_utente = ?";
        String formattedTarget = (target + ":" + MioNome);

        try (PreparedStatement stmt = connessione.prepareStatement(query)) {
            stmt.setString(1, formattedTarget);
            stmt.setInt(2, UserID);

            // per vedere se la ha cambiata
            int rowsAffected = stmt.executeUpdate();

            // se non ci sono cambiamenti allora inverti target e mio nome (dipende dal primo che ha avviato la chat)
            if (rowsAffected == 0) {
                formattedTarget = (MioNome + ":" + target);
                stmt.setString(1, formattedTarget);
                rowsAffected = stmt.executeUpdate();
            }

            // non dovrebbe mai succedere
            if (rowsAffected == 0) {
                System.out.println("nessun aggiornamento per " + target);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String caricaMessaggi(String target) {
        int targetId = getUserID(target); // Ottiene l'ID utente del destinatario
        boolean isGruppo = false;

        // Controlla se il target è un gruppo
        if (targetId == -1) {
            isGruppo = true;
            targetId = getGruppoId(target); // Ottiene l'ID del gruppo
        }

        StringBuilder messaggiConcatenati = new StringBuilder();

        // Query per messaggi privati o di gruppo
        String query;
        if (isGruppo) {
            query = "SELECT contenuto, mittente, u.nome AS mittente_nome " +
                    "FROM messaggi m " +
                    "JOIN user u ON u.id = m.mittente " +
                    "WHERE m.destinatario = ? " +
                    "ORDER BY m.created_at";
        } else {
            query = "SELECT contenuto, mittente FROM messaggi " +
                    "WHERE (mittente = ? AND destinatario = ?) OR (mittente = ? AND destinatario = ?) " +
                    "ORDER BY created_at";
        }

        try (PreparedStatement stmt = connessione.prepareStatement(query)) {
            if (isGruppo) {
                stmt.setInt(1, targetId); // ID del gruppo
            } else {
                stmt.setInt(1, UserID); // ID dell'utente mittente
                stmt.setInt(2, targetId); // ID del destinatario
                stmt.setInt(3, targetId); // ID del destinatario
                stmt.setInt(4, UserID); // ID dell'utente mittente
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String contenuto = rs.getString("contenuto");
                    int mittenteId = rs.getInt("mittente");
                    String mittenteNome = isGruppo ? rs.getString("mittente_nome") : target;

                    // Determina se il messaggio è stato inviato dall'utente corrente
                    if (mittenteId == UserID) {
                        messaggiConcatenati.append("Tu: ").append(contenuto).append("/ù");
                    } else {
                        messaggiConcatenati.append(mittenteNome).append(": ").append(contenuto).append("/ù");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return messaggiConcatenati.toString();
    }


    private void CreaGruppo(String partecipanti, String nome) {
        String[] Partecipanti = partecipanti.split(",\\s*");

        String query = "INSERT INTO gruppi (nome) VALUE (?)";
        try (PreparedStatement preparedStatement = connessione.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, nome);
            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                // Retrieve the generated group ID
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int groupId = generatedKeys.getInt(1); // Get the generated group ID
                        System.out.println("Gruppo creato con successo con ID: " + groupId);

                        // Insert each participant into the 'relazione_utenti' table
                        for (String partecipante : Partecipanti) {
                            int userId = getUserID(partecipante);
                            if (userId != -1) {
                                // Insert the participant into the 'relazione_utenti' table
                                String queryRelazione = "INSERT INTO relazione_utenti (id_gruppo, id_utente) VALUES (?, ?)";
                                try (PreparedStatement preparedStatementRelazione = connessione.prepareStatement(queryRelazione)) {
                                    preparedStatementRelazione.setInt(1, groupId);
                                    preparedStatementRelazione.setInt(2, userId);
                                    preparedStatementRelazione.executeUpdate();
                                }
                            } else {
                                System.out.println("Utente " + partecipante + " non trovato.");
                            }
                        }

                        System.out.println("Partecipanti aggiunti correttamente al gruppo.");
                    } else {
                        System.out.println("Errore: ID del gruppo non generato.");
                    }
                }
            } else {
                System.out.println("Errore: Nessuna riga inserita nella tabella gruppi.");
            }
        } catch (SQLException e) {
            System.out.println("Errore durante la creazione del gruppo!");
            e.printStackTrace();
        }
    }
    private ArrayList<String> getGruppoPartecipanti(int gruppoID) {
        String query = "SELECT u.nome FROM relazione_utenti r INNER JOIN user u ON r.id_utente = u.id WHERE r.id_gruppo = ?";
        ArrayList<String> partecipanti = new ArrayList<>();

        try (PreparedStatement preparedStatement = connessione.prepareStatement(query)) {
            preparedStatement.setInt(1, gruppoID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    partecipanti.add(resultSet.getString("nome"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Errore durante il recupero dei partecipanti del gruppo.");
            e.printStackTrace();
        }

        return partecipanti;
    }

    public void run() {
        BufferedReader RiceviDalClient;
        PrintWriter InviaAlClient;
        try {

            RiceviDalClient = new BufferedReader(new InputStreamReader(link.getInputStream())); // RICEVE MESSAGGI DAL CLIENT
            InviaAlClient = new PrintWriter(link.getOutputStream(), true); // INVIA MESSAGGI AL CLIENT
            Gson gson = new Gson(); // Crea istanza di gson
            Packet PacketRicevuto = null;
            while (PacketRicevuto == null || !"LOGOUT".equals(PacketRicevuto.getHeader())) {//  da cambiare questa condizione, non va bene, troviamo un altro modo
                String json = RiceviDalClient.readLine(); // Richiesta bloccante

                PacketRicevuto = gson.fromJson(json, Packet.class); // converte la stringa che gli è arrivata in Packet
                Packet PacketDaInviare = null;
                if ("REGISTRA".equals(PacketRicevuto.getHeader())) { // se REGISTRA == PacketRicevuto.getHeader();
                    if ((UserID = addUser( PacketRicevuto.getMittente(), PacketRicevuto.getContenuto())) == -1) {
                        PacketDaInviare = new Packet("NOTIFICATION", "","", "L'utente che hai scelto esiste già", true);
                    } else {
                        PacketDaInviare = new Packet("NOTIFICATION", "","", "Utente creato con successo", false);
                        MioNome = PacketRicevuto.getMittente();
                        UtentiOnline.Login(link, PacketRicevuto.getMittente());
                    }
                } else if ("LOGIN".equals(PacketRicevuto.getHeader())) {
                    if ((UserID = checkCredenziali(PacketRicevuto.getMittente(), PacketRicevuto.getContenuto())) == -1) {
                        PacketDaInviare = new Packet("NOTIFICATION", "","", "Le credenziali non sono corrette", true);
                    } else {
                        PacketDaInviare = new Packet("NOTIFICATION", "","", "Hai effettuato l'accesso !", false);
                        MioNome = PacketRicevuto.getMittente();
                        UtentiOnline.Login(link, PacketRicevuto.getMittente());
                    }
                } else if ("CHAT".equals(PacketRicevuto.getHeader())) {
                    PacketDaInviare = new Packet("CHAT", "", "", getChat(), false );
                } else if ("AVVIACHAT".equals(PacketRicevuto.getHeader())) {
                    int ChatID = startChat(PacketRicevuto.getDestinatario());
                    if (ChatID != -1) {
                        PacketDaInviare = new Packet("AVVIACHAT",  ChatID + "", "", "Puoi iniziare a chattare con " + PacketRicevuto.getDestinatario(), false);
                    } else {
                        PacketDaInviare = new Packet("AVVIACHAT", "", "", "L'utente che hai cercato non esiste", true);
                    }
                } else if ("MESSAGGIO".equals(PacketRicevuto.getHeader())) {
                    try {
                        if (getUserID(PacketRicevuto.getDestinatario()) != -1) { // sta inviando unicast per forza
                            Socket SocketDestinatario = UtentiOnline.isOnline(PacketRicevuto.getDestinatario());
                            if (SocketDestinatario != null) { // vuol dire che è online
                                // DA AGGIORNARE DATABASE
                                PacketDaInviare = new Packet("MESSAGGIO", PacketRicevuto.getDestinatario(), PacketRicevuto.getMittente(), PacketRicevuto.getContenuto(), false);
                                PrintWriter InviaAlDestinatario = new PrintWriter(SocketDestinatario.getOutputStream(), true);
                                json = gson.toJson(PacketDaInviare);
                                InviaAlDestinatario.println(json);
                                System.out.println("Online. Inviato correttamente");
                            } else {
                                System.out.println("non online");
                            }

                        } else { // deve prendere tutti i partecipanti del gruppo
                            ArrayList<String> destinatari = getGruppoPartecipanti(getGruppoId(PacketRicevuto.getDestinatario()));
                            for (String destinatario : destinatari) {
                                Socket SocketDestinatario = UtentiOnline.isOnline(destinatario);
                                if (SocketDestinatario != null) { // vuol dire che è online
                                    // DA AGGIORNARE DATABASE
                                    PacketDaInviare = new Packet("MESSAGGIO", PacketRicevuto.getDestinatario(), PacketRicevuto.getMittente(), PacketRicevuto.getContenuto(), false);
                                    PrintWriter InviaAlDestinatario = new PrintWriter(SocketDestinatario.getOutputStream(), true);
                                    json = gson.toJson(PacketDaInviare);
                                    InviaAlDestinatario.println(json);
                                    System.out.println(destinatario + "Online. Inviato correttamente");
                                } else {
                                    System.out.println("non online");
                                }
                            }
                        }
                        InserisciMessaggio(PacketRicevuto.getMittente(), PacketRicevuto.getDestinatario(), PacketRicevuto.getContenuto());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if ("GETALLUSERS".equals(PacketRicevuto.getHeader())) {
                    PacketDaInviare = new Packet("GETALLUSERS", "", "", getUsers(), false );
                } else if ("CREAGRUPPO".equals(PacketRicevuto.getHeader())) {
                    CreaGruppo(PacketRicevuto.getDestinatario(), PacketRicevuto.getContenuto());
                } else if ("CARICAMESSAGGI".equals(PacketRicevuto.getHeader())) {
                    resetDaLeggere(PacketRicevuto.getDestinatario());
                    PacketDaInviare = new Packet("CARICAMESSAGGI", "", "", caricaMessaggi(PacketRicevuto.getDestinatario()), false);
                } else if ("INTERROMPI".equals(PacketRicevuto.getHeader())) {
                    PacketDaInviare = new Packet("INTERROMPI", "", "", "", false);

                }
                if (PacketDaInviare != null ) {
                    json = gson.toJson(PacketDaInviare);
                    InviaAlClient.println(json);
                }
            }
            cleanup();
        }catch(IOException _) {
            System.out.println("Client " + MioNome + " sloggato correttamente.");
            cleanup();
            return;
        }
        try {
            RiceviDalClient.close();
            InviaAlClient.close();
        } catch (IOException _) {
        }
        cleanup();
    }
}
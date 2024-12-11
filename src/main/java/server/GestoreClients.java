package server;

import java.net.Socket;
import java.util.HashMap;


public class GestoreClients {
    HashMap<Socket, String> OnlineUsers = new HashMap<>();

    public synchronized void Login(Socket nuovoSocket, String nuovoClient) {
        OnlineUsers.put(nuovoSocket, nuovoClient);
        System.out.println("Nuovo client online con ip " + nuovoSocket.getInetAddress() + " e nome " + nuovoClient );
    }

    public synchronized void Logout(String nomeDaRimuovere) {
        OnlineUsers.entrySet().removeIf(entry -> entry.getValue().equals(nomeDaRimuovere));
    }

    public synchronized Socket isOnline(String utente) {
        return OnlineUsers.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(utente))
                .map(entry -> entry.getKey())
                .findFirst()
                .orElse(null);
    }
}

// FATTO DA CHAT GPT NEMMENO DIEGO SA PRECISAMENTE COSA FA, PERò I METODI RESTITUSTITUISCONO QUELLO CHE DICE IL NOME
package client;

import com.google.gson.Gson;
import data.Packet;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;

public class Ricezione implements Runnable {
    private boolean ricezione = true;
    private BufferedReader RiceviDalServer;
    private Gson gson;
    private BufferPacchetti buffer;
    private String target;
    private ChatPage chatpage;
    private MandaMessaggi mandaMessaggi;
    public Ricezione(BufferPacchetti b, BufferedReader ricevi) throws IOException {
        RiceviDalServer = ricevi;
        gson = new Gson();
        buffer = b;
        target = "";
    }
    public void setMandaMessaggi(MandaMessaggi manda) {
        mandaMessaggi = manda;
    }
    public synchronized void setRicezione(boolean how) {
        ricezione = how;
    }

    public synchronized void setTarget(String how) {
        target = how;
    }

    public void setChatPage(ChatPage chat) {
        chatpage = chat;
    }

    public void run() {
        try {
            while (true) {
                if (ricezione) {
                    String json = RiceviDalServer.readLine();

                    if (json != null) {
                        Packet pacchetto = gson.fromJson(json, Packet.class);
                        if (pacchetto.getHeader().equals("MESSAGGIO")) {
                            if ("".equals(target)) {
                                System.out.println("da aggiornare lista messaggi");
                                chatpage.updateNewMessage(pacchetto);
                            } else {
                                if ((pacchetto.getMittente().equals(target) || pacchetto.getDestinatario().equals(target)) ) {
                                    mandaMessaggi.aggiungiMessaggio(pacchetto);
                                }
                            }
                        } else {
                            buffer.add(pacchetto);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("CONNESSIONE INTERROTA, SERVER OFFLINE! ");
        }
    }
}

package client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import com.google.gson.Gson;
import data.Packet;
public class ClientMain {

    private static final int MAX_TRY = 3;
    private static String NomeClient = null;
    private static ArrayList<String> Chats;
    private static Scanner scanner;
    private static PrintWriter MandaAlServer;
    private static BufferedReader RiceviDalServer;
    private static Gson gson;
    public static void attendi(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException _) {
        }
    }


    public static void startSendMessage(String target) {
        String messaggio = "";
        Packet DaInviareAlServer;
        scanner.nextLine();
        System.out.println("Digita /0 per tornare indietro");
        System.out.println("Inizia a inviare messaggi a " + target);
        while (!messaggio.equals("/0")) {
            messaggio = scanner.nextLine();
            if (!messaggio.equals("/0")) {
                DaInviareAlServer = new Packet("MESSAGGIO", target, NomeClient, messaggio, false);
                String json = gson.toJson(DaInviareAlServer); // converto il pacchetto in json
                MandaAlServer.println(json);
            }
        }
    }
    public static void main(String[] args) {
        // Configura l'indirizzo IP e la porta del server
        String serverAddress = "127.0.0.1"; // IP del server (localhost)
        int port = 12345; // Porta del server
        Socket link = null;
        gson = new Gson(); // String json = gson.toJson(packet); Packet deserializedPacket = gson.fromJson(json, Packet.class)

        int i = 0;
        while( link == null && i < MAX_TRY ) {
            try {
                // Crea una connessione al server
                link = new Socket(serverAddress, port);
            }catch(IOException ex) {
                System.out.println("Connessione non ruscita, tentativo "+(i+1)+ "/3");

                attendi((long) (1000*Math.pow(2, i)));
            }
            i++;
        }

        if( link == null ) {
            System.out.println("Impossibile collegarsi al server");
            return;
        }

        try {
            MandaAlServer = new PrintWriter(link.getOutputStream(), true);
            RiceviDalServer = new BufferedReader(new InputStreamReader(link.getInputStream()));

            scanner = new Scanner(System.in);
            boolean avanti = false;
            int scelta;
            //menu inziale
            while (!avanti) {
                System.out.println("1 - Login");
                System.out.println("2 - Register");
                System.out.println("0 - Exit");
                Packet PacketRicevuto = null;
                scelta = scanner.nextInt();
                scanner.nextLine(); // pulisco buffer (non so perche ma sennò non funziona)
                int tentativi = 0;
                String reins = "";
                switch (scelta) {
                    case 1:
                        // TROVIAMO UN MODO DI FARE CASE 1 E 2 IN UNA FUNZIONE, DATO CHE CAMBIA POCHISSIME COSE
                        System.out.println("Hai scelto Login.");
                        while (PacketRicevuto == null || PacketRicevuto.getError()) {
                            if (tentativi > 0) {
                                reins = "di nuovo";
                            }
                            // DA AGGIUNGERE "PREMI 0 PER ANDARE INDIETRO" !!!
                            System.out.println("Inserisci " + reins + " il nome utente: ");
                            String nome = scanner.nextLine();
                            System.out.println("Inserisci " + reins + " la tua password: ");
                            String password = scanner.nextLine();
                            Packet DaInviareAlServer = new Packet("LOGIN", "", nome, password, false);
                            String json = gson.toJson(DaInviareAlServer); // converto il pacchetto in json
                            MandaAlServer.println(json);  // mando al server
                            json = RiceviDalServer.readLine(); // ASPETTO CHE MI RISPONDA COME è ANDATA LA REGISTRAZIONE
                            PacketRicevuto = gson.fromJson(json, Packet.class);
                            System.out.println(PacketRicevuto.getContenuto());
                            NomeClient = nome;
                            //    attendi(2000);
                            tentativi++;
                        }
                        avanti = true;
                        break;
                    case 2:
                        while (PacketRicevuto == null || PacketRicevuto.getError()) {
                            if (tentativi > 0) {
                                reins = "di nuovo";
                            }
                            // DA AGGIUNGERE "PREMI 0 PER ANDARE INDIETRO" !!!
                            System.out.println("Inserisci " + reins + " il nome utente: ");
                            String nome = scanner.nextLine();
                            System.out.println("Inserisci " + reins + " la tua password: ");
                            String password = scanner.nextLine();
                            // DA AGGIUNGERE CONTROLLO SE NON INSERISCE VUOTO !!!
                            Packet DaInviareAlServer = new Packet("REGISTRA", "", nome, password, false);
                            String json = gson.toJson(DaInviareAlServer); // converto il pacchetto in json
                            MandaAlServer.println(json);  // mando al server
                            json = RiceviDalServer.readLine(); // ASPETTO CHE MI RISPONDA COME è ANDATA LA REGISTRAZIONE
                            PacketRicevuto = gson.fromJson(json, Packet.class);
                            System.out.println(PacketRicevuto.getContenuto());
                            NomeClient = nome;
                            //    attendi(2000);
                            tentativi++;
                        }
                        avanti = true;
                        break;

                    case 0:
                        System.out.println("Uscita dal programma.");
                        break;

                    default:
                        System.out.println("Scelta non valida. Inserisci 1, 2 o 0.");
                        avanti = false;
                        break;
                }
                boolean continua = true;
                AttendiRichieste gestoreMenu = new AttendiRichieste(RiceviDalServer, MandaAlServer);

                Thread RiceviMessaggi = new Thread(gestoreMenu);


                //menu dopo aver loggato
                while (continua) {
                    // da capire come metterlo in pausa
                    Packet daInviareAlServer = new Packet("CHAT", "", "", "", false);
                    String json = gson.toJson(daInviareAlServer); // converto il pacchetto in JSON
                    MandaAlServer.println(json);
                    json = RiceviDalServer.readLine();
                    PacketRicevuto = gson.fromJson(json, Packet.class);
                    String[] Chats = gestoreMenu.getMenu("", PacketRicevuto);
                    RiceviMessaggi.start();
                    scelta = scanner.nextInt();
                    if (scelta == 0) {
                        continua = false;
                    }else if (scelta == 1) {
                        String Target;
                        scanner.nextLine();
                        boolean CicloCercaUtente = true;
                        while (CicloCercaUtente) {
                            System.out.println("Cerca l'utente con cui vuoi chattare o premi");
                            System.out.println("/1 - Per creare un gruppo");
                            System.out.println("/0 - Per tornare indietro");
                            Target = scanner.nextLine();
                            if ("/0".equals(Target)) {
                                CicloCercaUtente = false;
                                System.out.println("check1");
                            } else if ("/1".equals(Target)) {
                                // STA FACENDO IL PALLA
                               while(Target != "/0"){
                                    System.out.println("1 - aggiungi partecipante");
                                    System.out.println("2 - crea gruppo");
                                    System.out.println("/0 - Per tornare indietro");
                                    Target = scanner.nextLine();
                                    switch (Target) {
                                        case "1":
                                            Packet DaInviareAlServer;
                                            DaInviareAlServer = new Packet("GETALLUSERS", "", NomeClient, "", false);
                                            json = gson.toJson(DaInviareAlServer); // converto il pacchetto in json
                                            MandaAlServer.println(json);
                                            json = RiceviDalServer.readLine(); // ASPETTO CHE MI RISPONDA COME è ANDATA LA REGISTRAZIONE
                                            PacketRicevuto = gson.fromJson(json, Packet.class);
                                            String[] users = PacketRicevuto.getContenuto().split(",\\s*");


                                            String sceltaPartecipante = scanner.nextLine();
                                            String [] partecipanti= new String[100];


                                            Packet DaInviare;
                                            DaInviare = new Packet("GETALLUSERS", "", NomeClient, "", false);
                                            json = gson.toJson(DaInviare); // converto il pacchetto in json
                                            MandaAlServer.println(json);
                                            json = RiceviDalServer.readLine(); // ASPETTO CHE MI RISPONDA COME è ANDATA LA REGISTRAZIONE
                                            PacketRicevuto = gson.fromJson(json, Packet.class);
                                            String[] printPartecipanti = PacketRicevuto.getContenuto().split(",\\s*");

                                            break;
                                        case "2":

                                            break;
                                    }
                                }
                            } else {
                                boolean esisteGia = false;
                                for (int j = 0; j < Chats.length; j++) {
                                    if (Chats[j].equals(Target)) {
                                        esisteGia = true;
                                    }
                                }
                                if (!esisteGia) {
                                    Packet DaInviareAlServer;
                                    DaInviareAlServer = new Packet("AVVIACHAT", Target, NomeClient, "", false);
                                    json = gson.toJson(DaInviareAlServer); // converto il pacchetto in json
                                    MandaAlServer.println(json);
                                    json = RiceviDalServer.readLine(); // ASPETTO CHE MI RISPONDA COME è ANDATA LA REGISTRAZIONE
                                    PacketRicevuto = gson.fromJson(json, Packet.class);
                                    if (PacketRicevuto.getError()) {
                                        System.out.println(PacketRicevuto.getContenuto());
                                    } else {  // inizia a chattare
                                        System.out.println(PacketRicevuto.getContenuto());
                                        gestoreMenu.setChat(Target);
                                        startSendMessage(Target);
                                        gestoreMenu.setChat("");
                                        CicloCercaUtente = false;
                                    }
                                } else {
                                    System.out.println("Hai già una chat avviata con questo utente !");
                                }
                            }
                        }
                    } else { // chatta con ...
                        gestoreMenu.setChat(Chats[scelta - 2]);
                        startSendMessage(Chats[scelta - 2]);
                        gestoreMenu.setChat("");
                    }
                }
            }
            link.close();
        } catch (Exception e) {
            // Gestisce eventuali errori
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }

}


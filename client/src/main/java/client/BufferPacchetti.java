package client;

import data.Packet;

import java.util.ArrayList;

public class BufferPacchetti {
    private final ArrayList<Packet> buffer = new ArrayList<>();

    public BufferPacchetti() {
        // Costruttore di default
    }

    public synchronized void add(Packet pacchetto) {
        buffer.add(pacchetto);
    }

    public synchronized Packet consuma() {
        if (buffer.isEmpty()) {
            return null;
        }
        Packet ultimo = buffer.getLast(); // Ottieni l'ultimo elemento
        buffer.removeLast();
        return ultimo;
    }

    public synchronized Packet getLast() {
        if (buffer.isEmpty()) {
            return null;
        }
        return buffer.getLast();
    }
}

package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Packet {
    public String header; // LOGIN, LOGOUT; REGISTRA; AVVIACHAT; MESSAGGIO; CREAGRUPPO; NOTIFICATION; CHAT; GETSTORICO
    public String destinatario;
    public String mittente;
    public String contenuto;
    public boolean errore;

    public Packet(String h, String d, String m, String c, boolean e) {
        this.header = h;
        this.destinatario = d;
        this.mittente = m;
        this.contenuto = c;
        this.errore = e;
    }

    public boolean getError() {
        return errore;
    }

    public void setErrore(boolean errore) {
        this.errore = errore;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getMittente() {
        return mittente;
    }

    public void setMittente(String mittente) {
        this.mittente = mittente;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getContenuto() {
        return contenuto;
    }

    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

}
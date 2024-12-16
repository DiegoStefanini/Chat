README – Sistema di Chat Client-Server
Descrizione:
Questo progetto implementa un sistema di chat client-server con funzionalità di:

  - Login/Registrazione per autenticare gli utenti.
  - Chat Singole e Gruppi per la comunicazione in tempo reale.
  - Persistenza dei Dati tramite un database relazionale.
  - L’applicazione è stata sviluppata con JavaFX per l’interfaccia grafica, Socket TCP per la comunicazione e JSON per la formattazione dei messaggi scambiati.

Requisiti:

  - Java 11 o superiore.
  - Database MySQL (o equivalente).

Librerie Necessarie:
  - Gson per la manipolazione di JSON.
  - Dipendenze JavaFX (se non integrate nel JDK).
    
Server:
  - IP e Porta configurabili (default: 79.35.203.96, porta 12345).
  - Accesso al database per memorizzare utenti, messaggi e gruppi.

Istruzioni per l'Uso: Configurazione del Server
  Configura il file del database con:
    - Tabella utenti (username, password).
    - Tabella chat (chat_id, chat_name, partecipanti).
    - Tabella messaggi (id, chat_id, mittente, contenuto, timestamp).
    - Avvia il server e specifica l’IP e la porta.

Configurazione del Client: 
Modifica l’indirizzo IP e la porta del server nel file ClientMain

Compila ed esegui il progetto:
  - Login: Inserisci le credenziali di un account esistente.
  - Registrazione: Crea un nuovo account.

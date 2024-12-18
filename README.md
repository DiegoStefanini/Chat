README – Sistema di Chat Client-Server
Descrizione:
Questo progetto implementa un sistema di chat client-server con funzionalità di:

  - Login/Registrazione per autenticare gli utenti.
  - Chat Singole e Gruppi per la comunicazione in tempo reale.
  - Persistenza dei Dati tramite un database relazionale.
  - L’applicazione è stata sviluppata con JavaFX per l’interfaccia grafica, Socket TCP per la comunicazione e JSON per la formattazione dei messaggi scambiati.
Se si usa Intellij: 
  - Andare su clone repository --> Repository url 
  - Incollare l'url di questa repository
    
Requisiti:
  - Java 11 o superiore.
  - Maven
  - Java FX
  - Database MySQL (o equivalente).

Librerie Necessarie:
  - Gson per la manipolazione di JSON (com.google.code.gson).
  - Dipendenze JavaFX (se non integrate nel JDK).
  - mysql
    
Configurazione database:
  - Avvia xampp (o qualsiasi programma simile) e i servizi Apache o Mysql.
  - Importa il database "chat.sql" (se si usa xampp vai su http://localhost/phpmyadmin/  --> Importa).
    
Configurazione Server:
  - Configura la porta sulla quale vuoi far collegare il client (default: 12345) nel ServerMain.java.
  - Configura URL, USER e PASSWORD nel ServerMain.java per la connessione al database.

Configurazione Client: 
  - Nel ClientMain.java configura serverAddres e port (se si lascia default la porta nel server e si avvia il ServerMain sulla stessa macchina del ClientMain non c'è bisongno di cambiare)
    
Compila ed esegui prima il ServerMain, e poi il ClientMain

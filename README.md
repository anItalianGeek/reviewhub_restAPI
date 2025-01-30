# Server - Backend Spring Boot

## Prerequisiti
- Java 17 o superiore
- Gradle (`brew install gradle` su macOS, `apt install gradle` su Ubuntu)

---

## Installazione
1. Clona il repository:
   ```bash
   git clone [URL_DEL_REPO]
   cd [NOME_CARTELLA_SERVER]
   ```

2. Compila il progetto:
   ```bash
   ./gradlew build
   ```

---

## Avvio Locale
1. Avvia l'app:
   ```bash
   ./gradlew bootRun

2. L'app sarà disponibile su [http://localhost:8080](http://localhost:8080).

---

## Build per Produzione
1. Genera il file `.jar`:
   ```bash
   ./gradlew bootJar
   ```

2. Troverai il file nella directory `build/libs`.

---

## Deploy su Server
1. Carica il file `.jar` sul server.  
2. Avvia l'app manualmente:  
   ```bash
   java -jar nome-progetto-0.0.1-SNAPSHOT.jar
   ```
3. (Opzionale) Configura come servizio systemd:  
   ```bash
   sudo nano /etc/systemd/system/nome-progetto.service
   ```
   Contenuto del file:
   ```ini
   [Unit]
   Description=Spring Boot Application
   After=network.target

   [Service]
   User=tuo-utente
   ExecStart=/usr/bin/java -jar /path/al/file/nome-progetto-0.0.1-SNAPSHOT.jar
   SuccessExitStatus=143
   Restart=always
   StandardOutput=journal
   StandardError=journal

   [Install]
   WantedBy=multi-user.target
   ```

4. Abilita e avvia il servizio:
   ```bash
   sudo systemctl enable nome-progetto
   sudo systemctl start nome-progetto
   ```

---

## Note
- Configura CORS per il frontend se necessario.
- Assicurati che il server risponda alle richieste del frontend in produzione.
```
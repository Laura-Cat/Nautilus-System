-- ==============================================================================
-- 1. CREAZIONE DATABASE E SETUP INIZIALE
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS nautilus_db;
USE nautilus_db;

-- ==============================================================================
-- 2. CREAZIONE DELLE TABELLE (DOMAIN MODEL MAPPING)
-- ==============================================================================


-- SET FOREIGN_KEY_CHECKS = 0; -- Disabilita il controllo delle chiavi esterne

DROP TABLE IF EXISTS utenti; -- Ora puoi cancellarla senza problemi!
DROP TABLE IF EXISTS titoli_accesso;
DROP TABLE IF EXISTS lezioni;
DROP TABLE IF EXISTS ruoli;
DROP TABLE IF EXISTS corsie;
DROP TABLE IF EXISTS corsi;
DROP TABLE IF EXISTS prenotazioni;
DROP TABLE IF EXISTS notifiche;
DROP TABLE IF EXISTS pagamenti;

-- SET FOREIGN_KEY_CHECKS = 1; -- Riabilita il controllo (MOLTO IMPORTANTE)

CREATE TABLE ruoli (
    id INT PRIMARY KEY,
    nome_ruolo VARCHAR(50) NOT NULL
);
-- Tabella Utenti (Pattern Single Table Inheritance per User, Cliente e Istruttore)
CREATE TABLE utenti (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cf VARCHAR(16) UNIQUE NOT NULL,
    nome VARCHAR(50) NOT NULL,
    cognome VARCHAR(50) NOT NULL,
    data_nascita DATE,
    luogo_nascita VARCHAR(100),
    indirizzo VARCHAR(150),
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    id_ruolo INT NOT NULL,
    -- Attributi specifici del Cliente
    certificato_valido BOOLEAN DEFAULT FALSE,
    -- Attributi specifici dell'Istruttore
    specializzazione VARCHAR(100),
    FOREIGN KEY (id_ruolo) REFERENCES ruoli(id)
);


CREATE TABLE titoli_accesso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    tipo_titolo ENUM('PACCHETTO_CREDITI', 'ABBONAMENTO_PERIODICO') NOT NULL,
    crediti_rimanenti INT DEFAULT 0,
    data_inizio DATE,
    data_fine DATE,
    FOREIGN KEY (id_cliente) REFERENCES utenti(id) ON DELETE CASCADE
);

-- Tabella Corsie

CREATE TABLE corsie (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_corsia INT NOT NULL UNIQUE,
    capienza_massima INT NOT NULL
);

-- Tabella Corsi

CREATE TABLE corsi (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    stato_attivita VARCHAR(50) DEFAULT 'Attivo',
    data_inizio DATE NOT NULL,
    num_posti INT NOT NULL,
    descrizione TEXT
);


-- Tabella Lezioni
CREATE TABLE lezioni (
    id INT AUTO_INCREMENT PRIMARY KEY,
    data DATE NOT NULL,
    ora_inizio TIME NOT NULL,
    ora_fine TIME NOT NULL,
    num_posti_prenotati INT DEFAULT 0,
    tipo_attivita ENUM('CORSO', 'NUOTO_LIBERO', 'PRIVATA') NOT NULL,
    id_istruttore INT,
    id_corso INT,
    id_corsia INT,
    FOREIGN KEY (id_istruttore) REFERENCES utenti(id),
    FOREIGN KEY (id_corso) REFERENCES corsi(id),
    FOREIGN KEY (id_corsia) REFERENCES corsie(id)
);




INSERT INTO ruoli (id, nome_ruolo) VALUES 
(1, 'AMMINISTRAZIONE'), (2, 'ISTRUTTORE'), (3, 'CLIENTE'), (4, 'LOGIN');






-- Tabella Prenotazioni

CREATE TABLE prenotazioni (
    id INT AUTO_INCREMENT PRIMARY KEY,
    data_richiesta DATE NOT NULL,
    stato VARCHAR(50) DEFAULT 'In Attesa',
    tipologia VARCHAR(50) NOT NULL,
    id_cliente INT NOT NULL,
    id_lezione INT NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES utenti(id),
    FOREIGN KEY (id_lezione) REFERENCES lezioni(id)
);

-- Tabella Notifiche

CREATE TABLE notifiche (
    id INT AUTO_INCREMENT PRIMARY KEY,
    messaggio TEXT NOT NULL,
    letta BOOLEAN DEFAULT FALSE,
    data_invio DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_destinatario INT NOT NULL,
    FOREIGN KEY (id_destinatario) REFERENCES utenti(id) ON DELETE CASCADE
);

-- Tabella Pagamenti

CREATE TABLE pagamenti (
    id_transazione VARCHAR(50) PRIMARY KEY,
    importo DECIMAL(10,2) NOT NULL,
    data_acquisto DATE NOT NULL,
    stato VARCHAR(50) DEFAULT 'Completato',
    id_cliente INT NOT NULL,
    id_titolo_acquistato INT,
    FOREIGN KEY (id_cliente) REFERENCES utenti(id),
    FOREIGN KEY (id_titolo_acquistato) REFERENCES titoli_accesso(id)
);

-- ==============================================================================
-- 3. CREAZIONE DEGLI UTENTI DEL DATABASE E RELATIVI GRANT (SISTEMATI)
-- ==============================================================================

-- A. UTENTE LOGIN
DROP USER IF EXISTS 'login_user'@'localhost';
CREATE USER IF NOT EXISTS 'login_user'@'localhost'  IDENTIFIED WITH mysql_native_password BY  'login_pass';
GRANT SELECT ON nautilus_db.utenti TO 'login_user'@'localhost';
GRANT SELECT ON nautilus_db.ruoli TO 'login_user'@'localhost';

-- B. UTENTE CLIENTE
DROP USER IF EXISTS 'cliente_user'@'localhost';
CREATE USER IF NOT EXISTS 'cliente_user'@'localhost'  IDENTIFIED WITH mysql_native_password BY  'cliente_pass';
GRANT SELECT ON nautilus_db.corsi TO 'cliente_user'@'localhost';
GRANT SELECT ON nautilus_db.lezioni TO 'cliente_user'@'localhost';
GRANT SELECT ON nautilus_db.corsie TO 'cliente_user'@'localhost';
GRANT SELECT ON nautilus_db.utenti TO 'cliente_user'@'localhost';
GRANT SELECT, INSERT, UPDATE ON nautilus_db.prenotazioni TO 'cliente_user'@'localhost';
GRANT SELECT, INSERT, UPDATE ON nautilus_db.titoli_accesso TO 'cliente_user'@'localhost';
GRANT SELECT, INSERT ON nautilus_db.pagamenti TO 'cliente_user'@'localhost';
GRANT SELECT, UPDATE ON nautilus_db.notifiche TO 'cliente_user'@'localhost';

-- C. UTENTE ISTRUTTORE
DROP USER IF EXISTS 'istruttore_user'@'localhost';
CREATE USER IF NOT EXISTS 'istruttore_user'@'localhost'  IDENTIFIED WITH mysql_native_password BY  'istruttore_pass';
GRANT SELECT ON nautilus_db.corsi TO 'istruttore_user'@'localhost';
GRANT SELECT, UPDATE ON nautilus_db.lezioni TO 'istruttore_user'@'localhost';
GRANT SELECT, UPDATE ON nautilus_db.prenotazioni TO 'istruttore_user'@'localhost';
GRANT SELECT ON nautilus_db.utenti TO 'istruttore_user'@'localhost';
GRANT SELECT, INSERT, UPDATE ON nautilus_db.notifiche TO 'istruttore_user'@'localhost';

-- D. UTENTE AMMINISTRATORE
DROP USER IF EXISTS 'admin_user'@'localhost';
CREATE USER IF NOT EXISTS 'admin_user'@'localhost' IDENTIFIED WITH mysql_native_password BY  'admin_pass';
GRANT ALL PRIVILEGES ON nautilus_db.* TO 'admin_user'@'localhost';

GRANT SELECT ON nautilus_db.* TO 'login_user'@'localhost';

-- Pulizia utenti precedenti (per sicurezza)
DROP USER IF EXISTS 'login_user'@'localhost';
DROP USER IF EXISTS 'cliente_user'@'localhost';

-- Creazione con le password esatte del tuo file properties/codice
CREATE USER 'login_user'@'localhost' IDENTIFIED BY 'login_pass';
CREATE USER 'cliente_user'@'localhost' IDENTIFIED BY 'cliente_pass';

-- Permessi per il LOGIN (solo lettura su utenti e ruoli)
GRANT SELECT ON nautilus_db.utenti TO 'login_user'@'localhost';
GRANT SELECT ON nautilus_db.ruoli TO 'login_user'@'localhost';

-- Permessi per il CLIENTE (lettura tutto, scrittura solo prenotazioni/pagamenti)
GRANT SELECT ON nautilus_db.* TO 'cliente_user'@'localhost';
GRANT INSERT, UPDATE ON nautilus_db.prenotazioni TO 'cliente_user'@'localhost';
GRANT INSERT ON nautilus_db.pagamenti TO 'cliente_user'@'localhost';

FLUSH PRIVILEGES;

-- ==============================================================================
-- 4. INSERIMENTO UTENTI DI PROVA (Corretto con l'ID Ruolo)
-- ==============================================================================

-- Inseriamo un Cliente (id_ruolo = 3)
INSERT INTO utenti (cf, nome, cognome, email, password, id_ruolo)
VALUES ('CF123', 'Cate', 'User', 'cliente@test.it', '1234', 3);

-- Inseriamo un Trainer (id_ruolo = 2)
INSERT INTO utenti (cf, nome, cognome, email, password, id_ruolo)
VALUES ('CF456', 'Marco', 'Trainer', 'trainer@test.it', '1234', 2);

-- Inseriamo un Admin (id_ruolo = 1)
INSERT INTO utenti (cf, nome, cognome, email, password, id_ruolo)
VALUES ('CF789', 'Admin', 'Boss', 'admin@test.it', '1234', 1);
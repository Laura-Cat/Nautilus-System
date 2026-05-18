-- ==============================================================================
-- 1. CREAZIONE DATABASE E SETUP INIZIALE
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS nautilus_db;
USE nautilus_db;

-- ==============================================================================
-- 2. CREAZIONE DELLE TABELLE (DOMAIN MODEL MAPPING)
-- ==============================================================================


SET FOREIGN_KEY_CHECKS = 0; -- Disabilita il controllo delle chiavi esterne

DROP TABLE IF EXISTS utenti; -- Ora puoi cancellarla senza problemi!
DROP TABLE IF EXISTS titoli_accesso;
DROP TABLE IF EXISTS lezioni;
DROP TABLE IF EXISTS ruoli;
DROP TABLE IF EXISTS corsie;
DROP TABLE IF EXISTS corsi;
DROP TABLE IF EXISTS prenotazioni;
DROP TABLE IF EXISTS notifiche;
DROP TABLE IF EXISTS pagamenti;

SET FOREIGN_KEY_CHECKS = 1; -- Riabilita il controllo (MOLTO IMPORTANTE)

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

-- ==========================================================
-- 1. POPOLAMENTO TABELLA UTENTI (Istruttori id_ruolo=2, Clienti id_ruolo=3)
-- ==========================================================
INSERT INTO utenti ( cf, nome, cognome, data_nascita, luogo_nascita, indirizzo, email, password, id_ruolo, certificato_valido, specializzazione)
VALUES 
-- ISTRUTTORI (id_ruolo = 2, compilano la specializzazione)
('RSSMRA75A01H501Z', 'Mario', 'Rossi', '1975-01-01', 'Roma', 'Via delle Rose 12', 'mario.rossi@piscina.it', 'password123', 2, FALSE, 'Perfezionamento Stile Libero e Agonismo'),
('BNCELN88M41H501Y', 'Elena', 'Bianchi', '1988-08-20', 'Milano', 'Corso Italia 45', 'elena.bianchi@piscina.it', 'password123', 2, FALSE, 'AcquaGym e Hydrobike'),

-- CLIENTI (id_ruolo = 3, compilano il certificato_valido)
('VRDGLC95A15H501X', 'Gianluca', 'Verdi', '1995-03-15', 'Napoli', 'Via Napoli 8', 'verdi@email.com', '1234', 3, TRUE, NULL),
('GLLNNA00A41H501W', 'Anna', 'Gialli', '2000-01-01', 'Firenze', 'Via Firenze 99', 'gialli@email.com', 'password123', 3, TRUE, NULL);


-- ==========================================================
-- 2. POPOLAMENTO TABELLA TITOLI_ACCESSO (Legati ai Clienti)
-- ==========================================================
INSERT INTO titoli_accesso (id, id_cliente, tipo_titolo, crediti_rimanenti, data_inizio, data_fine)
VALUES 
(1, 3, 'PACCHETTO_CREDITI', 10, '2026-05-01', '2026-08-01'), -- Gianluca (id=3) ha 10 crediti: prenotazione OK!
(2, 4, 'PACCHETTO_CREDITI', 0, '2026-05-01', '2026-08-01');  -- Anna (id=4) ha 0 crediti: farà scattare la tua CreditiInsufficientiException!


-- ==========================================================
-- 3. POPOLAMENTO TABELLA CORSIE (Usa numero_corsia e capienza_massima)
-- ==========================================================
INSERT INTO corsie (id, numero_corsia, capienza_massima)
VALUES 
(1, 1, 5),
(2, 2, 5),
(3, 3, 5);


-- ==========================================================
-- 4. POPOLAMENTO TABELLA CORSI
-- ==========================================================
INSERT INTO corsi (id, nome, stato_attivita, data_inizio, num_posti, descrizione)
VALUES 
(1, 'AcquaGym', 'Attività', '2026-01-10', 15, 'Attività aerobica in acqua media a ritmo di musica'),
(2, 'Scuola Nuoto Adulti', 'Attività', '2026-01-15', 12, 'Corso di nuoto per livelli principiante e intermedio');


-- ==========================================================
-- 5. POPOLAMENTO TABELLA LEZIONI (Fasce orarie coordinate per tipo_attivita)
-- ==========================================================
INSERT INTO lezioni (id, data, ora_inizio, ora_fine, num_posti_prenotati, tipo_attivita, id_istruttore, id_corso, id_corsia)
VALUES 
-- --- NUOTO LIBERO (id_istruttore e id_corso sono NULL, legati alla corsia) ---
-- Turni per Oggi (18 Maggio 2026)
(1, '2026-05-18', '09:00:00', '10:00:00', 2, 'NUOTO_LIBERO', NULL, NULL, 1), -- 3 posti liberi in Corsia 1
(2, '2026-05-18', '10:00:00', '11:00:00', 5, 'NUOTO_LIBERO', NULL, NULL, 2), -- ESAURITO (5 su 5) in Corsia 2
(3, '2026-05-18', '11:00:00', '12:00:00', 0, 'NUOTO_LIBERO', NULL, NULL, 1), -- Completamente vuoto

-- Turni per Domani (19 Maggio 2026)
(4, '2026-05-19', '09:00:00', '10:00:00', 1, 'NUOTO_LIBERO', NULL, NULL, 2),
(5, '2026-05-19', '15:00:00', '16:00:00', 0, 'NUOTO_LIBERO', NULL, NULL, 1),

-- --- CORSI DI GRUPPO (id_istruttore, id_corso e id_corsia tutti compilati) ---
(6, '2026-05-18', '18:00:00', '19:00:00', 10, 'CORSO', 2, 1, 3), -- AcquaGym con Elena (id_istruttore=2) in Corsia 3
(7, '2026-05-19', '13:00:00', '14:00:00', 4, 'CORSO', 1, 2, 1),  -- Scuola Nuoto con Mario (id_istruttore=1) in Corsia 1

-- --- LEZIONI PRIVATE (id_corso è NULL, id_istruttore e id_corsia compilati) ---
(8, '2026-05-18', '14:00:00', '15:00:00', 0, 'PRIVATA', 1, NULL, 1), -- Slot privato Libero con Mario
(9, '2026-05-19', '10:00:00', '11:00:00', 1, 'PRIVATA', 2, NULL, 2); -- Slot privato Occupato con Elena
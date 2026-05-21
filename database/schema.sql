-- ==============================================================================
-- 1. CREAZIONE DATABASE E SETUP INIZIALE
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS nautilus_db;
USE nautilus_db;

-- ==============================================================================
-- 2. CREAZIONE DELLE TABELLE (DOMAIN MODEL MAPPING)
-- ==============================================================================
/*
SET FOREIGN_KEY_CHECKS = 0; -- Disabilita il controllo delle chiavi esterne per fare i DROP sicuri

DROP TABLE IF EXISTS pagamenti;
DROP TABLE IF EXISTS notifiche;
DROP TABLE IF EXISTS prenotazioni;
DROP TABLE IF EXISTS lezioni;
DROP TABLE IF EXISTS corsi;
DROP TABLE IF EXISTS corsie;
DROP TABLE IF EXISTS abbonamenti_corsi_inclusi;
DROP TABLE IF EXISTS titoli_accesso;
DROP TABLE IF EXISTS utenti;
DROP TABLE IF EXISTS ruoli;

SET FOREIGN_KEY_CHECKS = 1; -- Riabilita il controllo (MOLTO IMPORTANTE)
*/

-- Tabella Ruoli
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
    descrizione TEXT,
    foto_path VARCHAR(255) DEFAULT '/images/default_istruttore.png',
    FOREIGN KEY (id_ruolo) REFERENCES ruoli(id)
);

-- Tabella Titoli Accesso
CREATE TABLE titoli_accesso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    tipo_titolo ENUM('PACCHETTO_CREDITI', 'ABBONAMENTO_PERIODICO') NOT NULL,
    crediti_rimanenti INT DEFAULT 0,
    data_inizio DATE,
    data_fine DATE,
    FOREIGN KEY (id_cliente) REFERENCES utenti(id) ON DELETE CASCADE
);

-- Tabella di Mapping per la List<TipoCorso> degli Abbonamenti Periodici
CREATE TABLE abbonamenti_corsi_inclusi (
    id_titolo INT NOT NULL,
    tipo_corso ENUM('ACQUAGYM', 'HYDROBIKE', 'NUOTO_MASTER', 'SCUOLA_NUOTO_ADULTI', 'ACQUACROSSFIT', 'NEONATALE') NOT NULL,
    PRIMARY KEY (id_titolo, tipo_corso),
    FOREIGN KEY (id_titolo) REFERENCES titoli_accesso(id) ON DELETE CASCADE
);

-- Tabella Corsie
CREATE TABLE corsie (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_corsia INT NOT NULL UNIQUE,
    capienza_massima INT NOT NULL
);

-- Tabella Corsi (Modificata con il tuo ENUM TipoCorso!)
CREATE TABLE corsi (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_corso ENUM('ACQUAGYM', 'HYDROBIKE', 'NUOTO_MASTER', 'SCUOLA_NUOTO_ADULTI', 'ACQUACROSSFIT', 'NEONATALE') NOT NULL,
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
    tipo_attivita ENUM('CORSO_GRUPPO', 'NUOTO_LIBERO', 'PRIVATA') NOT NULL,
    id_istruttore INT,
    id_corso INT,
    id_corsia INT,
    FOREIGN KEY (id_istruttore) REFERENCES utenti(id),
    FOREIGN KEY (id_corso) REFERENCES corsi(id),
    FOREIGN KEY (id_corsia) REFERENCES corsie(id)
);

-- Tabella Prenotazioni
CREATE TABLE prenotazioni (
    id INT AUTO_INCREMENT PRIMARY KEY,
    data_richiesta DATE NOT NULL,
    stato VARCHAR(50) DEFAULT 'In Attesa',
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
-- 3. PERMESSI (GRANTS) E CREAZIONE UTENTI MYSQL
-- ==============================================================================
DROP USER IF EXISTS 'login_user'@'localhost';
DROP USER IF EXISTS 'cliente_user'@'localhost';

CREATE USER 'login_user'@'localhost' IDENTIFIED BY 'login_pass';
CREATE USER 'cliente_user'@'localhost' IDENTIFIED BY 'cliente_pass';

GRANT SELECT ON nautilus_db.utenti TO 'login_user'@'localhost';
GRANT SELECT ON nautilus_db.ruoli TO 'login_user'@'localhost';

GRANT SELECT ON nautilus_db.* TO 'cliente_user'@'localhost';
GRANT INSERT, UPDATE ON nautilus_db.prenotazioni TO 'cliente_user'@'localhost';
GRANT INSERT, UPDATE ON nautilus_db.pagamenti TO 'cliente_user'@'localhost';
GRANT INSERT, UPDATE ON nautilus_db.lezioni TO 'cliente_user'@'localhost';
GRANT INSERT, UPDATE ON nautilus_db.titoli_accesso TO 'cliente_user'@'localhost';

FLUSH PRIVILEGES;

-- ==========================================================
-- 4. POPOLAMENTO DATI TEST
-- ==========================================================

INSERT INTO ruoli (id, nome_ruolo) VALUES 
(1, 'AMMINISTRAZIONE'), (2, 'ISTRUTTORE'), (3, 'CLIENTE'), (4, 'LOGIN');

-- UTENTI
INSERT INTO utenti ( cf, nome, cognome, data_nascita, luogo_nascita, indirizzo, email, password, id_ruolo, certificato_valido, specializzazione)
VALUES 

-- Clienti
('VRDGLC95A15H501X', 'Gianluca', 'Verdi', '1995-03-15', 'Napoli', 'Via Napoli 8', 'verdi@email.com', '1234', 3, TRUE, NULL),
('GLLNNA00A41H501W', 'Anna', 'Gialli', '2000-01-01', 'Firenze', 'Via Firenze 99', 'gialli@email.com', '1234', 3, TRUE, NULL),
-- Admin
('CF789', 'Admin', 'Boss', '1980-01-01', 'Roma', 'Centro', 'admin@test.it', '1234', 1, FALSE, NULL);

-- TITOLI DI ACCESSO
INSERT INTO titoli_accesso (id, id_cliente, tipo_titolo, crediti_rimanenti, data_inizio, data_fine)
VALUES 
(1, 1, 'PACCHETTO_CREDITI', 10, '2026-05-01', '2026-08-01'),     -- Gianluca: Pacchetto 10 crediti (OK Nuoto Libero)
(2, 2, 'ABBONAMENTO_PERIODICO', 0, '2026-01-01', '2026-12-31'); -- Anna: Abbonamento annuale (OK Corsi)

-- CORSI INCLUSI NELL'ABBONAMENTO (Anna può fare Acquagym e Hydrobike!)
INSERT INTO abbonamenti_corsi_inclusi (id_titolo, tipo_corso)
VALUES 
(2, 'ACQUAGYM'),
(2, 'HYDROBIKE');

-- CORSIE
INSERT INTO corsie (id, numero_corsia, capienza_massima)
VALUES (1, 1, 5), (2, 2, 5), (3, 3, 5);

-- CORSI (Usano il nuovo Enum)
INSERT INTO corsi (id, tipo_corso, stato_attivita, data_inizio, num_posti, descrizione)
VALUES 
(1, 'ACQUAGYM', 'Attività', '2026-01-10', 15, 'Attività aerobica in acqua media a ritmo di musica'),
(2, 'SCUOLA_NUOTO_ADULTI', 'Attività', '2026-01-15', 12, 'Corso di nuoto per livelli principiante e intermedio');

-- LEZIONI (Aggiornate al 20 e 21 Maggio 2026)
INSERT INTO lezioni (id, data, ora_inizio, ora_fine, num_posti_prenotati, tipo_attivita, id_istruttore, id_corso, id_corsia)
VALUES 
-- Nuoto Libero (20 Maggio)
(1, '2026-05-20', '09:00:00', '10:00:00', 2, 'NUOTO_LIBERO', NULL, NULL, 1), 
(2, '2026-05-20', '10:00:00', '11:00:00', 5, 'NUOTO_LIBERO', NULL, NULL, 2), -- ESAURITO
(3, '2026-05-20', '11:00:00', '12:00:00', 0, 'NUOTO_LIBERO', NULL, NULL, 1), 

-- Nuoto Libero (21 Maggio)
(4, '2026-05-21', '09:00:00', '10:00:00', 1, 'NUOTO_LIBERO', NULL, NULL, 2),

-- Corsi di Gruppo (Associazione con id_corso=1(Acquagym) e id_corso=2(Scuola Nuoto))
(5, '2026-05-20', '18:00:00', '19:00:00', 10, 'CORSO_GRUPPO', 2, 1, 3), -- Acquagym (Oggi)
(6, '2026-05-21', '13:00:00', '14:00:00', 4, 'CORSO_GRUPPO', 1, 2, 1);  -- Scuola Nuoto (Domani)

INSERT INTO utenti (cf, nome, cognome, data_nascita, luogo_nascita, indirizzo, email, password, id_ruolo, certificato_valido, specializzazione, descrizione, foto_path) 
VALUES 
-- 1. MARIO ROSSI (Sistemata la virgola mancante e il percorso della foto)
('RSSMRA75A01H501Z', 'Mario', 'Rossi', '1975-01-01', 'Roma', 'Via delle Rose 12', 'mario.rossi@piscina.it', 'password123', 2, FALSE, 
 'Perfezionamento Stile Libero e Agonismo', 
 'Specializzato nell''affinamento della tecnica e nello sviluppo della velocità. Ideale per chi sa già nuotare e vuole prepararsi per le gare o migliorare drasticamente i propri tempi.', 
 '/images/istruttori/mario.jpg'),

-- 2. ELENA BIANCHI (Perfetta)
('BNCELN88M41H501Y', 'Elena', 'Bianchi', '1988-08-20', 'Milano', 'Corso Italia 45', 'elena.bianchi@piscina.it', 'password123', 2, FALSE, 
 'Acquaticità & Nuoto per Principianti', 
 'La mia priorità è farti sentire a tuo agio in acqua. Con calma e pazienza, ti aiuterò a superare la paura e a padroneggiare le basi del galleggiamento e della respirazione.', 
 '/images/istruttori/elena.jpg'),

-- 3. ALESSANDRO VERDI (Ora ha TUTTI gli attributi richiesti dalla tabella!)
('VRDLSN82A05F205H', 'Alessandro', 'Verdi', '1982-05-12', 'Firenze', 'Via Garibaldi 89', 'alessandro.verdi@piscina.it', 'password123', 2, FALSE, 
 'Recupero Funzionale in Acqua', 
 'Le mie lezioni private si concentrano sulla riabilitazione e sul rinforzo muscolare a basso impatto. Sfruttiamo la resistenza dell''acqua per un allenamento sicuro ed efficace, perfetto nel post-infortunio.', 
 '/images/istruttori/alessandro.jpg');
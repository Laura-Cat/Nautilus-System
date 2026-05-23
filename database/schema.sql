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
    tipologia VARCHAR(45),
    note TEXT,
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
    tipo VARCHAR(50) DEFAULT 'INFO',
    id_riferimento INT,
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
GRANT INSERT, UPDATE ON nautilus_db.notifiche TO 'cliente_user'@'localhost';


GRANT SELECT ON nautilus_db.* TO 'istruttore_user'@'localhost';
GRANT INSERT, UPDATE ON nautilus_db.prenotazioni TO 'istruttore_user'@'localhost';
GRANT INSERT, UPDATE ON nautilus_db.lezioni TO 'istruttore_user'@'localhost';

FLUSH PRIVILEGES;

-- ==========================================================
-- 4. POPOLAMENTO DATI TEST (Aggiornato a Maggio 2026)
-- ==========================================================

INSERT INTO ruoli (id, nome_ruolo) VALUES 
(1, 'AMMINISTRAZIONE'), (2, 'ISTRUTTORE'), (3, 'CLIENTE'), (4, 'LOGIN');

-- ----------------------------------------------------------
-- A. UTENTI
-- ----------------------------------------------------------
INSERT INTO utenti (id, cf, nome, cognome, data_nascita, luogo_nascita, indirizzo, email, password, id_ruolo, certificato_valido, specializzazione, descrizione, foto_path)
VALUES 
-- Clienti (Ruolo 3)
(1, 'VRDGLC95A15H501X', 'Gianluca', 'Verdi', '1995-03-15', 'Napoli', 'Via Napoli 8', 'verdi@email.com', '1234', 3, TRUE, NULL, NULL, NULL),
(2, 'GLLNNA00A41H501W', 'Anna', 'Gialli', '2000-01-01', 'Firenze', 'Via Firenze 99', 'gialli@email.com', '1234', 3, TRUE, NULL, NULL, NULL),
(7, 'MRCNCL85M20H501K', 'Marco', 'Neri', '1985-06-20', 'Roma', 'Via Roma 1', 'marco.neri@email.com', '1234', 3, FALSE, NULL, NULL, NULL), -- Certificato scaduto!
(8, 'SFAFRR92M10H501J', 'Sofia', 'Ferrari', '1992-10-10', 'Milano', 'Corso Como 5', 'sofia.ferrari@email.com', '1234', 3, TRUE, NULL, NULL, NULL),

-- Admin (Ruolo 1)
(3, 'CF789', 'Admin', 'Boss', '1980-01-01', 'Roma', 'Centro', 'admin@test.it', '1234', 1, FALSE, NULL, NULL, NULL),

-- Istruttori (Ruolo 2)
(4, 'RSSMRA75A01H501Z', 'Mario', 'Rossi', '1975-01-01', 'Roma', 'Via delle Rose 12', 'mario.rossi@piscina.it', '1234', 2, FALSE, 'Perfezionamento Stile Libero e Agonismo', 'Specializzato nell''affinamento della tecnica e nello sviluppo della velocità.', '/images/istruttori/mario.jpg'),
(5, 'BNCELN88M41H501Y', 'Elena', 'Bianchi', '1988-08-20', 'Milano', 'Corso Italia 45', 'elena.bianchi@piscina.it', '1234', 2, FALSE, 'Acquaticità & Nuoto per Principianti', 'La mia priorità è farti sentire a tuo agio in acqua. Ti aiuterò a superare la paura.', '/images/istruttori/elena.jpg'),
(6, 'VRDLSN82A05F205H', 'Alessandro', 'Verdi', '1982-05-12', 'Firenze', 'Via Garibaldi 89', 'alessandro.verdi@piscina.it', '1234', 2, FALSE, 'Recupero Funzionale in Acqua', 'Le mie lezioni private si concentrano sulla riabilitazione e sul rinforzo muscolare a basso impatto.', '/images/istruttori/alessandro.jpg');


-- ----------------------------------------------------------
-- B. TITOLI DI ACCESSO & CORSI INCLUSI
-- ----------------------------------------------------------
INSERT INTO titoli_accesso (id, id_cliente, tipo_titolo, crediti_rimanenti, data_inizio, data_fine)
VALUES 
(1, 1, 'PACCHETTO_CREDITI', 8, '2026-05-01', '2026-08-01'),       -- Gianluca: Pacchetto a ingressi (valido)
(2, 2, 'ABBONAMENTO_PERIODICO', 0, '2026-01-01', '2026-12-31'),  -- Anna: Annuale (valido)
(3, 7, 'PACCHETTO_CREDITI', 10, '2025-10-01', '2026-04-01'),     -- Marco: Pacchetto scaduto
(4, 8, 'ABBONAMENTO_PERIODICO', 0, '2026-05-01', '2026-05-31');  -- Sofia: Mensile in scadenza (tra 9 giorni)

-- Corsi associati agli abbonamenti periodici
INSERT INTO abbonamenti_corsi_inclusi (id_titolo, tipo_corso) VALUES 
(2, 'ACQUAGYM'), (2, 'HYDROBIKE'), (2, 'ACQUACROSSFIT'), -- Anna
(4, 'NUOTO_MASTER'), (4, 'SCUOLA_NUOTO_ADULTI');         -- Sofia


-- ----------------------------------------------------------
-- C. CORSIE E CORSI 
-- ----------------------------------------------------------
INSERT INTO corsie (id, numero_corsia, capienza_massima) VALUES 
(1, 1, 6), (2, 2, 6), (3, 3, 6), (4, 4, 8), (5, 5, 8); -- Ampliate a 5 corsie per gestire più attività

INSERT INTO corsi (id, tipo_corso, stato_attivita, data_inizio, num_posti, descrizione) VALUES 
(1, 'ACQUAGYM', 'Attivo', '2026-01-10', 15, 'Attività aerobica in acqua media a ritmo di musica'),
(2, 'SCUOLA_NUOTO_ADULTI', 'Attivo', '2026-01-15', 12, 'Corso di nuoto per livelli principiante e intermedio'),
(3, 'HYDROBIKE', 'Attivo', '2026-02-01', 10, 'Pedalare in acqua per tonificare e bruciare calorie'),
(4, 'NUOTO_MASTER', 'Attivo', '2026-01-01', 15, 'Allenamento intensivo per agonisti e nuotatori esperti'),
(5, 'ACQUACROSSFIT', 'Attivo', '2026-03-01', 12, 'Circuito ad alta intensità con attrezzi in acqua'),
(6, 'NEONATALE', 'Attivo', '2026-04-01', 8, 'Acquaticità per bambini dai 3 ai 36 mesi con genitore');


-- ----------------------------------------------------------
-- D. LEZIONI (IL PALINSESTO DI MAGGIO 2026)
-- ----------------------------------------------------------
INSERT INTO lezioni (id, data, ora_inizio, ora_fine, num_posti_prenotati, tipo_attivita, id_istruttore, id_corso, id_corsia)
VALUES 
-- Lezioni Passate (20 e 21 Maggio)
(1, '2026-05-20', '18:00:00', '19:00:00', 10, 'CORSO_GRUPPO', 5, 1, 1), -- Acquagym con Elena
(2, '2026-05-21', '09:00:00', '10:00:00', 4,  'NUOTO_LIBERO', NULL, NULL, 5), 

-- Lezioni di OGGI (Venerdì 22 Maggio 2026)
(3, '2026-05-22', '09:00:00', '10:00:00', 0,  'PRIVATA', 6, NULL, 1),       -- Lezione privata di Alessandro
(4, '2026-05-22', '10:00:00', '11:00:00', 6,  'CORSO_GRUPPO', 5, 6, 2),     -- Neonatale con Elena
(5, '2026-05-22', '13:00:00', '14:00:00', 2,  'NUOTO_LIBERO', NULL, NULL, 3),-- Pausa pranzo
(6, '2026-05-22', '18:00:00', '19:00:00', 12, 'CORSO_GRUPPO', 4, 4, 1),     -- Nuoto Master con Mario (Corsia 1)
(7, '2026-05-22', '18:00:00', '19:00:00', 8,  'CORSO_GRUPPO', 5, 1, 2),     -- Acquagym con Elena (Corsia 2, stesso orario!)
(8, '2026-05-22', '18:00:00', '19:00:00', 5,  'NUOTO_LIBERO', NULL, NULL, 5),-- Nuoto libero in contemporanea (Corsia 5)

-- Lezioni Future (Sabato 23 e oltre)
(9, '2026-05-23', '10:00:00', '11:00:00', 0, 'PRIVATA', 4, NULL, 1),        -- Privata con Mario
(10,'2026-05-25', '19:00:00', '20:00:00', 5, 'CORSO_GRUPPO', 6, 3, 2);      -- Hydrobike con Alessandro


-- ----------------------------------------------------------
-- E. PRENOTAZIONI
-- ----------------------------------------------------------
INSERT INTO prenotazioni (id, data_richiesta, stato, id_cliente, id_lezione) VALUES 
(1, '2026-05-18', 'Confermato', 2, 7), -- Anna prenota Acquagym di Oggi
(2, '2026-05-20', 'Confermato', 1, 8), -- Gianluca prenota Nuoto Libero di Oggi alle 18
(3, '2026-05-21', 'In Attesa',  8, 6); -- Sofia si prenota in lista d'attesa per Nuoto Master


-- ----------------------------------------------------------
-- F. NOTIFICHE (Coerenti con la data del 22 Maggio 2026)
-- ----------------------------------------------------------

INSERT INTO notifiche (messaggio, letta, data_invio, id_destinatario, tipo, id_riferimento) VALUES 

-- 1. CASISTICA: NOTIFICA CON PAGAMENTO (Non Letta) -> Destinata a Gianluca (id 1)
('La tua richiesta per la lezione privata con Mario è stata accettata! Procedi al pagamento per confermare definitivamente lo slot.', 0, '2026-05-22 10:15:00', 1, 'RICHIESTA_PAGAMENTO', 11),
-- 2. CASISTICA: CAMBIO PROGRAMMA URGENTE (Non Letta) -> Destinata ad Anna (id 2)
('Avviso: La lezione di Acquagym di stasera alle 18:00 è stata spostata alla Corsia 4 per manutenzione tecnica. A più tardi!', 0, '2026-05-22 08:30:00', 2, 'INFO', 7),
-- 3. CASISTICA: SUCCESSO LISTA D'ATTESA (Non Letta) -> Destinata a Sofia (id 8)
('Ottime notizie! Si è liberato un posto per il corso di Nuoto Master di stasera. La tua prenotazione in lista d''attesa è ora passata a "Confermata".', 0, '2026-05-21 17:45:00', 8, 'INFO', 6),
-- 4. CASISTICA: PROMEMORIA NORMALE (Già Letta) -> Destinata a Sofia (id 8)
('Promemoria: il tuo abbonamento periodico scade a fine mese (31/05/2026). Ricordati di rinnovarlo per non perdere i progressi!', 1, '2026-05-20 09:00:00', 8, 'INFO', NULL),
-- 5. CASISTICA: MESSAGGIO DALLA SEGRETERIA (Già Letta) -> Destinata a Marco (id 7)
('La segreteria ha appena respinto il caricamento del tuo certificato medico: il documento risulta sfocato e illeggibile. Ti preghiamo di ricaricarlo.', 1, '2026-05-18 14:20:00', 7, 'INFO', NULL);
-- ----------------------------------------------------------
-- G. PAGAMENTI
-- ----------------------------------------------------------
INSERT INTO pagamenti (id_transazione, importo, data_acquisto, stato, id_cliente, id_titolo_acquistato) VALUES 
('TRX-2026-001', 75.00, '2026-05-01', 'Completato', 1, 1),
('TRX-2026-002', 450.00, '2026-01-01', 'Completato', 2, 2),
('TRX-2025-099', 90.00, '2025-10-01', 'Completato', 7, 3),
('TRX-2026-004', 55.00, '2026-05-01', 'Completato', 8, 4);
package org.example.model.dao.db;

import org.example.model.dao.Interface.LezioneDAO;
import org.example.model.domain.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LezioneDAOMySQL implements LezioneDAO {
    private static final Logger logger = Logger.getLogger(LezioneDAOMySQL.class.getName());

    @Override
    public List<Lezione> trovaPerTipoEData(TipoAttivita tipo, java.time.LocalDate data) {
        List<Lezione> lista = new ArrayList<>();

        // LA NUOVA QUERY: Uniamo sia la tabella corsi (per il nome) che corsie (per il nuoto libero)
        String query = "SELECT l.*, c.tipo_corso, c.num_posti AS posti_corso, cor.numero_corsia, cor.capienza_massima AS posti_corsia " +
                "FROM lezioni l " +
                "LEFT JOIN corsi c ON l.id_corso = c.id " +
                "LEFT JOIN corsie cor ON l.id_corsia = cor.id " +
                "WHERE l.tipo_attivita = ? AND l.data = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tipo.name());
            stmt.setDate(2, java.sql.Date.valueOf(data));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    // 1. Capiamo quanti posti totali ci sono (da corsi o da corsie)
                    int postiTotali = 0;
                    String tipoCorsoTrovato = rs.getString("tipo_corso");

                    if (tipoCorsoTrovato != null) {
                        postiTotali = rs.getInt("posti_corso");
                    } else {
                        postiTotali = rs.getInt("posti_corsia");
                    }

                    // 2. Creazione della lezione
                    Lezione l = new Lezione(
                            rs.getInt("id"),
                            rs.getDate("data").toLocalDate(),
                            rs.getTime("ora_inizio").toLocalTime(),
                            rs.getTime("ora_fine").toLocalTime(),
                            rs.getInt("num_posti_prenotati"),
                            postiTotali, // Usiamo la variabile calcolata
                            tipo
                    );

                    // 3. FONDAMENTALE: Se è un corso, lo creiamo e lo agganciamo!
                    if (tipoCorsoTrovato != null) {
                        Corso corsoDellaLezione = new Corso();
                        corsoDellaLezione.setNome(TipoCorso.valueOf(tipoCorsoTrovato));
                        l.setCorsoAppartenenza(corsoDellaLezione);
                    }

                    // 4. Sistemiamo la corsia (se presente)
                    int numCorsia = rs.getInt("numero_corsia");
                    if (!rs.wasNull()) {
                        Corsia corsia = new Corsia();
                        corsia.setIdCorsia(numCorsia);
                        l.setCorsiaAssegnata(corsia);
                    }

                    lista.add(l);
                }
            }
        } catch (SQLException e) {
            logger.severe("Errore recupero lezioni: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public void aggiornaPostiOccupati(Lezione lezione) {
        String query = "UPDATE lezioni SET num_posti_prenotati = ? WHERE id = ?";
        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, lezione.getNumPostiPrenotati());
            stmt.setInt(2, lezione.getIdLezione());
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.severe("Errore aggiornamento posti lezione: " + e.getMessage());
        }
    }

    @Override
    public Lezione trovaPerId(Integer id) {
        Lezione l = null;

        // 🌟 QUERY AGGIORNATA: Prende anche nome e cognome dell'istruttore associato!
        String query = "SELECT l.*, c.tipo_corso, c.num_posti AS posti_corso, cor.capienza_massima AS posti_corsia, cor.numero_corsia, " +
                "u.nome AS nome_ist, u.cognome AS cognome_ist " +
                "FROM lezioni l " +
                "LEFT JOIN corsi c ON l.id_corso = c.id " +
                "LEFT JOIN corsie cor ON l.id_corsia = cor.id " +
                "LEFT JOIN utenti u ON l.id_istruttore = u.id " +
                "WHERE l.id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    int postiTotali = 0;
                    String tipoCorsoTrovato = rs.getString("tipo_corso");

                    if (tipoCorsoTrovato != null) {
                        postiTotali = rs.getInt("posti_corso");
                    } else {
                        postiTotali = rs.getInt("posti_corsia");
                    }

                    l = new Lezione(
                            rs.getInt("id"),
                            rs.getDate("data").toLocalDate(),
                            rs.getTime("ora_inizio").toLocalTime(),
                            rs.getTime("ora_fine").toLocalTime(),
                            rs.getInt("num_posti_prenotati"),
                            postiTotali,
                            TipoAttivita.valueOf(rs.getString("tipo_attivita"))
                    );

                    if (tipoCorsoTrovato != null) {
                        Corso corsoDellaLezione = new Corso();
                        corsoDellaLezione.setNome(TipoCorso.valueOf(tipoCorsoTrovato));
                        l.setCorsoAppartenenza(corsoDellaLezione);
                    }

                    int numCorsia = rs.getInt("numero_corsia");
                    if (!rs.wasNull()) {
                        Corsia corsia = new Corsia();
                        corsia.setIdCorsia(numCorsia);
                        l.setCorsiaAssegnata(corsia);
                    }

                    // 🌟 NUOVA LOGICA: Se la lezione ha un istruttore, popoliamo l'oggetto Java!
                    int idIstruttore = rs.getInt("id_istruttore");
                    if (!rs.wasNull()) {
                        Istruttore ist = new Istruttore();
                        ist.setId(idIstruttore);
                        ist.setNome(rs.getString("nome_ist"));
                        ist.setCognome(rs.getString("cognome_ist"));
                        l.setIstruttore(ist); // Agganciamo l'istruttore alla lezione!
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Errore nel recupero della lezione tramite ID: " + e.getMessage());
        }
        return l;
    }

    public List<Lezione> trovaPerCorso(TipoCorso tipoCorso) {
        List<Lezione> lista = new ArrayList<>();

        // QUI C'È IL SEGRETO: Notare "c.num_posti AS posti_totali"
        String query = "SELECT l.*, c.num_posti AS posti_totali FROM lezioni l " +
                "JOIN corsi c ON l.id_corso = c.id " +
                "WHERE c.tipo_corso = ? AND l.data >= CURDATE() " +
                "ORDER BY l.data, l.ora_inizio";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tipoCorso.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Lezione l = new Lezione();
                    l.setIdLezione(rs.getInt("id"));
                    l.setData(rs.getDate("data").toLocalDate());
                    l.setOraInizio(rs.getTime("ora_inizio").toLocalTime());
                    l.setOraFine(rs.getTime("ora_fine").toLocalTime());

                    // ORA USIAMO IL SOPRANNOME ESATTO CHE ABBIAMO DATO NELLA QUERY
                    l.setNumPostiTotali(rs.getInt("posti_totali"));

                    l.setNumPostiPrenotati(rs.getInt("num_posti_prenotati"));

                    Corso corsoDellaLezione = new Corso();
                    corsoDellaLezione.setNome(tipoCorso);
                    l.setCorsoAppartenenza(corsoDellaLezione);

                    lista.add(l);
                }
            }
        } catch (SQLException e) {
            logger.severe("Errore nel recupero lezioni per corso: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<Lezione> trovaPrivateDisponibiliPerIstruttore(Integer idIstruttore) {
        List<Lezione> lezioniDisponibili = new ArrayList<>();

        String query = "SELECT * FROM lezioni " +
                "WHERE id_istruttore = ? " +
                "AND tipo_attivita = 'PRIVATA' " +
                "AND num_posti_prenotati = 0 " +
                "AND data >= CURRENT_DATE() " +
                "ORDER BY data ASC, ora_inizio ASC";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idIstruttore);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Lezione l = new Lezione();

                    // Colonna corretta: 'id'
                    l.setIdLezione(rs.getInt("id"));

                    if (rs.getDate("data") != null) l.setData(rs.getDate("data").toLocalDate());
                    if (rs.getTime("ora_inizio") != null) l.setOraInizio(rs.getTime("ora_inizio").toLocalTime());
                    if (rs.getTime("ora_fine") != null) l.setOraFine(rs.getTime("ora_fine").toLocalTime());

                    // Impostiamo a 1 di default per le private
                    l.setNumPostiTotali(1);
                    l.setNumPostiPrenotati(rs.getInt("num_posti_prenotati"));

                    String tipo = rs.getString("tipo_attivita");
                    if (tipo != null) l.setTipoAttivita(TipoAttivita.valueOf(tipo));

                    Istruttore istr = new Istruttore();
                    istr.setId(idIstruttore);
                    l.setIstruttore(istr);

                    lezioniDisponibili.add(l);
                }
            }
        } catch (SQLException e) {
            // Se c'è un errore SQL, lo cattura qui senza far crashare il programma
            logger.log(java.util.logging.Level.SEVERE, "Errore DB nel recupero slot privati per l'istruttore " + idIstruttore, e);
        }

        // Ritorna la lista piena o vuota al Controller
        return lezioniDisponibili;
    }

    @Override
    public List<Lezione> trovaImpegniIstruttore(Integer idIstruttore, java.time.LocalDate dataInizio, java.time.LocalDate dataFine) {
        List<Lezione> lista = new java.util.ArrayList<>();
        String query = "SELECT l.*, c.tipo_corso, c.num_posti AS posti_corso, cor.numero_corsia, " +
                "u_cli.nome AS nome_cli, u_cli.cognome AS cognome_cli, p.note AS note_pren " +
                "FROM lezioni l " +
                "LEFT JOIN corsi c ON l.id_corso = c.id " +
                "LEFT JOIN corsie cor ON l.id_corsia = cor.id " +
                "LEFT JOIN prenotazioni p ON l.id = p.id_lezione " +
                "LEFT JOIN utenti u_cli ON p.id_cliente = u_cli.id " +
                "WHERE (l.id_istruttore = ? AND l.tipo_attivita != 'PRIVATA' AND l.data BETWEEN ? AND ?) " +
                "   OR (l.id_istruttore = ? AND l.tipo_attivita = 'PRIVATA' AND p.stato IN ('Accettata - In attesa di pagamento', 'Confermata e Pagata') AND l.data BETWEEN ? AND ?) " +
                "ORDER BY l.data ASC, l.ora_inizio ASC";

        try (java.sql.Connection conn = DBConnectionFactory.getInstance().createConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idIstruttore); stmt.setDate(2, java.sql.Date.valueOf(dataInizio)); stmt.setDate(3, java.sql.Date.valueOf(dataFine));
            stmt.setInt(4, idIstruttore); stmt.setDate(5, java.sql.Date.valueOf(dataInizio)); stmt.setDate(6, java.sql.Date.valueOf(dataFine));

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Lezione l = new Lezione();
                    l.setIdLezione(rs.getInt("id"));
                    l.setData(rs.getDate("data").toLocalDate());
                    l.setOraInizio(rs.getTime("ora_inizio").toLocalTime());
                    l.setOraFine(rs.getTime("ora_fine").toLocalTime());
                    l.setTipoAttivita(TipoAttivita.valueOf(rs.getString("tipo_attivita")));

                    String tipoCorso = rs.getString("tipo_corso");
                    if (tipoCorso != null) {
                        Corso corso = new Corso();
                        corso.setNome(TipoCorso.valueOf(tipoCorso));
                        l.setCorsoAppartenenza(corso);
                    }

                    // 🌟 Estraiamo nome e note del cliente
                    String nomeCli = rs.getString("nome_cli");
                    if (nomeCli != null) {
                        l.setInfoClientePrivata(nomeCli + " " + rs.getString("cognome_cli"));
                        l.setNoteClientePrivata(rs.getString("note_pren"));
                    }
                    lista.add(l);
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
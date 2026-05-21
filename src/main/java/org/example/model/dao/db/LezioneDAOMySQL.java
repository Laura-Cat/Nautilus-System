package org.example.model.dao.db;

import org.example.model.dao.Interface.LezioneDAO;
import org.example.model.domain.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

        // AGGIORNAMENTO QUERY: LEFT JOIN su corsi e corsie per coprire tutte le tipologie di attività!
        String query = "SELECT l.*, c.tipo_corso, c.num_posti AS posti_corso, cor.capienza_massima AS posti_corsia, cor.numero_corsia " +
                "FROM lezioni l " +
                "LEFT JOIN corsi c ON l.id_corso = c.id " +
                "LEFT JOIN corsie cor ON l.id_corsia = cor.id " +
                "WHERE l.id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    // 1. Determiniamo i posti totali corretti (se corso prende da corsi, se nuoto libero da corsie)
                    int postiTotali = 0;
                    String tipoCorsoTrovato = rs.getString("tipo_corso");

                    if (tipoCorsoTrovato != null) {
                        postiTotali = rs.getInt("posti_corso");
                    } else {
                        postiTotali = rs.getInt("posti_corsia");
                    }

                    // 2. Creiamo la lezione usando il tuo costruttore a 7 argomenti
                    l = new Lezione(
                            rs.getInt("id"),
                            rs.getDate("data").toLocalDate(),
                            rs.getTime("ora_inizio").toLocalTime(),
                            rs.getTime("ora_fine").toLocalTime(),
                            rs.getInt("num_posti_prenotati"),
                            postiTotali, // Il 6° parametro dinamico!
                            TipoAttivita.valueOf(rs.getString("tipo_attivita")) // Il 7° parametro
                    );

                    // 3. FONDAMENTALE PER LA STRATEGY: Se è un corso, creiamo e associamo l'oggetto Corso
                    if (tipoCorsoTrovato != null) {
                        Corso corsoDellaLezione = new Corso();
                        corsoDellaLezione.setNome(TipoCorso.valueOf(tipoCorsoTrovato));
                        l.setCorsoAppartenenza(corsoDellaLezione);
                    }

                    // 4. Sistemiamo la corsia (se presente, per compatibilità Nuoto Libero)
                    int numCorsia = rs.getInt("numero_corsia");
                    if (!rs.wasNull()) {
                        Corsia corsia = new Corsia();
                        corsia.setIdCorsia(numCorsia);
                        l.setCorsiaAssegnata(corsia);
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
}
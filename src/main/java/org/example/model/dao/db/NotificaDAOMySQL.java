package org.example.model.dao.db;

import org.example.model.dao.Interface.NotificaDAO;
import org.example.model.dao.db.DBConnectionFactory;
import org.example.model.domain.Notifica;
import org.example.model.domain.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificaDAOMySQL implements  NotificaDAO{

    private static final Logger logger = Logger.getLogger(NotificaDAO.class.getName());

    // ==============================================================================
    // 1. RECUPERA TUTTE LE NOTIFICHE (STORICO COMPLETO)
    // ==============================================================================
    public List<Notifica> recuperaTuttePerUtente(User utente, Integer idDestinatario) {
        List<Notifica> lista = new ArrayList<>();
        String query = "SELECT * FROM notifiche WHERE id_destinatario = ? ORDER BY data_invio DESC";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idDestinatario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(estraiNotificaDaResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel recupero di tutte le notifiche per l'utente " + idDestinatario, e);
        }
        return lista;
    }

    // ==============================================================================
    // 2. RECUPERA SOLO LE NOTIFICHE NON LETTE (PALLINO ROSSO)
    // ==============================================================================
    public List<Notifica> recuperaNonLettePerUtente(User utente, Integer idDestinatario) {
        List<Notifica> lista = new ArrayList<>();
        String query = "SELECT * FROM notifiche WHERE id_destinatario = ? AND letta = 0 ORDER BY data_invio DESC";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idDestinatario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(estraiNotificaDaResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel recupero delle notifiche non lette per l'utente " + idDestinatario, e);
        }
        return lista;
    }

    // ==============================================================================
    // 3. AGGIORNA NOTIFICA (ES. SEGNA COME LETTA)
    // ==============================================================================
    public boolean aggiornaStato(Notifica notifica) {
        String query = "UPDATE notifiche SET letta = ?, tipo = ?, id_riferimento = ? WHERE id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, notifica.getLetta());

            if (notifica.getTipo() != null) {
                stmt.setString(2, notifica.getTipo());
            } else {
                stmt.setString(2, "INFO");
            }

            if (notifica.getIdRiferimento() != null) {
                stmt.setInt(3, notifica.getIdRiferimento());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setInt(4, notifica.getId());

            int righeModificate = stmt.executeUpdate();
            return righeModificate > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore durante l'aggiornamento della notifica " + notifica.getId(), e);
            return false;
        }
    }

    // ==============================================================================
    // 4. CREA E INVIA NUOVA NOTIFICA
    // ==============================================================================
    public boolean invia(Notifica notifica, Integer idDestinatario) {
        String query = "INSERT INTO notifiche (messaggio, letta, data_invio, id_destinatario, tipo, id_riferimento) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, notifica.getMessaggio());
            stmt.setBoolean(2, notifica.getLetta()); // Solitamente false
            stmt.setInt(3, idDestinatario);

            stmt.setString(4, notifica.getTipo() != null ? notifica.getTipo() : "INFO");

            if (notifica.getIdRiferimento() != null) {
                stmt.setInt(5, notifica.getIdRiferimento());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            int righeInserite = stmt.executeUpdate();
            return righeInserite > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore durante l'invio della notifica al destinatario " + idDestinatario, e);
            return false;
        }
    }

    // ==============================================================================
    // UTILITY: MAPPA IL RESULTSET NELL'OGGETTO JAVA
    // ==============================================================================
    private Notifica estraiNotificaDaResultSet(ResultSet rs) throws SQLException {
        Notifica n = new Notifica();

        n.setId(rs.getInt("id"));
        n.setMessaggio(rs.getString("messaggio"));

        // 🌟 Lettura corretta dello stato (vero/falso)
        n.setLetta(rs.getBoolean("letta"));

        // Gestione data/ora (con fallback nel caso sia nulla nel db)
        Timestamp timestamp = rs.getTimestamp("data_invio");
        if (timestamp != null) {
            n.setDataInvio(timestamp.toLocalDateTime());
        }

        // I due nuovi campi aggiunti per la logica dei pagamenti
        n.setTipo(rs.getString("tipo"));

        // Per gli Integer (che possono essere NULL), dobbiamo fare un controllo extra
        int idRif = rs.getInt("id_riferimento");
        if (!rs.wasNull()) {
            n.setIdRiferimento(idRif);
        } else {
            n.setIdRiferimento(null);
        }

        return n;
    }
}
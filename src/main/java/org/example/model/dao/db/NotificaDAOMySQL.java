package org.example.model.dao.db;

import org.example.model.dao.Interface.NotificaDAO;
import org.example.model.domain.Notifica;
import org.example.model.domain.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NotificaDAOMySQL implements NotificaDAO {

    private static final Logger logger = Logger.getLogger(NotificaDAOMySQL.class.getName());

    @Override
    public boolean invia(Notifica notifica, Integer idDestinatario) {
        String query = "INSERT INTO notifiche (messaggio, letta, data_invio, id_destinatario) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, notifica.getMessaggio());
            stmt.setBoolean(2, notifica.isLetta());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, idDestinatario);
            stmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            logger.severe("Errore durante l'inserimento della notifica nel DB: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Notifica> recuperaNonLettePerUtente(User utente, Integer idUtente) {
        List<Notifica> notifiche = new ArrayList<>();
        String query = "SELECT id, messaggio, data_invio FROM notifiche WHERE id_destinatario = ? AND letta = FALSE";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idUtente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notifica n = new Notifica(rs.getString("messaggio"), utente);
                    n.setId(rs.getInt("id"));
                    notifiche.add(n);
                }
            }
        } catch (SQLException e) {
            logger.severe("Errore durante il recupero delle notifiche: " + e.getMessage());
        }
        return notifiche;
    }

    @Override
    public void segnaComeLetta(Notifica notifica) {
        String query = "UPDATE notifiche SET letta = TRUE WHERE id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, notifica.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.severe("Errore durante l'aggiornamento della notifica: " + e.getMessage());
        }
    }

    @Override
    public void aggiornaStato(Notifica notifica) {
        String query = "UPDATE notifiche SET letta = ? WHERE id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, notifica.isLetta());
            stmt.setInt(2, notifica.getId());
            stmt.executeUpdate(); // FIX: mancava questa riga — la query non veniva mai eseguita

        } catch (SQLException e) {
            logger.severe("Errore DAO durante l'aggiornamento della notifica: " + e.getMessage());
        }
    }
}

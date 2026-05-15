package org.example.model.dao;

import org.example.model.domain.Notifica;
import org.example.model.domain.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificaDAOMySQL implements NotificaDAO {

    @Override
    public void inserisci(Notifica notifica, Integer idDestinatario) {
        // La query SQL nuda e cruda. I punti interrogativi (?) sono i parametri.
        String query = "INSERT INTO notifiche (messaggio, letta, data_invio, id_destinatario) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Sostituiamo i punti interrogativi con i dati veri
            stmt.setString(1, notifica.getMessaggio());
            stmt.setBoolean(2, notifica.isLetta());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // Convertiamo la data di Java in data di SQL
            stmt.setInt(4, idDestinatario);

            // Eseguiamo la query sul DB!
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento della notifica nel DB: " + e.getMessage());
        }
    }

    @Override
    public List<Notifica> recuperaNonLettePerUtente(User utente, Integer idUtente) {
        List<Notifica> notifiche = new ArrayList<>();
        String query = "SELECT id, messaggio, data_invio FROM notifiche WHERE id_destinatario = ? AND letta = FALSE";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idUtente);

            try (ResultSet rs = stmt.executeQuery()) {
                // Cicliamo su tutte le righe trovate dal database
                while (rs.next()) {
                    // Creiamo l'oggetto Java leggendo le colonne del DB
                    Notifica n = new Notifica(rs.getString("messaggio"), utente);
                    // n.setId(rs.getInt("id")); // Scommentalo se in Notifica.java aggiungi il setter per l'ID!

                    notifiche.add(n);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero delle notifiche: " + e.getMessage());
        }
        return notifiche;
    }

    @Override
    public void segnaComeLetta(Notifica notifica) {
        // Immaginiamo che la notifica abbia un ID preso dal DB
        String query = "UPDATE notifiche SET letta = TRUE WHERE id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, notifica.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiornamento della notifica: " + e.getMessage());
        }
    }
}

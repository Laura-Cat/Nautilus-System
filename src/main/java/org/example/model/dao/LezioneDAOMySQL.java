package org.example.model.dao;

import org.example.model.domain.Lezione;
import org.example.model.domain.TipoAttivita;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LezioneDAOMySQL implements LezioneDAO {

    @Override
    public List<Lezione> trovaPerTipoEData(TipoAttivita tipo, java.time.LocalDate data) {
        List<Lezione> lista = new ArrayList<>();
        String query = "SELECT * FROM lezioni WHERE tipo_attivita = ? AND data = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tipo.toString());
            stmt.setDate(2, Date.valueOf(data));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Lezione l = new Lezione(
                            rs.getInt("id"),
                            rs.getDate("data").toLocalDate(),
                            rs.getTime("ora_inizio").toLocalTime(),
                            rs.getTime("ora_fine").toLocalTime(),
                            rs.getInt("num_posti_prenotati"),
                            tipo
                    );
                    lista.add(l);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore recupero lezioni: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public void aggiornaPostiOccupati(Lezione lezione) {
        String query = "UPDATE lezioni SET num_posti_prenotati = ? WHERE id = ?";
        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, lezione.getNumPostiPrenotati());
            stmt.setInt(2, lezione.getIDLezione());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore aggiornamento posti lezione: " + e.getMessage());
        }
    }

    @Override
    public Lezione trovaPerId(Integer id) {
        Lezione lezione = null;
        String query = "SELECT * FROM lezioni WHERE id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Recuperiamo il tipo attività come stringa e lo trasformiamo nell'Enum Java
                    TipoAttivita tipo = TipoAttivita.valueOf(rs.getString("tipo_attivita"));

                    // Costruiamo l'oggetto Lezione
                    lezione = new Lezione(
                            rs.getInt("id"),
                            rs.getDate("data").toLocalDate(),
                            rs.getTime("ora_inizio").toLocalTime(),
                            rs.getTime("ora_fine").toLocalTime(),
                            rs.getInt("num_posti_prenotati"),
                            tipo
                    );

                    /* Nota: Se la lezione è di tipo CORSO, potresti voler caricare
                       anche l'ID del corso associato se il tuo oggetto Lezione lo prevede. */
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero della lezione tramite ID: " + e.getMessage());
        }
        return lezione;
    }
}
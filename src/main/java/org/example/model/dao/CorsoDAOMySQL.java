package org.example.model.dao;

import org.example.model.domain.Corso;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CorsoDAOMySQL implements CorsoDAO {

    @Override
    public List<Corso> recuperaCorsiAttivi() {
        List<Corso> lista = new ArrayList<>();
        String query = "SELECT * FROM corsi WHERE stato_attivita = 'Attivo'";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Corso c = new Corso(
                        rs.getString("nome"),
                        rs.getString("stato_attivita"),
                        rs.getDate("data_inizio").toLocalDate(),
                        rs.getInt("id"),
                        rs.getInt("num_posti"),
                        rs.getString("descrizione")
                );
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Errore recupero corsi: " + e.getMessage());
        }
        return lista;
    }
}

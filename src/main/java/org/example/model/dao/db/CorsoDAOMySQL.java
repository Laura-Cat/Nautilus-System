package org.example.model.dao.db;

import org.example.model.dao.interfaces.CorsoDAO;
import org.example.model.domain.Corso;
import org.example.model.domain.TipoCorso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CorsoDAOMySQL implements CorsoDAO {

    private static final Logger logger = Logger.getLogger(CorsoDAOMySQL.class.getName());
    @Override
    public List<Corso> recuperaCorsiAttivi() {
        List<Corso> lista = new ArrayList<>();
        String query = "SELECT nome, stato_attivita, data_inizio, id, num_posti, descrizione FROM corsi WHERE stato_attivita = 'Attivo'";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nomeDb = rs.getString("nome");
                TipoCorso tipoCorso = null;
                if (nomeDb != null) {
                    try {
                        tipoCorso = TipoCorso.valueOf(nomeDb.toUpperCase().replace(" ", "_"));
                    } catch (IllegalArgumentException e) {
                        logger.warning("Attenzione: Il corso '" + nomeDb + "' nel DB non esiste nell'Enum TipoCorso!");
                    }
                }
                Corso c = new Corso(
                        tipoCorso,
                        rs.getString("stato_attivita"),
                        rs.getDate("data_inizio").toLocalDate(),
                        rs.getInt("id"),
                        rs.getInt("num_posti"),
                        rs.getString("descrizione")
                );
                lista.add(c);
            }
        } catch (SQLException e) {
            logger.severe("Errore recupero corsi: " + e.getMessage());
        }
        return lista;
    }
}

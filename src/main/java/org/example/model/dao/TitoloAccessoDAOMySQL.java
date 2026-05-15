package org.example.model.dao;

import org.example.model.domain.TitoloAccesso;
import org.example.model.domain.PacchettoCrediti;
import org.example.model.domain.AbbonamentoPeriodico;

import java.sql.*;
import java.util.logging.Logger;

public class TitoloAccessoDAOMySQL implements TitoloAccessoDAO {
    private static final Logger logger = Logger.getLogger(TitoloAccessoDAOMySQL.class.getName());
    @Override
    public void salvaNuovo(TitoloAccesso titolo, Integer idCliente) {
        String query = "INSERT INTO titoli_accesso (id_cliente, tipo_titolo, crediti_rimanenti, data_inizio, data_fine) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, idCliente);

            // Sfruttiamo il polimorfismo per capire che tipo di titolo stiamo salvando!
            if (titolo instanceof PacchettoCrediti) {
                PacchettoCrediti pc = (PacchettoCrediti) titolo;
                stmt.setString(2, "PACCHETTO_CREDITI");
                stmt.setInt(3, pc.getCreditiRimanenti());
                stmt.setNull(4, Types.DATE); // Un pacchetto crediti non ha data di inizio
                stmt.setNull(5, Types.DATE);
            } else if (titolo instanceof AbbonamentoPeriodico) {
                AbbonamentoPeriodico ap = (AbbonamentoPeriodico) titolo;
                stmt.setString(2, "ABBONAMENTO_PERIODICO");
                stmt.setInt(3, 0); // L'abbonamento non usa crediti
                stmt.setDate(4, Date.valueOf(ap.getDataInizio()));
                stmt.setDate(5, Date.valueOf(ap.getDataFine()));
            }

            stmt.executeUpdate();

            // Recuperiamo l'ID generato dal DB e lo assegniamo all'oggetto Java
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    titolo.setTitoloID(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            logger.severe("Errore durante il salvataggio del titolo: " + e.getMessage());
        }
    }

    @Override
    public void aggiornaCrediti(PacchettoCrediti pacchetto) {
        String query = "UPDATE titoli_accesso SET crediti_rimanenti = ? WHERE id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, pacchetto.getCreditiRimanenti());
            stmt.setInt(2, pacchetto.getTitoloID());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.severe("Errore durante l'aggiornamento dei crediti: " + e.getMessage());
        }
    }

    @Override
    public void aggiornaRinnovo(AbbonamentoPeriodico abbonamento) {
        String query = "UPDATE titoli_accesso SET data_fine = ? WHERE id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(abbonamento.getDataFine()));
            stmt.setInt(2, abbonamento.getTitoloID());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.severe("Errore durante il rinnovo dell'abbonamento: " + e.getMessage());
        }
    }

    @Override
    public TitoloAccesso trovaPerCliente(Integer idCliente) {
        // Da implementare quando faremo il Login completo, serve per ricaricare la tessera dal DB
        return null;
    }
}

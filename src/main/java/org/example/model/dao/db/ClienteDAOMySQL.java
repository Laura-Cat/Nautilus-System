package org.example.model.dao.db;

import org.example.model.dao.interfaces.ClienteDAO;
import org.example.model.domain.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Logger;

public class ClienteDAOMySQL implements ClienteDAO {
    private static final Logger logger = Logger.getLogger(ClienteDAOMySQL.class.getName());
    @Override
    public Cliente trovaPerId(Integer id) {
        Cliente cliente = null;
        // Cerchiamo l'utente tramite ID
        String query = "SELECT * FROM utenti WHERE id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date sqlDateNascita = rs.getDate("data_nascita");
                    LocalDate dataNascita = (sqlDateNascita != null) ? sqlDateNascita.toLocalDate() : null;
                    cliente = new Cliente(
                            id,
                            rs.getString("cf"),
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            dataNascita,
                            rs.getString("luogo_nascita"),
                            rs.getString("indirizzo"),
                            rs.getString("email"),
                            rs.getString("password"),
                            id,
                            rs.getBoolean("certificato_valido")
                    );

                    /* Se il cliente ha anche un Abbonamento da caricare,
                       qui potrai in futuro chiamare il TitoloAccessoDAO! */
                }
            }
        } catch (SQLException e) {
            logger.severe("Errore nel recupero del cliente tramite ID: " + e.getMessage());
        }

        return cliente;
    }
}

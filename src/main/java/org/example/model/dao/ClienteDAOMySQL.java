package org.example.model.dao;

import org.example.model.domain.Cliente;
// Importa anche le classi del tuo DBConnectionFactory ecc.
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ClienteDAOMySQL implements ClienteDAO {

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
                    java.sql.Date sqlDate = rs.getDate("dataNascita");
                    LocalDate dataNascita = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                    cliente = new Cliente(
                            rs.getString("cf"),
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            dataNascita,
                            rs.getString("luogoNascita"),
                            rs.getString("indirizzo"),
                            rs.getString("email"),
                            rs.getString("password"),
                            id,                 // Questo è il clienteID
                            rs.getBoolean("certificatoValido")
                    );

                    /* Se il cliente ha anche un Abbonamento da caricare,
                       qui potrai in futuro chiamare il TitoloAccessoDAO! */
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero del cliente tramite ID: " + e.getMessage());
        }

        return cliente;
    }
}

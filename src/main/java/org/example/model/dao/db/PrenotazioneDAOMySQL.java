package org.example.model.dao.db;

import org.example.model.dao.DAOFactory;
import org.example.model.dao.Interface.PrenotazioneDAO;
import org.example.model.domain.Prenotazione;
import org.example.model.domain.Cliente;
import org.example.model.domain.Lezione;
import org.example.model.domain.TipoAttivita;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PrenotazioneDAOMySQL implements PrenotazioneDAO {
    private static final Logger logger = Logger.getLogger(PrenotazioneDAOMySQL.class.getName());
    @Override
    public void salva(Prenotazione p) {
        String query = "INSERT INTO prenotazioni (data_richiesta, stato, tipologia, id_cliente, id_lezione) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDate(1, Date.valueOf(p.getDataRichiesta()));
            stmt.setString(2, p.getStato()); // Stato iniziale (es. "In Attesa")
            stmt.setString(3, p.getTipologia().name());
            stmt.setInt(4, p.getCliente().getClienteID());
            stmt.setInt(5, p.getLezionePrenotata().getIdLezione());

            stmt.executeUpdate();

            // CHICCA DA ESAME: Recuperiamo l'ID auto-generato dal database e lo mettiamo nell'oggetto Java
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    p.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            logger.severe("Errore salvataggio prenotazione: " + e.getMessage());
        }
    }

    @Override
    public void aggiornaStato(Prenotazione p) {
        String query = "UPDATE prenotazioni SET stato = ? WHERE id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, p.getStato()); // Il nuovo stato impostato dal controller
            stmt.setInt(2, p.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.severe("Errore aggiornamento stato prenotazione: " + e.getMessage());
        }
    }

    @Override
    public List<Prenotazione> trovaPerCliente(Cliente c) {
        List<Prenotazione> lista = new ArrayList<>();
        // Query semplice: in un progetto reale useremmo una JOIN, ma per ora teniamola base
        String query = "SELECT id, data_richiesta, tipologia FROM prenotazioni WHERE id_cliente = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, c.getClienteID());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Creiamo l'oggetto Prenotazione (Mapping)
                    Prenotazione p = new Prenotazione(
                            rs.getInt("id"),
                            rs.getDate("data_richiesta").toLocalDate(),
                            TipoAttivita.valueOf(rs.getString("tipologia")),

                            c
                    );
                    p.setStato(rs.getString("stato"));

                    // Nota: Qui dovresti anche recuperare la Lezione dal DB usando un LezioneDAO
                    // p.setLezionePrenotata(lezioneDAO.trovaPerId(rs.getInt("id_lezione")));

                    lista.add(p);
                }
            }

        } catch (SQLException e) {
            logger.severe("Errore recupero prenotazioni cliente: " + e.getMessage());
        }
        return lista;
    }
    @Override
    public Prenotazione trovaPerId(Integer id) {
        Prenotazione prenotazione = null;
        // La query cerca la riga specifica tramite la chiave primaria
        String query = "SELECT id, id_cliente, id_lezione, data_richiesta, tipologia, stato FROM prenotazioni WHERE id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // 1. Recuperiamo l'ID del cliente e della lezione dal DB
                    int idCliente = rs.getInt("id_cliente");
                    int idLezione = rs.getInt("id_lezione");

                    Cliente cliente = DAOFactory.getInstance().getClienteDAO().trovaPerId(idCliente);
                    Lezione lezione = DAOFactory.getInstance().getLezioneDAO().trovaPerId(idLezione);

                    prenotazione = new Prenotazione(
                            rs.getInt("id"),
                            rs.getDate("data_richiesta").toLocalDate(),
                            TipoAttivita.valueOf(rs.getString("tipologia")),
                            null // Qui andrebbe l'oggetto cliente caricato dal DAO
                    );
                    prenotazione.setStato(rs.getString("stato"));

                    // Se avessi caricato la lezione:
                    // prenotazione.setLezionePrenotata(lezione);
                }
            }
        } catch (SQLException e) {
            logger.severe("Errore nel recupero della prenotazione tramite ID: " + e.getMessage());
        }
        return prenotazione;
    }
}

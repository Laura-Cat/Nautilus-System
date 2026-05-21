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
        String query = "INSERT INTO prenotazioni (data_richiesta, stato, id_cliente, id_lezione) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDate(1, Date.valueOf(p.getDataRichiesta()));
            stmt.setString(2, p.getStato());
            stmt.setInt(3, p.getCliente().getId());
            stmt.setInt(4, p.getLezionePrenotata().getIdLezione());
            stmt.executeUpdate();

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

            stmt.setString(1, p.getStato());
            stmt.setInt(2, p.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.severe("Errore aggiornamento stato prenotazione: " + e.getMessage());
        }
    }

    @Override
    public List<Prenotazione> trovaPerCliente(Cliente c) {
        List<Prenotazione> lista = new ArrayList<>();
        String query = "SELECT id, data_richiesta, tipologia, stato FROM prenotazioni WHERE id_cliente = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, c.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prenotazione p = new Prenotazione(
                            rs.getInt("id"),
                            rs.getDate("data_richiesta").toLocalDate(),
                            TipoAttivita.valueOf(rs.getString("tipologia")),
                            c
                    );
                    p.setStato(rs.getString("stato"));
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
        String query = "SELECT id, id_cliente, id_lezione, data_richiesta, tipologia, stato FROM prenotazioni WHERE id = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idCliente = rs.getInt("id_cliente");
                    int idLezione = rs.getInt("id_lezione");

                    // FIX: i dati caricati vengono ora effettivamente usati
                    Cliente cliente = DAOFactory.getInstance().getClienteDAO().trovaPerId(idCliente);
                    Lezione lezione = DAOFactory.getInstance().getLezioneDAO().trovaPerId(idLezione);

                    prenotazione = new Prenotazione(
                            rs.getInt("id"),
                            rs.getDate("data_richiesta").toLocalDate(),
                            TipoAttivita.valueOf(rs.getString("tipologia")),
                            cliente   // FIX: passato cliente reale invece di null
                    );
                    prenotazione.setStato(rs.getString("stato"));
                    prenotazione.setLezionePrenotata(lezione); // FIX: lezione collegata
                }
            }
        } catch (SQLException e) {
            logger.severe("Errore nel recupero della prenotazione tramite ID: " + e.getMessage());
        }
        return prenotazione;
    }

    @Override
    public boolean esisteGia(int idCliente, int idLezione) {
        // Conta quante righe esistono con questo cliente e questa lezione
        String query = "SELECT COUNT(*) FROM prenotazioni WHERE id_cliente = ? AND id_lezione = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idCliente);
            stmt.setInt(2, idLezione);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int conteggio = rs.getInt(1);
                    return conteggio > 0; // Se è maggiore di 0, significa che ha già prenotato!
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Errore durante il controllo doppioni: " + e.getMessage());
        }
        return false;
    }
}
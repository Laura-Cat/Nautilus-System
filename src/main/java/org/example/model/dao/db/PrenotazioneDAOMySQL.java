package org.example.model.dao.db;

import org.example.model.dao.DAOFactory;
import org.example.model.dao.Interface.PrenotazioneDAO;
import org.example.model.domain.Prenotazione;
import org.example.model.domain.Cliente;
import org.example.model.domain.Lezione;
import org.example.model.domain.TipoAttivita;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PrenotazioneDAOMySQL implements PrenotazioneDAO {
    private static final Logger logger = Logger.getLogger(PrenotazioneDAOMySQL.class.getName());

    @Override
    public void salva(Prenotazione p) {
        String query = "INSERT INTO prenotazioni (data_richiesta, stato, id_cliente, id_lezione, tipologia, note) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDate(1, Date.valueOf(p.getDataRichiesta()));
            stmt.setString(2, p.getStato());
            stmt.setInt(3, p.getCliente().getId());
            stmt.setInt(4, p.getLezionePrenotata().getIdLezione());
            stmt.setString(6, p.getNote());
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
    public List<Prenotazione> trovaAgendatiCliente(Integer idCliente, LocalDate dataInizio, LocalDate dataFine) {
        List<Prenotazione> lista = new ArrayList<>();
        String query = "SELECT id FROM prenotazioni " +
                "WHERE id_cliente = ? " +
                "  AND stato IN ('Confermata', 'Confermata e Pagata') " +
                "  AND data_richiesta BETWEEN ? AND ?"; // Nota: se usi la data della lezione, fai un JOIN con lezioni l ON id_lezione = l.id e filtra per l.data

        // Approccio più sicuro: uniamo direttamente la tabella lezioni per filtrare sulla data effettiva della lezione!
        String queryConJoin = "SELECT p.id FROM prenotazioni p " +
                "JOIN lezioni l ON p.id_lezione = l.id " +
                "WHERE p.id_cliente = ? " +
                "  AND p.stato IN ('Confermata', 'Confermata e Pagata') " +
                "  AND l.data BETWEEN ? AND ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(queryConJoin)) {

            stmt.setInt(1, idCliente);
            stmt.setDate(2, java.sql.Date.valueOf(dataInizio));
            stmt.setDate(3, java.sql.Date.valueOf(dataFine));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prenotazione p = trovaPerId(rs.getInt("id"));
                    if (p != null) {
                        lista.add(p);
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Errore nel recupero agenda cliente: " + e.getMessage());
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

                    String tipoStringa = rs.getString("tipologia"); // Usa il nome esatto della colonna nel tuo DB!
                    TipoAttivita tipoAttivita = null;

                    if (tipoStringa != null && !tipoStringa.isEmpty()) {
                        try {
                            tipoAttivita = TipoAttivita.valueOf(tipoStringa);
                        } catch (IllegalArgumentException e) {
                            logger.warning("Tipo attività non riconosciuto nel DB: " + tipoStringa);
                            tipoAttivita = TipoAttivita.PRIVATA; // Salvagente anche se la stringa è sbagliata
                        }
                    } else {
                        // Se è null nel DB, mettiamo un valore di default per non far crashare tutto
                        tipoAttivita = TipoAttivita.PRIVATA;
                    }

                    prenotazione = new Prenotazione(
                            rs.getInt("id"),
                            rs.getDate("data_richiesta").toLocalDate(),
                            tipoAttivita,
                            cliente
                    );

                    prenotazione.setStato(rs.getString("stato"));
                    prenotazione.setLezionePrenotata(lezione);
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
            System.out.println("Errore durante il controllo doppioni: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Prenotazione> trovaInAttesaPerIstruttore(Integer idIstruttore) {
        List<Prenotazione> lista = new ArrayList<>();
        // Uniamo prenotazioni e lezioni per filtrare in base all'istruttore
        String query = "SELECT p.id FROM prenotazioni p " +
                "JOIN lezioni l ON p.id_lezione = l.id " +
                "WHERE l.id_istruttore = ? AND p.stato = 'In Attesa'";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idIstruttore);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Sfruttiamo il metodo che hai già scritto per caricare tutto perfettamente!
                    Prenotazione p = trovaPerId(rs.getInt("id"));
                    if (p != null) {
                        lista.add(p);
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Errore recupero richieste per istruttore: " + e.getMessage());
        }
        return lista;
    }
}
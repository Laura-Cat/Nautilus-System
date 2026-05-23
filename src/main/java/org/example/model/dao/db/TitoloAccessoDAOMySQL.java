package org.example.model.dao.db;

import org.example.model.dao.Interface.TitoloAccessoDAO;
import org.example.model.domain.TipoCorso;
import org.example.model.domain.TitoloAccesso;
import org.example.model.domain.PacchettoCrediti;
import org.example.model.domain.AbbonamentoPeriodico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
                    titolo.setTitoloId(generatedKeys.getInt(1));
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
            stmt.setInt(2, pacchetto.getTitoloId());

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
            stmt.setInt(2, abbonamento.getTitoloId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.severe("Errore durante il rinnovo dell'abbonamento: " + e.getMessage());
        }
    }

    @Override
    public TitoloAccesso trovaPerCliente(Integer idCliente) {
        TitoloAccesso titolo = null;
        String query = "SELECT * FROM titoli_accesso WHERE id_cliente = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idTitolo = rs.getInt("id");
                    String tipo = rs.getString("tipo_titolo");
                    java.sql.Date dataInizio = rs.getDate("data_inizio");
                    java.sql.Date dataFine = rs.getDate("data_fine");

                    // POLIMORFISMO: Creiamo l'oggetto figlio corretto
                    if ("PACCHETTO_CREDITI".equals(tipo)) {
                        org.example.model.domain.PacchettoCrediti pacchetto = new org.example.model.domain.PacchettoCrediti();
                        pacchetto.setTitoloId(idTitolo);
                        pacchetto.setCreditiRimanenti(rs.getInt("crediti_rimanenti"));

                        titolo = pacchetto;

                    } else if ("ABBONAMENTO_PERIODICO".equals(tipo)) {
                        org.example.model.domain.AbbonamentoPeriodico abbonamento = new org.example.model.domain.AbbonamentoPeriodico();
                        abbonamento.setTitoloId(idTitolo);

                        if (dataInizio != null) abbonamento.setDataInizio(dataInizio.toLocalDate());
                        if (dataFine != null) abbonamento.setDataFine(dataFine.toLocalDate());

                        // NUOVA LOGICA: Carichiamo i corsi inclusi per questo abbonamento dal DB
                        List<TipoCorso> corsiInclusi = recuperoCorsiInclusiAbbonamento(idTitolo, conn);
                        abbonamento.setCorsiInclusi(corsiInclusi);

                        titolo = abbonamento;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Errore nel recupero del titolo d'accesso: " + e.getMessage());
        }

        return titolo;
    }

    // METODO DI SUPPORTO INTERNO (Sempre nel DAO) per leggere la tabella ponte dei corsi inclusi
    private List<TipoCorso> recuperoCorsiInclusiAbbonamento(int idTitolo, Connection conn) throws SQLException {
        List<TipoCorso> lista = new ArrayList<>();
        String queryCorsi = "SELECT tipo_corso FROM abbonamenti_corsi_inclusi WHERE id_titolo = ?";

        try (PreparedStatement stmtCorsi = conn.prepareStatement(queryCorsi)) {
            stmtCorsi.setInt(1, idTitolo);
            try (ResultSet rsCorsi = stmtCorsi.executeQuery()) {
                while (rsCorsi.next()) {
                    String nomeEnum = rsCorsi.getString("tipo_corso");
                    lista.add(TipoCorso.valueOf(nomeEnum));
                }
            }
        }
        return lista;
    }


}

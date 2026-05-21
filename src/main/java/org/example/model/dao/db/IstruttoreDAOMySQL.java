package org.example.model.dao.db;

import org.example.model.dao.Interface.IstruttoreDAO;

// L'implementazione si trova nella cartella "db
import org.example.model.domain.Istruttore;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IstruttoreDAOMySQL implements IstruttoreDAO{

    @Override
    public List<Istruttore> recuperaTutti() {
        List<Istruttore> lista = new ArrayList<>();

        // La query sulla tabella utenti filtrando chi ha la specializzazione
        String query = "SELECT * FROM utenti WHERE specializzazione IS NOT NULL";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Istruttore ist = new Istruttore();

                // Campi di User
                ist.setId(rs.getInt("id"));
                ist.setCf(rs.getString("cf"));
                ist.setNome(rs.getString("nome"));
                ist.setCognome(rs.getString("cognome"));
                if (rs.getDate("data_nascita") != null) {
                    ist.setDataNascita(rs.getDate("data_nascita").toLocalDate());
                }
                ist.setLuogoNascita(rs.getString("luogo_nascita"));
                ist.setIndirizzo(rs.getString("indirizzo"));
                ist.setEmail(rs.getString("email"));

                // Campi di Istruttore
                ist.setSpecializzazione(rs.getString("specializzazione"));
                ist.setDescrizione(rs.getString("descrizione"));
                ist.setFotoPath(rs.getString("foto_path"));

                lista.add(ist);
            }
        } catch (SQLException e) {
            System.out.println("❌ Errore nel recupero degli istruttori: " + e.getMessage());
        }

        return lista;
    }
}

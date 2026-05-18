package org.example.model.dao.db;

import org.example.model.dao.DAOFactory;
import org.example.model.dao.Interface.LoginDAO;
import org.example.model.domain.Ruolo;
import org.example.model.domain.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class LoginDAOMySQL implements LoginDAO {
    private static final Logger logger = Logger.getLogger(LoginDAOMySQL.class.getName());
    @Override
    public User trovaPerCredenziali(String email, String password) {
        User utente = null;

        // ATTENZIONE: Assicurati che la tabella si chiami 'users' o 'utenti' (come avevi scritto prima per il ClienteDAO)
        String query = "SELECT * FROM utenti WHERE email = ? AND password = ?";

        try (Connection conn = DBConnectionFactory.getInstance().createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int ruoloId = rs.getInt("id_ruolo");
                    Ruolo ruolo = Ruolo.fromInt(ruoloId);

                    // Prendiamo l'ID dell'utente appena trovato
                    int idUtente = rs.getInt("id"); // Metti il nome esatto della tua colonna ID

                    // ORA COSTRUIAMO L'UTENTE IN BASE AL RUOLO!
                    switch (ruolo) {
                        case CLIENTE:
                            // Usiamo il tuo fantastico ClienteDAO per "resuscitare" il cliente con tutti i suoi dati!
                            utente = DAOFactory.getInstance().getClienteDAO().trovaPerId(idUtente);
                            break;

                        case ISTRUTTORE:
                            // utente = DAOFactory.getInstance().getIstruttoreDAO().trovaPerId(idUtente);
                            break;

                        case AMMINISTRAZIONE:
                            // utente = DAOFactory.getInstance().getAmministratoreDAO().trovaPerId(idUtente);
                            break;

                        default:
                            throw new IllegalArgumentException("Errore: Ruolo utente non riconosciuto (" + ruolo + ")");
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Errore DAO: " + e.getMessage());
        }

        return utente; // Ora non sarà più null (se le credenziali sono giuste)!
    }
}
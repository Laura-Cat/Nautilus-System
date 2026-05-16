package org.example.model.dao;

import org.example.model.domain.Ruolo;

import java.io.InputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnectionFactory {
    private static final Logger logger = Logger.getLogger(DBConnectionFactory.class.getName());
    private static DBConnectionFactory instance;
    private  String url;
    private  String username;
    private  String password;
    private  Properties props;

    private DBConnectionFactory() {
        props = new Properties();

        // Questo è il modo corretto per leggere i file in un progetto Maven!
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {

            if (input == null) {
                logger.severe("Attenzione: Impossibile trovare il file db.properties in src/main/resources!");
                return;
            }

            props.load(input);
            url = props.getProperty("CONNECTION_URL");

            // Impostiamo l'utente di base all'avvio
            username = props.getProperty("LOGIN_USER");
            password = props.getProperty("LOGIN_PASS");

            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (IOException | ClassNotFoundException e) {
            // Logger sistemato per SonarCloud!
            logger.log(Level.SEVERE, "Errore di inizializzazione del Database", e);
        }
    }

    public static DBConnectionFactory getInstance() {
        if (instance == null) {
            instance = new DBConnectionFactory();
        }
        return instance;
    }

    // Ora leggiamo le password in modo sicuro dal file properties
    public void changeRole(Ruolo nuovoRuolo) {
        if (props == null) return;

        switch (nuovoRuolo) {
            case LOGIN:
                username = props.getProperty("LOGIN_USER");
                password = props.getProperty("LOGIN_PASS");
                break;
            case CLIENTE:
                username = props.getProperty("CLIENTE_USER");
                password = props.getProperty("CLIENTE_PASS");
                break;
            case ISTRUTTORE:
                username = props.getProperty("ISTRUTTORE_USER");
                password = props.getProperty("ISTRUTTORE_PASS");
                break;
            case AMMINISTRAZIONE:
                username = props.getProperty("AMMINISTRAZIONE_USER");
                password = props.getProperty("AMMINISTRAZIONE_PASS");
                break;
            default: // Aggiunto il default per togliere l'altro Code Smell!
                logger.warning("Ruolo non riconosciuto, impossibile cambiare credenziali.");
                break;
        }
    }

    public Connection createConnection() throws SQLException {
        if (url == null || username == null || password == null) {
            throw new SQLException("Credenziali del database mancanti o file non letto correttamente.");
        }
        return DriverManager.getConnection(url, username, password);
    }
}
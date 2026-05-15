package org.example.model.dao;

import org.example.model.domain.Ruolo;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnectionFactory {

    private static DBConnectionFactory instance;
    private static String url;
    private static String username;
    private static String password;
    private static Properties props;

    private DBConnectionFactory() {
        props = new Properties();
        try {
            // Assicurati che il file si chiami esattamente così e sia nella cartella root del progetto
            props.load(new FileInputStream("db.properties"));

            url = props.getProperty("CONNECTION_URL");
            username = props.getProperty("LOGIN_USER");
            password = props.getProperty("LOGIN_PASS");

            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore di inizializzazione o file properties non trovato: " + e.getMessage());
        }
    }

    public static DBConnectionFactory getInstance() {
        if (instance == null) {
            instance = new DBConnectionFactory();
        }
        return instance;
    }

    // Il metodo che cambia utente leggendo dal TUO file properties
    public static void changeRole(Ruolo nuovoRuolo) throws SQLException {
        if (props == null) return;

        switch (nuovoRuolo) {
            case LOGIN:
                username = "login_user";
                password = "login_pass";
                break;
            case CLIENTE:
                username = "cliente_user";
                password = "cliente_pass";
                break;
            case ISTRUTTORE:
                username = "istruttore_user";
                password = "istruttore_pass";
                break;
            case AMMINISTRAZIONE:
                username = "admin_user";
                password = "admin_pass";
                break;
        }
    }

    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}

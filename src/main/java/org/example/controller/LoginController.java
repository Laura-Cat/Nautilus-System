package org.example.controller;

import org.example.model.dao.DAOFactory;
import org.example.model.dao.DBConnectionFactory;
import org.example.model.dao.LoginDAO;
import org.example.model.domain.Cliente;
import org.example.model.domain.Istruttore;
import org.example.model.domain.Ruolo;
import org.example.model.domain.User;
import org.example.model.dto.LoginDTO;

import java.sql.SQLException;
import java.util.logging.Logger;

public class LoginController {
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());
    private static  LoginController instance;
    private User utenteAttivo;
    private  LoginController() {
        this.utenteAttivo = null;
    }

    // utilizzo la parola chiave synchronized per rendere il Singleton thread-safe
    public static synchronized LoginController getInstance() {
        if (instance == null) {
            instance = new  LoginController();
        }
        return instance;
    }

    // Metodi di business
    public Boolean autenticaUtente(LoginDTO credenziali) {
        logger.info("Cerco nel DB: Email=" + credenziali.email() + " | Pass=" + credenziali.password());
        // Chiediamo alla Factory il DAO corretto in base alla configurazione
        LoginDAO dao = DAOFactory.getInstance().getLoginDAO();
        User utenteTrovato = dao.trovaPerCredenziali(credenziali.email(), credenziali.password());
        logger.info("Risultato dal DAO: " + utenteTrovato);
        if (utenteTrovato == null) {
            return false;
        }

        this.utenteAttivo = utenteTrovato;

        try {
            if (utenteTrovato instanceof Istruttore) {
                DBConnectionFactory.changeRole(Ruolo.ISTRUTTORE);
            } else if (utenteTrovato instanceof Cliente) {
                DBConnectionFactory.changeRole(Ruolo.CLIENTE);
            } else {
                DBConnectionFactory.changeRole(Ruolo.AMMINISTRAZIONE);
            }
        } catch (SQLException e) {
            System.err.println("Errore DB durante il cambio ruolo: " + e.getMessage());
            this.utenteAttivo = null;
            return false;
        }
        return true;
    }

    public void effettuaLogout() {
        this.utenteAttivo = null;

        // Ripristiniamo la connessione "base" per permettere ad altri di loggarsi
        try {
            DBConnectionFactory.changeRole(Ruolo.LOGIN);
        } catch (SQLException e) {
            System.err.println("Errore DB durante il logout: " + e.getMessage());
        }
    }

    // Getter
    public User getUtenteAttivo() {
        return utenteAttivo;
    }


}

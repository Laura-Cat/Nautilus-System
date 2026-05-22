package org.example.controller;

import org.example.model.dao.DAOFactory;
import org.example.model.dao.db.DBConnectionFactory;
import org.example.model.dao.Interface.LoginDAO;
import org.example.model.domain.Cliente;
import org.example.model.domain.Istruttore;
import org.example.model.domain.Ruolo;
import org.example.model.domain.User;
import org.example.model.bean.LoginBean;
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
    public Boolean autenticaUtente(LoginBean credenziali) {
        logger.info("Cerco nel DB: Email=" + credenziali.email() + " | Pass=" + credenziali.password());

        // Chiediamo alla Factory il DAO corretto in base alla configurazione
        LoginDAO dao = DAOFactory.getInstance().getLoginDAO();
        User utenteTrovato = dao.trovaPerCredenziali(credenziali.email(), credenziali.password());

        logger.info("Risultato dal DAO: " + utenteTrovato);

        if (utenteTrovato == null) {
            return false;
        }

        this.utenteAttivo = utenteTrovato;
        if (utenteTrovato instanceof Istruttore) {
            DBConnectionFactory.getInstance().changeRole(Ruolo.ISTRUTTORE);
        } else if (utenteTrovato instanceof Cliente) {
            DBConnectionFactory.getInstance().changeRole(Ruolo.CLIENTE);
        } else {
            DBConnectionFactory.getInstance().changeRole(Ruolo.AMMINISTRAZIONE);
        }

        return true;
    }

    public void effettuaLogout() {
        this.utenteAttivo = null;
        DBConnectionFactory.getInstance().changeRole(Ruolo.LOGIN);
    }

    // Getter
    public User getUtenteAttivo() {
        return utenteAttivo;
    }


}



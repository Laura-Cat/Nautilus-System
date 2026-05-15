package org.example.model.dao;

public class DAOFactory {
    private static DAOFactory instance;
    private PersistenceType tipoCorrente;

    public enum PersistenceType { DATABASE, FILE }

    private DAOFactory() {
        this.tipoCorrente = PersistenceType.DATABASE; // Default
    }

    public static synchronized DAOFactory getInstance() {
        if (instance == null) instance = new DAOFactory();
        return instance;
    }

    public void setPersistenceType(PersistenceType tipo) {
        this.tipoCorrente = tipo;
    }

    public LoginDAO getLoginDAO() {
        if (tipoCorrente == PersistenceType.FILE) {
            // return new LoginDAOFile(); // Implementazione su file
            return null;
        }
        return new LoginDAOMySQL(); // Implementazione su DB
    }

    public NotificaDAO getNotificaDAO() {
        if (tipoCorrente == PersistenceType.FILE) {
            return null; // O ritorni un ipotetico NotificaDAOFile
        }
        return new NotificaDAOMySQL();
    }

    public PrenotazioneDAO getPrenotazioneDAO() {
        if (tipoCorrente == PersistenceType.FILE) {
            return null; // Eventuale PrenotazioneDAOFile
        }
        return new PrenotazioneDAOMySQL();
    }

    public TitoloAccessoDAO getTitoloAccessoDAO() {
        if (tipoCorrente == PersistenceType.FILE) {
            return null; // O la versione File in futuro
        }
        return new TitoloAccessoDAOMySQL();
    }

    public LezioneDAO getLezioneDAO() {
        if (tipoCorrente == PersistenceType.FILE) {
            return null; // Eventuale LezioneDAOFile futuro
        }
        return new LezioneDAOMySQL();
    }

    public CorsoDAO getCorsoDAO() {
        if (tipoCorrente == PersistenceType.FILE) {
            return null; // Eventuale CorsoDAOFile futuro
        }
        return new CorsoDAOMySQL();
    }

    public ClienteDAO getClienteDAO() {
        if (tipoCorrente == PersistenceType.FILE) {
            return null; // O un eventuale ClienteDAOFile in futuro
        }
        return new ClienteDAOMySQL(); // Implementazione su DB
    }

}

package org.example.controller;

import org.example.model.domain.User;

public class SessionManager {
    private static SessionManager instance;
    private User utenteAttivo;

    private SessionManager() {
        this.utenteAttivo = null;
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getUtenteAttivo() {
        return utenteAttivo;
    }

    public void setUtenteAttivo(User utente) {
        this.utenteAttivo = utente;
    }

    public void chiudiSessione() {
        this.utenteAttivo = null;
    }
}
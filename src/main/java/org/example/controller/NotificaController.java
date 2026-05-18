package org.example.controller;

import org.example.model.dao.DAOFactory;
import org.example.model.domain.Notifica;
import org.example.model.domain.User;

import java.util.List;
import java.util.logging.Logger;

public class NotificaController {
    private static final Logger logger = Logger.getLogger(NotificaController.class.getName());
    /**
     * Recupera tutte le notifiche non lette per un dato utente (Cliente o Istruttore).
     */
    public List<Notifica> recuperaNotificheNonLette(User utente) {
        return utente.getNotificheDaLeggere();
    }

    /**
     * Segna una notifica come letta.
     * In futuro, questo metodo comunicherà con il DAO per aggiornare il Database.
     */
    public void apriNotifica(Notifica notifica) {
        if (!notifica.isLetta()) {
            notifica.segnaComeLetta();
            logger.info("Notifica aperta e segnata come letta: " + notifica.getMessaggio());

            DAOFactory.getInstance().getNotificaDAO().aggiornaStato(notifica);
        }
    }

    /**
     * (Opzionale) Elimina una notifica letta
     */
    public void eliminaNotifica(User utente, Notifica notifica) {
        // Logica per rimuovere la notifica dalla lista dell'utente e dal DB
    }
}
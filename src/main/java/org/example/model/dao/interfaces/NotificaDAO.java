package org.example.model.dao.interfaces;

import org.example.model.domain.Notifica;
import org.example.model.domain.User;

import java.util.List;

public interface NotificaDAO {
    // 1. Salva una nuova notifica nel DB
    boolean invia(Notifica notifica, Integer idDestinatario);

    // 2. Legge dal DB tutte le notifiche non lette di una persona
    List<Notifica> recuperaNonLettePerUtente(User utente, Integer idUtente);
    List<Notifica> recuperaTuttePerUtente(User utente, Integer idDestinatario);

    boolean aggiornaStato(Notifica notifica);

}
package org.example.model.dao;

import org.example.model.domain.Notifica;
import org.example.model.domain.User;

import java.util.List;

public interface NotificaDAO {
    // 1. Salva una nuova notifica nel DB
    void inserisci(Notifica notifica, Integer idDestinatario);

    // 2. Legge dal DB tutte le notifiche non lette di una persona
    List<Notifica> recuperaNonLettePerUtente(User utente, Integer idUtente);

    // 3. Aggiorna il DB quando l'utente legge il messaggio
    void segnaComeLetta(Notifica notifica);
}
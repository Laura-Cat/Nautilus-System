package org.example.controller;

import org.example.model.dao.DAOFactory;
import org.example.model.domain.*;

import java.util.List;


public class NotificaController {

    public boolean inviaRichiestaLezionePrivata(Cliente cliente, Istruttore istruttore, Lezione lezione, String noteCliente) {

        String livello = (noteCliente == null || noteCliente.trim().isEmpty()) ? "Nessuna nota sul livello fornita." : noteCliente;

        String corpoMessaggio = String.format(
                "Nuova richiesta Lezione Privata in attesa di accettazione:\nDa: %s %s\nData: %s - Ore: %s\nLivello/Note: %s",
                cliente.getNome(), cliente.getCognome(), lezione.getData().toString(), lezione.getOraInizio().toString(), livello
        );

        Notifica nuovaRichiesta = new Notifica();
        nuovaRichiesta.setMessaggio(corpoMessaggio);
        nuovaRichiesta.setLetta(false);
        // Assicurati che l'invio corrisponda al tuo DAO (es. passando l'ID destinatario)
        return DAOFactory.getInstance().getNotificaDAO().invia(nuovaRichiesta, istruttore.getId());
    }

    public List<Notifica> recuperaNonLettePerUtente(org.example.model.domain.User utente, Integer idUtente) {
        return org.example.model.dao.DAOFactory.getInstance().getNotificaDAO().recuperaNonLettePerUtente(utente, idUtente);
    }

}
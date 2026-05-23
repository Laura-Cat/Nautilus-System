package org.example.controller;

import org.example.model.dao.DAOFactory;
import org.example.model.domain.Cliente;
import org.example.model.domain.Istruttore;
import org.example.model.domain.Lezione;
import org.example.model.domain.Notifica;

import java.util.List;

public class NotificaController {

    public boolean inviaRichiestaLezionePrivata(Cliente cliente, Istruttore istruttore, Lezione lezione, String noteCliente, Integer idPrenotazione) {

        String livello = (noteCliente == null || noteCliente.trim().isEmpty()) ? "Nessuna nota sul livello fornita." : noteCliente;

        String corpoMessaggio = String.format(
                "Nuova richiesta Lezione Privata in attesa di accettazione:\nDa: %s %s\nData: %s - Ore: %s\nLivello/Note: %s",
                cliente.getNome(), cliente.getCognome(), lezione.getData().toString(), lezione.getOraInizio().toString(), livello
        );

        Notifica nuovaRichiesta = new Notifica();
        nuovaRichiesta.setMessaggio(corpoMessaggio);
        nuovaRichiesta.setLetta(false);

        nuovaRichiesta.setTipo("DA_ACCETTARE");
        nuovaRichiesta.setIdRiferimento(idPrenotazione);
        // Assicurati che l'invio corrisponda al tuo DAO (es. passando l'ID destinatario)
        return DAOFactory.getInstance().getNotificaDAO().invia(nuovaRichiesta, istruttore.getId());
    }

    public List<Notifica> recuperaNonLettePerUtente(org.example.model.domain.User utente, Integer idUtente) {
        return DAOFactory.getInstance().getNotificaDAO().recuperaNonLettePerUtente(utente, idUtente);
    }

    public boolean aggiornaNotifica(Notifica notifica) {
        return DAOFactory.getInstance().getNotificaDAO().aggiornaStato(notifica);
    }

    public List<Notifica> recuperaTuttePerUtente(org.example.model.domain.User utente, Integer idUtente) {
        return DAOFactory.getInstance().getNotificaDAO().recuperaTuttePerUtente(utente, idUtente);
    }

}
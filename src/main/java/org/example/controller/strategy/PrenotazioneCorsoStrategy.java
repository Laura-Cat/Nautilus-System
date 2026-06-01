package org.example.controller.strategy;

import org.example.exception.CreditiInsufficientiException;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.Cliente;
import org.example.model.domain.Lezione;
import org.example.model.domain.TitoloAccesso;

import java.util.logging.Logger;

public class PrenotazioneCorsoStrategy implements PrenotazioneStrategy {

    private static final Logger logger = Logger.getLogger(PrenotazioneCorsoStrategy.class.getName());

    @Override
    public boolean eseguiPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {
        // Prendiamo il titolo dalla memoria
        TitoloAccesso titolo = cliente.getTitoloAccesso();


        if (titolo == null) {
            logger.info(() ->"Titolo non presente in memoria. Ricerca nel DB in corso per il cliente ID: " + cliente.getId());
            titolo = DAOFactory.getInstance().getTitoloAccessoDAO().trovaPerCliente(cliente.getId());
            cliente.setTitoloAccesso(titolo);
        }

        if (titolo == null) {
            logger.warning(() ->"Fallimento prenotazione: Nessun titolo di accesso trovato per il cliente ID: " + cliente.getId());
            throw new CreditiInsufficientiException("Non hai nessun titolo di accesso valido!");
        }

        if (!(titolo instanceof org.example.model.domain.AbbonamentoPeriodico)) {
            logger.warning("Fallimento prenotazione: Il titolo posseduto non è un Abbonamento Periodico.");
            throw new CreditiInsufficientiException("Necessario Abbonamento Periodico per i corsi!");
        }

        org.example.model.domain.AbbonamentoPeriodico abbonamento = (org.example.model.domain.AbbonamentoPeriodico) titolo;

        // Controllo date di validità
        if (abbonamento.getDataFine() != null && abbonamento.getDataFine().isBefore(java.time.LocalDate.now())) {
            logger.info(() ->"Fallimento prenotazione: L'abbonamento del cliente ID " + cliente.getId() + " è scaduto in data " + abbonamento.getDataFine());
            throw new CreditiInsufficientiException("Il tuo abbonamento è scaduto!");
        }

        // Controllo se il corso è tra quelli inclusi nel suo abbonamento
        if (abbonamento.getCorsiInclusi() == null || !abbonamento.getCorsiInclusi().contains(lezione.getCorsoAppartenenza().getNome())) {
            logger.info(() -> "Fallimento prenotazione: Il corso " + lezione.getCorsoAppartenenza().getNome() + " non è incluso nell'abbonamento.");
            throw new CreditiInsufficientiException("Questo specifico corso non è incluso nel tuo abbonamento!");
        }

        // Tutto perfetto
        logger.info(() ->"Controlli di business superati con successo per il cliente ID: " + cliente.getId() + ". Autorizzazione alla prenotazione concessa.");
        return true;
    }
}
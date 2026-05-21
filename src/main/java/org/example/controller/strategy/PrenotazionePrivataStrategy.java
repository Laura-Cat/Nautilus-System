package org.example.controller.strategy;

import org.example.exception.CreditiInsufficientiException;
import org.example.model.domain.Cliente;
import org.example.model.domain.Lezione;

import java.util.logging.Logger;

public class PrenotazionePrivataStrategy implements PrenotazioneStrategy {
    private static final Logger logger = Logger.getLogger(PrenotazionePrivataStrategy.class.getName());

    @Override
    public boolean eseguiPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {

        if (lezione.getNumPostiPrenotati() >= 1) {
            logger.warning("Attenzione: La lezione privata selezionata è già stata occupata.");
            return false;
        }

        logger.info("Controlli Strategy superati: Lezione libera, in attesa di accettazione istruttore.");
        return true;
    }
}
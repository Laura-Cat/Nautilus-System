package org.example.controller.strategy;

import org.example.controller.LoginController;
import org.example.exception.CreditiInsufficientiException;
import org.example.model.domain.Cliente;
import org.example.model.domain.Lezione;
import org.example.model.domain.TitoloAccesso;

import java.util.logging.Logger;

public class PrenotazioneCorsoStrategy implements StrategiaPrenotazione {
    private static final Logger logger = Logger.getLogger(PrenotazioneCorsoStrategy.class.getName());
    private static final int COSTO_CORSO = 3;

    @Override
    public boolean eseguiPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {
        TitoloAccesso titolo = cliente.getTitoloAccesso();

        // 1. Controllo Crediti con Eccezione
        if (titolo == null || !titolo.checkValidita(COSTO_CORSO)) {
            throw new CreditiInsufficientiException("Crediti insufficienti per prenotare questo corso. (Richiesti: " + COSTO_CORSO + ")");
        }

        // 2. Controllo Posti (restituisce false perché non è un errore dell'utente, è solo pieno)
        if (lezione.getPostiDisponibili() <= 0) {
            System.err.println("Errore: Il corso è al completo.");
            return false;
        }

        // 3. Conferma ed esecuzione immediata (Sincrona)
        lezione.setNumPostiPrenotati(lezione.getNumPostiPrenotati() + 1);
        titolo.registraAccesso(COSTO_CORSO); // Paga subito!

        logger.info("Prenotazione al Corso confermata con successo!");
        return true;
    }
}
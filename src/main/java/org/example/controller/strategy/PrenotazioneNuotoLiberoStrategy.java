package org.example.controller.strategy;

import org.example.exception.CreditiInsufficientiException;
import org.example.model.domain.Cliente;
import org.example.model.domain.Lezione;
import org.example.model.domain.TitoloAccesso;

import java.util.logging.Logger;

public class PrenotazioneNuotoLiberoStrategy implements StrategiaPrenotazione {
    private static final Logger logger = Logger.getLogger(PrenotazioneNuotoLiberoStrategy .class.getName());
    private static final int COSTO_NUOTO_LIBERO = 1;

    @Override
    public boolean eseguiPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {
        TitoloAccesso titolo = cliente.getTitoloAccesso();

        // 1. Controllo Crediti con l'Eccezione
        if (titolo == null || !titolo.checkValidita(COSTO_NUOTO_LIBERO)) {
            // Invece di restituire false, "urliamo" l'errore alla View!
            throw new CreditiInsufficientiException(
                    "Crediti insufficienti per accedere al Nuoto Libero. (Richiesti: " + COSTO_NUOTO_LIBERO + ")"
            );
        }

        // 2. Controllo Posti Fisici in Corsia
        // Se non c'è posto non è un "errore dell'utente", quindi restituiamo semplicemente false
        if (lezione.getPostiDisponibili() <= 0) {
            System.err.println("Errore: La corsia per il nuoto libero è attualmente al completo.");
            return false;
        }

        // 3. Conferma ed esecuzione immediata (Sincrona)
        lezione.setNumPostiPrenotati(lezione.getNumPostiPrenotati() + 1);
        titolo.registraAccesso(COSTO_NUOTO_LIBERO); // Scala 1 credito

        logger.info("Prenotazione per Nuoto Libero confermata con successo!");
        return true;
    }
}
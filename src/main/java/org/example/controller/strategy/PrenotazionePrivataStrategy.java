package org.example.controller.strategy;

import org.example.exception.CreditiInsufficientiException;
import org.example.model.domain.Cliente;
import org.example.model.domain.Lezione;
import org.example.model.domain.Prenotazione;
import org.example.model.domain.TitoloAccesso;

import java.time.LocalDate;

public class PrenotazionePrivataStrategy implements StrategiaPrenotazione {

    private static final int COSTO_PRIVATA = 10;


    @Override
    public boolean eseguiPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {
        TitoloAccesso titolo = cliente.getTitoloAccesso();

            // 1. Controllo preventivo crediti (Lancia l'eccezione se non bastano)
        if (titolo == null || !titolo.checkValidita(COSTO_PRIVATA)) {
                throw new CreditiInsufficientiException("Crediti insufficienti (" + COSTO_PRIVATA + ") per richiedere una lezione privata.");
        }

            // 2. Controllo posti fisici (massimo 1)
        if (lezione.getNumPostiPrenotati() >= 1) {
                System.err.println("Errore: Lezione privata già occupata o richiesta in corso.");
                return false; // Fallimento logico, ma non è un'eccezione dei crediti
        }

            // 3. Creiamo la prenotazione "congelata"
        Prenotazione p = new Prenotazione(null, LocalDate.now(), "PRIVATA", cliente);
        p.setLezionePrenotata(lezione);
        p.setCorsiaPrenotata(lezione.getCorsiaAssegnata());

        // 4. Cambiamo stato e blocchiamo il posto
        p.attesaAccettazione();
        lezione.setNumPostiPrenotati(1);

        return true; // Tutto ok! Passiamo la palla al Controller.
    }
}
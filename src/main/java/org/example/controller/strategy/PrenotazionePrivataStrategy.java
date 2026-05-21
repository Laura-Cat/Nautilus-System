package org.example.controller.strategy;

import org.example.exception.CreditiInsufficientiException;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.*;

import java.time.LocalDate;
import java.util.logging.Logger;

public class PrenotazionePrivataStrategy implements PrenotazioneStrategy {
    private static final Logger logger = Logger.getLogger(PrenotazionePrivataStrategy.class.getName());
    private static final int COSTO_PRIVATA = 10;


    @Override
    public boolean eseguiPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {
        TitoloAccesso titolo = cliente.getTitoloAccesso();

        // 1. Controllo preventivo crediti
        if (titolo == null || !titolo.checkValidita(COSTO_PRIVATA)) {
            throw new CreditiInsufficientiException(
                    "Crediti insufficienti (" + COSTO_PRIVATA + ") per richiedere una lezione privata."
            );
        }

        // 2. Controllo posti fisici (massimo 1 per lezione privata)
        if (lezione.getNumPostiPrenotati() >= 1) {
            logger.severe("Errore: Lezione privata già occupata o richiesta in corso.");
            return false;
        }

        // 3. Creiamo la prenotazione "congelata" in attesa di accettazione
        Prenotazione p = new Prenotazione(null, LocalDate.now(), TipoAttivita.PRIVATA, cliente);
        p.setLezionePrenotata(lezione);
        p.setCorsiaPrenotata(lezione.getCorsiaAssegnata());
        p.attesaAccettazione();

        // 4. Blocchiamo il posto fisico sulla lezione
        lezione.setNumPostiPrenotati(1);

        // FIX: salviamo la prenotazione nel DB e aggiorniamo i posti
        DAOFactory.getInstance().getPrenotazioneDAO().salva(p);
        DAOFactory.getInstance().getLezioneDAO().aggiornaPostiOccupati(lezione);

        logger.info("Prenotazione privata in attesa salvata nel DB con ID: " + p.getId());
        return true;
    }
}
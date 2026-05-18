package org.example.controller.strategy;

import org.example.exception.CreditiInsufficientiException;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.*;

import java.time.LocalDate;
import java.util.logging.Logger;

public class PrenotazioneNuotoLiberoStrategy implements StrategiaPrenotazione {
    private static final Logger logger = Logger.getLogger(PrenotazioneNuotoLiberoStrategy .class.getName());
    private static final int COSTO_NUOTO_LIBERO = 1;

    @Override
    public boolean eseguiPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {
        // 1. Logica di business del Nuoto Libero
        TitoloAccesso titolo = cliente.getTitoloAccesso();
        if (titolo == null || !titolo.checkValidita(1)) throw new CreditiInsufficientiException("...");
        if (lezione.getPostiDisponibili() <= 0) return false;

        // 2. Modifiche in memoria
        lezione.setNumPostiPrenotati(lezione.getNumPostiPrenotati() + 1);
        titolo.registraAccesso(1);

        // 3. Salvataggio specifico per il Nuoto Libero
        try {
            Prenotazione p = new Prenotazione(null, LocalDate.now(), TipoAttivita.NUOTO_LIBERO, cliente);
            p.setLezionePrenotata(lezione);
            p.conferma(); // Il nuoto libero è confermato subito!

            DAOFactory.getInstance().getPrenotazioneDAO().salva(p);
            DAOFactory.getInstance().getLezioneDAO().aggiornaPostiOccupati(lezione);
            DAOFactory.getInstance().getTitoloAccessoDAO().aggiornaCrediti((PacchettoCrediti) titolo);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

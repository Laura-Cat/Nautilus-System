package org.example.controller.strategy; // Verifica che il package sia corretto per te

import org.example.exception.CreditiInsufficientiException;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.*;

import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrenotazioneNuotoLiberoStrategy implements PrenotazioneStrategy {

    private static final Logger logger = Logger.getLogger(PrenotazioneNuotoLiberoStrategy.class.getName());
    private static final int COSTO_NUOTO_LIBERO = 1;

    @Override
    public boolean eseguiPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {
        logger.info(() -> "Avvio prenotazione Nuoto Libero per cliente ID: " + cliente.getId());

        // 1. Recupero titolo dal DB se assente in memoria
        if (cliente.getTitoloAccesso() == null) {
            logger.info("Titolo in memoria vuoto. Recupero dal DB...");
            try {
                TitoloAccesso titoloDb = DAOFactory.getInstance().getTitoloAccessoDAO().trovaPerCliente(cliente.getId());
                cliente.setTitoloAccesso(titoloDb);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Errore nel recupero del titolo di accesso dal DB", e);
                return false;
            }
        }

        TitoloAccesso titolo = cliente.getTitoloAccesso();

        // 2. Controllo Validità Titolo
        if (titolo == null || !titolo.checkValidita(COSTO_NUOTO_LIBERO)) {
            logger.warning("BLOCCO: Crediti o abbonamento non sufficienti per il Nuoto Libero.");
            throw new CreditiInsufficientiException("Crediti non sufficienti o abbonamento non valido.");
        }

        // 3. LA PARTE MANCANTE: Creazione della Prenotazione e aggiornamento DB
        try {
            logger.info("Controlli superati. Generazione della prenotazione in corso...");

            Prenotazione prenotazione = new Prenotazione();
            prenotazione.setCliente(cliente);
            prenotazione.setLezionePrenotata(lezione);
            prenotazione.setDataRichiesta(LocalDate.now());
            prenotazione.setStato("Confermata"); // Il nuoto libero è subito confermato

            DAOFactory.getInstance().getPrenotazioneDAO().salva(prenotazione);
            if (titolo instanceof PacchettoCrediti) {
                PacchettoCrediti pacchetto = (PacchettoCrediti) titolo;

                pacchetto.setCreditiRimanenti(pacchetto.getCreditiRimanenti() - COSTO_NUOTO_LIBERO);
                DAOFactory.getInstance().getTitoloAccessoDAO().aggiornaCrediti(pacchetto);

                logger.info(() -> "✅ Prenotazione completata! Crediti residui aggiornati nel DB: " + pacchetto.getCreditiRimanenti());
            } else {
                logger.info("✅ Prenotazione completata tramite Abbonamento (ingressi illimitati).");
            }
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Eccezione durante il salvataggio della prenotazione", e);
            return false;
        }
    }
}
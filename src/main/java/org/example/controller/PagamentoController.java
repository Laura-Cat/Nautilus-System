package org.example.controller;

import org.example.model.dao.DAOFactory;
import org.example.model.domain.AbbonamentoPeriodico;
import org.example.model.domain.Cliente;
import org.example.model.domain.PacchettoCrediti;
import org.example.model.domain.TitoloAccesso;

import java.time.LocalDate;
import java.util.logging.Logger;

public class PagamentoController {
    private static final Logger logger = Logger.getLogger(PagamentoController.class.getName());
    // ==========================================================
    // 1. ACQUISTO CREDITI
    // ==========================================================
    public boolean acquistaCrediti(Cliente cliente, int creditiDaAggiungere, String metodoPagamento) {
        TitoloAccesso titolo = cliente.getTitoloAccesso();

        if (titolo instanceof PacchettoCrediti) {
            PacchettoCrediti crediti = (PacchettoCrediti) titolo;
            crediti.aggiungiCrediti(creditiDaAggiungere);

            // Aggiorna il numero di crediti nel database
            DAOFactory.getInstance().getTitoloAccessoDAO().aggiornaCrediti(crediti);
            // --------------------

            logger.info("Aggiunti " + creditiDaAggiungere + " crediti. Pagamento con " + metodoPagamento);
            return true;
        } else {
            logger.severe("Errore: Il cliente non possiede una tessera a crediti su cui fare la ricarica.");
            return false;
        }
    }

    // ==========================================================
    // 2. RINNOVO / ACQUISTO ABBONAMENTO PERIODICO
    // ==========================================================
    public boolean compraNuovoAbbonamento(Cliente cliente, int mesiValidita, String metodoPagamento) {
        if (elaboraTransazione(metodoPagamento, mesiValidita * 30)) {

            // 1. CALCOLIAMO LE DATE
            LocalDate dataInizio = LocalDate.now();
            LocalDate dataFine = dataInizio.plusMonths(mesiValidita);

            // 2. CREIAMO IL NUOVO OGGETTO (Passiamo null come ID, lo creerà poi il Database)
            AbbonamentoPeriodico nuovoAbbonamento = new AbbonamentoPeriodico(null, dataInizio, dataFine);

            // 3. LO ASSEGNIAMO AL CLIENTE
            cliente.setTitoloAccesso(nuovoAbbonamento);
            DAOFactory.getInstance().getTitoloAccessoDAO().salvaNuovo(nuovoAbbonamento, cliente.getClienteID());

            logger.info("Acquisto completato! Nuovo Abbonamento mensile assegnato a " + cliente.getNome());
            return true;
        }
        return false;
    }

    public boolean rinnovaAbbonamento(Cliente cliente, int mesiDaAggiungere, String metodoPagamento) {
        TitoloAccesso titolo = cliente.getTitoloAccesso();

        // Controlliamo prima se il cliente ha l'abbonamento giusto
        if (titolo instanceof AbbonamentoPeriodico) {

            // Facciamo subito il casting per comodità, così non impazziamo con le parentesi
            AbbonamentoPeriodico abbonamento = (AbbonamentoPeriodico) titolo;

            // Tentiamo il pagamento
            if (elaboraTransazione(metodoPagamento, mesiDaAggiungere * 30)) {

                // 1. Rinnoviamo l'oggetto Java
                abbonamento.rinnova(mesiDaAggiungere);

                // 2. Salviamo nel database passandogli l'oggetto forzato (abbonamento)
                DAOFactory.getInstance().getTitoloAccessoDAO().aggiornaRinnovo(abbonamento);

                logger.info("Abbonamento prolungato di " + mesiDaAggiungere + " mesi.");
                return true;
            } else {
                logger.severe("Errore: Transazione rifiutata.");
                return false;
            }

        } else {
            logger.severe("Errore: Il cliente non ha un abbonamento periodico da rinnovare.");
            return false;
        }
    }

    private boolean elaboraTransazione(String metodoPagamento, int importo) {
        logger.info("Contattando il sistema per il pagamento di " + importo + " euro tramite " + metodoPagamento + "...");
        return true;
    }


}

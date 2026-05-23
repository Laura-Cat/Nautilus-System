package org.example.controller;

import org.example.controller.strategy.MetodoPagamentoStrategy;
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
    // IL MOTORE DEL PATTERN STRATEGY (Risolve il primo errore)
    // ==========================================================
    private MetodoPagamentoStrategy strategiaPagamento;

    public void setStrategiaPagamento(MetodoPagamentoStrategy strategia) {
        this.strategiaPagamento = strategia;
    }

    private boolean elaboraTransazione(double importo) {
        if (strategiaPagamento == null) {
            logger.severe("Errore: Nessun metodo di pagamento selezionato!");
            return false;
        }

        logger.info(() ->"Avvio transazione di €" + importo + " tramite " + strategiaPagamento.getNomePiattaforma());
        return strategiaPagamento.processaPagamento(importo);
    }

    // ==========================================================
    // PAGAMENTO LEZIONE PRIVATA (Risolve il secondo errore)
    // ==========================================================
    public boolean pagaLezionePrivata(int idPrenotazione, double costo, org.example.model.domain.Notifica notifica) {
        if (elaboraTransazione(costo)) {

            // 1. Recuperiamo la prenotazione dal DB e la segniamo come pagata
            org.example.model.domain.Prenotazione p = DAOFactory.getInstance().getPrenotazioneDAO().trovaPerId(idPrenotazione);
            if (p != null) {
                p.pagata(); // Mette lo stato "Confermata e Pagata"
                DAOFactory.getInstance().getPrenotazioneDAO().aggiornaStato(p);
            }

            // 2. Disinneschiamo la notifica
            notifica.setTipo("PAGAMENTO_COMPLETATO");
            notifica.setMessaggio("✅ Pagamento di €" + costo + " completato. La tua lezione è confermata!");
            notifica.setLetta(true);

            DAOFactory.getInstance().getNotificaDAO().aggiornaStato(notifica);

            logger.info(() ->"Prenotazione " + idPrenotazione + " pagata con successo e notifica disinnescata!");
            return true;
        }
        return false;
    }

    // ==========================================================
    // I TUOI VECCHI METODI AGGIORNATI AL NUOVO SISTEMA
    // ==========================================================
    public boolean acquistaCrediti(Cliente cliente, int creditiDaAggiungere, double costoTotale) {
        TitoloAccesso titolo = cliente.getTitoloAccesso();

        if (titolo instanceof PacchettoCrediti) {
            if (elaboraTransazione(costoTotale)) {
                PacchettoCrediti crediti = (PacchettoCrediti) titolo;
                crediti.aggiungiCrediti(creditiDaAggiungere);

                DAOFactory.getInstance().getTitoloAccessoDAO().aggiornaCrediti(crediti);
                logger.info(() ->"Aggiunti " + creditiDaAggiungere + " crediti.");
                return true;
            }
        }
        return false;
    }

    public boolean compraNuovoAbbonamento(Cliente cliente, int mesiValidita, double costoTotale) {
        if (elaboraTransazione(costoTotale)) {
            LocalDate dataInizio = LocalDate.now();
            LocalDate dataFine = dataInizio.plusMonths(mesiValidita);

            AbbonamentoPeriodico nuovoAbbonamento = new AbbonamentoPeriodico(null, dataInizio, dataFine);
            cliente.setTitoloAccesso(nuovoAbbonamento);

            DAOFactory.getInstance().getTitoloAccessoDAO().salvaNuovo(nuovoAbbonamento, cliente.getId());
            logger.info(() ->"Nuovo Abbonamento mensile assegnato a " + cliente.getNome());
            return true;
        }
        return false;
    }

    public boolean rinnovaAbbonamento(Cliente cliente, int mesiDaAggiungere, double costoTotale) {
        TitoloAccesso titolo = cliente.getTitoloAccesso();

        if (titolo instanceof AbbonamentoPeriodico) {
            AbbonamentoPeriodico abbonamento = (AbbonamentoPeriodico) titolo;

            if (elaboraTransazione(costoTotale)) {
                abbonamento.rinnova(mesiDaAggiungere);
                DAOFactory.getInstance().getTitoloAccessoDAO().aggiornaRinnovo(abbonamento);
                logger.info(() ->"Abbonamento prolungato di " + mesiDaAggiungere + " mesi.");
                return true;
            }
        }
        return false;
    }
}
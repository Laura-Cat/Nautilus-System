package org.example.controller;

import org.example.controller.strategy.PrenotazioneCorsoStrategy;
import org.example.controller.strategy.PrenotazioneNuotoLiberoStrategy;
import org.example.controller.strategy.PrenotazionePrivataStrategy;
import org.example.controller.strategy.StrategiaPrenotazione;
import org.example.exception.CreditiInsufficientiException;
import org.example.model.dao.DAOFactory;
import org.example.model.dao.PrenotazioneDAO;
import org.example.model.domain.*;

import java.time.LocalDate;
import java.util.logging.Logger;

public class PrenotazioneController {
    private static final Logger logger = Logger.getLogger(PrenotazioneController.class.getName());
    private StrategiaPrenotazione strategiaCorrente;

    // ==========================================================
    // FASE 1: IL CLIENTE CHIEDE LA PRENOTAZIONE (O IL CORSO)
    // ==========================================================
    public boolean finalizzaPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {
        // ... (controlli base come certificato medico) ...

        impostaStrategiaCorretta(lezione);
        boolean successo = this.strategiaCorrente.eseguiPrenotazione(cliente, lezione);

        if (successo) {
            // --- NUOVO CODICE DAO ---
            // Se è andata bene, creiamo l'oggetto Prenotazione per il database
            Prenotazione p = new Prenotazione(null, LocalDate.now(), lezione.getTipoAttivita().toString(), cliente);
            p.setLezionePrenotata(lezione);

            // Se è una privata parte "In attesa", altrimenti è subito "Confermata"
            if (lezione.getTipoAttivita() == TipoAttivita.PRIVATA) {
                p.attesaAccettazione();
                inviaNotificaIstruttore(lezione, cliente);
            } else {
                TitoloAccesso titolo = cliente.getTitoloAccesso();
                if (titolo instanceof PacchettoCrediti) {
                    DAOFactory.getInstance().getTitoloAccessoDAO().aggiornaCrediti((PacchettoCrediti) titolo);
                }
                p.conferma();
            }

            // SALVATAGGIO FISICO SUL DB
            PrenotazioneDAO dao = DAOFactory.getInstance().getPrenotazioneDAO();
            dao.salva(p);

            DAOFactory.getInstance().getLezioneDAO().aggiornaPostiOccupati(lezione);
            // ------------------------
        }

        return successo;
    }

    private void impostaStrategiaCorretta(Lezione lezione) {
        switch (lezione.getTipoAttivita()) {
            case CORSO: this.strategiaCorrente = new PrenotazioneCorsoStrategy(); break;
            case NUOTO_LIBERO: this.strategiaCorrente = new PrenotazioneNuotoLiberoStrategy(); break;
            case PRIVATA: this.strategiaCorrente = new PrenotazionePrivataStrategy(); break;
        }
    }

    private void inviaNotificaIstruttore(Lezione lezione, Cliente cliente) {
        Istruttore istruttore = lezione.getIstruttore();
        String testo = "Il cliente " + cliente.getNome() + " ha richiesto una lezione privata per il " + lezione.getData();

        Notifica nuovaNotifica = new Notifica(testo, istruttore);
        istruttore.riceviNotifica(nuovaNotifica);
    }

    // ==========================================================
    // FASE 2: L'ISTRUTTORE RISPONDE (Solo per le Private)
    // ==========================================================
    public void gestisciRispostaIstruttore(Prenotazione prenotazione, boolean accetta) {
        if (!prenotazione.inAttesa()) {
            throw new IllegalStateException("Prenotazione già gestita.");
        }

        // Recuperiamo il cliente da notificare
        Cliente clienteRichiedente = prenotazione.getCliente();

        if (accetta) {
            prenotazione.accettataDaIstruttore();
            logger.info("Sistema: L'istruttore ha accettato. Cliente pronto per pagare.");

            // Creiamo e inviamo la notifica al CLIENTE (Successo)
            String testoOk = "L'istruttore ha accettato la tua richiesta per la lezione del " + prenotazione.getLezionePrenotata().getData() + ". Puoi procedere al pagamento.";
            clienteRichiedente.riceviNotifica(new Notifica(testoOk, clienteRichiedente));

        } else {
            prenotazione.rifiuta();

            // Liberiamo il posto fisicamente nella lezione
            Lezione lezione = prenotazione.getLezionePrenotata();
            lezione.setNumPostiPrenotati(lezione.getNumPostiPrenotati() - 1);
            logger.info("Sistema: L'istruttore ha rifiutato. Posto liberato.");

            // Creiamo e inviamo la notifica al CLIENTE (Rifiuto)
            String testoNo = "Ci dispiace, l'istruttore ha rifiutato la lezione privata per il " + prenotazione.getLezionePrenotata().getData() + ".";
            clienteRichiedente.riceviNotifica(new Notifica(testoNo, clienteRichiedente));
        }

        PrenotazioneDAO dao = DAOFactory.getInstance().getPrenotazioneDAO();
        dao.aggiornaStato(prenotazione);
    }

    // ==========================================================
    // FASE 3: IL CLIENTE PAGA (Solo per le Private)
    // ==========================================================
    public boolean confermaPagamentoPrivata(Cliente cliente, Prenotazione prenotazione) throws CreditiInsufficientiException {
        if (!prenotazione.isProntaPerPagamento()) {
            throw new IllegalStateException("Impossibile pagare ora.");
        }

        int costoPrivata = 10;
        TitoloAccesso titolo = cliente.getTitoloAccesso();

        if (titolo == null || !titolo.checkValidita(costoPrivata)) {
            throw new CreditiInsufficientiException("Crediti insufficienti per confermare il pagamento.");
        }

        titolo.registraAccesso(costoPrivata);
        prenotazione.pagata();

        PrenotazioneDAO dao = DAOFactory.getInstance().getPrenotazioneDAO();
        dao.aggiornaStato(prenotazione);

        // Ora che il pagamento della privata è confermato, scaliamo fisicamente i crediti dal DB
        if (titolo instanceof PacchettoCrediti) {
            DAOFactory.getInstance().getTitoloAccessoDAO().aggiornaCrediti((PacchettoCrediti) titolo);
        }

        return true;
    }
}
package org.example.controller;


import org.example.controller.strategy.*;
import org.example.model.bean.LezioneBean;
import org.example.model.dao.Interface.PrenotazioneDAO;
import org.example.model.domain.*;
import org.example.model.dao.*; // Assicurati che i tuoi DAO siano importati correttamente
import org.example.exception.CreditiInsufficientiException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PrenotazioneController {
    private static final Logger logger = Logger.getLogger(PrenotazioneController.class.getName());
    private static PrenotazioneController instance; // AGGIUNTO PER SINGLETON
    private StrategiaPrenotazione strategiaCorrente;

    // Costruttore privato e getInstance per il Singleton
    private PrenotazioneController() {}

    public static PrenotazioneController getInstance() {
        if (instance == null) {
            instance = new PrenotazioneController();
        }
        return instance;
    }

    // ==========================================================
    // AGGIUNTA: IL PONTE TRA LA VIEW (BEAN) E IL TUO CONTROLLER
    // ==========================================================

    // 1. Converte i dati reali del DB in Bean per la View
    public List<LezioneBean> trovaTurniNuotoLibero(LocalDate data) {
        // Prende le lezioni reali dal DB
        List<Lezione> turni = DAOFactory.getInstance().getLezioneDAO().trovaPerTipoEData(TipoAttivita.NUOTO_LIBERO, data);
        List<LezioneBean> listaBean = new ArrayList<>();

        // Riempie i pacchettini "Bean" da dare alla grafica
        for (Lezione l : turni) {
            LezioneBean bean = new LezioneBean();
            bean.setIdLezione(l.getIdLezione());
            bean.setOrario(l.getOraInizio() + " - " + l.getOraFine());
            bean.setIdCorsia(String.valueOf(l.getCorsiaAssegnata().getIdCorsia()));
            bean.setPostiTotali(l.getNumPostiTotali());
            bean.setPostiLiberi(l.getNumPostiTotali() - l.getNumPostiPrenotati());
            listaBean.add(bean);
        }
        return listaBean;
    }

    // 2. Riceve l'ID dalla View, recupera la lezione vera e avvia la tua Fase 1
    public boolean finalizzaPrenotazioneDaId(Cliente cliente, int idLezione) throws CreditiInsufficientiException {
        Lezione lezioneVera = DAOFactory.getInstance().getLezioneDAO().trovaPerId(idLezione);
        return finalizzaPrenotazione(cliente, lezioneVera);
    }


    // ==========================================================
    // FASE 1: IL CLIENTE CHIEDE LA PRENOTAZIONE (IL TUO CODICE ORIGINALE)
    // ==========================================================
    public boolean finalizzaPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {
        // 1. CONTROLLI VALIDI PER TUTTI (Esempio)
        if (!cliente.verificaValiditaCertificato()) {
            throw new IllegalStateException("Certificato medico scaduto o assente!");
        }

        // 2. CAMBIO DI STRATEGIA AUTOMATICO (Quello che dicevi tu!)
        impostaStrategiaCorretta(lezione);

        try {
            return this.strategiaCorrente.eseguiPrenotazione(cliente, lezione);
        } catch (Exception e) {
            logger.severe("Errore durante l'esecuzione della strategia");
            return false;
        }
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

        Cliente clienteRichiedente = prenotazione.getCliente();

        if (accetta) {
            prenotazione.accettataDaIstruttore();
            logger.info("Sistema: L'istruttore ha accettato. Cliente pronto per pagare.");

            String testoOk = "L'istruttore ha accettato la tua richiesta per la lezione del " + prenotazione.getLezionePrenotata().getData() + ". Puoi procedere al pagamento.";
            clienteRichiedente.riceviNotifica(new Notifica(testoOk, clienteRichiedente));

        } else {
            prenotazione.rifiuta();

            Lezione lezione = prenotazione.getLezionePrenotata();
            lezione.setNumPostiPrenotati(lezione.getNumPostiPrenotati() - 1);
            logger.info("Sistema: L'istruttore ha rifiutato. Posto liberato.");

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

        if (titolo instanceof PacchettoCrediti) {
            DAOFactory.getInstance().getTitoloAccessoDAO().aggiornaCrediti((PacchettoCrediti) titolo);
        }

        return true;
    }
}
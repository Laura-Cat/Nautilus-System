package org.example.controller;


import org.example.controller.strategy.*;
import org.example.model.bean.LezioneBean;
import org.example.model.dao.Interface.PrenotazioneDAO;
import org.example.model.domain.*;
import org.example.model.dao.*; // Assicurati che i tuoi DAO siano importati correttamente
import org.example.exception.CreditiInsufficientiException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.time.LocalDate.now;

public class PrenotazioneController {
    private static final Logger logger = Logger.getLogger(PrenotazioneController.class.getName());
    private static PrenotazioneController instance; // AGGIUNTO PER SINGLETON
    private PrenotazioneStrategy strategiaCorrente;

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

    public List<LezioneBean> trovaPerTipoEData(TipoAttivita tipo, LocalDate data) {
        List<Lezione> turni = DAOFactory.getInstance().getLezioneDAO().trovaPerTipoEData(tipo, data);
        List<LezioneBean> listaBean = new ArrayList<>();

        for (Lezione l : turni) {
            LezioneBean bean = new LezioneBean();
            bean.setIdLezione(l.getIdLezione());
            bean.setOrario(l.getOraInizio() + " - " + l.getOraFine());

            if (l.getCorsiaAssegnata() != null) {
                bean.setIdCorsia(String.valueOf(l.getCorsiaAssegnata().getIdCorsia()));
            } else {
                bean.setIdCorsia("N/D");
            }

            if (l.getCorsoAppartenenza() != null) {
                bean.setNomeCorso(l.getCorsoAppartenenza().getNome());
            }

            // 4. Calcolo dei posti
            int postiTotali = (l.getNumPostiTotali() != null) ? l.getNumPostiTotali() : 5;
            int postiPrenotati = (l.getNumPostiPrenotati() != null) ? l.getNumPostiPrenotati() : 0;
            bean.setPostiTotali(postiTotali);
            bean.setPostiLiberi(postiTotali - postiPrenotati);

            listaBean.add(bean);
        }
        return listaBean;
    }

    public boolean finalizzaPrenotazioneDaId(Cliente cliente, int idLezione) throws CreditiInsufficientiException {
        Lezione lezioneVera = DAOFactory.getInstance().getLezioneDAO().trovaPerId(idLezione);
        return finalizzaPrenotazione(cliente, lezioneVera);
    }


    // ==========================================================
    // FASE 1: IL CLIENTE CHIEDE LA PRENOTAZIONE (IL TUO CODICE ORIGINALE)
    // ==========================================================
    public boolean finalizzaPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {
        // 1. CONTROLLI VALIDI PER TUTTI
        if (!cliente.verificaValiditaCertificato()) {
            throw new IllegalStateException("Certificato medico scaduto o assente!");
        }

        // 2. CONTROLLO DOPPIONI (La tua nuova regola di business!)
        boolean giaPrenotato = org.example.model.dao.DAOFactory.getInstance()
                .getPrenotazioneDAO()
                .esisteGia(cliente.getId(), lezione.getIdLezione());

        if (giaPrenotato) {
            throw new IllegalStateException("Hai già prenotato questa lezione! Non puoi occupare due posti.");
        }

        // 3. CAMBIO DI STRATEGIA AUTOMATICO
        impostaStrategiaCorretta(lezione);

        try {
            // A. Eseguiamo i controlli della strategia e salviamo il risultato in una variabile
            boolean esito = this.strategiaCorrente.eseguiPrenotazione(cliente, lezione);

            // B. Se la strategia ha dato il via libera, facciamo le modifiche sul Database!
            if (esito) {
                logger.info("La strategia ha approvato. Aggiorno i posti per la lezione ID: " + lezione.getIdLezione());

                // 1. Aggiorniamo i posti della lezione (questo lo avevamo già fatto)
                lezione.setNumPostiPrenotati(lezione.getNumPostiPrenotati() + 1);
                org.example.model.dao.DAOFactory.getInstance().getLezioneDAO().aggiornaPostiOccupati(lezione);

                // 2. CREIAMO LA PRENOTAZIONE USANDO IL PATTERN BEAN!
                org.example.model.domain.Prenotazione nuovaPrenotazione = new org.example.model.domain.Prenotazione();
                nuovaPrenotazione.setDataRichiesta(now());
                nuovaPrenotazione.setStato("Confermata");
                nuovaPrenotazione.setTipologia(lezione.getTipoAttivita());
                nuovaPrenotazione.setCliente(cliente);
                nuovaPrenotazione.setLezionePrenotata(lezione);

                // Se è nuoto libero e c'è una corsia assegnata, salviamo anche quella
                if (lezione.getCorsiaAssegnata() != null) {
                    nuovaPrenotazione.setCorsiaPrenotata(lezione.getCorsiaAssegnata());
                }
                // 3. Mandiamo il Bean al DAO per il salvataggio finale nel DB!
                org.example.model.dao.DAOFactory.getInstance().getPrenotazioneDAO().salva(nuovaPrenotazione);
            }
            // C. Solo adesso restituiamo l'esito alla View
            return esito;

        } catch (CreditiInsufficientiException creditiEx) {
            // LASCIA PASSARE LA NOSTRA ECCEZIONE: La rilanciamo alla View!
            throw creditiEx;
        } catch (Exception e) {
            // CATTURA GLI ERRORI VERI (es. database offline, null pointer)
            logger.severe("Errore durante l'esecuzione della strategia o durante il salvataggio");
            e.printStackTrace();
            return false;
        }
    }


    private void impostaStrategiaCorretta(Lezione lezione) {
        switch (lezione.getTipoAttivita()) {
            case CORSO_GRUPPO: this.strategiaCorrente = new PrenotazioneCorsoStrategy(); break;
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
            prenotazione.accettataDaIstruttore(); // Mette lo stato "Accettata - In attesa di pagamento"
            logger.info("Sistema: L'istruttore ha accettato. Cliente pronto per pagare.");

            // 🌟 CREIAMO LA NOTIFICA MAGICA PER IL PAGAMENTO!
            Notifica n = new Notifica();
            n.setMessaggio("L'istruttore ha accettato la tua richiesta per la lezione del " + prenotazione.getLezionePrenotata().getData() + ". Puoi procedere al pagamento.");
            n.setLetta(false);
            n.setTipo("RICHIESTA_PAGAMENTO");
            n.setIdRiferimento(prenotazione.getId()); // AGGANCIAMO L'ID DELLA PRENOTAZIONE!

            DAOFactory.getInstance().getNotificaDAO().invia(n, clienteRichiedente.getId());

        } else {
            prenotazione.rifiuta();
            Lezione lezione = prenotazione.getLezionePrenotata();
            lezione.setNumPostiPrenotati(lezione.getNumPostiPrenotati() - 1);

            Notifica n = new Notifica();
            n.setMessaggio("Ci dispiace, l'istruttore ha rifiutato la lezione privata per il " + prenotazione.getLezionePrenotata().getData() + ".");
            n.setLetta(false);
            n.setTipo("INFO");

            DAOFactory.getInstance().getNotificaDAO().invia(n, clienteRichiedente.getId());
        }

        // Salviamo il nuovo stato della prenotazione nel DB
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

    public List<LezioneBean> trovaPerCorso(TipoCorso tipo) {
        List<Lezione> lezioni = DAOFactory.getInstance().getLezioneDAO().trovaPerCorso(tipo);
        List<LezioneBean> listaBean = new ArrayList<>();

        for (Lezione l : lezioni) {
            LezioneBean bean = new LezioneBean();
            bean.setIdLezione(l.getIdLezione());

            // Formattiamo l'orario includendo la data (visto che i giorni cambiano!)
            bean.setOrario(l.getData().toString() + " | " + l.getOraInizio() + " - " + l.getOraFine());
            bean.setNomeCorso(tipo);

            int tot = l.getNumPostiTotali() != null ? l.getNumPostiTotali() : 15;
            int occ = l.getNumPostiPrenotati() != null ? l.getNumPostiPrenotati() : 0;
            bean.setPostiLiberi(tot - occ);
            bean.setPostiTotali(tot);

            listaBean.add(bean);
        }
        return listaBean;
    }

    public List<Istruttore> recuperaTuttiIstruttori() {
        return DAOFactory.getInstance().getIstruttoreDAO().recuperaTutti();
    }

    // 1. Metodo per la View: recupera gli slot per riempire la ComboBox
    public List<Lezione> recuperaPrivateDisponibili(Integer idIstruttore) {
        return DAOFactory.getInstance().getLezioneDAO().trovaPrivateDisponibiliPerIstruttore(idIstruttore);
    }

    // 2. Metodo per la View: elabora la richiesta vera e propria
    public boolean richiediLezionePrivata(Cliente cliente, Lezione lezioneSelezionata, String noteLivello) {
        try {
            this.strategiaCorrente = new PrenotazionePrivataStrategy();
            boolean valida = this.strategiaCorrente.eseguiPrenotazione(cliente, lezioneSelezionata);

            if (valida) {
                // A. Occupiamo fisicamente il posto sulla lezione
                lezioneSelezionata.setNumPostiPrenotati(1);
                DAOFactory.getInstance().getLezioneDAO().aggiornaPostiOccupati(lezioneSelezionata);

                // B. Creiamo la prenotazione in stato "In Attesa"
                Prenotazione p = new Prenotazione(null, now(), TipoAttivita.PRIVATA, cliente);
                p.setLezionePrenotata(lezioneSelezionata);
                p.attesaAccettazione(); // O p.setStato("IN ATTESA"); a seconda di come hai chiamato il metodo in Prenotazione
                p.setNote(noteLivello);
                DAOFactory.getInstance().getPrenotazioneDAO().salva(p);

                // C. Inviamo la notifica all'istruttore
                NotificaController nc = new NotificaController();
                nc.inviaRichiestaLezionePrivata(cliente, lezioneSelezionata.getIstruttore(), lezioneSelezionata, noteLivello, p.getId());

                return true;
            }
            return false;
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Errore imprevisto durante la richiesta della lezione privata", e);
            return false;
        }
    }
}
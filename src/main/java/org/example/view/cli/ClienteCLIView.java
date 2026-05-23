package org.example.view.cli;

import org.example.controller.LoginController;
import org.example.controller.PagamentoController;
import org.example.controller.PrenotazioneController;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("java:S106")
public class ClienteCLIView {

    private final Scanner scanner;
    private final Cliente cliente;
    private static final String TESTO_SCELTA = "Scegli un'opzione: ";
    private static final String TESTO_ERRORE_INPUT = "❌ Input non valido.";

    public ClienteCLIView(Scanner scanner, Cliente cliente) {
        this.scanner = scanner;
        this.cliente = cliente;
    }

    public void mostraMenu() {
        boolean attivo = true; // <-- Ho cambiato "loggato" in "attivo" per togliere il typo!

        while (attivo) {
            System.out.println("\n--- MENU CLIENTE: " + cliente.getNomeCompleto() + " ---");
            System.out.println("1. Visualizza il mio Calendario");
            System.out.println("2. Prenota Nuova Lezione");
            System.out.println("3. Visualizza Notifiche");
            System.out.println("0. Logout");
            System.out.print(TESTO_SCELTA);


            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    mostraCalendario();
                    break;
                case "2":
                    prenotaLezione();
                    break;
                case "3":
                    visualizzaNotifiche();
                    break;
                case "0":
                    System.out.println("Logout in corso...");
                    LoginController.getInstance().effettuaLogout();
                    attivo = false;
                    break;
                default:
                    System.out.println("❌ Opzione non valida. Riprova.");
            }
        }
    }

    private void mostraCalendario() {
        System.out.println("\n=== 📅 IL TUO CALENDARIO (Prossimi 7 giorni) ===");

        LocalDate oggi = LocalDate.now();
        LocalDate fineSettimana = oggi.plusDays(6);

        List<Prenotazione> prenotazioni = org.example.model.dao.DAOFactory.getInstance()
                .getPrenotazioneDAO().trovaAgendatiCliente(cliente.getId(), oggi, fineSettimana);

        if (prenotazioni == null || prenotazioni.isEmpty()) {
            System.out.println("Nessun appuntamento in programma per questa settimana. Riposati! 🛋️");
            return;
        }

        for (Prenotazione p : prenotazioni) {
            Lezione l = p.getLezionePrenotata();
            if (l != null) {
                System.out.println("🗓️ " + l.getData() + " | 🕒 " + l.getOraInizio() + " - " + l.getOraFine());

                if (l.getTipoAttivita() == TipoAttivita.CORSO_GRUPPO && l.getCorsoAppartenenza() != null) {
                    System.out.println("   ➤ Corso: " + l.getCorsoAppartenenza().getNome());
                } else if (l.getTipoAttivita() == TipoAttivita.NUOTO_LIBERO) {
                    System.out.println("   ➤ Nuoto Libero Turno");
                } else {
                    String nomeProf = (l.getIstruttore() != null) ? l.getIstruttore().getCognome() : "Assegnato";
                    System.out.println("   ➤ ⭐ Lezione Privata con Prof. " + nomeProf);
                }
                System.out.println("   -----------------------------------------");
            }
        }
    }

    private void prenotaLezione() {
        System.out.println("\n=== 🏊 PRENOTA UNA NUOVA LEZIONE ===");
        System.out.println("Che tipo di attività vuoi prenotare?");
        System.out.println("1. Corso di Gruppo");
        System.out.println("2. Nuoto Libero");
        System.out.println("3. Lezione Privata");
        System.out.print(TESTO_SCELTA);

        String sceltaTipo = scanner.nextLine();
        TipoAttivita tipoScelto;

        switch (sceltaTipo) {
            case "1": tipoScelto = TipoAttivita.CORSO_GRUPPO; break;
            case "2": tipoScelto = TipoAttivita.NUOTO_LIBERO; break;
            case "3": tipoScelto = TipoAttivita.PRIVATA; break;
            default:
                System.out.println("❌ Scelta non valida. Ritorno al menu principale.");
                return;
        }

        List<Lezione> disponibili; // La lista che conterrà i risultati

        if (tipoScelto == TipoAttivita.PRIVATA) {
            disponibili = gestisciPrenotazionePrivata();
        } else {
            // ==========================================================
            // FLUSSO CORSI E NUOTO LIBERO (Chiediamo la data)
            // ==========================================================
            System.out.print("\nInserisci la data (Formato YYYY-MM-DD, es. " + LocalDate.now() + "): ");
            String dataInput = scanner.nextLine();
            LocalDate dataScelta;
            try {
                dataScelta = LocalDate.parse(dataInput);
            } catch (Exception e) {
                System.out.println("❌ Formato data non valido. Ritorno al menu.");
                return;
            }

            disponibili = DAOFactory.getInstance().getLezioneDAO().trovaPerTipoEData(tipoScelto, dataScelta);
        }


        // ==========================================================
        // STAMPA LISTA DETTAGLIATA E PRENOTAZIONE
        // ==========================================================
        if (disponibili == null || disponibili.isEmpty()) {
            System.out.println("\n😔 Nessuna lezione disponibile trovata.");
            return;
        }

        System.out.println("\n--- LEZIONI DISPONIBILI ---");
        for (int i = 0; i < disponibili.size(); i++) {
            Lezione l = disponibili.get(i);

            int numPrenotati = l.getNumPostiPrenotati() != null ? l.getNumPostiPrenotati() : 0;
            int postiLiberi = l.getNumPostiTotali() - numPrenotati;

            String dettaglioTipologia = "";
            if (l.getTipoAttivita() == TipoAttivita.CORSO_GRUPPO && l.getCorsoAppartenenza() != null) {
                dettaglioTipologia = "Corso di " + l.getCorsoAppartenenza().getNome();
            } else if (l.getTipoAttivita() == TipoAttivita.NUOTO_LIBERO) {
                dettaglioTipologia = "Turno Nuoto Libero";
            } else if (l.getTipoAttivita() == TipoAttivita.PRIVATA) {
                dettaglioTipologia = "Lezione Privata";
            }

            // 🌟 Ho aggiunto l.getData() per far vedere il giorno esatto!
            System.out.println((i + 1) + ". 🗓️ " + l.getData() + " 🕒 " + l.getOraInizio() + " - " + l.getOraFine() +
                    " | 🏷️ " + dettaglioTipologia +
                    " | 👥 Posti liberi: " + postiLiberi);
        }

        System.out.print("\nInserisci il numero della lezione da prenotare (oppure 0 per annullare): ");
        int indiceScelto;
        try {
            indiceScelto = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println(TESTO_ERRORE_INPUT);
            return;
        }

        if (indiceScelto == 0) {
            System.out.println("Prenotazione annullata.");
            return;
        }

        if (indiceScelto > 0 && indiceScelto <= disponibili.size()) {
            Lezione lezioneSelezionata = disponibili.get(indiceScelto - 1);

            try {
                boolean successo;
                if (tipoScelto == TipoAttivita.PRIVATA) {
                    System.out.print("Vuoi lasciare una nota per l'istruttore? (Scrivi qui o premi Invio per saltare): ");
                    String note = scanner.nextLine();
                    successo = PrenotazioneController.getInstance().richiediLezionePrivata(cliente, lezioneSelezionata, note);
                } else {
                    successo = PrenotazioneController.getInstance().finalizzaPrenotazione(cliente, lezioneSelezionata);
                }

                if (successo) {
                    System.out.println("✅ PRENOTAZIONE CONFERMATA CON SUCCESSO!");
                } else {
                    System.out.println("❌ Errore: Posti esauriti o problemi interni.");
                }
            } catch (Exception e) {
                System.out.println("❌ Impossibile prenotare: " + e.getMessage());
            }

        } else {
            System.out.println(TESTO_ERRORE_INPUT);
        }
    }

    private List<Lezione> gestisciPrenotazionePrivata() {
        List<Lezione> disponibili;
        Istruttore istruttoreSelezionato;
        System.out.println("\n--- SCEGLI L'ISTRUTTORE ---");
        List<Istruttore> istruttori = DAOFactory.getInstance().getIstruttoreDAO().recuperaTutti();

        if (istruttori == null || istruttori.isEmpty()) {
            System.out.println("❌ Nessun istruttore disponibile al momento.");
            return java.util.Collections.emptyList();
        }

        for (int i = 0; i < istruttori.size(); i++) {
            System.out.println((i + 1) + ". Prof. " + istruttori.get(i).getNomeCompleto());
        }
        System.out.println("0. Torna al menu principale");

        System.out.print("\nSeleziona il numero dell'istruttore: ");
        try {
            int indiceIstr = Integer.parseInt(scanner.nextLine());
            if (indiceIstr == 0) return java.util.Collections.emptyList();
            if (indiceIstr < 1 || indiceIstr > istruttori.size()) {
                System.out.println("❌ Scelta non valida.");
                return java.util.Collections.emptyList();
            }
            istruttoreSelezionato = istruttori.get(indiceIstr - 1);
        } catch (NumberFormatException e) {
            System.out.println(TESTO_ERRORE_INPUT);
            return java.util.Collections.emptyList();
        }

        System.out.println("\n--- 👤 Profilo: Prof. " + istruttoreSelezionato.getCognome() + " ---");
        System.out.println("1. Vedi lezioni private disponibili e prenota");
        System.out.println("0. Torna indietro");
        System.out.print(TESTO_SCELTA);
        String subScelta = scanner.nextLine();

        if (!subScelta.equals("1")) {
            System.out.println("Operazione annullata.");
            return java.util.Collections.emptyList();
        }


        disponibili = DAOFactory.getInstance().getLezioneDAO().trovaPrivateDisponibiliPerIstruttore(istruttoreSelezionato.getId());
        return disponibili;
    }


    private void visualizzaNotifiche() {
        System.out.println("\n=== 🔔 LE TUE NOTIFICHE ===");

        try {
            List<Notifica> notifiche = org.example.model.dao.DAOFactory.getInstance()
                    .getNotificaDAO().recuperaTuttePerUtente(cliente, cliente.getId());

            if (notifiche == null || notifiche.isEmpty()) {
                System.out.println("📭 Nessun messaggio in bacheca. Tutto tranquillo!");
                return;
            }

            org.example.controller.NotificaController notificaController = new org.example.controller.NotificaController();

            // 🌟 LA NUOVA STRATEGIA: Salviamo le notifiche di pagamento mentre le stampiamo
            List<Notifica> notificheDaPagare = new ArrayList<>();

            for (int i = 0; i < notifiche.size(); i++) {
                Notifica n = notifiche.get(i);

                String stato = (n.getLetta()) ? "" : "🟢 [NUOVA] ";
                System.out.println((i + 1) + ". " + stato + "🗓️ " + n.getDataInvio());
                System.out.println("   ✉️ " + n.getMessaggio());
                System.out.println("   -------------------------------------------------");

                // Segniamo come letta
                if (!n.getLetta()) {
                    n.setLetta(true);
                    notificaController.aggiornaNotifica(n);
                }

                // Se la notifica è una richiesta di pagamento, la mettiamo nella lista da pagare!
                // (Usiamo contains per essere flessibili ed evitiamo quelle già pagate)
                if (n.getTipo() != null && n.getTipo().toUpperCase().contains("PAGAMENTO") && !n.getTipo().equals("PAGAMENTO_COMPLETATO")) {
                    notificheDaPagare.add(n);
                }
            }


            gestisciPagamentiPendenti(notificheDaPagare);

        } catch (Exception e) {
            System.out.println("❌ Errore nel caricamento delle notifiche: " + e.getMessage());
        }
    }

    private void gestisciPagamentiPendenti(List<Notifica> notificheDaPagare) {
        if (!notificheDaPagare.isEmpty()) {
            System.out.print("\n💳 Hai " + notificheDaPagare.size() + " pagamento/i in sospeso! Vuoi procedere adesso? (S/N): ");
            String risposta = scanner.nextLine();

            if (risposta.equalsIgnoreCase("S")) {
                System.out.println("\n--- SELEZIONA LA RICHIESTA DA PAGARE ---");
                for (int i = 0; i < notificheDaPagare.size(); i++) {
                    Notifica n = notificheDaPagare.get(i);
                    System.out.println((i + 1) + ". 🗓️ Del " + n.getDataInvio() + " -> " + n.getMessaggio());
                }
                System.out.print("Inserisci il numero (oppure 0 per annullare): ");

                try {
                    int scelta = Integer.parseInt(scanner.nextLine());
                    if (scelta > 0 && scelta <= notificheDaPagare.size()) {
                        Notifica notificaScelta = notificheDaPagare.get(scelta - 1);

                        System.out.println("\nSimulazione transazione in corso... 💸");

                        PagamentoController pagController = new PagamentoController();
                        pagController.setStrategiaPagamento(new org.example.controller.strategy.PayPalStrategy());

                        // Usiamo direttamente l'ID riferimento della notifica
                        boolean esito = pagController.pagaLezionePrivata(notificaScelta.getIdRiferimento(), 25.00, notificaScelta);

                        if (esito) {
                            System.out.println("✅ PAGAMENTO RIUSCITO! La lezione è stata confermata.");
                        } else {
                            System.out.println("❌ Errore durante il pagamento.");
                        }
                    }
                } catch (Exception e) {
                    System.out.println(TESTO_ERRORE_INPUT);
                }
            }
        } else {
            System.out.print("\nPremi INVIO per tornare al menu principale...");
            scanner.nextLine();
        }
    }
}
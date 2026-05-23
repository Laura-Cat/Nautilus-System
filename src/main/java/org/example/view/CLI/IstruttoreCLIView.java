package org.example.view.CLI;

import org.example.controller.LoginController;
import org.example.controller.NotificaController;
import org.example.controller.PrenotazioneController;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("java:S106")
public class IstruttoreCLIView {
    private Istruttore istruttore;
    private Scanner scanner;
    private NotificaController notificaController;

    public IstruttoreCLIView(Scanner scanner, Istruttore istruttore) {
        this.scanner = scanner;
        this.istruttore = istruttore;
        this.notificaController = new NotificaController();
    }

    public void mostraMenu() {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n===  MENU ISTRUTTORE: " + istruttore.getCognome() + " ===");
            System.out.println("1. 📅 Visualizza Calendario Impegni");
            System.out.println("2. 📩 Gestisci Richieste Lezioni Private");
            System.out.println("0. 🚪 Logout");
            System.out.print("Scegli un'opzione: ");

            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    visualizzaCalendario();
                    break;
                case "2":
                    gestisciRichieste();
                    break;
                case "0":
                    System.out.println("👋 Disconnessione in corso... Buon lavoro, Prof!");
                    // Richiama il tuo metodo centralizzato di logout
                    LoginController.getInstance().effettuaLogout();
                    esci = true;
                    break;
                default:
                    System.out.println("❌ Scelta non valida. Riprova.");
            }
        }
    }

    // ==========================================================
    // 1. VISUALIZZA IL CALENDARIO (Come in CalendarioView.java)
    // ==========================================================
    private void visualizzaCalendario() {
        System.out.println("\n=== 📅 I TUOI IMPEGNI (Prossimi 7 giorni) ===");
        LocalDate oggi = LocalDate.now();
        LocalDate dataFine = oggi.plusDays(7);

        try {
            // Sfruttiamo il DAO esattamente come fa popolaCalendarioIstruttore() nella GUI
            List<Lezione> impegni = DAOFactory.getInstance().getLezioneDAO()
                    .trovaImpegniIstruttore(istruttore.getId(), oggi, dataFine);

            if (impegni == null || impegni.isEmpty()) {
                System.out.println("🏖️ Nessuna lezione in programma per i prossimi 7 giorni.");
                return;
            }

            for (Lezione l : impegni) {
                System.out.println("🗓️ " + l.getData() + " | 🕒 " + l.getOraInizio() + " - " + l.getOraFine());

                if (l.getTipoAttivita() == TipoAttivita.CORSO_GRUPPO && l.getCorsoAppartenenza() != null) {
                    System.out.println("   ➤ 🏋️‍ Corso di Gruppo: " + l.getCorsoAppartenenza().getNome());
                } else if (l.getTipoAttivita() == TipoAttivita.PRIVATA) {
                    System.out.println("   ➤ ⭐ Lezione Privata (Cliente: " + l.getInfoClientePrivata() + ")");
                    if (l.getNoteClientePrivata() != null && !l.getNoteClientePrivata().trim().isEmpty()) {
                        System.out.println("      📝 Note livello: " + l.getNoteClientePrivata());
                    }
                }
                System.out.println("   -----------------------------------------");
            }

            System.out.print("\nPremi INVIO per tornare al menu...");
            scanner.nextLine();

        } catch (Exception e) {
            System.out.println("❌ Errore nel caricamento del calendario: " + e.getMessage());
        }
    }

    // ==========================================================
    // 2. GESTIONE RICHIESTE (Come in NotificheView.java)
    // ==========================================================
    private void gestisciRichieste() {
        System.out.println("\n=== 📩 RICHIESTE LEZIONI PRIVATE ===");

        try {
            // 🌟 1. Recuperiamo le notifiche tramite il Controller come fa la GUI
            List<Notifica> tutteNotifiche = notificaController.recuperaTuttePerUtente(istruttore, istruttore.getId());
            List<Notifica> daAccettare = new ArrayList<>();

            // 🌟 2. Filtriamo solo quelle non lette di tipo DA_ACCETTARE
            if (tutteNotifiche != null) {
                for (Notifica n : tutteNotifiche) {
                    if ("DA_ACCETTARE".equals(n.getTipo()) && !n.getLetta()) {
                        daAccettare.add(n);
                    }
                }
            }

            if (daAccettare.isEmpty()) {
                System.out.println("📭 Nessuna nuova richiesta da gestire al momento.");
                return;
            }

            for (int i = 0; i < daAccettare.size(); i++) {
                Notifica n = daAccettare.get(i);
                System.out.println((i + 1) + ". 🗓️ Richiesta del: " + n.getDataInvio());
                System.out.println("   ✉️ " + n.getMessaggio());
                System.out.println("   -----------------------------------------");
            }

            System.out.print("\nInserisci il numero della richiesta da gestire (oppure 0 per tornare indietro): ");
            int scelta = Integer.parseInt(scanner.nextLine());

            if (scelta > 0 && scelta <= daAccettare.size()) {
                Notifica nScelta = daAccettare.get(scelta - 1);

                // 🌟 3. Recuperiamo la prenotazione dal DB usando l'id_riferimento della notifica (Stessa logica GUI)
                Prenotazione p = DAOFactory.getInstance().getPrenotazioneDAO().trovaPerId(nScelta.getIdRiferimento());

                if (p == null) {
                    System.out.println("❌ Errore: Prenotazione originale non trovata nel database.");
                    return;
                }

                System.out.println("\nCosa vuoi fare con la richiesta di " + p.getCliente().getNomeCompleto() + "?");
                System.out.println("1. ✅ Accetta (Invia notifica di pagamento)");
                System.out.println("2. ❌ Rifiuta");
                System.out.print("Scelta: ");
                String azione = scanner.nextLine();

                if (azione.equals("1") || azione.equals("2")) {
                    boolean accetta = azione.equals("1");

                    // 🌟 4. CHIAMATA MAGICA AL CONTROLLER: Fa tutto lui (cambia stato DB e avvisa cliente)!
                    PrenotazioneController.getInstance().gestisciRispostaIstruttore(p, accetta);

                    // 🌟 5. "Disinneschiamo" la notifica dell'istruttore trasformandola in una INFO letta
                    nScelta.setLetta(true);
                    nScelta.setTipo("INFO");
                    nScelta.setMessaggio(accetta ? "✅ Hai ACCETTATO la lezione per " + p.getCliente().getNomeCompleto()
                            : "❌ Hai RIFIUTATO la lezione per " + p.getCliente().getNomeCompleto());
                    notificaController.aggiornaNotifica(nScelta);

                    System.out.println(accetta ? "✅ Lezione accettata con successo! Il cliente è stato avvisato."
                            : "❌ Lezione rifiutata.");
                } else {
                    System.out.println("Operazione annullata.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Input non valido.");
        } catch (Exception e) {
            System.out.println("❌ Errore durante il caricamento delle richieste: " + e.getMessage());
        }
    }
}
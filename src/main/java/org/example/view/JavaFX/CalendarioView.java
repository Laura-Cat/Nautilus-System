package org.example.view.JavaFX;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.controller.LoginController;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarioView {

    @FXML private Label lblTitoloCalendario;
    @FXML private HBox contenitoreSettimana;

    // Mappa utile per rintracciare al volo i VBox dei giorni tramite la data come stringa
    private final Map<String, VBox> mappaContenitoriGiorni = new HashMap<>();

    @FXML
    public void initialize() {
        User utenteLoggato = LoginController.getInstance().getUtenteAttivo();
        lblTitoloCalendario.setText("Agenda Settimanale - " + utenteLoggato.getNome() + " " + utenteLoggato.getCognome());

        LocalDate oggi = LocalDate.now();
        LocalDate fineSettimana = oggi.plusDays(6);

        // 1. Prepariamo lo scheletro grafico dei 7 giorni
        costruisciColonneSettimana(oggi);

        // 2. Bivio logico: popoliamo con dati reali in base al ruolo dell'utente loggato
        if (utenteLoggato instanceof Cliente) {
            popolaCalendarioCliente(utenteLoggato.getId(), oggi, fineSettimana);
        } else if (utenteLoggato instanceof Istruttore) {
            popolaCalendarioIstruttore(utenteLoggato.getId(), oggi, fineSettimana);
        }
    }

    private void costruisciColonneSettimana(LocalDate oggi) {
        contenitoreSettimana.getChildren().clear();
        mappaContenitoriGiorni.clear();

        DateTimeFormatter formatGiornoNome = DateTimeFormatter.ofPattern("EEEE", Locale.ITALIAN);
        DateTimeFormatter formatGiornoNum = DateTimeFormatter.ofPattern("dd MMM", Locale.ITALIAN);

        for (int i = 0; i < 7; i++) {
            LocalDate giornoCorrente = oggi.plusDays(i);
            String dataChiave = giornoCorrente.toString(); // Formato YYYY-MM-DD

            VBox colonnaGiorno = new VBox(10);
            colonnaGiorno.setPrefWidth(220);
            colonnaGiorno.getStyleClass().add("colonna-giorno");
            colonnaGiorno.setStyle("-fx-padding: 15;");

            if (i == 0) {
                colonnaGiorno.getStyleClass().add("colonna-oggi");
            }

            String nomeGiorno = giornoCorrente.format(formatGiornoNome);
            nomeGiorno = nomeGiorno.substring(0, 1).toUpperCase() + nomeGiorno.substring(1);

            Label lblNomeGiorno = new Label(nomeGiorno);
            lblNomeGiorno.setStyle(i == 0 ? "-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3b82f6;" : "-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0f172a;");

            Label lblData = new Label(giornoCorrente.format(formatGiornoNum));
            lblData.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

            javafx.scene.shape.Line linea = new javafx.scene.shape.Line(0, 0, 190, 0);
            linea.setStroke(javafx.scene.paint.Color.web("#e2e8f0"));

            VBox contenitoreEventi = new VBox(10);

            colonnaGiorno.getChildren().addAll(lblNomeGiorno, lblData, linea, contenitoreEventi);
            contenitoreSettimana.getChildren().add(colonnaGiorno);

            // Salviamo il contenitore degli eventi nella mappa usando la data come chiave
            mappaContenitoriGiorni.put(dataChiave, contenitoreEventi);
        }
    }

    // ==============================================================================
    // LOGICA POPOLAMENTO CLIENTE (PRENOTAZIONI)
    // ==============================================================================
    private void popolaCalendarioCliente(Integer idCliente, LocalDate inizio, LocalDate fine) {
        List<Prenotazione> prenotazioni = DAOFactory.getInstance()
                .getPrenotazioneDAO().trovaAgendatiCliente(idCliente, inizio, fine);

        for (Prenotazione p : prenotazioni) {
            Lezione l = p.getLezionePrenotata();
            if (l == null) continue;

            String dataLezione = l.getData().toString();
            VBox boxGiorno = mappaContenitoriGiorni.get(dataLezione);

            if (boxGiorno != null) {
                String orario = l.getOraInizio() + " - " + l.getOraFine();
                String titolo = recuperaTitoloAttivita(l);
                String colore = recuperaColoreInBaseATipo(l.getTipoAttivita());

                boxGiorno.getChildren().add(creaCardEventoGrafico(orario, titolo, colore));
            }
        }
    }

    // ==============================================================================
    // LOGICA POPOLAMENTO ISTRUTTORE (LEZIONI ASSEGNATE)
    // ==============================================================================
    private void popolaCalendarioIstruttore(Integer idIstruttore, LocalDate inizio, LocalDate fine) {
        List<Lezione> lezioni = DAOFactory.getInstance()
                .getLezioneDAO().trovaImpegniIstruttore(idIstruttore, inizio, fine);

        for (Lezione l : lezioni) {
            String dataLezione = l.getData().toString();
            VBox boxGiorno = mappaContenitoriGiorni.get(dataLezione);

            if (boxGiorno != null) {
                String orario = l.getOraInizio() + " - " + l.getOraFine();
                String titolo = recuperaTitoloAttivita(l);
                String colore = recuperaColoreInBaseATipo(l.getTipoAttivita());

                boxGiorno.getChildren().add(creaCardEventoGrafico(orario, titolo, colore));
            }
        }
    }

    // ==============================================================================
    // METODI UTILITY GRAFICI
    // ==============================================================================
    private String recuperaTitoloAttivita(Lezione l) {
        User utenteLoggato = LoginController.getInstance().getUtenteAttivo();

        if (l.getTipoAttivita() == TipoAttivita.CORSO_GRUPPO && l.getCorsoAppartenenza() != null) {
            return "Corso: " + l.getCorsoAppartenenza().getNome();
        } else if (l.getTipoAttivita() == TipoAttivita.NUOTO_LIBERO) {
            return "Nuoto Libero Turno";
        } else {
            if (utenteLoggato instanceof Cliente) {
                if (l.getIstruttore() != null && l.getIstruttore().getNome() != null) {
                    return "⭐ Privata con Istruttore " + l.getIstruttore().getCognome();
                }
                return "⭐ Lezione Privata";
            } else {
                // Se è l'Istruttore, sa già di essere lui, quindi mostriamo un testo di servizio
                return "⭐ Lezione Privata Assegnata";
            }
        }
    }

    private String recuperaColoreInBaseATipo(TipoAttivita tipo) {
        switch (tipo) {
            case CORSO_GRUPPO: return "#0284c7";  // Splendido Azzurro
            case NUOTO_LIBERO: return "#8b5cf6";  // Viola Elegante
            case PRIVATA: return "#10b981";       // Verde Smeraldo per le private certificate
            default: return "#64748b";
        }
    }

    private VBox creaCardEventoGrafico(String orario, String titolo, String coloreBordo) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card-evento");
        card.setStyle("-fx-border-color: " + coloreBordo + ";");

        Label lblOrario = new Label(orario);
        lblOrario.getStyleClass().add("evento-orario");

        Label lblTitolo = new Label(titolo);
        lblTitolo.getStyleClass().add("evento-titolo");
        lblTitolo.setWrapText(true);

        card.getChildren().addAll(lblOrario, lblTitolo);
        return card;
    }
}
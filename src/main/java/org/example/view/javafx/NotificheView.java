package org.example.view.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.controller.LoginController;
import org.example.controller.NotificaController;
import org.example.controller.PrenotazioneController;
import org.example.controller.SessionManager;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.Notifica;
import org.example.model.domain.Prenotazione;
import org.example.model.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class NotificheView {

    private static final Logger logger = Logger.getLogger(NotificheView.class.getName());

    @FXML
    private VBox contenitoreNotifiche;
    private IstruttoreView istruttoreViewPrincipale;
    private ClienteView clienteViewPrincipale;
    private List<Notifica> notificheAttuali;

    public void setClienteViewPrincipale(ClienteView clienteView) {
        this.clienteViewPrincipale = clienteView;
    }

    @FXML
    public void initialize() {
        caricaNotifiche();
    }

    private void caricaNotifiche() {
        contenitoreNotifiche.getChildren().clear();
        User utenteCorrente = SessionManager.getInstance().getUtenteAttivo();
        NotificaController backendController = new NotificaController();

        // 1. 🌟 Cambiato: recuperiamo TUTTE le notifiche dello storico
        notificheAttuali = backendController.recuperaTuttePerUtente(utenteCorrente, utenteCorrente.getId());

        if (notificheAttuali == null || notificheAttuali.isEmpty()) {
            Label zeroNotifiche = new Label("Non hai nessuna notifica nello storico.");
            zeroNotifiche.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-font-style: italic;");
            contenitoreNotifiche.getChildren().add(zeroNotifiche);
        } else {
            for (Notifica n : notificheAttuali) {
                VBox bolla = new VBox(6);
                bolla.getStyleClass().add("bolla-notifica");

                // 2. 🌟 BIVIO GRAFICO: Applichiamo lo stile in base allo stato Letta/Non Letta
                if (n.getLetta()) {
                    bolla.getStyleClass().add("bolla-letta");
                } else {
                    bolla.getStyleClass().add("bolla-non-letta");
                }

                // Etichetta Data
                Label labelData = new Label((n.getLetta() ? "✓ Letta • " : "🔵 Nuova • ") + n.getDataInvio());
                labelData.getStyleClass().add("notifica-data");

                // Etichetta Messaggio
                Label labelMessaggio = new Label(n.getMessaggio());
                labelMessaggio.getStyleClass().add("notifica-messaggio");
                labelMessaggio.setWrapText(true);

                bolla.getChildren().addAll(labelData, labelMessaggio);

                // Click sulla bolla per aprire il popup dettagliato
                bolla.setOnMouseClicked(event -> apriPopupDettaglio(n));

                contenitoreNotifiche.getChildren().add(bolla);
            }
        }
    }


    private void apriPopupDettaglio(Notifica n) {
        // 1. Appena la apre, la consideriamo letta!
        n.setLetta(true);
        new NotificaController().aggiornaNotifica(n);
        if (clienteViewPrincipale != null) {
            clienteViewPrincipale.aggiornaPallinoNotifiche();
        }

        if (istruttoreViewPrincipale != null) {
            istruttoreViewPrincipale.aggiornaPallinoNotifiche();
        }

        // 2. Creiamo il popup base
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dettaglio Notifica");
        alert.setHeaderText("Messaggio del " + n.getDataInvio());
        alert.setContentText(n.getMessaggio());

        // 3. LOGICA CONDIZIONALE: Se è una lezione accettata, aggiungiamo il bottone per pagare
        if ("RICHIESTA_PAGAMENTO".equals(n.getTipo())) {

            alert.getButtonTypes().clear();
            ButtonType btnPaga = new ButtonType("💳 Effettua Pagamento");
            ButtonType btnChiudi = new ButtonType("Chiudi", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().addAll(btnPaga, btnChiudi);

            Optional<ButtonType> risultato = alert.showAndWait();

            if (risultato.isPresent() && risultato.get() == btnPaga) {
                try {
                    // 1. Carichiamo l'FXML del popup di pagamento
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/pagamentoPopup.fxml"));
                    javafx.scene.Parent root = loader.load();

                    // 2. Prendiamo il controller e gli passiamo i dati (ID della lezione e un costo fittizio per ora)
                    PagamentoPopupView controllerPagamento = loader.getController();

                    double costoLezione = 25.00;

                    // Gli passiamo l'ID di riferimento (la lezione), il costo, la notifica, e il comando per ricaricare la lista
                    controllerPagamento.setDati(n.getIdRiferimento(), costoLezione, n, this::caricaNotifiche);

                    // 3. Creiamo la nuova finestrella (Stage)
                    javafx.stage.Stage stage = new javafx.stage.Stage();
                    stage.setTitle("Cassa");
                    stage.setScene(new javafx.scene.Scene(root));

                    // Blocca la schermata dietro finché l'utente non chiude il pagamento
                    stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                    stage.show();

                } catch (Exception e) {
                    logger.severe("Errore: " + e.getMessage());
                }
            }
        } else if ("DA_ACCETTARE".equals(n.getTipo())) {

            alert.getButtonTypes().clear();
            ButtonType btnAccetta = new ButtonType("✅ Accetta");
            ButtonType btnRifiuta = new ButtonType("❌ Rifiuta");
            ButtonType btnChiudi = new ButtonType("Chiudi", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().addAll(btnAccetta, btnRifiuta, btnChiudi);

            Optional<ButtonType> risultato = alert.showAndWait();

            if (risultato.isPresent() && (risultato.get() == btnAccetta || risultato.get() == btnRifiuta)) {

                boolean accetta = (risultato.get() == btnAccetta);

                Prenotazione p = DAOFactory.getInstance().getPrenotazioneDAO().trovaPerId(n.getIdRiferimento());
                PrenotazioneController.getInstance().gestisciRispostaIstruttore(p, accetta);

                n.setTipo("INFO");
                n.setMessaggio(accetta ? "✅ Hai ACCETTATO la lezione per " + p.getCliente().getNomeCompleto() : "❌ Hai RIFIUTATO la lezione per " + p.getCliente().getNomeCompleto());
                new NotificaController().aggiornaNotifica(n);
            }

        } else {
            // È una notifica di tipo 'INFO' normale
            alert.showAndWait();
        }

        // 4. Dopo aver chiuso il popup, ricarichiamo la lista (così la notifica sparisce se mostri solo le non lette)
        caricaNotifiche();
    }

    @FXML
    private void segnaTutteComeLette() {
        if (notificheAttuali == null || notificheAttuali.isEmpty()) return;

        NotificaController backendController = new NotificaController();
        for (Notifica n : notificheAttuali) {
            n.setLetta(true);
            backendController.aggiornaNotifica(n);
        }

        caricaNotifiche();

        if (clienteViewPrincipale != null) {
            clienteViewPrincipale.aggiornaPallinoNotifiche();
        }
        if (istruttoreViewPrincipale != null) {
            istruttoreViewPrincipale.aggiornaPallinoNotifiche();
        }
    }

    public void setIstruttoreViewPrincipale(IstruttoreView istruttoreView) {
        this.istruttoreViewPrincipale = istruttoreView;
    }
}

package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.controller.LoginController;
import org.example.controller.NotificaController;
import org.example.model.domain.Notifica;
import org.example.model.domain.User;

import java.util.List;
import java.util.Optional;

public class NotificheView {

    @FXML
    private VBox contenitoreNotifiche;

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
        User utenteCorrente = LoginController.getInstance().getUtenteAttivo();
        NotificaController backendController = new NotificaController();

        // 1. 🌟 Cambiato: recuperiamo TUTTE le notifiche dello storico
        notificheAttuali = backendController.recuperaTuttePerUtente(utenteCorrente, utenteCorrente.getId());

        // 🌟 AGGIUNGI QUESTA RIGA:
        System.out.println("DEBUG: Sto cercando le notifiche per l'utente ID " + utenteCorrente.getId() + ". Ne ho trovate: " + (notificheAttuali != null ? notificheAttuali.size() : "NULL"));

        if (notificheAttuali == null || notificheAttuali.isEmpty()) {
            Label zeroNotifiche = new Label("Non hai nessuna notifica nello storico.");
            zeroNotifiche.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-font-style: italic;");
            contenitoreNotifiche.getChildren().add(zeroNotifiche);
        } else {
            for (Notifica n : notificheAttuali) {
                VBox bolla = new VBox(6);
                bolla.getStyleClass().add("bolla-notifica");

                // 2. 🌟 BIVIO GRAFICO: Applichiamo lo stile in base allo stato Letta/Non Letta
                if (n.isLetta()) {
                    bolla.getStyleClass().add("bolla-letta");
                } else {
                    bolla.getStyleClass().add("bolla-non-letta");
                }

                // Etichetta Data
                Label labelData = new Label((n.isLetta() ? "✓ Letta • " : "🔵 Nuova • ") + n.getDataInvio());
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
                System.out.println("Reindirizzamento alla schermata dei pagamenti...");
                // clienteViewPrincipale.apriPaginaPagamenti();
            }
        } else {
            // È una notifica di tipo 'INFO' o altro
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
    }
}

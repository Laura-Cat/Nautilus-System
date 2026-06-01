package org.example.view.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.controller.LoginController;
import org.example.controller.PrenotazioneController;
import org.example.controller.SessionManager;
import org.example.model.bean.LezioneBean;
import org.example.model.domain.Cliente;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.example.model.domain.TipoAttivita.NUOTO_LIBERO;

public class NuotoLiberoView {

    @FXML private DatePicker calendarioNuoto;
    @FXML private VBox boxListaCorsie;

    @FXML
    public void giornoSelezionato(ActionEvent event) {
        LocalDate dataScelta = calendarioNuoto.getValue();

        if (dataScelta != null) {
            boxListaCorsie.getChildren().clear();

            Label titoloGiornata = new Label("Disponibilità per il " + dataScelta.toString());
            titoloGiornata.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            boxListaCorsie.getChildren().add(titoloGiornata);

            // 1. Chiede i BEAN al Controller
            List<LezioneBean> turni = PrenotazioneController.getInstance().trovaPerTipoEData(NUOTO_LIBERO, dataScelta);

            if (turni.isEmpty()) {
                boxListaCorsie.getChildren().add(new Label("Nessuna corsia disponibile per questa data."));
            } else {
                for (LezioneBean bean : turni) {
                    creaRigaCorsia(bean); // Passa il Bean
                }
            }
        }
    }

    private void creaRigaCorsia(LezioneBean bean) {
        HBox riga = new HBox();
        riga.setAlignment(Pos.CENTER_LEFT);
        riga.setSpacing(15);
        riga.setStyle("-fx-border-color: #ecf0f1; -fx-border-width: 0 0 1 0; -fx-padding: 10 0 10 0;");

        // Usa i dati già pronti nel Bean!
        Label labelInfo = new Label("🕒 " + bean.getOrario() + "  |  🏊 Corsia " + bean.getIdCorsia() + "  |  Posti Liberi: " + bean.getPostiLiberi() + "/" + bean.getPostiTotali());
        labelInfo.setStyle("-fx-font-size: 14px;");

        Region spaziatore = new Region();
        HBox.setHgrow(spaziatore, Priority.ALWAYS);

        Button btnPrenota = new Button();

        if (bean.getPostiLiberi() <= 0) {
            btnPrenota.setText("ESAURITO");
            btnPrenota.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            btnPrenota.setDisable(true);
        } else {
            btnPrenota.setText("Prenota");
            btnPrenota.getStyleClass().add("bottone-prenota");
            btnPrenota.setOnAction(e -> mostraPopupConferma(bean)); // Passa il Bean al popup
        }

        riga.getChildren().addAll(labelInfo, spaziatore, btnPrenota);
        boxListaCorsie.getChildren().add(riga);
    }

    private void mostraPopupConferma(LezioneBean bean) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Prenotazione");
        alert.setHeaderText("Prenotazione Nuoto Libero");
        alert.setContentText("Vuoi confermare la tua presenza dalle " + bean.getOrario() + " nella Corsia " + bean.getIdCorsia() + "?");

        Optional<ButtonType> risposta = alert.showAndWait();
        if (risposta.isPresent() && risposta.get() == ButtonType.OK) {
            try {
                Cliente clienteAttivo = (Cliente) SessionManager.getInstance().getUtenteAttivo();

                // Imposta la strategia
                boolean successo = PrenotazioneController.getInstance().finalizzaPrenotazioneDaId(clienteAttivo, bean.getIdLezione());

                if (successo) {
                    Alert okAlert = new Alert(Alert.AlertType.INFORMATION);
                    okAlert.setHeaderText("Prenotazione confermata!");
                    okAlert.show();
                    giornoSelezionato(null);
                } else {
                    Alert errAlert = new Alert(Alert.AlertType.ERROR);
                    errAlert.setHeaderText("Errore durante la prenotazione.");
                    errAlert.show();
                }

            } catch (Exception e) {
                // Questo intercetta la tua CreditiInsufficientiException (sostituisci Exception con la tua classe precisa se preferisci)
                Alert creditiAlert = new Alert(Alert.AlertType.WARNING);
                creditiAlert.setTitle("Attenzione");
                creditiAlert.setHeaderText("Operazione fallita");
                creditiAlert.setContentText(e.getMessage());
                creditiAlert.show();
            }
        }
    }
}

package org.example.view.JavaFX;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.controller.LoginController;
import org.example.controller.PrenotazioneController;
import org.example.model.bean.LezioneBean;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.*;
import org.example.exception.CreditiInsufficientiException;
import java.time.LocalDate;
import java.util.List;

public class PrenotazioneCorsoView {

    @FXML private DatePicker dataPicker;
    @FXML private ComboBox<TipoCorso> corsoComboBox;
    @FXML private VBox corsiContainer;

    private Cliente clienteLoggato;

    @FXML
    public void initialize() {
        User utenteCorrente = LoginController.getInstance().getUtenteAttivo();

        if (utenteCorrente instanceof Cliente) {
            this.clienteLoggato = (Cliente) utenteCorrente;
        } else {
            System.out.println("⚠️ Attenzione: l'utente loggato non è un Cliente!");
        }
        corsoComboBox.getItems().setAll(TipoCorso.values());
        dataPicker.setValue(LocalDate.now());
        mostraCorsiPerData(LocalDate.now());
    }

    @FXML
    void gestisciCambioData(ActionEvent event) {
        if (dataPicker.getValue() != null) {
            corsoComboBox.getSelectionModel().clearSelection();
            mostraCorsiPerData(dataPicker.getValue());
        }
    }

    @FXML
    void gestisciCambioCorso(ActionEvent event) {
        TipoCorso corsoSelezionato = corsoComboBox.getValue();
        if (corsoSelezionato != null) {
            dataPicker.setValue(null);
            mostraCorsiPerTipologia(corsoSelezionato);
        }
    }

    // STRATEGIA DI COSTRUZIONE GRAFICA GENERICA
    private void popolaContenitoreGrafico(List<LezioneBean> lezioni) {
        corsiContainer.getChildren().clear();

        if (lezioni.isEmpty()) {
            corsiContainer.getChildren().add(new Label("Nessuna lezione trovata con i filtri selezionati."));
            return;
        }

        for (LezioneBean bean : lezioni) {
            HBox card = new HBox(20);
            card.getStyleClass().add("corso-card");

            VBox info = new VBox(6);
            Label lblNome = new Label(bean.getNomeCorso() + " | " + bean.getOrario());
            lblNome.getStyleClass().add("corso-titolo");
            Label lblPosti = new Label("Posti disponibili: " + bean.getPostiLiberi() + " / " + bean.getPostiTotali());
            info.getChildren().addAll(lblNome, lblPosti);

            Button btnPrenota = new Button("Prenota");
            btnPrenota.getStyleClass().add("btn-prenota");

            // GUARDA QUANTO È PULITO ORA!
            btnPrenota.setOnAction(e -> gestisciClickPrenotazione(bean));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            card.getChildren().addAll(info, spacer, btnPrenota);
            corsiContainer.getChildren().add(card);
        }
    }

    private void gestisciClickPrenotazione(LezioneBean bean) {
        if (clienteLoggato == null) {
            mostraPopup("Errore di Sistema", "Utente non riconosciuto. Impossibile prenotare.", Alert.AlertType.ERROR);
            return;
        }

        try {
            Lezione l = DAOFactory.getInstance().getLezioneDAO().trovaPerId(bean.getIdLezione());
            boolean esito = PrenotazioneController.getInstance().finalizzaPrenotazione(clienteLoggato, l);

            if (esito) {
                mostraPopup("Successo", "Prenotazione registrata!", Alert.AlertType.INFORMATION);

                // Rinfresca la ricerca corrente
                if (dataPicker.getValue() != null) {
                    mostraCorsiPerData(dataPicker.getValue());
                } else {
                    mostraCorsiPerTipologia(corsoComboBox.getValue());
                }
            } else {
                mostraPopup("Errore", "Prenotazione fallita. Qualcosa è andato storto nel controller.", Alert.AlertType.ERROR);
            }

        } catch (CreditiInsufficientiException ex) {
            mostraPopup("Attenzione", ex.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception ex) {
            ex.printStackTrace();
            mostraPopup("Errore di Sistema", "Si è verificato un errore: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostraCorsiPerData(LocalDate data) {
        List<LezioneBean> lezioni = PrenotazioneController.getInstance().trovaPerTipoEData(TipoAttivita.CORSO_GRUPPO, data);
        popolaContenitoreGrafico(lezioni);
    }

    private void mostraCorsiPerTipologia(TipoCorso tipo) {
        List<LezioneBean> lezioni = PrenotazioneController.getInstance().trovaPerCorso(tipo);
        popolaContenitoreGrafico(lezioni);
    }

    private void mostraPopup(String t, String txt, Alert.AlertType type) {
        Alert alert = new Alert(type, txt, ButtonType.OK);
        alert.setTitle(t);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
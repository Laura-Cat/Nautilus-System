package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.controller.PagamentoController;
import org.example.controller.strategy.MetodoPagamentoStrategy;
import org.example.model.domain.Notifica;
import org.example.controller.strategy.CartaStrategy;
import org.example.controller.strategy.PayPalStrategy;

public class PagamentoPopupView {

    @FXML private Label labelImporto;

    private int idPrenotazione;

    private double costo;
    private Notifica notificaCollegata;
    private Runnable azionePostPagamento; // Serve per ricaricare la pagina dietro dopo aver pagato

    // Questo metodo viene chiamato da NotificheView un istante prima di mostrare la finestra
    public void setDati(int idPrenotazione, double costo, Notifica notifica, Runnable azionePostPagamento) {
        this.idPrenotazione = idPrenotazione;
        this.costo = costo;
        this.notificaCollegata = notifica;
        this.azionePostPagamento = azionePostPagamento;

        labelImporto.setText(String.format("Importo da pagare: € %.2f", costo));
    }

    @FXML
    private void pagaConPayPal() {
        eseguiPagamento(new PayPalStrategy());
    }

    @FXML
    private void pagaConCarta() {
        eseguiPagamento(new CartaStrategy()); // Assicurati di aver creato anche questa classe!
    }

    private void eseguiPagamento(org.example.controller.strategy.MetodoPagamentoStrategy strategiaScelta) {
        PagamentoController backendController = new PagamentoController();
        backendController.setStrategiaPagamento(strategiaScelta);

        // Chiamata allineata: Passiamo (int, double, Notifica)
        boolean successo = backendController.pagaLezionePrivata(idPrenotazione, costo, notificaCollegata);

        if (successo) {
            mostraMessaggio("Successo", "Pagamento completato tramite " + strategiaScelta.getNomePiattaforma() + "!");

            // Ricarica le notifiche (così scompare il bottone Paga)
            if (azionePostPagamento != null) azionePostPagamento.run();

            chiudiFinestra();
        } else {
            mostraMessaggio("Errore", "Transazione fallita. Riprova.");
        }
    }

    private void mostraMessaggio(String titolo, String testo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(testo);
        alert.showAndWait();
    }

    private void chiudiFinestra() {
        Stage stage = (Stage) labelImporto.getScene().getWindow();
        stage.close();
    }
}
package org.example.view.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.example.controller.LoginController;
import org.example.controller.PrenotazioneController;
import org.example.model.domain.Cliente;
import org.example.model.domain.Istruttore;
import org.example.model.domain.Lezione;
import org.example.model.domain.User;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

public class DettagliIstruttoreView {
    private static final Logger logger = Logger.getLogger(DettagliIstruttoreView.class.getName());

    @FXML private ImageView fotoDettaglio;
    @FXML private Label lblNomeDettaglio;
    @FXML private Label lblSpecDettaglio;
    @FXML private Text txtBiografia;
    @FXML private ComboBox<Lezione> comboOrario;
    @FXML private TextArea txtNoteUtente;
    @FXML private Label lblMessaggio;

    private Istruttore istruttore;
    private ClienteView clienteViewPrincipale;
    private final PrenotazioneController prenotazioneController = PrenotazioneController.getInstance();

    public void setIstruttore(Istruttore istruttore, ClienteView principale) {
        this.istruttore = istruttore;
        this.clienteViewPrincipale = principale;

        lblNomeDettaglio.setText(istruttore.getNome() + " " + istruttore.getCognome());
        lblSpecDettaglio.setText(istruttore.getSpecializzazione());
        txtBiografia.setText(istruttore.getDescrizione());

        try {
            var imageStream = getClass().getResourceAsStream(istruttore.getFotoPath());
            if (imageStream != null) {
                fotoDettaglio.setImage(new Image(imageStream));
            } else {
                logger.warning("Immagine non trovata per: " + istruttore.getNome());
            }
        } catch (Exception e) {
            logger.warning("Errore caricamento immagine per: " + istruttore.getNome());
        }

        impostaGestioneComboBox();
        caricaSlotDisponibili();
    }

    private void impostaGestioneComboBox() {
        // Insegna alla tendina come mostrare l'oggetto Lezione (Es: "15/10/2026 alle ore 10:30")
        comboOrario.setConverter(new StringConverter<>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            @Override
            public String toString(Lezione lezione) {
                if (lezione == null) return null;
                return lezione.getData().format(dateFormatter) + " alle ore " + lezione.getOraInizio().format(timeFormatter);
            }

            @Override
            public Lezione fromString(String string) {
                return null; // Non serve convertire da stringa a oggetto in sola lettura
            }
        });
    }

    private void caricaSlotDisponibili() {
        // Chiama il metodo che abbiamo scritto nel Blocco 2
        List<Lezione> disponibili = prenotazioneController.recuperaPrivateDisponibili(istruttore.getId());

        comboOrario.getItems().setAll(disponibili);

        if (disponibili.isEmpty()) {
            comboOrario.setPromptText("Nessuna disponibilità attuale");
            comboOrario.setDisable(true);
        } else {
            comboOrario.setPromptText("Seleziona data e orario");
            comboOrario.setDisable(false);
        }
    }

    @FXML
    private void inviaRichiesta() {
        Lezione lezioneSelezionata = comboOrario.getValue();
        String noteLivello = txtNoteUtente.getText();

        if (lezioneSelezionata == null) {
            mostraMessaggio("Seleziona uno slot disponibile dal menu a tendina!", true);
            return;
        }

        User utenteLoggato = LoginController.getInstance().getUtenteAttivo();
        if (!(utenteLoggato instanceof Cliente cliente)) {
            mostraMessaggio("Errore: Solo i clienti possono effettuare prenotazioni.", true);
            return;
        }

        // Il Controller applicativo fa tutto il lavoro "sporco"
        boolean successo = prenotazioneController.richiediLezionePrivata(cliente, lezioneSelezionata, noteLivello);

        if (successo) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Richiesta Inviata");
            alert.setHeaderText("Slot prenotato in attesa di conferma.");
            alert.setContentText("L'istruttore è stato notificato. Riceverai presto una risposta.");
            alert.showAndWait();
            tornaIndietro();
        } else {
            mostraMessaggio("Errore durante la prenotazione dello slot.", true);
        }
    }

    private void mostraMessaggio(String testo, boolean isError) {
        lblMessaggio.setText(testo);
        lblMessaggio.setStyle(isError ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #22c55e;"); // Rosso errore, Verde successo
        lblMessaggio.setVisible(true);
    }

    @FXML
    private void tornaIndietro() {
        if (clienteViewPrincipale != null) {
            // Usa il metodo che avevi già nel tuo ClienteView per gestire la navigazione
            clienteViewPrincipale.sceltaLezionePrivata(null);
        }
    }
}
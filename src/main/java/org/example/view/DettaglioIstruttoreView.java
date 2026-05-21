package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.example.controller.NotificaController;
import org.example.controller.PrenotazioneController;
import org.example.model.domain.Istruttore;
import org.example.model.domain.Notifica;
import org.example.model.dao.DAOFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DettaglioIstruttoreView {
    private static final Logger logger = Logger.getLogger(DettaglioIstruttoreView.class.getName());

    @FXML private ImageView fotoDettaglio;
    @FXML private Label lblNomeDettaglio;
    @FXML private Label lblSpecDettaglio;
    @FXML private Text txtBiografia;

    @FXML private DatePicker datePickerLezione;
    @FXML private ComboBox<String> comboOrario;
    @FXML private TextArea txtNoteUtente;
    @FXML private Label lblErrore;

    private Istruttore istruttoreSelezionato;
    private ClienteView clienteViewPrincipale; // Riferimento per tornare indietro
    private final PrenotazioneController prenotazioneController = new PrenotazioneController();
    private final NotificaController notificaController = new NotificaController()
            ;
    // Metodo fondamentale per "passare" l'istruttore cliccato da una pagina all'altra
    public void setIstruttore(Istruttore istruttore, ClienteView principale) {
        this.istruttoreSelezionato = istruttore;
        this.clienteViewPrincipale = principale;

        // Riempiamo la grafica
        lblNomeDettaglio.setText(istruttore.getNome() + " " + istruttore.getCognome());
        lblSpecDettaglio.setText(istruttore.getSpecializzazione());
        txtBiografia.setText(istruttore.getDescrizione());

        try {
            Image img = new Image(getClass().getResourceAsStream(istruttore.getFotoPath()));
            fotoDettaglio.setImage(img);
        } catch (Exception e) {
            logger.warning("Immagine non trovata per l'istruttore: " + istruttore.getNome() + ". Verrà usata quella di default o lasciata vuota.");
        }

        // Blocchiamo le date passate nel calendario
        datePickerLezione.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f5f9;");
                }
            }
        });
    }

    // Eseguito in automatico quando l'utente sceglie un giorno sul calendario
    @FXML
    private void gestisciDataSelezionata() {
        LocalDate dataSelezionata = datePickerLezione.getValue();
        if (dataSelezionata == null) return;

        comboOrario.getItems().clear();
        lblErrore.setVisible(false);

        // 1. Generiamo gli slot 08:30 - 21:00
        LocalTime inizio = LocalTime.of(8, 30);
        LocalTime fine = LocalTime.of(21, 0);
        List<LocalTime> tuttiGliOrari = new ArrayList<>();
        while (inizio.isBefore(fine)) {
            tuttiGliOrari.add(inizio);
            inizio = inizio.plusHours(1);
        }

        // 2. Recuperiamo dal DB le ore in cui l'istruttore è già occupato quel giorno
        // Sfruttiamo la logica dei tuoi controller esistenti
        List<LocalTime> orariOccupati = DAOFactory.getInstance()
                .getPrenotazioneDAO()
                .trovaOrariOccupatiIstruttore(istruttoreSelezionato.getCf(), dataSelezionata);

        // 3. Filtriamo aggiungendo alla combo solo gli orari liberi
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        for (LocalTime slot : tuttiGliOrari) {
            if (!orariOccupati.contains(slot)) {
                comboOrario.getItems().add(slot.format(formatter));
            }
        }

        if (comboOrario.getItems().isEmpty()) {
            lblErrore.setText("L'istruttore non ha ore libere in questo giorno.");
            lblErrore.setVisible(true);
            comboOrario.setDisable(true);
        } else {
            comboOrario.setDisable(false);
            comboOrario.setPromptText("Scegli l'orario");
        }
    }

    @FXML
    private void inviaRichiesta() {
        LocalDate data = datePickerLezione.getValue();
        String orarioStr = comboOrario.getValue();
        String noteLivello = txtNoteUtente.getText();

        if (data == null || orarioStr == null) {
            lblErrore.setText("Seleziona sia la data che l'orario prima di inviare!");
            lblErrore.setVisible(true);
            return;
        }

        // Creazione della Notifica di richiesta per l'istruttore
        Notifica nuovaRichiesta = new Notifica();
        nuovaRichiesta.setDestinatario(istruttoreSelezionato);
        // nuovaRichiesta.setCfMittente(Sessione.getInstance().getUtenteAttivo().getCf());

        // Inseriamo il livello dell'utente nella descrizione della notifica
        String messaggioCompleto = "Richiesta Lezione Privata il " + data + " alle ore " + orarioStr + ".\n" +
                "Note Livello Cliente: " + (noteLivello.isEmpty() ? "Nessuna nota fornita" : noteLivello);
        nuovaRichiesta.setMessaggio(messaggioCompleto);
        nuovaRichiesta.setLetta(false);

        // Salvataggio nel database tramite DAO
        boolean successo = DAOFactory.getInstance().getNotificaDAO().invia(nuovaRichiesta, istruttoreSelezionato.getId());

        if (successo) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Richiesta Inviata");
            alert.setHeaderText(null);
            alert.setContentText("La richiesta è stata inoltrata all'istruttore. Riceverai una notifica in caso di conferma!");
            alert.showAndWait();
            tornaIndietro();
        } else {
            lblErrore.setText("Errore di rete durante l'invio della richiesta. Riprova.");
            lblErrore.setVisible(true);
        }
    }

    @FXML
    private void tornaIndietro() {
        if (clienteViewPrincipale != null) {
            // Richiama il metodo della vista principale che ricarica il FlowPane degli istruttori
            clienteViewPrincipale.sceltaLezionePrivata();
        }
    }
}

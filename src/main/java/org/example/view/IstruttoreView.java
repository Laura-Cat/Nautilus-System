package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.controller.LoginController;
import org.example.controller.NotificaController;
import org.example.model.domain.Istruttore;
import org.example.model.domain.Notifica;
import org.example.model.domain.User;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IstruttoreView {

    private static final Logger logger = Logger.getLogger(IstruttoreView.class.getName());

    @FXML private Label labelNomeIstruttore;
    @FXML private StackPane contentArea;
    @FXML private Circle pallinoRosso;

    private final NotificaController notificaController = new NotificaController();

    @FXML
    public void initialize() {
        User utente = LoginController.getInstance().getUtenteAttivo();
        if (utente instanceof Istruttore) {
            Istruttore istruttore = (Istruttore) utente;
            labelNomeIstruttore.setText("Ciao, Prof. " + istruttore.getCognome());
        }
        aggiornaPallinoNotifiche();
    }

    // Metodo per cambiare le schermate centrali (identico a quello del Cliente)
    public void impostaSchermataCentrale(Node nuovoNodo) {
        this.contentArea.getChildren().setAll(nuovoNodo);
    }

    private void aggiornaPallinoNotifiche() {
        User utenteCorrente = LoginController.getInstance().getUtenteAttivo();
        List<Notifica> notificheNonLette = notificaController.recuperaNonLettePerUtente(utenteCorrente, utenteCorrente.getId());

        if (notificheNonLette != null && !notificheNonLette.isEmpty()) {
            pallinoRosso.setVisible(true);
        } else {
            pallinoRosso.setVisible(false);
        }
    }

    @FXML
    public void apriGestioneRichieste() {
        // Qui caricheremo la schermata dove l'istruttore vede le richieste in attesa
        logger.info("Apertura schermata gestione richieste...");
        /*
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestioneRichieste.fxml"));
            Node nuovaPagina = loader.load();
            impostaSchermataCentrale(nuovaPagina);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Errore caricamento gestioneRichieste", e);
        }
        */
    }

    @FXML
    public void logout() throws IOException {
        LoginController.getInstance().effettuaLogout();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/loginpage.fxml"));
        Stage stage = (Stage) labelNomeIstruttore.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
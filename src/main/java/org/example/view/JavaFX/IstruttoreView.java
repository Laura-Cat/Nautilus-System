package org.example.view.JavaFX;

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

    public void aggiornaPallinoNotifiche() {
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
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/notifichePage.fxml"));
            javafx.scene.Node root = loader.load();

            NotificheView controllerNotifiche = loader.getController();
            controllerNotifiche.setIstruttoreViewPrincipale(this);

            impostaSchermataCentrale(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void apriCalendario() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/calendarioPage.fxml"));
            javafx.scene.Node root = loader.load();
            impostaSchermataCentrale(root);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Errore caricamento calendario istruttore", e);
        }
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
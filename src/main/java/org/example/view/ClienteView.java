package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.controller.LoginController;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.Cliente;
import org.example.model.domain.Notifica;
import org.example.model.domain.User;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteView {

    private static final Logger logger = Logger.getLogger(ClienteView.class.getName());
    @FXML private Label labelNomeCliente;
    @FXML private StackPane contentArea;
    @FXML private VBox sottoMenuPrenotazioni;
    @FXML private Circle pallinoRosso;

    @FXML
    public void initialize() {
        User utente = LoginController.getInstance().getUtenteAttivo();
        if (utente instanceof Cliente) {
            Cliente c = (Cliente) utente;
            labelNomeCliente.setText("Ciao, " + c.getNome());
        }
        aggiornaPallinoNotifiche();
    }

    // FA APRIRE/CHIUDERE LA TENDINA
    @FXML
    public void cliccaPrenotaLezione(ActionEvent event) {
        boolean isAperto = sottoMenuPrenotazioni.isVisible();
        sottoMenuPrenotazioni.setVisible(!isAperto);
        sottoMenuPrenotazioni.setManaged(!isAperto);
    }

    // AZIONI DEI 3 BOTTONCINI DEL SOTTOMENU

    // Funzione comodità per non ripetere il codice di caricamento 3 volte
    private void caricaPaginaAlCentro(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node nuovaPagina = loader.load();
            contentArea.getChildren().setAll(nuovaPagina);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Errore caricamento pagina: " + fxmlPath, e);
        }
    }

    public void aggiornaPallinoNotifiche() {
        User utenteCorrente = LoginController.getInstance().getUtenteAttivo();
        Integer idCliente = utenteCorrente.getId();

        List<Notifica> notificheNonLette = org.example.model.dao.DAOFactory.getInstance()
                .getNotificaDAO()
                .recuperaNonLettePerUtente(utenteCorrente, idCliente);

        if (notificheNonLette != null && !notificheNonLette.isEmpty()) {
            pallinoRosso.setVisible(true);
        } else {
            pallinoRosso.setVisible(false);
        }
    }

    // 🌟 2. Sostituisci il metodo apriNotifiche() vuoto con questo:
    @FXML
    public void apriNotifiche() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/notifichePage.fxml"));
            javafx.scene.Node root = loader.load();

            // Peschiamo il controller delle notifiche e gli passiamo la ClienteView principale
            org.example.view.NotificheView controllerNotifiche = loader.getController();
            controllerNotifiche.setClienteViewPrincipale(this);

            impostaSchermataCentrale(root);

        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Errore nel caricamento della pagina notifiche", e);
        }
    }
    @FXML
    public void sceltaCorso(ActionEvent event) {
        caricaPaginaAlCentro("/fxml/corsiPage.fxml");
    }

    @FXML
    public void sceltaLezionePrivata(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/privatePage.fxml"));
            javafx.scene.Node root = loader.load();

            // 2. Peschiamo il controller e facciamo le presentazioni!
            org.example.view.PrenotazionePrivateView controllerPrivata = loader.getController();
            controllerPrivata.setClienteViewPrincipale(this);

            impostaSchermataCentrale(root);

        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Errore nel caricamento della pagina privatePage", e);
        }
    }

    @FXML
    public void sceltaNuotoLibero(ActionEvent event) {
        caricaPaginaAlCentro("/fxml/nuotoLiberoPage.fxml");
    }
    // ----------------------------------------------

    @FXML
    public void logout() throws IOException {
        LoginController.getInstance().effettuaLogout();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/loginpage.fxml"));
        Stage stage = (Stage) labelNomeCliente.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void apriCalendario(ActionEvent event) {
        caricaPaginaAlCentro("/fxml/calendarioPage.fxml");
    }

    public void impostaSchermataCentrale(javafx.scene.Node nuovoNodo) {
        if (this.contentArea != null) {
            this.contentArea.getChildren().setAll(nuovoNodo);
        } else {
            System.err.println("Errore: contentArea è null in ClienteView!");
        }
    }
}
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

    @FXML public void apriNotifiche() {  }

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

    private void aggiornaPallinoNotifiche() {
        // 1. Recupera l'utente
        User utenteCorrente = LoginController.getInstance().getUtenteAttivo();
        Integer idCliente = LoginController.getInstance().getUtenteAttivo().getId();
        // 2. Sfrutta il metodo che hai già per recuperare la lista intera
        List<Notifica> notificheNonLette = DAOFactory.getInstance()
                .getNotificaDAO()
                .recuperaNonLettePerUtente(utenteCorrente, idCliente);

        // 3. Verifica se la lista esiste e se contiene almeno un elemento
        if (notificheNonLette != null && !notificheNonLette.isEmpty()) {
            pallinoRosso.setVisible(true);  // Accendi il pallino!
        } else {
            pallinoRosso.setVisible(false); // Spegni il pallino
        }
    }
    @FXML
    public void sceltaCorso(ActionEvent event) {
        caricaPaginaAlCentro("/fxml/corsiPage.fxml");
    }

    @FXML
    public void sceltaLezionePrivata(ActionEvent event) {
        caricaPaginaAlCentro("/fxml/privatePage.fxml");
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
}
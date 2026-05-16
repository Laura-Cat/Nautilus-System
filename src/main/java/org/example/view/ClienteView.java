package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.example.controller.LoginController; // Il tuo backend controller
import org.example.model.domain.Cliente;
import org.example.model.domain.User;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClienteView{
    private static final Logger logger = Logger.getLogger(ClienteView.class.getName());
    @FXML private Label labelNomeCliente;
    @FXML private javafx.scene.layout.StackPane contentArea;
    @FXML private BorderPane mainPane;
    @FXML
    public void initialize() {
        // Recuperiamo l'utente che è appena entrato dal Singleton del backend
        User utente = LoginController.getInstance().getUtenteAttivo();

        if (utente instanceof Cliente) {
            Cliente c = (Cliente) utente;
            labelNomeCliente.setText("Ciao, " + c.getNome());
        }
    }

    @FXML
    public void logout() throws IOException {
        // 1. Puliamo la sessione nel backend
        LoginController.getInstance().effettuaLogout();

        // 2. Torniamo al Login
        Parent root = FXMLLoader.load(getClass().getResource("/loginpage.fxml"));
        Stage stage = (Stage) labelNomeCliente.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void cliccaPrenotaLezione(ActionEvent event) {
        try {
            // Carica il "mattoncino" del palinsesto
            // Assicurati che il percorso del file sia esatto!
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/palinsestoPage.fxml"));
            Node palinsestoNode = loader.load();

            // Magia: prendi il mattoncino e incastralo al centro della finestra!
            // Questo farà sparire in automatico la scritta "Seleziona un'operazione..."
            mainPane.setCenter(palinsestoNode);

        } catch (IOException e) {
            logger.severe("Errore nel caricamento del Palinsesto");
        }
    }
}
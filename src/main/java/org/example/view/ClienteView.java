package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.controller.LoginController; // Il tuo backend controller
import org.example.model.domain.Cliente;
import org.example.model.domain.User;

import java.io.IOException;

public class ClienteView{

    @FXML private Label labelNomeCliente;
    @FXML private javafx.scene.layout.StackPane contentArea;

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
}
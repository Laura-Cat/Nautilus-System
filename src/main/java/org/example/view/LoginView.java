package org.example.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.example.controller.LoginController; // Assicurati che l'import sia corretto
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.model.domain.Cliente;
import org.example.model.domain.Istruttore;
import org.example.model.domain.User;
import org.example.model.bean.LoginDTO;

public class LoginView{

    // L'annotazione @FXML dice a Java: "Cerca nell'interfaccia FXML un elemento con questo id"
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    // Questo metodo scatta in automatico quando clicchi il bottone "ACCEDI"
    @FXML
    private void bottoneAccediCliccato() {
        // 1. Leggiamo cosa ha scritto l'utente
        String emailInserita = emailField.getText();
        String passwordInserita = passwordField.getText();

        // 2. Controllo base: ha lasciato i campi vuoti?
        if (emailInserita.isEmpty() || passwordInserita.isEmpty()) {
            mostraPopupErrore("Campi obbligatori", "Per favore, inserisci sia l'email che la password.");
            return; // Interrompiamo subito
        }

        // 3. Chiamiamo il VERO controller (La Logica di Business)
         LoginController backendController = LoginController.getInstance();
         LoginDTO credenziali = new LoginDTO(emailInserita, passwordInserita);
         boolean successo = backendController.autenticaUtente(credenziali);

        // 4. Decidiamo cosa fare in base alla risposta del DB
        if (successo) {
            mostraPopupSuccesso("Accesso Consentito", "Login effettuato! Benvenuto in Nautilus.");
            try {
                // 2. Chiediamo al backend CHI è entrato usando il tuo getter
                User utenteLoggato = backendController.getUtenteAttivo();

                String fileFxmlDaCaricare = "";

                // 3. IL VIGILE URBANO: Scegliamo quale View caricare
                if (utenteLoggato instanceof Cliente) {
                    fileFxmlDaCaricare = "/fxml/clientePage.fxml";
                }
                else if (utenteLoggato instanceof Istruttore) {
                    fileFxmlDaCaricare = "/fxml/istruttorePage.fxml";
                }
                else {
                    fileFxmlDaCaricare = "/fxml/adminPage.fxml";
                }

                // 4. Carichiamo la nuova finestra
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/clientePage.fxml"));
                Parent root = loader.load();

                // 5. Cambiamo la scenografia prendendo la finestra attuale dal bottone
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

            } catch (Exception e) {

                mostraPopupErrore("Error", " Errore nel caricamento della schermata successiva!");
            }
        } else {
            mostraPopupErrore("Accesso Negato", "Email o password errati. Riprova.");
        }
    }

    // --- Metodi di Utilità per mostrare finestrelle (Popup) veloci ---

    private void mostraPopupErrore(String titolo, String messaggio) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(titolo);
        alert.setContentText(messaggio);
        alert.showAndWait(); // Mette in pausa finché l'utente non clicca "OK"
    }

    private void mostraPopupSuccesso(String titolo, String messaggio) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(titolo);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
package org.example.view.javafx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.controller.LoginController;
import org.example.controller.SessionManager;
import org.example.model.bean.LoginBean;
import org.example.model.domain.Cliente;
import org.example.model.domain.Istruttore;
import org.example.model.domain.User;

import java.util.logging.Logger;

public class LoginView {
    private static final Logger logger = Logger.getLogger(LoginView.class.getName());

    // --- FXML ORIGINALI ---
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private StackPane rootPane; // <-- AGGIUNGI QUESTA RIGA
    @FXML private ImageView immagineSlideshow;


    // Lista delle immagini che scorrono, allineata alla tua struttura di cartelle
    private final String[] immaginiSlideshowArr = {
            "/images/scorrimento/acquagym.jpg",
            "/images/scorrimento/hydrobike.jpg",
            "/images/scorrimento/neonatale.jpg",
            "/images/scorrimento/nuotomaster.jpg"
    };
    private int indiceImmagineAttuale = 0;

    @FXML
    public void initialize() {
        // GESTIONE RQUADRO SLIDESHOW: Applichiamo il ritaglio stonato all'immagine via codice
        if (immagineSlideshow != null) {
            // Usa dinamicamente le nuove misure giganti (600x420) che abbiamo messo nell'FXML
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(
                    immagineSlideshow.getFitWidth(), immagineSlideshow.getFitHeight()
            );
            clip.setArcWidth(40); // Curvatura leggermente più ampia per foto grandi
            clip.setArcHeight(40);
            immagineSlideshow.setClip(clip);

            avviaSlideshow();
        }
    }

    private void avviaSlideshow() {
        cambiaImmagine();
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(4), event -> {
                    indiceImmagineAttuale = (indiceImmagineAttuale + 1) % immaginiSlideshowArr.length;
                    cambiaImmagine();
                })
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    private void cambiaImmagine() {
        try {
            var imageStream = getClass().getResourceAsStream(immaginiSlideshowArr[indiceImmagineAttuale]);
            if (imageStream != null) {
                immagineSlideshow.setImage(new Image(imageStream));
            } else {
                logger.warning(() ->"Immagine slideshow non trovata: " + immaginiSlideshowArr[indiceImmagineAttuale]);
            }
        } catch (Exception e) {
            logger.warning(() ->"Errore nel caricamento immagine slideshow: " + e.getMessage());
        }
    }

    // --- IL TUO METODO ORIGINALE PER IL LOGIN (Invariato) ---
    @FXML
    private void bottoneAccediCliccato() {
        String emailInserita = emailField.getText();
        String passwordInserita = passwordField.getText();

        if (emailInserita.isEmpty() || passwordInserita.isEmpty()) {
            mostraPopupErrore("Campi obbligatori", "Per favore, inserisci sia l'email che la password.");
            return;
        }

        LoginController backendController = LoginController.getInstance();
        LoginBean credenziali = new LoginBean(emailInserita, passwordInserita);
        boolean successo = backendController.autenticaUtente(credenziali);

        if (successo) {
            mostraPopupSuccesso("Accesso Consentito", "Login effettuato! Benvenuto in Nautilus.");
            try {
                User utenteLoggato = SessionManager.getInstance().getUtenteAttivo(); // Assicurati che il getter sia corretto

                String fileFxmlDaCaricare = "";

                if (utenteLoggato instanceof Cliente) {
                    fileFxmlDaCaricare = "/fxml/clientePage.fxml";
                }
                else if (utenteLoggato instanceof Istruttore) {
                    fileFxmlDaCaricare = "/fxml/istruttorePage.fxml";
                }
                else {
                    fileFxmlDaCaricare = "/fxml/adminPage.fxml";
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fileFxmlDaCaricare));
                Parent root = loader.load();

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));

                // Mantiene lo schermo intero anche dopo il login
                stage.setMaximized(true);
                stage.show();

            } catch (Exception e) {
                logger.log(java.util.logging.Level.SEVERE, "Errore nel caricamento della schermata successiva", e);
                mostraPopupErrore("Errore", "Errore nel caricamento della schermata successiva!");
            }
        } else {
            mostraPopupErrore("Accesso Negato", "Email o password errati. Riprova.");
        }
    }

    @FXML
    private void passwordDimenticataCliccata() {
        // Per ora mostriamo solo un popup informativo
        mostraPopupSuccesso("Recupero Password",
                "La funzione di recupero password è in fase di sviluppo.\nContatta l'amministratore di sistema.");
    }

    @FXML
    private void loginConGoogle() {
        mostraPopupSuccesso("Login Google", "Reindirizzamento al browser per l'autenticazione con Google in corso...");
        // Qui andrà implementata la logica OAuth2
    }

    @FXML
    private void loginConFacebook() {
        mostraPopupSuccesso("Login Facebook", "Reindirizzamento al browser per l'autenticazione con Facebook in corso...");
        // Qui andrà implementata la logica OAuth2
    }

    // --- I TUOI METODI DI UTILITA' ORIGINALI (Invariati) ---
    private void mostraPopupErrore(String titolo, String messaggio) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(titolo);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    private void mostraPopupSuccesso(String titolo, String messaggio) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(titolo);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
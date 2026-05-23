package org.example.view.javafx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.controller.PrenotazioneController;
import org.example.model.domain.Istruttore;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrenotazionePrivateView {
    private static final Logger logger = Logger.getLogger(PrenotazionePrivateView.class.getName());

    @FXML
    private FlowPane gridIstruttori;
    private ClienteView clienteViewPrincipale;
    public void setClienteViewPrincipale(ClienteView principale) {
        this.clienteViewPrincipale = principale;
    }

    @FXML
    public void initialize() {
        logger.info("Metodo initialize di PrenotazionePrivateView partito!");
        caricaIstruttori();
    }

    private void caricaIstruttori() {
        List<Istruttore> istruttori = PrenotazioneController.getInstance().recuperaTuttiIstruttori();
        gridIstruttori.getChildren().clear();

        // Per ogni istruttore trovato, creiamo una Card grafica
        for (Istruttore ist : istruttori) {
            VBox card = creaCardIstruttore(ist);
            gridIstruttori.getChildren().add(card);
        }
    }

    private VBox creaCardIstruttore(Istruttore istruttore) {
        VBox card = new VBox(10); // Spazio di 10px tra gli elementi
        card.getStyleClass().add("istruttore-card"); // Per il CSS

        // L'immagine dell'istruttore
        ImageView foto = new ImageView();
        foto.setFitWidth(150);
        foto.setFitHeight(150);
        try {
            var imageStream = getClass().getResourceAsStream(istruttore.getFotoPath());
            if (imageStream != null) {
                foto.setImage(new Image(imageStream));
            } else {
                logger.warning("Immagine non trovata per: " + istruttore.getNome());
            }
        } catch (Exception e) {
            logger.warning("Errore caricamento immagine per: " + istruttore.getNome());
        }

        // Il Nome
        Label lblNome = new Label(istruttore.getNome() + " " + istruttore.getCognome());
        lblNome.getStyleClass().add("nome-istruttore");

        // La Specializzazione
        Label lblSpec = new Label(istruttore.getSpecializzazione());
        lblSpec.getStyleClass().add("specializzazione-istruttore");
        lblSpec.setWrapText(true);

        // Aggiungiamo tutto al VBox (la Card)
        card.getChildren().addAll(foto, lblNome, lblSpec);

        // Prepariamo il click!
        card.setOnMouseClicked(event -> {
            try {
                logger.info("L'utente ha cliccato sull'istruttore: " + istruttore.getNome());

                // Carica la pagina di dettaglio
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dettagliIstruttore.fxml"));
                Parent root = loader.load();

                DettagliIstruttoreView dettaglioController = loader.getController();

                dettaglioController.setIstruttore(istruttore, clienteViewPrincipale);

                if (clienteViewPrincipale != null) {
                    clienteViewPrincipale.impostaSchermataCentrale(root);
                } else {
                    logger.severe("Errore: clienteViewPrincipale è null, impossibile navigare.");
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Errore critico: Impossibile caricare la pagina di dettaglio istruttore.", e);
            }
        });

        return card;
    }
}

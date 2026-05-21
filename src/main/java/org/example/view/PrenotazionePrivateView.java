package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.Istruttore;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrenotazionePrivateView {
    private static final Logger logger = Logger.getLogger(PrenotazionePrivateView.class.getName());

    @FXML
    private FlowPane gridIstruttori;
    @FXML
    public void initialize() {
        System.out.println("Metodo initialize partito!");
        caricaIstruttori();
    }

    private void caricaIstruttori() {
        // 1. Chiediamo al DAO la lista degli istruttori (il backend lavora per noi!)
        List<Istruttore> istruttori = DAOFactory.getInstance().getIstruttoreDAO().recuperaTutti();

        // 2. Svuotiamo la griglia per sicurezza
        gridIstruttori.getChildren().clear();

        // 3. Per ogni istruttore trovato, creiamo una Card grafica
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

        // Cerca di caricare l'immagine dal percorso, se fallisce ne mette una di default
        try {
            Image img = new Image(getClass().getResourceAsStream(istruttore.getFotoPath()));
            foto.setImage(img);
        } catch (Exception e) {
            System.err.println("Immagine non trovata per: " + istruttore.getNome());
            // foto.setImage(new Image(getClass().getResourceAsStream("/images/default_istruttore.png")));
        }

        // Il Nome
        Label lblNome = new Label(istruttore.getNome() + " " + istruttore.getCognome());
        lblNome.getStyleClass().add("nome-istruttore");

        // La Specializzazione
        Label lblSpec = new Label(istruttore.getSpecializzazione());
        lblSpec.getStyleClass().add("specializzazione-istruttore");
        lblSpec.setWrapText(true); // Se il testo è lungo, va a capo da solo

        // Aggiungiamo tutto al VBox (la Card)
        card.getChildren().addAll(foto, lblNome, lblSpec);

        // Prepariamo il click! (Per ora stampa solo in console, poi aprirà il dettaglio)
        card.setOnMouseClicked(event -> {
            try {
                // Log per tracciare l'azione dell'utente
                logger.info("L'utente ha cliccato sull'istruttore: " + istruttore.getNome());

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dettaglioIstruttore.fxml"));
                Parent root = loader.load();

                DettaglioIstruttoreView dettaglioController = loader.getController();
                dettaglioController.setIstruttore(istruttore, this.clienteViewPrincipale);

                this.contentArea.getChildren().setAll(root);

            } catch (Exception e) {
                // SOSTITUITO e.printStackTrace() E System.err CON UN LOG DI LIVELLO SEVERE
                logger.log(Level.SEVERE, "Errore critico: Impossibile caricare la pagina di dettaglio istruttore.", e);
            }
        });

        return card;
    }
}

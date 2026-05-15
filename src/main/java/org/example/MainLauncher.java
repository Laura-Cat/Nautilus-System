package org.example;

import org.example.model.dao.DAOFactory;
import javafx.application.Application;
import java.util.Scanner;
import java.util.logging.Logger;

public class MainLauncher {
    private static final Logger logger = Logger.getLogger(MainLauncher.class.getName());
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        logger.info("=== NAUTILUS SYSTEMS - SETUP DI AVVIO ===");

        // 1. Scelta della Persistenza
        logger.info("Seleziona il metodo di persistenza:");
        logger.info("1. Database MySQL");
        logger.info("2. File System (CSV/JSON)");
        int sceltaP = scanner.nextInt();

        if (sceltaP == 2) {
            DAOFactory.getInstance().setPersistenceType(DAOFactory.PersistenceType.FILE);
            logger.info("[Config] Modalità File System impostata.");
        } else {
            DAOFactory.getInstance().setPersistenceType(DAOFactory.PersistenceType.DATABASE);
            logger.info("[Config] Modalità MySQL impostata.");
        }

        // 2. Scelta dell'Interfaccia
        logger.info("\nSeleziona l'interfaccia utente:");
        logger.info("1. Interfaccia Grafica (GUI JavaFX)");
        logger.info("2. Interfaccia da Terminale (CLI)");
        int sceltaI = scanner.nextInt();

        if (sceltaI == 1) {
            logger.info("Avvio della GUI in corso...");
            // Avviamo la classe JavaFX (che ora scriveremo)
            Application.launch(NautilusGUI.class, args);
        } else {
            logger.info("Avvio della CLI in corso...");
            // Qui chiameresti il tuo metodo per la versione testuale
            // es: new TerminalView().start();
        }
    }
}

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
package org.example;

import javafx.application.Application;
import org.example.model.dao.DAOFactory;

import javax.swing.*;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Imposta lo stile grafico delle finestre Swing per farle sembrare native (Windows/Mac)
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        // 1. SCHERMATA DI SCELTA DELLA PERSISTENZA
        String[] opzioniDB = {"Database MySQL", "File JSON"};
        int sceltaDB = JOptionPane.showOptionDialog(null,
                "Seleziona il sistema di persistenza dei dati:",
                "Nautilus Setup - Database",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opzioniDB, opzioniDB[0]);

        // Se l'utente chiude la finestra con la 'X', usciamo dal programma
        if (sceltaDB == JOptionPane.CLOSED_OPTION) System.exit(0);

        // Configuriamo la Factory!
        if (sceltaDB == 1) {
            DAOFactory.getInstance().setPersistenceType(DAOFactory.PersistenceType.FILE);
            logger.info("Configurazione: Modalità File System (JSON)");
        } else {
            DAOFactory.getInstance().setPersistenceType(DAOFactory.PersistenceType.DATABASE);
            logger.info("Configurazione: Modalità Database (MySQL)");
        }

        // 2. SCHERMATA DI SCELTA DELL'INTERFACCIA
        String[] opzioniGUI = {"Interfaccia Grafica (JavaFX)", "Terminale (CLI)"};
        int sceltaGUI = JOptionPane.showOptionDialog(null,
                "Seleziona l'interfaccia utente da avviare:",
                "Nautilus Setup - GUI",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opzioniGUI, opzioniGUI[0]);

        if (sceltaGUI == JOptionPane.CLOSED_OPTION) System.exit(0);

        // 3. AVVIO DELL'APPLICAZIONE SCELTA
        if (sceltaGUI == 0) {
            logger.info("Avvio Interfaccia Grafica in corso...");
            Application.launch(NautilusGUI.class, args);
        } else {
            logger.info("Avvio Interfaccia da Terminale in corso...");
            // 🌟 AVVIA IL ROUTER PRINCIPALE!
            new org.example.view.CLI.MainCLIView().start();
        }
    }
}
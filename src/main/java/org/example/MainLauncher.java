package org.example;

import org.example.model.dao.DAOFactory;
import javafx.application.Application;
import java.util.Scanner;

public class MainLauncher {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== NAUTILUS SYSTEMS - SETUP DI AVVIO ===");

        // 1. Scelta della Persistenza
        System.out.println("Seleziona il metodo di persistenza:");
        System.out.println("1. Database MySQL");
        System.out.println("2. File System (CSV/JSON)");
        int sceltaP = scanner.nextInt();

        if (sceltaP == 2) {
            DAOFactory.getInstance().setPersistenceType(DAOFactory.PersistenceType.FILE);
            System.out.println("[Config] Modalità File System impostata.");
        } else {
            DAOFactory.getInstance().setPersistenceType(DAOFactory.PersistenceType.DATABASE);
            System.out.println("[Config] Modalità MySQL impostata.");
        }

        // 2. Scelta dell'Interfaccia
        System.out.println("\nSeleziona l'interfaccia utente:");
        System.out.println("1. Interfaccia Grafica (GUI JavaFX)");
        System.out.println("2. Interfaccia da Terminale (CLI)");
        int sceltaI = scanner.nextInt();

        if (sceltaI == 1) {
            System.out.println("Avvio della GUI in corso...");
            // Avviamo la classe JavaFX (che ora scriveremo)
            Application.launch(NautilusGUI.class, args);
        } else {
            System.out.println("Avvio della CLI in corso...");
            // Qui chiameresti il tuo metodo per la versione testuale
            // es: new TerminalView().start();
        }
    }
}
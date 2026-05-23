package org.example.view.CLI;

import org.example.model.domain.Cliente;
import org.example.model.domain.Istruttore;
import org.example.model.domain.User;


import java.util.Scanner;


@SuppressWarnings("java:S106")
public class MainCLIView {

    public void start() {
        // Creiamo l'unico Scanner di tutta l'applicazione
        Scanner scanner = new Scanner(System.in);
        boolean inEsecuzione = true;

        System.out.println("\n========================================");
        System.out.println("   BENVENUTO IN NAUTILUS SYSTEMS CLI    ");
        System.out.println("========================================");

        while (inEsecuzione) {
            // 1. Deleghiamo il login alla tua LoginCLIView
            LoginCLIView loginView = new LoginCLIView(scanner);
            User utenteLoggato = loginView.mostraEAutentica();

            // 2. Controlliamo il risultato
            if (utenteLoggato == null) {
                inEsecuzione = false;
            } else if (utenteLoggato instanceof Cliente) {
                new ClienteCLIView(scanner, (Cliente) utenteLoggato).mostraMenu();
            } else if (utenteLoggato instanceof Istruttore) {
                new IstruttoreCLIView(scanner, (Istruttore) utenteLoggato).mostraMenu();
            }
        }

        System.out.println("Grazie per aver usato Nautilus. Arrivederci! 👋");
        scanner.close();
    }
}

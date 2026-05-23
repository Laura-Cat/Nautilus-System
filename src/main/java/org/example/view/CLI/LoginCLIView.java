package org.example.view.CLI;

import org.example.controller.LoginController;
import org.example.model.bean.LoginBean;
import org.example.model.domain.User;
import java.util.Scanner;

@SuppressWarnings("java:S106")
public class LoginCLIView {

    private final Scanner scanner;

    // Riceve lo Scanner dal MainCLIView per non doverne creare uno nuovo ogni volta
    public LoginCLIView(Scanner scanner) {
        this.scanner = scanner;
    }

    public User mostraEAutentica() {
        System.out.println("\n--- ACCESSO NAUTILUS ---");
        System.out.print("Email: ");
        String email = scanner.nextLine();

        if (email.equalsIgnoreCase("esci")) {
            return null;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();

        LoginBean credenziali = new LoginBean(email, password);
        boolean successo = LoginController.getInstance().autenticaUtente(credenziali);

        if (successo) {
            return LoginController.getInstance().getUtenteAttivo();
        } else {
            System.out.println("❌ Errore: Credenziali non valide.");
            return null;
        }
    }
}

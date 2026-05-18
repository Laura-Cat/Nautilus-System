
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Questa riga va a prendere il tuo file FXML dalla cartella resources
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/loginpage.fxml"));

        // Impostiamo il titolo della finestra e la mostriamo!
        primaryStage.setTitle("Nautilus Systems - Accesso");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false); // Impedisce di allargare la finestra
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Accende il motore di JavaFX
        launch(args);
    }
}
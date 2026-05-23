package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NautilusGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carichiamo la pagina di Login
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/loginpage.fxml"));

        primaryStage.setTitle("Nautilus Systems");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true); // Schermo intero
        primaryStage.show();
    }
}
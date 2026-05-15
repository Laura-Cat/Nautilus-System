package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NautilusGUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carichiamo il file FXML che abbiamo pulito prima
        Parent root = FXMLLoader.load(getClass().getResource("/view/loginpage.fxml"));
        primaryStage.setTitle("Nautilus Systems - Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
package org.example.model.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Istruttore extends User {

    private String specializzazione;

    // Costruttore: deve chiedere TUTTI i dati di User + la specializzazione
    public Istruttore(String cf, String nome, String cognome, LocalDate dataNascita, String luogoNascita, String indirizzo, String email, String password, String specializzazione) {
        // super() passa i dati alla classe padre User
        super(cf, nome, cognome, dataNascita, luogoNascita, indirizzo, email, password);
        this.specializzazione = specializzazione;
    }

    // Getter e Setter di specializzazione
    public String getSpecializzazione() { return specializzazione; }
    public void setSpecializzazione(String specializzazione) { this.specializzazione = specializzazione; }

}
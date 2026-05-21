package org.example.model.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Istruttore extends User {


    private String specializzazione;
    private String descrizione; // AGGIUNTO: serve per la biografia nel dettaglio
    private String fotoPath;    // AGGIUNTO: serve per il percorso della foto in JavaFX

    // 1. COSTRUTTORE VUOTO
    public Istruttore() {
        super(); // Chiama il costruttore vuoto di User (assicurati che User ce l'abbia!)
    }

    // 2. IL TUO COSTRUTTORE
    public Istruttore(Integer id, String cf, String nome, String cognome, LocalDate dataNascita,
                      String luogoNascita, String indirizzo, String email, String password,
                      String specializzazione, String descrizione, String fotoPath) {

        super(id, cf, nome, cognome, dataNascita, luogoNascita, indirizzo, email, password);

        this.specializzazione = specializzazione;
        this.descrizione = descrizione;
        this.fotoPath = fotoPath;
    }

    // --- GETTER E SETTER ---
    public String getSpecializzazione() { return specializzazione; }
    public void setSpecializzazione(String specializzazione) { this.specializzazione = specializzazione; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getFotoPath() { return fotoPath; }
    public void setFotoPath(String fotoPath) { this.fotoPath = fotoPath; }

}
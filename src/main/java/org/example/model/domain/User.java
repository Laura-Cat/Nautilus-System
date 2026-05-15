package org.example.model.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class User {

    private String cf;
    private String nome;
    private String cognome;
    private LocalDate dataNascita;
    private String luogoNascita;
    private String indirizzo;
    private String email;
    private String password;
    protected List<Notifica> notifichePersonali = new ArrayList<>();

    // Costruttore
    public User(String cf, String nome, String cognome, LocalDate dataNascita, String luogoNascita, String indirizzo, String email, String password) {
        this.cf = cf;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.luogoNascita = luogoNascita;
        this.indirizzo = indirizzo;
        this.email = email;
        this.password = password;
    }

    // Metodi

    public String getCf() {
        return cf;
    }

    public void setCf(String cf) {
        this.cf = cf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(LocalDate dataNascita) {
        this.dataNascita = dataNascita;
    }

    public String getLuogoNascita() {
        return luogoNascita;
    }
    public void setLuogoNascita(String luogoNascita) {
        this.luogoNascita = luogoNascita;
    }

    public String getIndirizzo() {
        return indirizzo;
    }
    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getNomeCompleto() {
        return this.nome + " " + this.cognome;
    }

    public boolean checkPassword(String pwd) {
        // Confronta la password inserita con quella salvata
        return this.password.equals(pwd);
    }

    // Metodo ereditato da tutti
    public void riceviNotifica(Notifica notifica) {
        this.notifichePersonali.add(notifica);
    }

    // Metodo ereditato da tutti
    public List<Notifica> getNotificheDaLeggere() {
        List<Notifica> daLeggere = new ArrayList<>();
        for (Notifica n : this.notifichePersonali) {
            if (!n.isLetta()) {
                daLeggere.add(n);
            }
        }
        return daLeggere;
    }

}
package org.example.model.domain;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Corso {

    private String  nome;
    private String  statoAttivita;
    private LocalDate dataInizio;
    private Integer idCorso;
    private Integer numPosti;
    private String descrizione;
    private List<Lezione> lezioni;

    // Costruttore
    public Corso(String nome, String statoAttivita, LocalDate dataInizio, Integer idCorso, Integer numPosti, String descrizione) {
        this.nome= nome;
        this.statoAttivita= statoAttivita;
        this.dataInizio=  dataInizio;
        this.idCorso= idCorso;
        this.numPosti= numPosti;
        this.descrizione= descrizione;
        this.lezioni = new ArrayList<>();
    }

    // Getter e Setter Nome
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    // Getter e Setter idCorso
    public int getIDCorso() {
        return idCorso;
    }
    public void setIDCorso(int idCorso) {
        this.idCorso = idCorso;
    }

    // Getter e Setter DataInizio
    public LocalDate getDataInizio() {
        return dataInizio;
    }
    public void setDataInizio(LocalDate dataInizio) {
        this.dataInizio = dataInizio;
    }

    // Getter e Setter StatoAttività
    public String getStatoAttivita() {
        return statoAttivita;
    }
    public void setStatoAttivita(String statoAttivita) {
        this.statoAttivita = statoAttivita;
    }

    // Getter e Setter NumPosti
    public int getNumPosti() {
        return numPosti;
    }
    public void setNumPosti(int numPosti) {
        this.numPosti = numPosti;
    }

    public boolean isAttivo() {
        return "Attivo".equalsIgnoreCase(this.statoAttivita);
    }


    public List<Lezione> getLezioniDisponibili() {
        List<Lezione> disponibili = new ArrayList<>();
        for (Lezione lezione : this.lezioni) {
            if (lezione.getPostiDisponibili() > 0) {
                disponibili.add(lezione);
            }
        }
        return disponibili;
    }

    public Lezione getLezioneInData(LocalDate dataCercata) {
        for (Lezione lezione : this.lezioni) {
            if (lezione.getData().equals(dataCercata)) {
                return lezione;
            }
        }
        return null;
    }

    // Getter speciale per leggere le lezioni senza farle modificare da fuori
    public List<Lezione> getLezioni() {
        return java.util.Collections.unmodifiableList(this.lezioni);
    }

    public void aggiungiLezione(Lezione lezione) {
        if (lezione != null) {
            lezione.setCorsoAppartenenza(this); // Colleghiamo i due oggetti
            lezione.setTipoAttivita(TipoAttivita.CORSO); // Automatizziamo il tipo
            this.lezioni.add(lezione);
        }
    }
}
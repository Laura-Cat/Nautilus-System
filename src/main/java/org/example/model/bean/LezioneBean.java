package org.example.model.bean;

import org.example.model.domain.TipoCorso;

public class LezioneBean {
    // Solo i dati "pronti" per essere stampati a video
    private int idLezione;
    private String orario;
    private String idCorsia;
    private int postiLiberi;
    private int postiTotali;
    private TipoCorso nomeCorso;

    // Costruttore vuoto
    public LezioneBean() {}



    // Getters e Setters
    public int getIdLezione() { return idLezione; }
    public void setIdLezione(int idLezione) { this.idLezione = idLezione; }

    public String getOrario() { return orario; }
    public void setOrario(String orario) { this.orario = orario; }

    public String getIdCorsia() { return idCorsia; }
    public void setIdCorsia(String idCorsia) { this.idCorsia = idCorsia; }

    public int getPostiLiberi() { return postiLiberi; }
    public void setPostiLiberi(int postiLiberi) { this.postiLiberi = postiLiberi; }

    public int getPostiTotali() { return postiTotali; }
    public void setPostiTotali(int postiTotali) { this.postiTotali = postiTotali; }

    public TipoCorso getNomeCorso() { return nomeCorso; }
    public void setNomeCorso(TipoCorso nomeCorso) { this.nomeCorso = nomeCorso; }

}

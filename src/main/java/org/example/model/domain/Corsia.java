package org.example.model.domain;

public class Corsia {

    private Integer idCorsia;
    private Integer numeroCorsia; // Es. Corsia 1, Corsia 2
    private Integer capienzaMassima; // Es. 5 nuotatori al massimo


    // Costruttore
    public Corsia(Integer idCorsia, Integer numeroCorsia, Integer capienzaMassima, String livello) {
        this.idCorsia = idCorsia;
        this.numeroCorsia = numeroCorsia;
        this.capienzaMassima = capienzaMassima;

    }

    // Getter Setter
    public Integer getIdCorsia() {
        return idCorsia;
    }
    public void setIdCorsia(Integer idCorsia) {
        this.idCorsia = idCorsia;
    }

    public Integer getNumeroCorsia() {
        return numeroCorsia;
    }
    public void setNumeroCorsia(Integer numeroCorsia) {
        this.numeroCorsia = numeroCorsia;
    }

    public Integer getCapienzaMassima() {
        return capienzaMassima;
    }
    public void setCapienzaMassima(Integer capienzaMassima) {
        this.capienzaMassima = capienzaMassima;
    }



}
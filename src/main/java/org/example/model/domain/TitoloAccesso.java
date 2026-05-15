package org.example.model.domain;

public abstract class TitoloAccesso {

    private Integer titoloID;

    // Costruttore
    public TitoloAccesso(Integer titoloID) {
        this.titoloID = titoloID;
    }
    // Metodi di business
    public abstract Boolean checkValidita(int costoInCrediti);
    public abstract void registraAccesso(int costoInCrediti);

    // Getter e Setter
    public Integer getTitoloID() { return titoloID; }
    public void setTitoloID(Integer titoloID) { this.titoloID = titoloID; }
}

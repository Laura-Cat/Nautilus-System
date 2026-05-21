package org.example.model.domain;

public abstract class TitoloAccesso {

    private Integer titoloId;

    // Costruttore
    public TitoloAccesso(Integer titoloId) {
        this.titoloId = titoloId;
    }
    public TitoloAccesso() {
    }
    // Metodi di business
    public abstract Boolean checkValidita(int costoInCrediti);
    public abstract void registraAccesso(int costoInCrediti);

    // Getter e Setter
    public Integer getTitoloId() { return titoloId; }
    public void setTitoloId(Integer titoloId) { this.titoloId = titoloId; }
}

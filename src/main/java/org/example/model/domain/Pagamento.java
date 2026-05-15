package org.example.model.domain;

import java.time.LocalDate;

public class Pagamento {

    private String idTransazione;
    private Float importo;
    private LocalDate dataAcquisto;
    private String stato;
    private Cliente cliente;
    private TitoloAccesso titoloAcquistato;

    public Pagamento(String idTransazione, Float importo, LocalDate dataAcquisto, Cliente cliente, TitoloAccesso titoloAcquistato) {
        this.idTransazione = idTransazione;
        this.importo = importo;
        this.dataAcquisto = dataAcquisto;
        this.cliente = cliente;
        this.titoloAcquistato = titoloAcquistato;
        this.stato = "In elaborazione";
    }

    // Metodi di business

    public void impostaStato(String nuovoStato) {
        this.stato = nuovoStato;
    }

    public Boolean isCompletato() {
        return "Completato".equalsIgnoreCase(this.stato);
    }

    // Getter e setter

    public String getIdTransazione() { return idTransazione; }
    public void setIdTransazione(String idTransazione) { this.idTransazione = idTransazione; }

    public Float getImporto() { return importo; }
    public void setImporto(Float importo) { this.importo = importo; }

    public LocalDate getDataAcquisto() { return dataAcquisto; }
    public void setDataAcquisto(LocalDate dataAcquisto) { this.dataAcquisto = dataAcquisto; }

    public String getStato() { return stato; }
    // Il setStato() non lo metto perché abbiamo già impostaStato() definito dal tuo UML!

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public TitoloAccesso getTitoloAcquistato() { return titoloAcquistato; }
    public void setTitoloAcquistato(TitoloAccesso titoloAcquistato) { this.titoloAcquistato = titoloAcquistato; }

}
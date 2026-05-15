package org.example.model.domain;

import java.time.LocalDate;

public class Prenotazione {

    // --- 1. ATTRIBUTI DAL TUO UML ---
    private Integer id;
    private LocalDate dataRichiesta;
    private String stato;
    private String tipologia;
    private Cliente cliente;
    private Lezione lezionePrenotata;
    private Corsia corsiaPrenotata;

    // Costruttore
    public Prenotazione(Integer id, LocalDate dataRichiesta, String tipologia, Cliente cliente) {
        this.id = id;
        this.dataRichiesta = dataRichiesta;
        this.tipologia = tipologia;
        this.cliente = cliente;
        this.stato = "In Attesa";
    }

    // Metodi di business

    public void conferma() {
        this.stato = "Confermata";
    }

    public void rifiuta() {
        this.stato = "Rifiutata";
    }

    public Boolean inAttesa() {
        return "In Attesa".equalsIgnoreCase(this.stato);
    }

    // Getter Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDate getDataRichiesta() { return dataRichiesta; }
    public void setDataRichiesta(LocalDate dataRichiesta) { this.dataRichiesta = dataRichiesta; }


    public void attesaAccettazione() {
        this.stato = "In Attesa di Accettazione";
    }
    public void accettataDaIstruttore() {
        this.stato = "Accettata - In attesa di pagamento";
    }
    public void pagata() {
        this.stato = "Confermata e Pagata";
    }

    public Boolean isProntaPerPagamento() {
        return "Accettata - In attesa di pagamento".equalsIgnoreCase(this.stato);
    }

    public String getTipologia() { return tipologia; }
    public void setTipologia(String tipologia) { this.tipologia = tipologia; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Lezione getLezionePrenotata() { return lezionePrenotata; }
    public void setLezionePrenotata(Lezione lezionePrenotata) { this.lezionePrenotata = lezionePrenotata; }

    public Corsia getCorsiaPrenotata() { return corsiaPrenotata;    }
    public void setCorsiaPrenotata(Corsia corsiaPrenotata) { this.corsiaPrenotata = corsiaPrenotata;}

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}
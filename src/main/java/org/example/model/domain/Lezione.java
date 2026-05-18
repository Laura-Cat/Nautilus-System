package org.example.model.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class Lezione {

    private Integer idLezione;
    private LocalDate data;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private Integer numPostiTotali;
    private Integer numPostiPrenotati;
    private Istruttore istruttore;
    private Corso corsoAppartenenza;
    private Corsia corsiaAssegnata;
    private TipoAttivita tipoAttivita;

    //Costruttore
    public Lezione(Integer idLezione, LocalDate data, LocalTime oraInizio, LocalTime oraFine, Integer numPostiPrenotati, TipoAttivita tipoAttivita){
        this.idLezione=idLezione;
        this.data=data;
        this.oraInizio=oraInizio;
        this.oraFine = oraFine;
        this.numPostiPrenotati=numPostiPrenotati;
        this.tipoAttivita= tipoAttivita;

    }

    // Getter e Setter
    public Integer getIdLezione() {
        return idLezione;
    }
    public void setIdLezione(Integer idLezione) {
        this.idLezione = idLezione;
    }

    public LocalTime getOraInizio() { return oraInizio;}
    public void setOraInizio(LocalTime oraInizio) { this.oraInizio = oraInizio; }

    public LocalTime getOraFine() { return oraFine;}
    public void setOraFine(LocalTime oraFine) { this.oraFine = oraFine; }

    public LocalDate getData() { return data;}
    public void setData(LocalDate data) { this.data = data; }

    public TipoAttivita getTipoAttivita() {
        return this.tipoAttivita;
    }
    public void setTipoAttivita(TipoAttivita tipoAttivita) {
        this.tipoAttivita = tipoAttivita;
    }

    public Integer getNumPostiTotali() { return this.corsiaAssegnata.getCapienzaMassima(); }
    public void setNumPostiTotali(int numPostiTotali) { this.numPostiTotali = numPostiTotali; }

    public Integer getNumPostiPrenotati() {
        return numPostiPrenotati;
    }
    public void setNumPostiPrenotati(int numPostiPrenotati) { this.numPostiPrenotati = numPostiPrenotati; }

    public Corso getCorsoAppartenenza() { return corsoAppartenenza; }
    public void setCorsoAppartenenza(Corso corso) { this.corsoAppartenenza = corso; }

    public Istruttore getIstruttore() { return istruttore; }
    public void setIstruttore(Istruttore istruttore) {this.istruttore = istruttore;}

    public Corsia getCorsiaAssegnata() { return corsiaAssegnata; }
    public void setCorsiaAssegnata(Corsia corsiaAssegnata) { this.corsiaAssegnata = corsiaAssegnata; }

    // Metodi di business
    public Integer getPostiDisponibili(){
        return (numPostiTotali-numPostiPrenotati);
    }




}

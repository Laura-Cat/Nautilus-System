package org.example.model.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class Lezione {

    private Integer idLezione;
    private LocalDate data;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private Integer numPostiTotali;       // ora usato direttamente
    private Integer numPostiPrenotati;
    private Istruttore istruttore;
    private Corso corsoAppartenenza;
    private Corsia corsiaAssegnata;
    private TipoAttivita tipoAttivita;
    private String infoClientePrivata;
    private String noteClientePrivata;

    // Costruttore aggiornato: aggiunto numPostiTotali
    public Lezione(Integer idLezione, LocalDate data, LocalTime oraInizio, LocalTime oraFine,
                   Integer numPostiPrenotati, Integer numPostiTotali, TipoAttivita tipoAttivita) {
        this.idLezione = idLezione;
        this.data = data;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
        this.numPostiPrenotati = numPostiPrenotati;
        this.numPostiTotali = numPostiTotali;
        this.tipoAttivita = tipoAttivita;
    }

    public Lezione() {
    }
    // FIX: non dipende più da corsiaAssegnata (che può essere null)
    public Integer getNumPostiTotali() {
        return this.numPostiTotali != null ? this.numPostiTotali : 0;
    }
    public void setNumPostiTotali(int numPostiTotali) {
        this.numPostiTotali = numPostiTotali;
    }

    public Integer getPostiDisponibili() {
        return getNumPostiTotali() - (numPostiPrenotati != null ? numPostiPrenotati : 0);
    }

    public void setNumPostiPrenotati(Integer numPostiPrenotati) {
        this.numPostiPrenotati = numPostiPrenotati;
    }
    public void setNumPostiTotali(Integer numPostiTotali) {
        this.numPostiTotali = numPostiTotali;
    }

    // Tutti gli altri getter/setter rimangono invariati
    public Integer getIdLezione() { return idLezione; }
    public void setIdLezione(Integer idLezione) { this.idLezione = idLezione; }

    public LocalTime getOraInizio() { return oraInizio; }
    public void setOraInizio(LocalTime oraInizio) { this.oraInizio = oraInizio; }

    public LocalTime getOraFine() { return oraFine; }
    public void setOraFine(LocalTime oraFine) { this.oraFine = oraFine; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public TipoAttivita getTipoAttivita() { return this.tipoAttivita; }
    public void setTipoAttivita(TipoAttivita tipoAttivita) { this.tipoAttivita = tipoAttivita; }

    public Integer getNumPostiPrenotati() { return numPostiPrenotati; }
    public void setNumPostiPrenotati(int numPostiPrenotati) { this.numPostiPrenotati = numPostiPrenotati; }

    public Corso getCorsoAppartenenza() { return corsoAppartenenza; }
    public void setCorsoAppartenenza(Corso corso) { this.corsoAppartenenza = corso; }

    public Istruttore getIstruttore() { return istruttore; }
    public void setIstruttore(Istruttore istruttore) { this.istruttore = istruttore; }

    public Corsia getCorsiaAssegnata() { return corsiaAssegnata; }
    public void setCorsiaAssegnata(Corsia corsiaAssegnata) { this.corsiaAssegnata = corsiaAssegnata; }

    public String getInfoClientePrivata() {
        return infoClientePrivata;
    }

    public void setInfoClientePrivata(String infoClientePrivata) {
        this.infoClientePrivata = infoClientePrivata;
    }

    public String getNoteClientePrivata() {
        return noteClientePrivata;
    }

    public void setNoteClientePrivata(String noteClientePrivata) {
        this.noteClientePrivata = noteClientePrivata;
    }
}
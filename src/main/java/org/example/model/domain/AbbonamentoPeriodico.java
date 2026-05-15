package org.example.model.domain;

import java.time.LocalDate;

public class AbbonamentoPeriodico extends TitoloAccesso {

    private LocalDate dataInizio;
    private LocalDate dataFine;

    public AbbonamentoPeriodico(Integer titoloID, LocalDate dataInizio, LocalDate dataFine) {
        super(titoloID);
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
    }

    @Override
    public Boolean checkValidita(int costoIgnorato) {
        LocalDate oggi = LocalDate.now();
        return !oggi.isBefore(dataInizio) && !oggi.isAfter(dataFine);
    }

    @Override
    public void registraAccesso(int costoInCrediti) {
        System.out.println("Accesso registrato tramite abbonamento illimitato.");
    }

    public void rinnova(int mesiDaAggiungere) {
        if (mesiDaAggiungere <= 0) {
            throw new IllegalArgumentException("I mesi da aggiungere devono essere maggiori di zero.");
        }

        LocalDate oggi = LocalDate.now();

        // Se è già scaduto, il rinnovo parte da oggi
        if (this.dataFine.isBefore(oggi)) {
            this.dataFine = oggi.plusMonths(mesiDaAggiungere);
        }
        // Se è ancora valido, prolunghiamo la scadenza attuale
        else {
            this.dataFine = this.dataFine.plusMonths(mesiDaAggiungere);
        }
    }

    // Getter e Setter
    public LocalDate getDataInizio() { return dataInizio; }
    public void setDataInizio(LocalDate dataInizio) { this.dataInizio = dataInizio; }

    public LocalDate getDataFine() { return dataFine; }
    public void setDataFine(LocalDate dataFine) { this.dataFine = dataFine; }
}
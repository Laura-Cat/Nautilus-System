package org.example.model.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class AbbonamentoPeriodico extends TitoloAccesso {
    private static final Logger logger = Logger.getLogger(AbbonamentoPeriodico.class.getName());
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private List<TipoCorso> corsiInclusi;

    public AbbonamentoPeriodico(Integer titoloID, LocalDate dataInizio, LocalDate dataFine) {
        super(titoloID);
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
    }

    public AbbonamentoPeriodico() {
    }

    @Override
    public Boolean checkValidita(int costoIgnorato) {
        LocalDate oggi = LocalDate.now();
        return !oggi.isBefore(dataInizio) && !oggi.isAfter(dataFine);
    }

    @Override
    public void registraAccesso(int costoInCrediti) {
        logger.info("Accesso registrato tramite abbonamento illimitato.");
    }

    public void rinnova(int mesiDaAggiungere) {
        if (mesiDaAggiungere <= 0) {
            throw new IllegalArgumentException("I mesi da aggiungere devono essere maggiori di zero.");
        }
        LocalDate oggi = LocalDate.now();
        if (this.dataFine.isBefore(oggi)) {
            this.dataFine = oggi.plusMonths(mesiDaAggiungere);
        }
        else {
            this.dataFine = this.dataFine.plusMonths(mesiDaAggiungere);
        }
    }

    // Getter e Setter
    public LocalDate getDataInizio() { return dataInizio; }
    public void setDataInizio(LocalDate dataInizio) { this.dataInizio = dataInizio; }

    public LocalDate getDataFine() { return dataFine; }
    public void setDataFine(LocalDate dataFine) { this.dataFine = dataFine; }

    public List<TipoCorso> getCorsiInclusi() {
        return corsiInclusi;
    }
    public void setCorsiInclusi(List<TipoCorso> corsiInclusi) {
        this.corsiInclusi = corsiInclusi;
    }
}
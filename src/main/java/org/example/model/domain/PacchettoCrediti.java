package org.example.model.domain;

import java.util.logging.Logger;

public class PacchettoCrediti  extends TitoloAccesso{
    private static final Logger logger = Logger.getLogger(PacchettoCrediti.class.getName());
    private Integer creditiRimanenti;

    public PacchettoCrediti(Integer titoloID, Integer creditiTotali) {
        super(titoloID);
        this.creditiRimanenti = creditiTotali; // All'inizio i rimanenti sono uguali ai totali
    }

    // Metodi di business (Information Expert)
    @Override
    public void registraAccesso(int costoInCrediti) {
        if (this.creditiRimanenti >= costoInCrediti) {
            this.creditiRimanenti -= costoInCrediti;
        } else {
            // Opzionale: potresti lanciare un'eccezione se uno cerca di scalare senza crediti
            logger.severe("Errore: Crediti insufficienti!");
        }
    }

    @Override
    public Boolean checkValidita(int costoInCrediti) {
        return this.creditiRimanenti >= costoInCrediti; // Corretto: Maggiore o uguale
    }

    public void aggiungiCrediti(int nuoviCrediti) {
        if (nuoviCrediti > 0) {
            this.creditiRimanenti += nuoviCrediti;
        } else {
            throw new IllegalArgumentException("I crediti da aggiungere devono essere maggiori di zero.");
        }
    }

    public Integer getCreditiRimanenti() {
        return creditiRimanenti;
    }
    public void setCreditiRimanenti(Integer creditiRimanenti) {
        this.creditiRimanenti = creditiRimanenti;
    }
}

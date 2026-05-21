package org.example.controller.strategy;

import org.example.exception.CreditiInsufficientiException;
import org.example.model.domain.Cliente;
import org.example.model.domain.Lezione;


public interface PrenotazioneStrategy {
        // Aggiungiamo il "throws CreditiInsufficientiException" alla firma
        boolean eseguiPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException;
    }
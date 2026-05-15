package org.example.exception;


public class CreditiInsufficientiException extends Exception {

    // Costruttore che accetta il messaggio di errore personalizzato
    public CreditiInsufficientiException(String messaggio) {
        super(messaggio);
    }
}

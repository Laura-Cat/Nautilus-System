package org.example.model.domain;

import java.time.LocalDate;

public class Cliente extends User {

    private Integer clienteID;
    private Boolean certificatoValido;
    private TitoloAccesso titoloAccesso;

    // Costruttore
    public Cliente(String cf, String nome, String cognome, LocalDate dataNascita, String luogoNascita, String indirizzo, String email, String password, Integer clienteID, Boolean certificatoValido) {
        super(cf, nome, cognome, dataNascita, luogoNascita, indirizzo, email, password);
        this.clienteID = clienteID;
        this.certificatoValido = certificatoValido;
    }

    // Metodi
    public Boolean verificaValiditaCertificato() {
        return this.certificatoValido;
    }

    public Boolean haTitoloValido(int costoInCrediti) {
        // Se non ha nessun abbonamento/pacchetto associato
        if (this.titoloAccesso == null) {
            return false;
        }
        return this.titoloAccesso.checkValidita(costoInCrediti);
    }

    // Getter e Setter ClienteID
    public Integer getClienteID() { return clienteID; }
    public void setClienteID(Integer clienteID) { this.clienteID = clienteID; }

    // Getter e Setter CertificatoValido
    public Boolean getCertificatoValido() { return certificatoValido; }
    public void setCertificatoValido(Boolean certificatoValido) { this.certificatoValido = certificatoValido; }

    public TitoloAccesso getTitoloAccesso() {
        return titoloAccesso;
    }

    public void setTitoloAccesso(TitoloAccesso titoloAccesso) {
        this.titoloAccesso = titoloAccesso;
    }
}

package org.example.model.domain;

import java.time.LocalDate;

public class Cliente extends User {


    private Boolean certificatoValido;
    private TitoloAccesso titoloAccesso;

    // Costruttore
    public Cliente(Integer id, String cf, String nome, String cognome, LocalDate dataNascita, String luogoNascita, String indirizzo, String email, String password, Integer clienteId, Boolean certificatoValido) {
        super(id, cf, nome, cognome, dataNascita, luogoNascita, indirizzo, email, password);
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

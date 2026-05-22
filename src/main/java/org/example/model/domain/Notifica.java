package org.example.model.domain;

import java.time.LocalDateTime;

public class Notifica {
    private Integer id;
    private String messaggio;
    private User destinatario; // A chi è rivolta?
    private boolean letta;           // L'ha già aperta?
    private LocalDateTime dataInvio;
    private String tipo;
    private Integer idRiferimento;
    // Costruttore
    public Notifica(String messaggio, User destinatario) {
        this.messaggio = messaggio;
        this.destinatario = destinatario;
        this.letta = false; // Di default una nuova notifica non è letta
        this.dataInvio = LocalDateTime.now();
    }

    public Notifica(){

    }

    public Integer getIdRiferimento() {
        return idRiferimento;
    }

    public void setIdRiferimento(Integer idRiferimento) {
        this.idRiferimento = idRiferimento;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // Metodo di business utile
    public void segnaComeLetta() {
        this.letta = true;
    }

    // Getter e Setter
    public String getMessaggio() { return messaggio; }
    public boolean isLetta() { return letta; }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public User getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(User destinatario) {
        this.destinatario = destinatario;
    }

    public void setLetta(boolean letta) {
        this.letta = letta;
    }

    public LocalDateTime getDataInvio() {
        return dataInvio;
    }

    public void setDataInvio(LocalDateTime dataInvio) {
        this.dataInvio = dataInvio;
    }
}

package org.example.model.dao.interfaces;

import org.example.model.domain.Prenotazione;

import java.time.LocalDate;
import java.util.List;

public interface PrenotazioneDAO {
    // Salva la prenotazione (usato quando il cliente prenota)
    void salva(Prenotazione p);

    // Recupera la lista di prenotazioni di un cliente (per la sua area personale)
    List<Prenotazione> trovaAgendatiCliente(Integer idCliente, LocalDate dataInizio, LocalDate dataFine);

    // Aggiorna lo stato (es. da "In Attesa" a "Confermata")
    void aggiornaStato(Prenotazione p);

    // Recupera una singola prenotazione tramite ID
    Prenotazione trovaPerId(Integer id);

    List<Prenotazione> trovaInAttesaPerIstruttore(Integer idIstruttore);

    public boolean esisteGia(int idCliente, int idLezione);
}
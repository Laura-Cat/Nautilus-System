package org.example.model.dao.Interface;

import org.example.model.domain.Prenotazione;
import org.example.model.domain.Cliente;
import java.util.List;

public interface PrenotazioneDAO {
    // Salva la prenotazione (usato quando il cliente prenota)
    void salva(Prenotazione p);

    // Recupera la lista di prenotazioni di un cliente (per la sua area personale)
    List<Prenotazione> trovaPerCliente(Cliente c);

    // Aggiorna lo stato (es. da "In Attesa" a "Confermata")
    void aggiornaStato(Prenotazione p);

    // Recupera una singola prenotazione tramite ID
    Prenotazione trovaPerId(Integer id);
}
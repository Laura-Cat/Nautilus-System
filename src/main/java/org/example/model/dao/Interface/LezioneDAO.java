package org.example.model.dao.Interface;

import org.example.model.domain.Lezione;
import org.example.model.domain.TipoAttivita;
import java.time.LocalDate;
import java.util.List;

public interface LezioneDAO {
    // Recupera le lezioni di un certo tipo in una data specifica
    List<Lezione> trovaPerTipoEData(TipoAttivita tipo, LocalDate data);

    // Recupera una singola lezione tramite ID
    Lezione trovaPerId(Integer id);

    // Aggiorna il numero di posti occupati nel DB
    void aggiornaPostiOccupati(Lezione lezione);
}
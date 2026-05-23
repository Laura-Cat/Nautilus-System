package org.example.model.dao.interfaces;

import org.example.model.domain.AbbonamentoPeriodico;
import org.example.model.domain.PacchettoCrediti;
import org.example.model.domain.TitoloAccesso;

public interface TitoloAccessoDAO {
    // 1. Salva un nuovo titolo appena comprato nel DB
    void salvaNuovo(TitoloAccesso titolo, Integer idCliente);

    // 2. Aggiorna i crediti rimanenti di un pacchetto
    void aggiornaCrediti(PacchettoCrediti pacchetto);

    // 3. Aggiorna le date di un abbonamento (quando viene rinnovato)
    void aggiornaRinnovo(AbbonamentoPeriodico abbonamento);

    // 4. (Opzionale per il futuro) Recupera il titolo dal DB quando il cliente fa login
    TitoloAccesso trovaPerCliente(Integer idCliente);
}
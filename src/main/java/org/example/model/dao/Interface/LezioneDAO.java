package org.example.model.dao.Interface;

import org.example.model.domain.Lezione;
import org.example.model.domain.TipoAttivita;
import org.example.model.domain.TipoCorso;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface LezioneDAO {

    List<Lezione> trovaPerTipoEData(TipoAttivita tipo, LocalDate data);
    public List<Lezione> trovaPerCorso(TipoCorso tipoCorso);
    Lezione trovaPerId(Integer id);
    List<Lezione> trovaPrivateDisponibiliPerIstruttore(Integer idIstruttore);
    List<Lezione> trovaImpegniIstruttore(Integer idIstruttore, LocalDate dataInizio, LocalDate dataFine);
    void aggiornaPostiOccupati(Lezione lezione);
}
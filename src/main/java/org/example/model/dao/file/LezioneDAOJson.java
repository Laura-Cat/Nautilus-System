package org.example.model.dao.file;

import com.google.gson.reflect.TypeToken;
import org.example.model.dao.Interface.LezioneDAO;
import org.example.model.domain.Lezione;
import org.example.model.domain.TipoAttivita;
import org.example.model.domain.TipoCorso;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LezioneDAOJson implements LezioneDAO {

    private static final String FILE_LEZIONI = "data/lezioni.json";
    private final Type typeToken = new TypeToken<List<Lezione>>(){}.getType();

    @Override
    public List<Lezione> trovaPerTipoEData(TipoAttivita tipo, LocalDate data) {
        List<Lezione> tutte = JsonUtility.leggiLista(FILE_LEZIONI, typeToken);
        List<Lezione> filtrate = new ArrayList<>();

        for (Lezione l : tutte) {
            if (l.getTipoAttivita() == tipo && l.getData().equals(data)) {
                filtrate.add(l);
            }
        }
        return filtrate;
    }

    @Override
    public List<Lezione> trovaPerCorso(TipoCorso tipoCorso) {
        List<Lezione> tutte = JsonUtility.leggiLista(FILE_LEZIONI, typeToken);
        List<Lezione> filtrate = new ArrayList<>();

        for (Lezione l : tutte) {
            if (l.getCorsoAppartenenza() != null && l.getCorsoAppartenenza().getNome() == tipoCorso) {
                filtrate.add(l);
            }
        }
        return filtrate;
    }

    @Override
    public Lezione trovaPerId(Integer id) {
        if (id == null) return null;
        List<Lezione> tutte = JsonUtility.leggiLista(FILE_LEZIONI, typeToken);
        for (Lezione l : tutte) {
            if (l.getIdLezione().equals(id)) return l;
        }
        return null;
    }

    @Override
    public List<Lezione> trovaPrivateDisponibiliPerIstruttore(Integer idIstruttore) {
        List<Lezione> tutte = JsonUtility.leggiLista(FILE_LEZIONI, typeToken);
        List<Lezione> filtrate = new ArrayList<>();

        for (Lezione l : tutte) {
            // Se è privata, è di quell'istruttore, non ha prenotazioni ed è nel futuro (o oggi)
            if (l.getTipoAttivita() == TipoAttivita.PRIVATA &&
                    l.getIstruttore() != null && l.getIstruttore().getId().equals(idIstruttore) &&
                    (l.getNumPostiPrenotati() == null || l.getNumPostiPrenotati() == 0) &&
                    !l.getData().isBefore(LocalDate.now())) {

                filtrate.add(l);
            }
        }
        return filtrate;
    }

    @Override
    public List<Lezione> trovaImpegniIstruttore(Integer idIstruttore, LocalDate dataInizio, LocalDate dataFine) {
        List<Lezione> tutte = JsonUtility.leggiLista(FILE_LEZIONI, typeToken);
        List<Lezione> filtrate = new ArrayList<>();

        for (Lezione l : tutte) {
            if (l.getIstruttore() != null && l.getIstruttore().getId().equals(idIstruttore)) {
                if (!l.getData().isBefore(dataInizio) && !l.getData().isAfter(dataFine)) {
                    filtrate.add(l);
                }
            }
        }
        return filtrate;
    }

    @Override
    public void aggiornaPostiOccupati(Lezione lezione) {
        List<Lezione> tutte = JsonUtility.leggiLista(FILE_LEZIONI, typeToken);

        // Cerchiamo la lezione vecchia e la sostituiamo con quella aggiornata
        for (int i = 0; i < tutte.size(); i++) {
            if (tutte.get(i).getIdLezione().equals(lezione.getIdLezione())) {
                tutte.set(i, lezione);
                break;
            }
        }
        // Sovrascriviamo il file
        JsonUtility.scriviLista(FILE_LEZIONI, tutte);
    }
}
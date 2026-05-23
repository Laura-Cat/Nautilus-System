package org.example.model.dao.file;

import com.google.gson.reflect.TypeToken;
import org.example.model.dao.Interface.CorsoDAO;
import org.example.model.domain.Corso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CorsoDAOJson implements CorsoDAO {

    private static final String FILE_CORSI = "data/corsi.json";
    private final Type typeToken = new TypeToken<List<Corso>>(){}.getType();

    @Override
    public List<Corso> recuperaCorsiAttivi() {
        List<Corso> tutti = JsonUtility.leggiLista(FILE_CORSI, typeToken);
        List<Corso> attivi = new ArrayList<>();

        for (Corso c : tutti) {
            if ("Attivo".equalsIgnoreCase(c.getStatoAttivita())) {
                attivi.add(c);
            }
        }
        return attivi;
    }
}

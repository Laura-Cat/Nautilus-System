package org.example.model.dao.file;

import com.google.gson.reflect.TypeToken;
import org.example.model.dao.Interface.IstruttoreDAO;
import org.example.model.domain.Istruttore;

import java.lang.reflect.Type;
import java.util.List;

public class IstruttoreDAOJson implements IstruttoreDAO {

    private static final String FILE_ISTRUTTORI = "data/istruttori.json";
    private final Type typeToken = new TypeToken<List<Istruttore>>(){}.getType();

    @Override
    public List<Istruttore> recuperaTutti() {
        return JsonUtility.leggiLista(FILE_ISTRUTTORI, typeToken);
    }

    @Override
    public Istruttore trovaPerId(Integer id) {
        if (id == null) return null;

        List<Istruttore> tutti = JsonUtility.leggiLista(FILE_ISTRUTTORI, typeToken);
        for (Istruttore i : tutti) {
            if (i.getId().equals(id)) {
                return i;
            }
        }
        return null;
    }
}
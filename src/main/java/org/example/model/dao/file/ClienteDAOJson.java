package org.example.model.dao.file;

import com.google.gson.reflect.TypeToken;
import org.example.model.dao.interfaces.ClienteDAO;
import org.example.model.domain.Cliente;

import java.lang.reflect.Type;
import java.util.List;

public class ClienteDAOJson implements ClienteDAO {

    private static final String FILE_CLIENTI = "data/clienti.json";
    private final Type typeToken = new TypeToken<List<Cliente>>(){}.getType();

    @Override
    public Cliente trovaPerId(Integer id) {
        if (id == null) return null;

        List<Cliente> tutti = JsonUtility.leggiLista(FILE_CLIENTI, typeToken);
        for (Cliente c : tutti) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }
}
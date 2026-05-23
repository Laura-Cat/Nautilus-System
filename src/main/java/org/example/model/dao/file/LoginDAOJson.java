package org.example.model.dao.file;

import com.google.gson.reflect.TypeToken;
import org.example.model.dao.interfaces.LoginDAO;
import org.example.model.domain.Cliente;
import org.example.model.domain.Istruttore;
import org.example.model.domain.User;

import java.lang.reflect.Type;
import java.util.List;

public class LoginDAOJson implements LoginDAO {

    // Percorsi dei file (assicurati che la cartella 'data' esista nella root del progetto)
    private static final String FILE_CLIENTI = "data/clienti.json";
    private static final String FILE_ISTRUTTORI = "data/istruttori.json";

    @Override
    public User trovaPerCredenziali(String email, String password) {

        Type tipoListaClienti = new TypeToken<List<Cliente>>(){}.getType();
        List<Cliente> clienti = JsonUtility.leggiLista(FILE_CLIENTI, tipoListaClienti);

        for (Cliente c : clienti) {
            if (c.getEmail().equals(email) && c.getPassword().equals(password)) {
                return c;
            }
        }

        Type tipoListaIstruttori = new TypeToken<List<Istruttore>>(){}.getType();
        List<Istruttore> istruttori = JsonUtility.leggiLista(FILE_ISTRUTTORI, tipoListaIstruttori);

        for (Istruttore i : istruttori) {
            if (i.getEmail().equals(email) && i.getPassword().equals(password)) {
                return i;
            }
        }

        return null;
    }
}

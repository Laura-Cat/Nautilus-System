package org.example.model.dao.file;

import com.google.gson.reflect.TypeToken;
import org.example.model.dao.interfaces.NotificaDAO;
import org.example.model.domain.Cliente;
import org.example.model.domain.Notifica;
import org.example.model.domain.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotificaDAOJson implements NotificaDAO {

    private static final String FILE_NOTIFICHE = "data/notifiche.json";
    private final Type typeToken = new TypeToken<List<Notifica>>(){}.getType();

    @Override
    public boolean invia(Notifica notifica, Integer idDestinatario) {
        List<Notifica> tutte = JsonUtility.leggiLista(FILE_NOTIFICHE, typeToken);

        // 1. Calcoliamo l'ID progressivo simulando l'AUTO_INCREMENT
        int maxId = 0;
        for (Notifica n : tutte) {
            if (n.getId() != null && n.getId() > maxId) {
                maxId = n.getId();
            }
        }
        notifica.setId(maxId + 1);

        // 2. Assicuriamoci che l'ID del destinatario venga agganciato correttamente
        if (notifica.getDestinatario() == null) {
            // Creiamo un "finto" utente solo per fare da contenitore per l'ID nel file JSON
            Cliente fintoDestinatario = new Cliente(idDestinatario, null, null, null, null, null, null, null, null, idDestinatario, true);
            notifica.setDestinatario(fintoDestinatario);
        }

        tutte.add(notifica);
        JsonUtility.scriviLista(FILE_NOTIFICHE, tutte);
        return true;
    }

    @Override
    public List<Notifica> recuperaNonLettePerUtente(User utente, Integer idUtente) {
        List<Notifica> tutte = JsonUtility.leggiLista(FILE_NOTIFICHE, typeToken);
        List<Notifica> filtrate = new ArrayList<>();

        for (Notifica n : tutte) {
            // Verifichiamo che il destinatario esista e che l'ID coincida
            if (n.getDestinatario() != null && n.getDestinatario().getId().equals(idUtente)) {
                if (!n.getLetta()) {
                    filtrate.add(n);
                }
            }
        }
        return filtrate;
    }

    @Override
    public List<Notifica> recuperaTuttePerUtente(User utente, Integer idDestinatario) {
        List<Notifica> tutte = JsonUtility.leggiLista(FILE_NOTIFICHE, typeToken);
        List<Notifica> filtrate = new ArrayList<>();

        for (Notifica n : tutte) {
            if (n.getDestinatario() != null && n.getDestinatario().getId().equals(idDestinatario)) {
                filtrate.add(n);
            }
        }
        return filtrate;
    }

    @Override
    public boolean aggiornaStato(Notifica notifica) {
        List<Notifica> tutte = JsonUtility.leggiLista(FILE_NOTIFICHE, typeToken);
        boolean aggiornato = false;

        for (int i = 0; i < tutte.size(); i++) {
            if (tutte.get(i).getId().equals(notifica.getId())) {
                tutte.set(i, notifica); // Sostituiamo la vecchia notifica con quella nuova (letta)
                aggiornato = true;
                break;
            }
        }

        if (aggiornato) {
            JsonUtility.scriviLista(FILE_NOTIFICHE, tutte);
        }
        return aggiornato;
    }
}
package org.example.model.dao.file;

import com.google.gson.reflect.TypeToken;
import org.example.model.dao.Interface.PrenotazioneDAO;
import org.example.model.domain.Prenotazione;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrenotazioneDAOJson implements PrenotazioneDAO {

    private static final String FILE_PRENOTAZIONI = "data/prenotazioni.json";
    private final Type typeToken = new TypeToken<List<Prenotazione>>(){}.getType();

    @Override
    public void salva(Prenotazione p) {
        List<Prenotazione> tutte = JsonUtility.leggiLista(FILE_PRENOTAZIONI, typeToken);

        // Simuliamo l'AUTO_INCREMENT del Database
        int maxId = 0;
        for (Prenotazione esistente : tutte) {
            if (esistente.getId() != null && esistente.getId() > maxId) {
                maxId = esistente.getId();
            }
        }
        p.setId(maxId + 1);

        // Aggiungiamo alla lista e salviamo
        tutte.add(p);
        JsonUtility.scriviLista(FILE_PRENOTAZIONI, tutte);
    }

    @Override
    public void aggiornaStato(Prenotazione p) {
        List<Prenotazione> tutte = JsonUtility.leggiLista(FILE_PRENOTAZIONI, typeToken);
        for (int i = 0; i < tutte.size(); i++) {
            if (tutte.get(i).getId().equals(p.getId())) {
                tutte.set(i, p);
                break;
            }
        }
        JsonUtility.scriviLista(FILE_PRENOTAZIONI, tutte);
    }

    @Override
    public List<Prenotazione> trovaAgendatiCliente(Integer idCliente, LocalDate dataInizio, LocalDate dataFine) {
        List<Prenotazione> tutte = JsonUtility.leggiLista(FILE_PRENOTAZIONI, typeToken);
        List<Prenotazione> filtrate = new ArrayList<>();

        for (Prenotazione p : tutte) {
            if (p.getCliente() != null && p.getCliente().getId().equals(idCliente)) {
                // Filtriamo solo quelle confermate per il calendario
                if ("Confermata".equals(p.getStato()) || "Confermata e Pagata".equals(p.getStato())) {
                    // Controlliamo che la lezione non sia null e che sia nel range di date
                    if(p.getLezionePrenotata() != null && !p.getLezionePrenotata().getData().isBefore(dataInizio) && !p.getLezionePrenotata().getData().isAfter(dataFine)) {
                        filtrate.add(p);
                    }
                }
            }
        }
        return filtrate;
    }

    @Override
    public Prenotazione trovaPerId(Integer id) {
        if (id == null) return null;
        List<Prenotazione> tutte = JsonUtility.leggiLista(FILE_PRENOTAZIONI, typeToken);
        for (Prenotazione p : tutte) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    @Override
    public List<Prenotazione> trovaInAttesaPerIstruttore(Integer idIstruttore) {
        List<Prenotazione> tutte = JsonUtility.leggiLista(FILE_PRENOTAZIONI, typeToken);
        List<Prenotazione> filtrate = new ArrayList<>();

        for (Prenotazione p : tutte) {
            if ("In Attesa".equals(p.getStato()) || "In Attesa di Accettazione".equals(p.getStato())) {
                if (p.getLezionePrenotata() != null && p.getLezionePrenotata().getIstruttore() != null
                        && p.getLezionePrenotata().getIstruttore().getId().equals(idIstruttore)) {
                    filtrate.add(p);
                }
            }
        }
        return filtrate;
    }

    @Override
    public boolean esisteGia(int idCliente, int idLezione) {
        List<Prenotazione> tutte = JsonUtility.leggiLista(FILE_PRENOTAZIONI, typeToken);
        for (Prenotazione p : tutte) {
            if (p.getCliente() != null && p.getCliente().getId().equals(idCliente) &&
                    p.getLezionePrenotata() != null && p.getLezionePrenotata().getIdLezione().equals(idLezione)) {
                return true;
            }
        }
        return false;
    }
}
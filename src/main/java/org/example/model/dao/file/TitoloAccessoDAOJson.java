package org.example.model.dao.file;

import com.google.gson.reflect.TypeToken;
import org.example.model.dao.interfaces.TitoloAccessoDAO;
import org.example.model.domain.AbbonamentoPeriodico;
import org.example.model.domain.Cliente;
import org.example.model.domain.PacchettoCrediti;
import org.example.model.domain.TitoloAccesso;

import java.lang.reflect.Type;
import java.util.List;

public class TitoloAccessoDAOJson implements TitoloAccessoDAO {

    private static final String FILE_CLIENTI = "data/clienti.json";
    private final Type typeToken = new TypeToken<List<Cliente>>(){}.getType();

    @Override
    public void salvaNuovo(TitoloAccesso titolo, Integer idCliente) {
        List<Cliente> clienti = JsonUtility.leggiLista(FILE_CLIENTI, typeToken);

        // Calcoliamo l'ID fittizio
        int maxId = 0;
        for (Cliente c : clienti) {
            if (c.getTitoloAccesso() != null && c.getTitoloAccesso().getTitoloId() != null) {
                if (c.getTitoloAccesso().getTitoloId() > maxId) maxId = c.getTitoloAccesso().getTitoloId();
            }
        }
        titolo.setTitoloId(maxId + 1);

        // Agganciamo il titolo al cliente giusto e salviamo
        for (Cliente c : clienti) {
            if (c.getId().equals(idCliente)) {
                c.setTitoloAccesso(titolo);
                break;
            }
        }
        JsonUtility.scriviLista(FILE_CLIENTI, clienti);
    }

    @Override
    public void aggiornaCrediti(PacchettoCrediti pacchetto) {
        aggiornaTitoloUniversale(pacchetto);
    }

    @Override
    public void aggiornaRinnovo(AbbonamentoPeriodico abbonamento) {
        aggiornaTitoloUniversale(abbonamento);
    }

    private void aggiornaTitoloUniversale(TitoloAccesso titoloDaAggiornare) {
        List<Cliente> clienti = JsonUtility.leggiLista(FILE_CLIENTI, typeToken);
        for (Cliente c : clienti) {
            if (c.getTitoloAccesso() != null && c.getTitoloAccesso().getTitoloId().equals(titoloDaAggiornare.getTitoloId())) {
                c.setTitoloAccesso(titoloDaAggiornare); // Sovrascrive con i nuovi dati
                break;
            }
        }
        JsonUtility.scriviLista(FILE_CLIENTI, clienti);
    }

    @Override
    public TitoloAccesso trovaPerCliente(Integer idCliente) {
        List<Cliente> clienti = JsonUtility.leggiLista(FILE_CLIENTI, typeToken);
        for (Cliente c : clienti) {
            if (c.getId().equals(idCliente)) {
                return c.getTitoloAccesso(); // Ritorna il titolo (se c'è)
            }
        }
        return null;
    }
}

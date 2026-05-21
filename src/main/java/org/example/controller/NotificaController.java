package org.example.controller;

import org.example.model.dao.DAOFactory;
import org.example.model.domain.Notifica;
import org.example.model.domain.User;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class NotificaController {

        public boolean inviaRichiestaLezionePrivata(String cfMittente, String cfDestinatario, LocalDate data, String orario, String note) {

            // 1. Logica di Business: Assembliamo il messaggio in modo formattato
            String messaggio = "Richiesta Lezione Privata:\n" +
                    "Data: " + data.toString() + "\n" +
                    "Ore: " + orario + "\n" +
                    "Note Livello: " + (note.trim().isEmpty() ? "Nessuna nota" : note);

            // 2. Creiamo l'entità
            Notifica nuovaRichiesta = new Notifica();
            nuovaRichiesta.setMittente(cfMittente); // Se gestisci il mittente nel DB
            nuovaRichiesta.setDestinatario(cfDestinatario);
            nuovaRichiesta.setDescrizione(messaggio);
            nuovaRichiesta.setLetta(false);

            // 3. Persistenza: deleghiamo al DAO
            return DAOFactory.getInstance().getNotificaDAO().invia(nuovaRichiesta);
        }
    }

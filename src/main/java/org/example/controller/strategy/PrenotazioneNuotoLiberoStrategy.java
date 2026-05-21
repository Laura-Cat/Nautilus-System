package org.example.controller.strategy;

import org.example.exception.CreditiInsufficientiException;
import org.example.model.dao.DAOFactory;
import org.example.model.domain.*;

import java.util.logging.Logger;

public class PrenotazioneNuotoLiberoStrategy implements PrenotazioneStrategy {
    private static final Logger logger = Logger.getLogger(PrenotazioneNuotoLiberoStrategy.class.getName());
    private static final int COSTO_NUOTO_LIBERO = 1;

    /* @Override
    public boolean eseguiPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {
        // 1. Logica di business del Nuoto Libero
        if (cliente.getTitoloAccesso() == null) {
            try {
                // Nota: Verifica che il metodo nel tuo TitoloAccessoDAO si chiami proprio 'trovaPerCliente'
                // o adatta il nome a seconda di come lo hai battezzato (es. trovaPerIdCliente)
                TitoloAccesso titoloDb = DAOFactory.getInstance().getTitoloAccessoDAO().trovaPerCliente(cliente.getClienteId());
                cliente.setTitoloAccesso(titoloDb);
            } catch (Exception e) {
                System.out.println("Impossibile caricare il titolo d'accesso dal DB: " + e.getMessage());
            }
        }

        TitoloAccesso titolo = cliente.getTitoloAccesso();
        if (titolo == null || !titolo.checkValidita(1)) throw new CreditiInsufficientiException("Crediti non sufficienti");
        if (lezione.getPostiDisponibili() <= 0) return false;

        // 2. Modifiche in memoria
        lezione.setNumPostiPrenotati(lezione.getNumPostiPrenotati() + 1);
        titolo.registraAccesso(1);

        // 3. Salvataggio specifico per il Nuoto Libero
        try {
            Prenotazione p = new Prenotazione(null, LocalDate.now(), TipoAttivita.NUOTO_LIBERO, cliente);
            p.setLezionePrenotata(lezione);
            p.conferma(); // Il nuoto libero è confermato subito!

            DAOFactory.getInstance().getPrenotazioneDAO().salva(p);
            DAOFactory.getInstance().getLezioneDAO().aggiornaPostiOccupati(lezione);
            DAOFactory.getInstance().getTitoloAccessoDAO().aggiornaCrediti((PacchettoCrediti) titolo);
            return true;
        } catch (Exception e) {
            return false;
        }
    }*/
    @Override
    public boolean eseguiPrenotazione(Cliente cliente, Lezione lezione) throws CreditiInsufficientiException {

        System.out.println("\n=== INIZIO DEBUG STRATEGY ===");
        System.out.println("1. Cliente ID che sta prenotando: " + cliente.getId());

        if (cliente.getTitoloAccesso() == null) {
            System.out.println("2. Titolo in memoria vuoto. Cerco nel DB...");
            try {
                // Usa il nome del metodo giusto del tuo DAO (es. trovaPerCliente)
                TitoloAccesso titoloDb = DAOFactory.getInstance().getTitoloAccessoDAO().trovaPerCliente(cliente.getId());
                cliente.setTitoloAccesso(titoloDb);

                if (titoloDb == null) {
                    System.out.println("3. RISULTATO: Il DAO non ha trovato nessun titolo per questo ID!");
                } else if (titoloDb instanceof org.example.model.domain.PacchettoCrediti) {
                    // Facciamo il casting per leggere i crediti!
                    org.example.model.domain.PacchettoCrediti pacchetto = (org.example.model.domain.PacchettoCrediti) titoloDb;
                    System.out.println("3. RISULTATO: Pacchetto trovato! Crediti: " + pacchetto.getCreditiRimanenti());
                } else {
                    System.out.println("3. RISULTATO: Titolo trovato (Non è un pacchetto crediti).");
                }
            } catch (Exception e) {
                System.out.println("ERRORE DAO: " + e.getMessage());
            }
        }

        TitoloAccesso titolo = cliente.getTitoloAccesso();

        if (titolo == null) {
            System.out.println("4. BLOCCO: Titolo definitivamente null, lancio eccezione.");
            throw new CreditiInsufficientiException("Crediti non sufficienti");
        }

        System.out.println("4. Verifico checkValidita(1)...");
        boolean valido = titolo.checkValidita(1);
        System.out.println("5. Risultato checkValidita: " + valido);

        if (!valido) {
            System.out.println("6. BLOCCO: checkValidita ha detto FALSE. Lancio eccezione.");
            throw new CreditiInsufficientiException("Crediti non sufficienti");
        }

        System.out.println("=== FINE DEBUG: LA PRENOTAZIONE PUO' PROCEDERE! ===\n");

        // ... il resto del tuo codice ...
        return true;
    }
}

package org.example.model.dao.Interface;

import org.example.model.domain.Corso;
import java.util.List;

public interface CorsoDAO {
    // Recupera tutti i corsi attivi
    List<Corso> recuperaCorsiAttivi();
}
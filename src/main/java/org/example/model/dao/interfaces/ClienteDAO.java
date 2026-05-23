package org.example.model.dao.interfaces;

import org.example.model.domain.Cliente;

public interface ClienteDAO {
    Cliente trovaPerId(Integer id);
    // In futuro potrai aggiungere metodi come: List<Cliente> trovaTutti(), ecc.
}
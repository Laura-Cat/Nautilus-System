package org.example.model.dao.interfaces;

import org.example.model.domain.Istruttore;

import java.util.List;

public interface IstruttoreDAO {
    List<Istruttore> recuperaTutti();
    Istruttore trovaPerId(Integer id);
}

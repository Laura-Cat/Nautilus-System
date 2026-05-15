package org.example.model.dao;

import org.example.model.domain.User;

public interface LoginDAO {
    // Metodo non statico che definisce cosa deve fare il DAO
    User trovaPerCredenziali(String email, String password);
}
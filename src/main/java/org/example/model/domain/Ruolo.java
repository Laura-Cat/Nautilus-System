package org.example.model.domain;

public enum Ruolo {
    AMMINISTRAZIONE (1),
    ISTRUTTORE(2),
    CLIENTE(3),
    LOGIN(4);
    private final int id;

    private Ruolo(int id) {
        this.id = id;
    }

    public static Ruolo fromInt(int id) {
        for (Ruolo type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }
}
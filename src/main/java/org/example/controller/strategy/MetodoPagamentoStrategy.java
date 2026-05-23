package org.example.controller.strategy;

public interface MetodoPagamentoStrategy {
    boolean processaPagamento(double importo);
    String getNomePiattaforma();
}

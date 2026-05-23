package org.example.controller.strategy;

public class PayPalStrategy implements MetodoPagamentoStrategy {
    @Override
    public boolean processaPagamento(double importo) {
        System.out.println("🔄 Reindirizzamento sicuro a PayPal...");
        try { Thread.sleep(1500); } catch (InterruptedException e) {} // Simula il caricamento web
        System.out.println("✅ Transazione PayPal completata: €" + importo);
        return true;
    }

    @Override
    public String getNomePiattaforma() {
        return "PayPal";
    }
}
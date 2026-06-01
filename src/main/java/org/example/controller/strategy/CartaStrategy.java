package org.example.controller.strategy;

public class CartaStrategy implements MetodoPagamentoStrategy {
    @Override
    public boolean processaPagamento(double importo) {
        System.out.println("🔄 Reindirizzamento sicuro a Postepay...");
        try { Thread.sleep(1500); } catch (InterruptedException e) {} // Simula il caricamento web
        System.out.println("✅ Transazione postepay completata: €" + importo);
        return true;
    }

    @Override
    public String getNomePiattaforma() {
        return "Postepay";
    }
}
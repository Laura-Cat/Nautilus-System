package org.example.controller.strategy; // Controlla che il package sia quello giusto!

import java.util.logging.Level;
import java.util.logging.Logger;

public class PayPalStrategy implements MetodoPagamentoStrategy {

    private static final Logger logger = Logger.getLogger(PayPalStrategy.class.getName());
    @Override
    public boolean processaPagamento(double importo) {
        logger.info("🔄 Reindirizzamento sicuro a PayPal...");

        try {
            Thread.sleep(1500); // Simula il caricamento web
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Simulazione del caricamento web interrotta", e);
            Thread.currentThread().interrupt();
        }
        logger.info(() -> "✅ Transazione PayPal completata: €" + importo);

        return true;
    }

    @Override
    public String getNomePiattaforma() {
        return "PayPal";
    }
}
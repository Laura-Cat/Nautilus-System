package org.example.controller.strategy;

import org.example.controller.strategy.MetodoPagamentoStrategy;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CartaStrategy implements MetodoPagamentoStrategy {
    private static final Logger logger = Logger.getLogger(CartaStrategy.class.getName());

    @Override
    public boolean processaPagamento(double importo) {
        // 2. Sostituisci System.out.println con logger.info
        logger.info("🔄 Reindirizzamento sicuro a Postepay...");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "❌ Il pagamento è stato interrotto.", e);
            return false;
        }

        logger.info("✅ Transazione postepay completata: €" + importo);
        return true;
    }

    @Override
    public String getNomePiattaforma() {
        return "Postepay";
    }
}
package com.erp.stock.messaging;

import com.erp.stock.model.Produit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStockUpdated(Produit produit) {
        StockUpdatedEvent event = new StockUpdatedEvent(
            produit.getId(),
            produit.getReference(),
            produit.getNom(),
            produit.getPrixUnitaire(),
            produit.getQuantiteEnStock(),
            LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.STOCK_UPDATED_KEY, event);
        log.info("📦 Stock updated event published: {}", produit.getReference());
    }

    public void publishStockAlert(Produit produit) {
        StockAlertEvent alert = new StockAlertEvent(
            produit.getId(),
            produit.getReference(),
            produit.getNom(),
            produit.getQuantiteEnStock(),
            produit.getSeuilAlerte(),
            LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.STOCK_ALERT_KEY, alert);
        log.warn("⚠️  Stock alert published: {} (qty={} <= seuil={})",
            produit.getReference(), produit.getQuantiteEnStock(), produit.getSeuilAlerte());
    }

    public record StockUpdatedEvent(
        Long produitId, String reference, String nom,
        BigDecimal prixUnitaire, Integer quantiteEnStock, LocalDateTime timestamp) {}

    public record StockAlertEvent(
        Long produitId, String reference, String nom,
        Integer quantiteActuelle, Integer seuilAlerte, LocalDateTime timestamp) {}
}

package com.erp.ventes.messaging;

import com.erp.ventes.model.Commande;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
class RabbitMQConfig {

    public static final String EXCHANGE = "erp.exchange";
    public static final String COMMANDE_CREATED = "commande.created";
    public static final String FINANCE_QUEUE = "finance.commande.queue";

    @Bean
    TopicExchange erpExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue financeQueue() {
        return QueueBuilder.durable(FINANCE_QUEUE).build();
    }

    @Bean
    Binding financeBinding(Queue financeQueue, TopicExchange erpExchange) {
        return BindingBuilder.bind(financeQueue).to(erpExchange).with(COMMANDE_CREATED);
    }

    @Bean
    Jackson2JsonMessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @SuppressWarnings("null")
    RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        @SuppressWarnings("null")
        RabbitTemplate t = new RabbitTemplate(cf);
        @SuppressWarnings("null")
        RabbitTemplate result = t;
        @SuppressWarnings("null")
        RabbitTemplate setResult = result;
        setResult.setMessageConverter(conv);
        return t;
    }
}

@Component
@RequiredArgsConstructor
@Slf4j
public class VentesEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCommandeCreated(Commande commande) {
        CommandeCreatedEvent event = new CommandeCreatedEvent(
                commande.getId(),
                commande.getNumero(),
                commande.getClient(),
                commande.getMontantTotal(),
                commande.getLignes().stream()
                        .map(l -> new CommandeCreatedEvent.LigneEvent(
                                l.getProduitReference(), l.getQuantite(), l.getSousTotal()))
                        .toList(),
                LocalDateTime.now());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.COMMANDE_CREATED, event);
        log.info("🛒 Commande created event published: {}", commande.getNumero());
    }

    public record CommandeCreatedEvent(
            Long commandeId, String numero, String client, BigDecimal montantTotal,
            List<LigneEvent> lignes, LocalDateTime timestamp) {
        public record LigneEvent(String produitRef, Integer quantite, BigDecimal sousTotal) {
        }
    }
}

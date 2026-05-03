package com.erp.finance.messaging;

import com.erp.finance.model.Facture;
import com.erp.finance.repository.FRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
class FinanceRabbitConfig {

    public static final String EXCHANGE = "erp.exchange";
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
        return BindingBuilder.bind(financeQueue).to(erpExchange).with("commande.created");
    }

    @Bean
    Jackson2JsonMessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @SuppressWarnings("null")
    RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        RabbitTemplate t = new RabbitTemplate(cf);
        RabbitTemplate result = t;
        RabbitTemplate setResult = result;
        setResult.setMessageConverter(conv);
        return result;
    }
}

@Component
@RequiredArgsConstructor
@Slf4j
public class FinanceEventConsumer {

    private final FRepository factureRepo;

    @RabbitListener(queues = FinanceRabbitConfig.FINANCE_QUEUE)
    @SuppressWarnings("null")
    public void onCommandeCreated(CommandeCreatedEvent event) {
        log.info("💶 Finance received commande: {}", event.numero());

        // Éviter les doublons
        if (factureRepo.findByCommandeNumero(event.numero()).isPresent()) {
            log.warn("Facture déjà générée pour commande {}", event.numero());
            return;
        }

        String numero = "FAC-" + event.numero().replace("CMD-", "");

        Facture facture = Facture.builder()
                .numero(numero)
                .commandeNumero(event.numero())
                .client(event.client())
                .montantHT(event.montantTotal())
                .tauxTVA(new BigDecimal("0.19"))
                .dateEcheance(LocalDate.now().plusDays(30))
                .statut(Facture.StatutFacture.NON_PAYEE)
                .build();

        factureRepo.save(facture);
        log.info("✅ Facture {} générée pour commande {} - TTC={}",
                numero, event.numero(), facture.getMontantTTC());
    }

    public record CommandeCreatedEvent(
            Long commandeId, String numero, String client, BigDecimal montantTotal,
            List<LigneEvent> lignes, LocalDateTime timestamp) {
        public record LigneEvent(String produitRef, Integer quantite, BigDecimal sousTotal) {
        }
    }
}

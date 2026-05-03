package com.erp.stock.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "erp.exchange";
    public static final String STOCK_UPDATED_KEY = "stock.updated";
    public static final String STOCK_ALERT_KEY = "stock.alert";
    public static final String VENTES_QUEUE = "ventes.stock.queue";

    @Bean
    TopicExchange erpExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue ventesStockQueue() {
        return QueueBuilder.durable(VENTES_QUEUE).build();
    }

    @Bean
    Binding ventesStockBinding(Queue ventesStockQueue, TopicExchange erpExchange) {
        return BindingBuilder.bind(ventesStockQueue).to(erpExchange).with(STOCK_UPDATED_KEY);
    }

    @Bean
    Jackson2JsonMessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @SuppressWarnings("null")
    RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter converter) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        RabbitTemplate result = tpl;
        RabbitTemplate setResult = result;
        setResult.setMessageConverter(converter);
        return tpl;
    }
}

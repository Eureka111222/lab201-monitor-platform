package com.lab201.monitor.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitorRabbitConfig {

    @Bean
    public DirectExchange gpuReportExchange(MonitorIntegrationProperties properties) {
        return new DirectExchange(properties.getAmqp().getGpuReportExchange(), true, false);
    }

    @Bean
    public Queue gpuReportQueue(MonitorIntegrationProperties properties) {
        return new Queue(properties.getAmqp().getGpuReportQueue(), true);
    }

    @Bean
    public Binding gpuReportBinding(
            Queue gpuReportQueue,
            DirectExchange gpuReportExchange,
            MonitorIntegrationProperties properties
    ) {
        return BindingBuilder.bind(gpuReportQueue)
                .to(gpuReportExchange)
                .with(properties.getAmqp().getGpuReportRoutingKey());
    }

    @Bean
    public Jackson2JsonMessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter rabbitMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        return factory;
    }
}

package com.fpt.h2s.configurations;

import com.fpt.h2s.workers.BaseWorker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Startup implements ApplicationListener<ApplicationReadyEvent> {
    private final List<BaseWorker<?>> workers;
    
    private final AmqpAdmin admin;
    
    private final MessageListenerContainer container;
    
    private final RabbitListenerEndpointRegistry registry;
    
    private final SimpleRabbitListenerContainerFactory factory;
    
    @PostConstruct
    private void createQueues() {
        final SimpleMessageListenerContainer container = (SimpleMessageListenerContainer) this.container;
        
        this.workers.forEach(worker -> {
            final BaseWorker.QueueBeanContainer beans = worker.getBeans();
            
            this.admin.declareQueue(beans.queue());
            this.admin.declareExchange(beans.exchange());
            this.admin.declareBinding(beans.binding());
            
            final Queue queue = beans.queue();
            container.setQueues(queue);
            
            final SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
            endpoint.setId("endpoint-" + worker.hashCode());
            endpoint.setMessageListener(worker);
            endpoint.setQueues(queue);
            
            this.registry.registerListenerContainer(endpoint, this.factory, true);
        });
    }
    
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        final String message =
            """
                ===========================================================================================================
                ===========================================================================================================
                ========================================// Application is running //=======================================
                ===========================================================================================================
                ===========================================================================================================
                """;
        System.out.println(message);
    }
}

package com.fpt.h2s.workers;

import ananta.utility.StringEx;
import com.fpt.h2s.utilities.Generalizable;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.SpringBeans;
import lombok.NonNull;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


public abstract class BaseWorker<T> extends Generalizable<T> implements MessageListener {
    
    protected abstract void execute(T request);
    
    protected abstract String getQueueName();
    
    protected abstract String getExchangeName();
    
    protected abstract String getRoutingKey();
    
    public QueueBeanContainer getBeans() {
        final Queue queue = new Queue(this.getQueueName(), false);
        final TopicExchange exchange = new TopicExchange(this.getExchangeName());
        final Binding binding = BindingBuilder.bind(queue).to(exchange).with(this.getRoutingKey());
        return new QueueBeanContainer(queue, exchange, binding);
    }
    
    public void send(@NonNull final T request) {
        final String json = Mappers.jsonOf(request);
        SpringBeans.getBean(RabbitTemplate.class).convertAndSend(this.getExchangeName(), this.getRoutingKey(), json);
    }
    
    @Override
    public void onMessage(final Message message) {
        this.execute(this.extractRequestFrom(message));
    }
    
    private T extractRequestFrom(final Message message) {
        try {
            final String content = new String(message.getBody());
            return Mappers.mapToObjectFrom(content, this.getGenericClass());
        } catch (final Exception e) {
            final String body = new String(message.getBody()).translateEscapes();
            final String content = StringEx.largestBetween("\"", "\"", body);
            return Mappers.mapToObjectFrom(content, this.getGenericClass());
        }
    }
    
    public record QueueBeanContainer(Queue queue, TopicExchange exchange, Binding binding) {
    
    }
    
}
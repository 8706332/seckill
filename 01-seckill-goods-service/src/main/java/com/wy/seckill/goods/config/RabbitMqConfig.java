package com.wy.seckill.goods.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 *
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue secKillQueue() {//队列

        return new Queue("secKillQueue");
    }

    @Bean
    public DirectExchange secKillExchange() {//交换机

        return new DirectExchange("secKillExchange");
    }

    @Bean
    public Binding bindingBuilder(Queue secKillQueue, DirectExchange secKillExchange) {//交换机和队列进行绑定

        return BindingBuilder.bind(secKillQueue).to(secKillExchange).with("secKillRoutingKey");
    }
}

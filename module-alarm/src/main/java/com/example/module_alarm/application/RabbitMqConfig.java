package com.example.module_alarm.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

@Configuration
public class RabbitMqConfig {
    @Bean
    public Queue forexQueue() {
        return new Queue("queue.forex.alert", true);
    }

    @Bean
    public Queue goalQueue() {
        return new Queue("queue.goal.alert", true);
    }

    @Bean
    public Queue halfGoalQueue() {return new Queue("queue.half.alert", true);}

    @Bean
    public DirectExchange forexExchange() {
        return new DirectExchange("exchange.forex");
    }

    @Bean
    public DirectExchange goalExchange() {
        return new DirectExchange("exchange.goal");
    }

    @Bean
    public DirectExchange halfGoalExchange() {return new DirectExchange("exchange.halfGoal");}

    @Bean
    public DirectExchange goalEndExchange() {return new DirectExchange("exchange.goal");}

    @Bean
    public Binding bindingForex(Queue forexQueue, DirectExchange forexExchange) {
        return BindingBuilder.bind(forexQueue).to(forexExchange).with("forex.alert");
    }

    @Bean
    public Binding bindingGoal(Queue goalQueue, DirectExchange goalExchange) {
        return BindingBuilder.bind(goalQueue).to(goalExchange).with("goal.alert");
    }

    @Bean
    public Binding bindingHalfGoal(Queue halfGoalQueue, DirectExchange halfGoalExchange) {
        return BindingBuilder.bind(halfGoalQueue).to(halfGoalExchange).with("half.alert");
    }

    @Bean
    public Binding bindingGoalEnd(Queue goalQueue, DirectExchange goalExchange) {
        return BindingBuilder.bind(goalQueue).to(goalExchange).with("end.alert");
    }
}

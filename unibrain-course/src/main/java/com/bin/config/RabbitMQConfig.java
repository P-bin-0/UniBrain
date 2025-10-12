package com.bin.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 配置RabbitTemplate
 */
@Configuration
public class RabbitMQConfig {
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        // 设置消息确认模式，当消息被正确路由到队列时回调
        rabbitTemplate.setConfirmCallback((correlationData, b, s) -> {
            if (b) {
                // 消息成功发送到Exchange时的处理逻辑
                System.out.println("消息发送成功，消息id：" + (correlationData != null ? correlationData.getId() : "未知"));
            } else {
                // 消息发送失败时的处理逻辑
                System.err.println("消息发送失败，原因：" + s);
            }
        });
        // 设置消息返回模式，当消息无法路由到任何队列时回调
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("消息无法路由到队列，消息：" + returned.getMessage() +
                    "，回复码：" + returned.getReplyCode() +
                    "，回复文本：" + returned.getReplyText() +
                    "，交换机：" + returned.getExchange() +
                    "，路由键：" + returned.getRoutingKey());
        });
        // 设置消息发送超时时间（毫秒）
        rabbitTemplate.setReplyTimeout(10000); // 10秒
        // 设置强制消息确认模式，确保消息可靠投递
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    /**
     * 定义选课消息的 Exchange（Topic 类型，便于扩展）
     */
    @Bean
    public TopicExchange courseSelectExchange() {
        return new TopicExchange("course.select.exchange", true, false);
    }
    /**
     * 定义选课消息的 Queue
     */
    @Bean
    public Queue courseSelectQueue() {
        return QueueBuilder.durable("course.select.queue")
                .withArgument("x-message-ttl", 600000) // 消息过期时间 10分钟
                .withArgument("x-max-length", 10000)   // 队列最大长度
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "course.select.dlq")
                .build();
    }
    /**
     * 定义退课消息的 Queue
     */
    @Bean
    public Queue courseDropQueue() {
        return QueueBuilder.durable("course.drop.queue")
                .withArgument("x-message-ttl", 600000)     // 10分钟过期
                .withArgument("x-max-length", 10000)       // 最大长度
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "course.drop.dlq")
                .build();
    }

    @Bean
    public Queue courseDropDlq() {
        return QueueBuilder.durable("course.drop.dlq").build();
    }

    @Bean
    public Binding courseDropDlqBinding() {
        return BindingBuilder.bind(courseDropDlq())
                .to(dlxExchange())
                .with("course.drop.dlq");
    }


    /**
     * 将 Queue 绑定到 Exchange
     */
    @Bean
    public Binding courseSelectBinding() {
        return BindingBuilder.bind(courseSelectQueue())
                .to(courseSelectExchange())
                .with("course.select.routing.key"); // routing key
    }

    /**
     * 将退课 Queue 绑定到 Exchange
     */
    @Bean
    public Binding courseDropBinding() {
        return BindingBuilder.bind(courseDropQueue())
                .to(courseSelectExchange())
                .with("course.drop.routing.key"); // routing key
    }

    /**
     * 配置监听容器工厂，启用自动 ACK 和重试
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO); // 自动 ACK
        factory.setRetryTemplate(retryTemplate()); // 启用重试
        factory.setMessageConverter(jsonMessageConverter); // 设置 JSON 消息转换器
        return factory;
    }

    /**
     * 定义重试策略
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 1. 退避策略：指数级延迟重试
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);   // 初始等待 1 秒
        backOffPolicy.setMultiplier(2.0);         // 乘数因子：2（即 1s, 2s, 4s, 8s...）
        backOffPolicy.setMaxInterval(10000);      // 最大等待 10 秒
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 2. 重试策略：最多重试 3 次（注意：总共执行 1 + 3 = 4 次）
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // 最多重试 3 次
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    /**
     * 死信交换机（DLX）
     */
    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange("dlx.exchange", true, false);
    }

    /**
     * 死信队列（DLQ） - 用于接收选课失败的消息
     */
    @Bean
    public Queue courseSelectDlq() {
        return QueueBuilder.durable("course.select.dlq").build();
    }

    /**
     * 将死信队列绑定到死信交换机
     */
    @Bean
    public Binding courseSelectDlqBinding() {
        return BindingBuilder.bind(courseSelectDlq())
                .to(dlxExchange())
                .with("course.select.dlq"); // 必须和 x-dead-letter-routing-key 一致
    }
}

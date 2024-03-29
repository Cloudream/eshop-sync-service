package com.cloudream.eshop.sync.rabbitmq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQSender {

	@Autowired
	private AmqpTemplate rabbitTemplate;

	public void send(String topic, String message) {
		this.rabbitTemplate.convertAndSend(topic, message);
	}
}

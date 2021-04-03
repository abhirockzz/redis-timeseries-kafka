package com.demo.azure;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConsumerApp {

	@Value("${topic.name}")
	private String topicName;

	@Value("${topic.replication-factor}")
	private short replicationFactor;

	@Value("${topic.partitions-num}")
	private Integer partitions;

	@Bean
	NewTopic topic() {
		return new NewTopic(topicName, partitions, replicationFactor);
	}

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApp.class, args);
	}

}

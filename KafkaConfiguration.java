package com.lowes.storeelasticsearch.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

/**
 * Confluence Kafka Producer and Consumer Configuration
 * 
 * @author ndevara
 *
 */
@EnableKafka
@Configuration
public class KafkaConfiguration {

	// private static final Logger logger =
	// LoggerFactory.getLogger(KafkaConfiguration.class);

	@Value("${spring.kafka.bootstrap-servers}")
	private List<String> kafkaBootstrapServersList;

	@Value("${spring.kafka.consumer.group-id}")
	private String consumerGroupId;

	@Value("${spring.kafka.dlq.topic}")
	private String dlqTopic;

//	@Autowired
//	private KafkaTemplate<String, byte[]> kafkaTemplate;

	@Bean
	public ConsumerFactory<String, byte[]> consumerFactory() {
		Map<String, Object> config = new HashMap<>();

		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServersList);
		config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		config.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG,
				"io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor");

		return new DefaultKafkaConsumerFactory<>(config);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<String, byte[]>();
		factory.setConsumerFactory(consumerFactory());
//		factory.setRetryTemplate(retryTemplate());
//		factory.setRecoveryCallback(this::recoveryCallback);
		// Set whether or not the container should commit offsets (ack messages) when
		// the listener throws exception
		factory.getContainerProperties().setAckOnError(false);
		// Commit after each record is processed by the listener.
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
		return factory;
	}

//	@Bean
//	public RetryTemplate retryTemplate() {
//		RetryTemplate retryTemplate = new RetryTemplate();
//
//		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
//		backOffPolicy.setMaxInterval(20000); // 20s
//		backOffPolicy.setMultiplier(3);
//
//		retryTemplate.setBackOffPolicy(backOffPolicy);
//
//		retryTemplate.setRetryPolicy(new CustomExceptionRetryPolicy());
//
//		return retryTemplate;
//	}
//
//	@SuppressWarnings("unchecked")
//	private Object recoveryCallback(RetryContext context) {
//		ConsumerRecord<String, byte[]> rec = (ConsumerRecord<String, byte[]>) context
//				.getAttribute(RetryingMessageListenerAdapter.CONTEXT_RECORD);
//		Consumer<String, byte[]> consumer = (Consumer<String, byte[]>) context
//				.getAttribute(RetryingMessageListenerAdapter.CONTEXT_CONSUMER);
//		try {
//			kafkaTemplate.send(dlqTopic, rec.partition(), rec.timestamp(), rec.key(), rec.value());
//			consumer.seek(new TopicPartition(rec.topic(), rec.partition()), rec.offset() + 1);
//		} catch (Exception e) {
//			logger.error(
//					"Exception occured while sending malformed message to Dead Letter Topic, seeking offsets to current");
//			consumer.seek(new TopicPartition(rec.topic(), rec.partition()), rec.offset());
//			e.printStackTrace();
//		}
//		return null;
//	}

	// For End-to-End Testing locally
	@Bean
	public ProducerFactory<String, byte[]> producerFactory() {
		Map<String, Object> config = new HashMap<>();

		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServersList);
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
		config.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
				"io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor");

		return new DefaultKafkaProducerFactory<>(config);

	}

	@Bean
	public KafkaTemplate<String, byte[]> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

}

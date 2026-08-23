package com.example.processor.config // Defines package location for Kafka configuration beans.

import org.apache.kafka.clients.CommonClientConfigs // Kafka common client configuration constants.
import org.apache.kafka.clients.consumer.ConsumerConfig // Kafka consumer configuration constants.
import org.apache.kafka.clients.producer.ProducerConfig // Kafka producer configuration constants.
import org.apache.kafka.common.config.SaslConfigs // Kafka SASL configuration constants.
import org.apache.kafka.common.serialization.StringDeserializer // Serializer for reading string records.
import org.apache.kafka.common.serialization.StringSerializer // Serializer for writing string records.
import org.springframework.beans.factory.annotation.Value // Annotation injecting configuration property values.
import org.springframework.context.annotation.Bean // Annotation marking bean creation methods.
import org.springframework.context.annotation.Configuration // Annotation marking configuration class.
import org.springframework.kafka.annotation.EnableKafka // Annotation enabling Kafka listener annotations.
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory // Factory creating Kafka listener container instances.
import org.springframework.kafka.core.ConsumerFactory // Interface for creating Kafka consumer instances.
import org.springframework.kafka.core.DefaultKafkaConsumerFactory // Default implementation of ConsumerFactory.
import org.springframework.kafka.core.DefaultKafkaProducerFactory // Default implementation of ProducerFactory.
import org.springframework.kafka.core.KafkaTemplate // Spring helper template executing high-level Kafka operations.
import org.springframework.kafka.core.ProducerFactory // Interface for creating Kafka producer instances.

@EnableKafka // Enables support for @KafkaListener annotations.
@Configuration // Marks class as Spring configuration container.
class KafkaConfig {

    // Injects KAFKA_BOOTSTRAP_SERVERS environment property or defaults to localhost:9092.
    @Value("\${spring.kafka.bootstrap-servers:eventhubs-emulator:9092}")
    private lateinit var bootstrapServers: String

    // Helper generating EventHub SASL JAAS authentication string.
    private fun getJaasConfig(): String {
        val hostname = bootstrapServers.split(":")[0] // Extracts hostname from bootstrap server string.
        val connectionString = "Endpoint=sb://$hostname;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;" // Formats EventHub emulator connection string.
        return "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"\$ConnectionString\" password=\"$connectionString\";" // Constructs SASL PLAIN login module credentials.
    }

    // Configures ProducerFactory with string serializers and EventHub SASL credentials.
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configProps = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SASL_PLAINTEXT", // Required protocol for EventHubs Kafka endpoint.
            SaslConfigs.SASL_MECHANISM to "PLAIN", // Required SASL mechanism.
            SaslConfigs.SASL_JAAS_CONFIG to getJaasConfig() // Injects EventHubs SASL authentication credentials.
        )
        return DefaultKafkaProducerFactory(configProps) // Instantiates DefaultKafkaProducerFactory with configuration properties.
    }

    // Registers thread-safe KafkaTemplate Spring bean for publishing Kafka events.
    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        val template = KafkaTemplate(producerFactory()) // Returns KafkaTemplate configured with producerFactory.
        template.setObservationEnabled(true) // Enables Micrometer Observation tracing for Kafka producers.
        return template
    }

    // Configures ConsumerFactory with string deserializers and EventHub SASL credentials.
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val configProps = mapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "processor-cg", // Consumer group for processor service.
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest", // Reads from offset 0 when starting fresh.
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SASL_PLAINTEXT",
            SaslConfigs.SASL_MECHANISM to "PLAIN",
            SaslConfigs.SASL_JAAS_CONFIG to getJaasConfig()
        )
        return DefaultKafkaConsumerFactory(configProps) // Returns DefaultKafkaConsumerFactory.
    }

    // Configures ConcurrentKafkaListenerContainerFactory for @KafkaListener annotations.
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>() // Instantiates container factory.
        factory.setConsumerFactory(consumerFactory()) // Sets consumer factory using setter.
        factory.containerProperties.isObservationEnabled = true // Enables Micrometer Observation tracing for Kafka consumers.
        return factory // Returns listener container factory bean.
    }
}

package com.example.processor.service // Defines package for processor service layer.

import org.slf4j.LoggerFactory // SLF4J logger interface.
import org.springframework.kafka.core.KafkaTemplate // Spring Kafka template.
import org.springframework.stereotype.Service // Spring service annotation.

@Service // Marks ResponsePublisherService as a Spring managed service bean.
class ResponsePublisherService(
    private val kafkaTemplate: KafkaTemplate<String, String> // Injects autoconfigured KafkaTemplate bean.
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ResponsePublisherService::class.java) // Logger instance for ResponsePublisherService.
    }

    // Publishes response Kafka event to target topic with optional key and payload using automatic Micrometer Observation tracing.
    fun publishResponse(topic: String, message: String, key: String? = null) {
        LOGGER.info("[processor-service] Producing response Kafka message to topic '{}' with key '{}'", topic, key) // Logs response publishing event.
        if (key != null) {
            kafkaTemplate.send(topic, key, message) // Sends response message with key
        } else {
            kafkaTemplate.send(topic, message) // Sends response message without key
        }
    }
}

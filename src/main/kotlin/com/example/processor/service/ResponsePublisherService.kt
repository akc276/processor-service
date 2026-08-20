package com.example.processor.service // Defines package for processor service layer.

import org.apache.kafka.clients.producer.ProducerRecord // Kafka ProducerRecord model.
import org.apache.kafka.common.header.internals.RecordHeader // Header class for Kafka records.
import org.slf4j.LoggerFactory // SLF4J logger interface.
import org.springframework.kafka.core.KafkaTemplate // Spring Kafka template.
import org.springframework.stereotype.Service // Spring service annotation.
import java.nio.charset.StandardCharsets // Standard charset definitions for string encoding.

@Service // Marks ResponsePublisherService as a Spring managed service bean.
class ResponsePublisherService(
    private val kafkaTemplate: KafkaTemplate<String, String> // Injects autoconfigured KafkaTemplate bean.
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ResponsePublisherService::class.java) // Logger instance for ResponsePublisherService.
    }

    // Publishes response Kafka event to target topic with optional key, payload, and attached trace ID headers.
    fun publishResponse(topic: String, message: String, traceId: String, key: String? = null) {
        val record = ProducerRecord<String, String>(topic, key, message) // Constructs ProducerRecord with topic, optional key, and message payload.
        record.headers().add(RecordHeader("X-Correlation-ID", traceId.toByteArray(StandardCharsets.UTF_8))) // Attaches X-Correlation-ID header.
        record.headers().add(RecordHeader("traceId", traceId.toByteArray(StandardCharsets.UTF_8))) // Attaches traceId header for cross-compatibility.

        LOGGER.info("[processor-service] Producing response Kafka message to topic '{}' with key '{}' and traceId: {}", topic, key, traceId) // Logs response publishing event.
        kafkaTemplate.send(record) // Asynchronously publishes enriched ProducerRecord to EventHub broker.
    }
}

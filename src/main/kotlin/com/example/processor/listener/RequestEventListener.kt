package com.example.processor.listener // Defines package for Kafka request event listeners.

import com.example.processor.service.ResponsePublisherService // Imports ResponsePublisherService to produce response events.
import org.apache.kafka.clients.consumer.ConsumerRecord // Kafka record class containing headers and message payload.
import org.slf4j.LoggerFactory // SLF4J logger factory interface.
import org.slf4j.MDC // Mapped Diagnostic Context for trace logging.
import org.springframework.kafka.annotation.KafkaListener // Kafka listener annotation.
import org.springframework.messaging.handler.annotation.Payload // Annotation binding incoming event payload.
import org.springframework.stereotype.Component // Component annotation.
import java.nio.charset.StandardCharsets // Charset decoding constants.
import java.util.UUID // Fallback UUID generation.

@Component // Registers RequestEventListener as a Spring bean.
class RequestEventListener(
    private val responsePublisherService: ResponsePublisherService // Injects ResponsePublisherService bean.
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(RequestEventListener::class.java) // Logger instance for RequestEventListener.
    }

    @KafkaListener(topics = ["gateway-requests"], groupId = "processor-cg") // Subscribes to `gateway-requests` topic using consumer group `processor-cg`.
    fun listenRequest(
        @Payload message: String, // Injects event payload.
        record: ConsumerRecord<String, String> // Injects Kafka consumer record metadata.
    ) {
        // Extracts X-Correlation-ID header byte array from record headers, falling back to traceId header or generating a UUID.
        val headerBytes = record.headers().lastHeader("X-Correlation-ID")?.value()
            ?: record.headers().lastHeader("traceId")?.value()
        val traceId = headerBytes?.let { String(it, StandardCharsets.UTF_8) } ?: UUID.randomUUID().toString()

        try {
            MDC.put("correlationId", traceId) // Sets correlationId in MDC logging context.
            MDC.put("traceId", traceId) // Sets traceId in MDC logging context.

            LOGGER.info("[processor-service] Received request event from consumer-api-gateway. Payload: '{}'", message) // Logs event receipt with trace ID.

            val responsePayload = "Response from processor-service acknowledging: '$message'" // Formats response event message.
            responsePublisherService.publishResponse("service-responses", responsePayload, traceId) // Produces response event back to gateway with SAME trace ID.

        } finally {
            MDC.remove("correlationId") // Clears MDC correlationId context.
            MDC.remove("traceId") // Clears MDC traceId context.
        }
    }
}

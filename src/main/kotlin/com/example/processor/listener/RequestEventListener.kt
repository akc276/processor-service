package com.example.processor.listener // Defines package for Kafka request event listeners.

import com.example.processor.service.ResponsePublisherService // Imports ResponsePublisherService to produce response events.
import org.slf4j.LoggerFactory // SLF4J logger factory interface.
import org.springframework.kafka.annotation.KafkaListener // Kafka listener annotation.
import org.springframework.messaging.handler.annotation.Payload // Annotation binding incoming event payload.
import org.springframework.stereotype.Component // Component annotation.

@Component // Registers RequestEventListener as a Spring bean.
class RequestEventListener(
    private val responsePublisherService: ResponsePublisherService // Injects ResponsePublisherService bean.
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(RequestEventListener::class.java) // Logger instance for RequestEventListener.
    }

    @KafkaListener(topics = ["gateway-requests"], groupId = "processor-cg") // Subscribes to `gateway-requests` topic using consumer group `processor-cg`.
    fun listenRequest(@Payload message: String) {
        LOGGER.info("[processor-service] Received request event from consumer-api-gateway. Payload: '{}'", message) // Logs event receipt with automatic trace ID.

        val responsePayload = "Response from processor-service acknowledging: '$message'" // Formats response event message.
        responsePublisherService.publishResponse("service-responses", responsePayload) // Produces response event back to gateway with automatic trace propagation.
    }
}

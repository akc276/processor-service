package com.example.processor.tracing

import org.apache.kafka.clients.producer.ProducerInterceptor
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.nio.charset.StandardCharsets

/**
 * Pure Kotlin Kafka ProducerInterceptor that injects active trace context (traceparent & X-Correlation-ID headers)
 * into outgoing Kafka records using Micrometer Tracer or SLF4J MDC fallback without AdminClient calls.
 */
class CustomKafkaProducerInterceptor : ProducerInterceptor<String, String> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun onSend(record: ProducerRecord<String, String>): ProducerRecord<String, String> {
        val currentSpan = TracingHolder.tracer?.currentSpan()
        val traceId = currentSpan?.context()?.traceId() ?: MDC.get("traceId")
        val spanId = currentSpan?.context()?.spanId() ?: (MDC.get("spanId") ?: "0000000000000000")

        if (traceId != null) {
            val traceparent = "00-$traceId-$spanId-01"

            record.headers().remove("traceparent")
            record.headers().add("traceparent", traceparent.toByteArray(StandardCharsets.UTF_8))
            record.headers().remove("X-Correlation-ID")
            record.headers().add("X-Correlation-ID", traceId.toByteArray(StandardCharsets.UTF_8))
            log.info("Injected traceparent: {} and X-Correlation-ID: {} for topic: {}", traceparent, traceId, record.topic())
        } else {
            log.warn("No active traceId or MDC traceId found when sending record to topic: {}", record.topic())
        }
        return record
    }

    override fun onAcknowledgement(metadata: RecordMetadata?, exception: Exception?) {}
    override fun close() {}
    override fun configure(configs: MutableMap<String, *>) {}
}

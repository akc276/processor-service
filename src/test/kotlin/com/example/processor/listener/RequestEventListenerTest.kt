package com.example.processor.listener // Defines package for listener unit tests.

import com.example.processor.service.ResponsePublisherService // Imports ResponsePublisherService to mock.
import org.apache.kafka.clients.consumer.ConsumerRecord // Kafka record model.
import org.apache.kafka.common.header.internals.RecordHeader // Record header class.
import org.junit.jupiter.api.Assertions.assertEquals // JUnit 5 assertion.
import org.junit.jupiter.api.Test // JUnit 5 Test annotation.
import org.mockito.Mockito.mock // Mockito mock creation.
import org.mockito.kotlin.any // Mockito Kotlin any matcher.
import org.mockito.kotlin.eq // Mockito Kotlin eq matcher.
import org.mockito.kotlin.verify // Mockito Kotlin verify method.
import org.slf4j.MDC // SLF4J MDC context.
import java.nio.charset.StandardCharsets // Standard charsets for header byte encoding.

class RequestEventListenerTest {

    private val responsePublisherService: ResponsePublisherService = mock(ResponsePublisherService::class.java) // Creates mock instance.
    private val requestEventListener = RequestEventListener(responsePublisherService) // Instantiates listener under test.

    @Test
    fun `listenRequest extracts trace ID header and produces response with matching trace ID`() {
        val testCorrelationId = "processor-test-trace-8888" // Sample trace ID string.
        val record = ConsumerRecord<String, String>("gateway-requests", 0, 0L, "key", "Hello from gateway") // Constructs Kafka consumer record.
        record.headers().add(RecordHeader("X-Correlation-ID", testCorrelationId.toByteArray(StandardCharsets.UTF_8))) // Attaches trace header.

        requestEventListener.listenRequest("Hello from gateway", record) // Triggers listener.

        // Verifies responsePublisherService.publishResponse was called with topic `service-responses` and matching trace ID.
        verify(responsePublisherService).publishResponse(eq("service-responses"), any(), eq(testCorrelationId))
    }
}

package com.example.processor.listener // Defines package for listener unit tests.

import com.example.processor.service.ResponsePublisherService // Imports ResponsePublisherService to mock.
import org.apache.kafka.clients.consumer.ConsumerRecord // Kafka record model.
import org.apache.kafka.common.header.internals.RecordHeader // Record header class.
import org.junit.jupiter.api.Assertions.assertEquals // JUnit 5 assertion.
import org.junit.jupiter.api.Test // JUnit 5 Test annotation.
import org.mockito.Mockito.mock // Mockito mock creation.
import org.mockito.kotlin.any // Mockito Kotlin any matcher.
import org.mockito.kotlin.anyOrNull // Mockito Kotlin null-safe anyOrNull matcher.
import org.mockito.kotlin.eq // Mockito Kotlin eq matcher.
import org.mockito.kotlin.verify // Mockito Kotlin verify method.
import org.slf4j.MDC // SLF4J MDC context.
import java.nio.charset.StandardCharsets // Standard charsets for header byte encoding.

class RequestEventListenerTest {

    private val responsePublisherService: ResponsePublisherService = mock(ResponsePublisherService::class.java) // Creates mock instance.
    private val requestEventListener = RequestEventListener(responsePublisherService) // Instantiates listener under test.

    @Test
    fun `listenRequest consumes payload and produces response`() {
        requestEventListener.listenRequest("Hello from gateway") // Triggers listener.

        // Verifies responsePublisherService.publishResponse was called with topic `service-responses`, payload, and optional key.
        verify(responsePublisherService).publishResponse(eq("service-responses"), any(), anyOrNull())
    }
}

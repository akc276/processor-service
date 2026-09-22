package com.example.processor.tracing

import io.micrometer.tracing.Tracer
import org.springframework.stereotype.Component

/**
 * Spring component holding static reference to Tracer bean for access in Kafka ProducerInterceptors.
 */
@Component
class TracingHolder(tracer: Tracer) {
    init {
        Companion.tracer = tracer
    }

    companion object {
        var tracer: Tracer? = null
    }
}

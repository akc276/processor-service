package com.example.processor.tracing

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.RecordInterceptor
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

/**
 * Spring BeanPostProcessor that automatically configures all Kafka producer factories,
 * Kafka templates, and container listener factories with tracing and AdminClient log flood prevention.
 */
@Component
class KafkaTracingBeanPostProcessor : BeanPostProcessor {

    private val log = LoggerFactory.getLogger(javaClass)

    @Suppress("UNCHECKED_CAST")
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        when (bean) {
            is DefaultKafkaProducerFactory<*, *> -> {
                val customInterceptor = CustomKafkaProducerInterceptor::class.java.name
                val configs = HashMap(bean.configurationProperties)
                val existing = configs[ProducerConfig.INTERCEPTOR_CLASSES_CONFIG]

                val interceptorsList = mutableListOf<String>()
                when (existing) {
                    is List<*> -> interceptorsList.addAll(existing.filterIsInstance<String>())
                    is String -> interceptorsList.add(existing)
                }

                if (!interceptorsList.contains(customInterceptor)) {
                    interceptorsList.add(customInterceptor)
                }

                configs[ProducerConfig.INTERCEPTOR_CLASSES_CONFIG] = interceptorsList
                bean.updateConfigs(configs)
                log.info("Auto-configured CustomKafkaProducerInterceptor on ProducerFactory bean: {}", beanName)
            }

            is KafkaTemplate<*, *> -> {
                // Keep observation disabled to prevent AdminClient 1000s/sec log flooding against Azure EventHubs
                bean.setObservationEnabled(false)
                log.info("Enforced observationEnabled=false on KafkaTemplate bean: {}", beanName)
            }

            is ConcurrentKafkaListenerContainerFactory<*, *> -> {
                val factory = bean as ConcurrentKafkaListenerContainerFactory<Any, Any>
                // Keep observation disabled to prevent AdminClient 1000s/sec log flooding against Azure EventHubs
                factory.containerProperties.isObservationEnabled = false

                // Automatically register RecordInterceptor to extract trace context & activate MDC traceId
                factory.setRecordInterceptor(object : RecordInterceptor<Any, Any> {
                    override fun intercept(record: ConsumerRecord<Any, Any>, consumer: Consumer<Any, Any>): ConsumerRecord<Any, Any> {
                        val traceparent = record.headers().lastHeader("traceparent")?.value()?.let { String(it, StandardCharsets.UTF_8) }
                        val correlationHeader = record.headers().lastHeader("X-Correlation-ID")?.value()?.let { String(it, StandardCharsets.UTF_8) }

                        var traceId: String? = null
                        var spanId: String? = null

                        if (traceparent != null && traceparent.startsWith("00-")) {
                            val parts = traceparent.split("-")
                            if (parts.size >= 4) {
                                traceId = parts[1]
                                spanId = parts[2]
                            }
                        }
                        if (traceId == null) {
                            traceId = correlationHeader
                        }

                        if (traceId != null) {
                            MDC.put("traceId", traceId)
                            if (spanId != null) {
                                MDC.put("spanId", spanId)
                            }
                            log.info("Populated MDC traceId={} spanId={} for incoming record topic={}", traceId, spanId, record.topic())
                        }
                        return record
                    }

                    override fun afterRecord(record: ConsumerRecord<Any, Any>, consumer: Consumer<Any, Any>) {
                        MDC.remove("traceId")
                        MDC.remove("spanId")
                    }
                })
                log.info("Auto-configured tracing RecordInterceptor on ListenerContainerFactory bean: {}", beanName)
            }
        }
        return bean
    }
}

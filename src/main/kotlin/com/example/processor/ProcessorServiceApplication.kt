package com.example.processor // Defines root package for processor service.

import org.springframework.boot.autoconfigure.SpringBootApplication // Annotates Spring Boot main class.
import org.springframework.boot.runApplication // Spring Boot Kotlin run function helper.

@SpringBootApplication // Enables Spring Boot autoconfiguration and component scanning.
class ProcessorServiceApplication // Main application class declaration.

fun main(args: Array<String>) {
    runApplication<ProcessorServiceApplication>(*args) // Starts Spring Boot processor service application.
}

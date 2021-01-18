package com.kicinger.ks.contracts.consumer

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class ProviderApplication

fun main(args: Array<String>) {
    SpringApplication.run(ProviderApplication::class.java, *args)
}
package com.kicinger.ks.contracts.consumer.services

import com.kicinger.ks.contracts.contract.Message
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

const val GET_MESSAGE_PATH = "/api/message"

@Component
class ProviderClient(
        restTemplateBuilder: RestTemplateBuilder,
        @Value("\${application.services.provider.url}") url: String,
) {

    private val restTemplate = restTemplateBuilder.rootUri(url).build()

    fun getMessage(): ResponseEntity<Message> = restTemplate.getForEntity(GET_MESSAGE_PATH, Message::class.java)

//    fun createMessage() = restTemplate.postForEntity()

}
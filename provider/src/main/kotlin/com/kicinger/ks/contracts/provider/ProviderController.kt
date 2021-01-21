package com.kicinger.ks.contracts.provider

import com.kicinger.ks.contracts.contract.Message
import com.kicinger.ks.contracts.contract.requests.CreateMessageCommand
import com.kicinger.ks.contracts.provider.services.MessageService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import kotlin.random.Random

@RestController
class ProviderController(
        private val messageService: MessageService,
) {

    // STEP 9
    @GetMapping(path = ["/api/messages/{messageId}"], produces = ["application/vnd.pricing-facade.messages.v1+json"])
    fun getMessage(@PathVariable messageId: Long): Message {
        println("Test: ${messageId}")
        return messageService.getMessage(messageId)
    }

    @PostMapping(path = ["/api/messages"], consumes = ["application/vnd.pricing-facade.messages.v1+json"], produces = ["application/vnd.pricing-facade.messages.v1+json"])
    fun getMessage(@RequestBody command: CreateMessageCommand): Message {
        val id = Random.nextLong(500)
        return messageService.createMessage(id, command)
    }


}
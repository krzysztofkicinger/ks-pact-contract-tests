package com.kicinger.ks.contracts.provider

import com.kicinger.ks.contracts.contract.Message
import com.kicinger.ks.contracts.provider.services.MessageService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProviderController(
        val messageService: MessageService,
) {

    @GetMapping("/api/message")
    fun getMessage(): Message = messageService.getMessage()

//    @GetMapping("/api/message")
//    fun getMessage(): String = "ala"

}
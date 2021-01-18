package com.kicinger.ks.contracts.provider

import com.kicinger.ks.contracts.contract.Message
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProviderController {

    @GetMapping("/api/message")
    fun getMessage(): Message {
        return Message(1, "John Doe", "Test message")
    }

}
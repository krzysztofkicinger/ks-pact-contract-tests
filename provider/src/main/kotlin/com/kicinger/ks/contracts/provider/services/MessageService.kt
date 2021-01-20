package com.kicinger.ks.contracts.provider.services

import com.kicinger.ks.contracts.contract.Message
import org.springframework.stereotype.Service

@Service
open class MessageService {

    open fun getMessage(): Message = Message(1, "John Doe", "Test message")

}
package com.kicinger.ks.contracts.provider.services

import com.kicinger.ks.contracts.contract.Message
import com.kicinger.ks.contracts.contract.requests.CreateMessageCommand
import org.springframework.stereotype.Service

@Service
open class MessageService {

    val messages = mutableMapOf<Long, Message>()

    open fun createMessage(id: Long, command: CreateMessageCommand): Message {
        return with(command) {
            Message(id, author, message).apply { messages[id] = this }
        }
    }

    open fun getMessage(id: Long): Message {
        return messages.getValue(id)
    }

}
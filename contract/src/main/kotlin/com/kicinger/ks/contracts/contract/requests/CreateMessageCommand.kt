package com.kicinger.ks.contracts.contract.requests

data class CreateMessageCommand(
        val author: String,
        val message: String,
)
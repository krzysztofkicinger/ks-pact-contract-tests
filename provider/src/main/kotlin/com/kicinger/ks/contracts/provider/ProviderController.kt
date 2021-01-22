package com.kicinger.ks.contracts.provider

import com.kicinger.ks.contracts.provider.services.MessageService
import org.springframework.web.bind.annotation.RestController

@RestController
class ProviderController(
        private val messageService: MessageService,
) {

    // TODO: STEP 9

}
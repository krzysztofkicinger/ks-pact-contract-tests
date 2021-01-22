package com.kicinger.ks.contracts.provider

import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import com.kicinger.ks.contracts.provider.services.MessageService
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.junit.jupiter.SpringExtension

const val PROVIDER = "provider"

// TODO: STEP 6.1
class ProviderContractVerificationTest {

    @Autowired
    lateinit var messageService: MessageService

    // TODO: STEP 6.3
    // TODO: STEP 6.4
    // TODO: STEP 6.5

    // TODO: STEP 10

}
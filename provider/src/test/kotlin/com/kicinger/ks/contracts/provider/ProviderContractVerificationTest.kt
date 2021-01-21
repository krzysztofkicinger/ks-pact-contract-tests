package com.kicinger.ks.contracts.provider

import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import com.kicinger.ks.contracts.contract.requests.CreateMessageCommand
import com.kicinger.ks.contracts.provider.services.MessageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

const val PROVIDER = "provider"

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Provider(PROVIDER)
@PactBroker
// TODO STEP 6.1
class ProviderContractVerificationTest {

    @Autowired
    lateinit var messageService: MessageService

    // TODO STEP 6.3
    @LocalServerPort
    var port: Int = 0

    @BeforeEach
    internal fun setUp(context: PactVerificationContext) {
        context.target = HttpTestTarget.fromUrl(URL("http://localhost:$port"))
    }

    // TODO STEP 6.4
    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider::class)
    internal fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    // TODO STEP 6.5
    @State("Provider is up")
    fun providerIsUpState() {
    }

    // STEP 10
    @State("Message with ID 1234 exists in the system")
    fun messageWithId1234ExistsState() {
        messageService.createMessage(1234, CreateMessageCommand("John", "Lorem ipsum"))
    }

    // STEP 10
    @State("No message exists in the system")
    fun noMessageExistInTheSystemState() {
        messageService.clear()
    }

    private fun MessageService.clear() = messages.clear()

}
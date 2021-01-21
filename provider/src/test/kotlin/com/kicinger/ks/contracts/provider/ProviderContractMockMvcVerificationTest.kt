package com.kicinger.ks.contracts.provider

import au.com.dius.pact.core.model.RequestResponseInteraction
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.StateChangeAction.SETUP
import au.com.dius.pact.provider.junitsupport.StateChangeAction.TEARDOWN
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget
import com.kicinger.ks.contracts.contract.Message
import com.kicinger.ks.contracts.contract.requests.CreateMessageCommand
import com.kicinger.ks.contracts.provider.services.MessageService
import com.nhaarman.mockitokotlin2.eq
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.anyLong
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension


@ExtendWith(MockitoExtension::class)
//@WebMvcTest
@Provider("provider")
@PactBroker(host = "localhost", port = "9292", scheme = "http")
class ProviderContractMockMvcVerificationTest {

    @Mock
    lateinit var messageService: MessageService

    @InjectMocks
    lateinit var providerController: ProviderController

//    @Autowired
//    lateinit var mockMvc: MockMvc

    @BeforeEach
    internal fun setUp(context: PactVerificationContext) {
        context.target = MockMvcTestTarget().apply { setControllers(providerController) }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        val requestPath = (context.interaction as RequestResponseInteraction).request.path
        if(!requestPath.startsWith("/actuator")) {
            context.verifyInteraction()
        }
    }

    //  EXTRAS
    @State(value = [ "Provider is up" ], action = TEARDOWN)
    fun providerIsUpState() {
    }

    @State(value = [ "Message with ID 1234 exists in the system"], action = SETUP)
    fun messageWithId1234ExistsState() {
        `when`(messageService.getMessage(eq(1234))).thenReturn(Message(1234, "John", "Lorem ipsum"))
    }

    // STEP 10
    @State("No message exists in the system")
    fun noMessageExistInTheSystemState() {
        val command = CreateMessageCommand("John", "Lorem ipsum")
        `when`(messageService.createMessage(anyLong(), eq(command))).thenReturn(Message(1234, "John", "Lorem ipsum"))
    }

}
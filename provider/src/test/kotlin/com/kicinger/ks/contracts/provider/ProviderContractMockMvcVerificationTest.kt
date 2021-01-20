package com.kicinger.ks.contracts.provider

import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget
import com.kicinger.ks.contracts.contract.Message
import com.kicinger.ks.contracts.provider.services.MessageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest
@Provider("provider")
@PactBroker(host = "localhost", port = "9292", scheme = "http")
class ProviderContractMockMvcVerificationTest {

    @MockBean
    lateinit var messageService: MessageService

    @Autowired
    lateinit var mockMvc: MockMvc

    @BeforeEach
    internal fun setUp(context: PactVerificationContext) {
        context.target = MockMvcTestTarget(mockMvc)
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    @State("")
    fun toGetState() {
//        BDDMockito.given(messageService.getMessage()).willThrow(RuntimeException())
        BDDMockito.given(messageService.getMessage()).willReturn(Message(1, "John Doe", "Test message"))
    }

}
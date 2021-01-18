package com.kicinger.ks.contracts.consumer

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import com.kicinger.ks.contracts.consumer.services.GET_MESSAGE_PATH
import com.kicinger.ks.contracts.contract.Message
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.web.client.RestTemplate

const val CONSUMER = "consumer"
const val PROVIDER = "provider"

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = PROVIDER)
internal class ProviderClientCDCTest {

    private lateinit var restTemplate: RestTemplate

    @BeforeEach
    internal fun setUp(mockServer: MockServer) {
        restTemplate = RestTemplateBuilder()
                .rootUri(mockServer.getUrl())
                .build()
    }

    // @formatter:off
    @Pact(consumer = CONSUMER)
    fun getMessageFromProvider(build: PactDslWithProvider): RequestResponsePact = build
            .given("")
            .uponReceiving("Should return correct message from ")
                .method(GET.name)
                .path(GET_MESSAGE_PATH)
            .willRespondWith()
                .status(OK.value())
                .body(PactDslJsonBody()
                        .numberType("id")
                        .stringType("author")
                        .stringType("message"))
            .toPact()
    // @formatter:on

    @Test
    @PactTestFor(pactMethod = "getMessageFromProvider")
    internal fun getMessageFromProviderTest() {
        val response = restTemplate.getForEntity(GET_MESSAGE_PATH, Message::class.java)
        assertThat(response.statusCode).isEqualTo(OK)
        assertThat(response.body!!.id).isNotNull()
        assertThat(response.body!!.author).isNotEmpty()
        assertThat(response.body!!.message).isNotEmpty()
    }
}
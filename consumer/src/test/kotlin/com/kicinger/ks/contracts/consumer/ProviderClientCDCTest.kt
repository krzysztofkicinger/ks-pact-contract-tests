package com.kicinger.ks.contracts.consumer

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import com.kicinger.ks.contracts.contract.Health
import com.kicinger.ks.contracts.contract.Message
import com.kicinger.ks.contracts.contract.requests.CreateMessageCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.MediaType.parseMediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

const val CONSUMER = "consumer"
const val PROVIDER = "provider"

const val ACTUATOR_HEALTH = "/actuator/health"

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = PROVIDER)
// TODO STEP 4.1 - Add annotations
internal class ProviderClientCDCTest {

    // TODO STEP 4.2 - Add rest template
    private lateinit var restTemplate: RestTemplate

    @BeforeEach
    internal fun setUp(mockServer: MockServer) {
        restTemplate = RestTemplateBuilder()
                .rootUri(mockServer.getUrl())
                .build()
    }

    // TODO STEP 4.3 - Add contract definition
    // STEP 4: Define the contract requirements for actuator's health endpoint
    // @formatter:off
    // BLOG - 1. HOW DOES IT WORK?: Define expected requests and responses
    @Pact(consumer = CONSUMER)
    fun actuatorHealth(build: PactDslWithProvider): RequestResponsePact = build
            .given("Provider is up")
                .uponReceiving("the status of the application")
                .method("GET")
                .path(ACTUATOR_HEALTH)
            .willRespondWith()
                .status(OK.value())
                .headers(mapOf(CONTENT_TYPE to "application/vnd.spring-boot.actuator.v3+json"))
                .body(PactDslJsonBody()
                    .stringType("status", "UP"))
            .toPact()

    // TODO STEP 4.4 - Add contract test
    @Test
    @PactTestFor(pactMethod = "actuatorHealth")
    // BLOG - 2. HOW DOES IT WORK?: Send a real request to a mock provider
    internal fun actuatorHealthTest() {
        val response = restTemplate.getForEntity(ACTUATOR_HEALTH, Health::class.java)

        // BLOG - 3. HOW DOES IT WORK?: Compare the actual result with the expected request
        assertThat(response.statusCode).isEqualTo(OK)
        assertThat(response.body!!.status).isEqualTo("UP")
    }
    // @formatter:on

    // TODO: STEP 8 GET
    // TODO: STEP 8 POST

}
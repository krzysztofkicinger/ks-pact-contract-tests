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

    // @formatter:off
    @Pact(consumer = CONSUMER)
    fun getMessage(build: PactDslWithProvider): RequestResponsePact = build
            .given("Message with ID 1234 exists in the system")
            .uponReceiving("should return that message")
                .method("GET")
                .path("/api/messages/1234")
            .willRespondWith()
                .status(OK.value())
                .headers(mapOf(CONTENT_TYPE to "application/vnd.pricing-facade.messages.v1+json"))
                .body(PactDslJsonBody()
                        .numberType("id", 1234)
                        .stringType("author")
                        .stringType("message"))
            .toPact()

    @Test
    @PactTestFor(pactMethod = "getMessage")
    internal fun getMessageTest() {
        val response = restTemplate.getForEntity("/api/messages/1234", Message::class.java)

        // 3. HOW DOES IT WORK?: Compare the actual result with the expected request
        assertThat(response.statusCode).isEqualTo(OK)
        assertThat(response.body!!.id).isNotNull()
        assertThat(response.body!!.author).isNotEmpty()
        assertThat(response.body!!.message).isNotEmpty()
    }
    // @formatter:on

    // @formatter:off
    @Pact(consumer = CONSUMER)
    fun createMessage(build: PactDslWithProvider): RequestResponsePact = build
            .given("No message exists in the system")
                .uponReceiving("should create a message")
                .method("POST")
                .path("/api/messages")
                .headers(mapOf(
                        CONTENT_TYPE to "application/vnd.pricing-facade.messages.v1+json",
                        ACCEPT to "application/vnd.pricing-facade.messages.v1+json"
                ))
                .body(PactDslJsonBody()
                        .stringMatcher("author", "[a-zA-Z]{1,10}", "John")
                        .stringType("message", "Lorem ipsum")
                )
            .willRespondWith()
                .status(OK.value())
                .headers(mapOf(CONTENT_TYPE to "application/vnd.pricing-facade.messages.v1+json"))
                .body(PactDslJsonBody()
                    .integerType("id")
                    .stringType("author", "John")
                    .stringType("message", "Lorem ipsum"))
            .toPact()

    @Test
    @PactTestFor(pactMethod = "createMessage")
    internal fun createMessageTest() {
        val body = CreateMessageCommand("John", "Some message")
        val headers = HttpHeaders()
        headers.accept = listOf(parseMediaType("application/vnd.pricing-facade.messages.v1+json"))
        headers.contentType = parseMediaType("application/vnd.pricing-facade.messages.v1+json")

        val request = HttpEntity(body, headers)
        val response = restTemplate.postForEntity("/api/messages", request, Message::class.java)

        // 3. HOW DOES IT WORK?: Compare the actual result with the expected request
        assertThat(response.statusCode).isEqualTo(OK)
        assertThat(response.body!!.id).isNotNull()
        assertThat(response.body!!.author).isNotEmpty()
        assertThat(response.body!!.message).isNotEmpty()
    }
    // @formatter:on


}
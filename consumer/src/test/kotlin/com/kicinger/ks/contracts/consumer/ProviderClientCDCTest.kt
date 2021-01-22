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

// TODO: STEP 4.1
internal class ProviderClientCDCTest {

    // TODO: STEP 4.2
    // TODO: STEP 4.3
    // TODO: STEP 4.4

    // TODO: STEP 8 GET
    // TODO: STEP 8 POST

}
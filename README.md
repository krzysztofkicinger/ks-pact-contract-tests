# Knowledge Sharing - Pact Contract Test

## Prerequisites

* Docker
* Java (1.8+)

### Step 1: Prepare environment

Go to the terminal and enter the following commands:

```shell
cd docker
docker-compose up
```

If you are working on Windows please additionally invoke the following command:

```shell
docker-machine ip
```

Pact Broker should be available via the link:

```html
http://localhost:9292
http://
<docker-machine-ip>:9292
```

### STEP 2: Add Pact to the project

```groovy
buildscript {
    ext {
        ...
        pactVersion = '4.1.15'
        pactJUnit5Version = '4.0.10'

        pact_broker_scheme = "http"
        pact_broker_host = "localhost"
        pact_broker_port = "9292"
    }
}

plugins {
    ...
    id "au.com.dius.pact" version "$pactVersion"
}
```

Add plugin usage in consumer and provider applications:

```text
plugins {
    ...
    id "au.com.dius.pact"
}
```

### STEP 3: Configure Pact plugin in the Customer application

Add pact plugin configuration that points to the Pact Broker:

```text
pact {
    publish {
        pactBrokerUrl = "$pact_broker_scheme://$pact_broker_host:$pact_broker_port"
    }
}
```

Add test dependency for Pact (junit5):

```groovy
buildscript {
    ext {
        pactJUnit5Version = '4.0.10'
    }
}

dependencies {
    ...
    testImplementation "au.com.dius:pact-jvm-consumer-junit5:$pactJUnit5Version"
}
```

### STEP 4: Define the contract requirements for actuator's health endpoint

**Step 4.1: Annotate test class with correct annotations**

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = PROVIDER)
```

**Step 4.2: Initialize rest template that samples Pact mock server**

```kotlin
private lateinit var restTemplate: RestTemplate

@BeforeEach
internal fun setUp(mockServer: MockServer) {
    restTemplate = RestTemplateBuilder()
            .rootUri(mockServer.getUrl())
            .build()
}
```

**Step 4.3: Define the interaction**

```kotlin
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
```

**Step 4.4: Add test for the interaction and assert all the required values**

```kotlin
@Test
@PactTestFor(pactMethod = "actuatorHealth")
internal fun actuatorHealthTest() {
    val response = restTemplate.getForEntity(ACTUATOR_HEALTH, Health::class.java)
    
    assertThat(response.statusCode).isEqualTo(OK)
    assertThat(response.body!!.status).isEqualTo("UP")
}
```

### STEP 5: Create Pact and publish it to the pact broker

When running all the consumer tests all pacts for the consumer-provider interactions should be created. If all went
well, you could expect pacts to be created in `consumer/build/pacts` directory.

```shell
./gradlew :consumer:test
```

To publish pacts to pact broker, use the following command:

```shell
./gradlew :consumer:pactPublish
```

### STEP 6: Verify whether the provider fulfills the contract

**Step 6.0: Add provider configuration**

```groovy
buildscript {
    ext {
        pactSpringVersion = '4.1.14'
    }
}


plugins {
    ...
    id "au.com.dius.pact"
}

dependencies {
    ...
    testImplementation("au.com.dius.pact.provider:junit5spring:${pactSpringVersion}")
}

pact {
    serviceProviders {
        'provider' {
            host = 'localhost'
            port = 8081

            hasPactsFromPactBroker("$pact_broker_scheme://$pact_broker_host:$pact_broker_port")
        }
    }
}
```

**Step 6.1: Annotate test class with correct annotations**

```java
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Provider(PROVIDER)
@PactBroker
```

**Step 6.2: Add pact broker configuration to test application.yml**

```yaml
pactbroker:
  scheme: http
  host: localhost/<docker-machine-ip>
  port: 9292
```

**Step 6.3: Initialize test target**

```kotlin
@LocalServerPort
var port: Int = 0

@BeforeEach
internal fun setUp(context: PactVerificationContext) {
    context.target = HttpTestTarget.fromUrl(URL("http://localhost:$port"))
}
```

**Step 6.4: Add test template that will verify all interactions for the provider**

```kotlin
@TestTemplate
@ExtendWith(PactVerificationSpringProvider::class)
internal fun pactVerificationTestTemplate(context: PactVerificationContext) {
    context.verifyInteraction()
}
```

**IMPORTANT: Step 6.5: Add state method that matches the given section from the test scenario (interaction)**

```kotlin
@State("Provider is up")
fun providerIsUpState() {
}
```

### STEP 7: Verify whether the provider fulfills the contract

```shell
./gradlew :provider:test
```

### STEP 8: Request the contract from provider according to the definitions

**GET /api/messages/{messageId}**

| Name | Value |
| --- | --- |
| Method                    | GET |
| Path                      | /api/messages/{messageId} |
| Response Headers          | Content-Type: application/vnd.pact-contract-test-app.messages.v1+json |
| Response Schema           | ```{ "id": "<number>", "author": "<string>", "message": <string> }``` |

<details>
  <summary>GET Request</summary>

```shell
@Pact(consumer = CONSUMER)
fun getMessage(build: PactDslWithProvider): RequestResponsePact = build
        .given("Message with ID 1234 exists in the system")
        .uponReceiving("should return that message")
            .method("GET")
            .path("/api/messages/1234")
        .willRespondWith()
            .status(OK.value())
            .headers(mapOf(CONTENT_TYPE to "application/vnd.pact-contract-test-app.messages.v1+json"))
            .body(PactDslJsonBody()
                    .numberType("id", 1234)
                    .stringType("author")
                    .stringType("message"))
        .toPact()

@Test
@PactTestFor(pactMethod = "getMessage")
internal fun getMessageTest() {
    val response = restTemplate.getForEntity("/api/messages/1234", Message::class.java)

    assertThat(response.statusCode).isEqualTo(OK)
    assertThat(response.body!!.id).isNotNull()
    assertThat(response.body!!.author).isNotEmpty()
    assertThat(response.body!!.message).isNotEmpty()
}
```

</details>

**POST /api/messages**

| Name | Value |
| --- | --- |
| Method                    | POST |
| Path                      | /api/messages |
| Request Headers           | Accept: application/vnd.pact-contract-test-app.messages.v1+json, Content-Type: application/vnd.pact-contract-test-app.messages.v1+json |
| Response Schema           | ```{ "author": "<string>", "message": <string> }``` |
| Response Headers          | Content-Type: application/vnd.pact-contract-test-app.messages.v1+json |
| Response Schema           | ```{ "id": "<number>", "author": "<string>", "message": <string> }``` |

<details>
  <summary>POST Request</summary>

```shell
@Pact(consumer = CONSUMER)
fun createMessage(build: PactDslWithProvider): RequestResponsePact = build
        .given("No message exists in the system")
            .uponReceiving("should create a message")
            .method("POST")
            .path("/api/messages")
            .headers(mapOf(
                    CONTENT_TYPE to "application/vnd.pact-contract-test-app.messages.v1+json",
                    ACCEPT to "application/vnd.pact-contract-test-app.messages.v1+json"
            ))
            .body(PactDslJsonBody()
                    .stringMatcher("author", "[a-zA-Z]{1,10}", "John")
                    .stringType("message", "Lorem ipsum")
            )
        .willRespondWith()
            .status(OK.value())
            .headers(mapOf(CONTENT_TYPE to "application/vnd.pact-contract-test-app.messages.v1+json"))
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
    headers.accept = listOf(parseMediaType("application/vnd.pact-contract-test-app.messages.v1+json"))
    headers.contentType = parseMediaType("application/vnd.pact-contract-test-app.messages.v1+json")

    val request = HttpEntity(body, headers)
    val response = restTemplate.postForEntity("/api/messages", request, Message::class.java)

    // 3. HOW DOES IT WORK?: Compare the actual result with the expected request
    assertThat(response.statusCode).isEqualTo(OK)
    assertThat(response.body!!.id).isNotNull()
    assertThat(response.body!!.author).isNotEmpty()
    assertThat(response.body!!.message).isNotEmpty()
}
```

</details>

To publish pacts to pact broker, use the following command:

```shell
./gradlew :consumer:test
./gradlew :consumer:pactPublish
```

### STEP 9: Implement requested endpoints on provider side

```kotlin
@GetMapping(path = ["/api/messages/{messageId}"], produces = ["application/vnd.pact-contract-test-app.messages.v1+json"])
fun getMessage(@PathVariable messageId: Long): Message {
    println("Test: ${messageId}")
    return messageService.getMessage(messageId)
}

@PostMapping(path = ["/api/messages"], consumes = ["application/vnd.pact-contract-test-app.messages.v1+json"], produces = ["application/vnd.pact-contract-test-app.messages.v1+json"])
fun getMessage(@RequestBody command: CreateMessageCommand): Message {
    val id = Random.nextLong(500)
    return messageService.createMessage(id, command)
}
```

### STEP 10: Verify the contract on producer side

```kotlin
@State("Message with ID 1234 exists in the system")
fun messageWithId1234ExistsState() {
    messageService.createMessage(1234, CreateMessageCommand("John", "Lorem ipsum"))
}

@State("No message exists in the system")
fun noMessageExistInTheSystemState() {
    messageService.clear()
}

private fun MessageService.clear() = messages.clear()
```

### EXTRAS: Verify the contract on producer side (Contracts in isolation)

<details>
    <summary>Expand code</summary>

```kotlin
package com.kicinger.ks.contracts.provider

import au.com.dius.pact.core.model.Interaction
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
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(ProviderController::class)
@Provider(PROVIDER)
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
        if (context.interaction.isNotActuatorInteraction()) {
            context.verifyInteraction()
        }
    }

    //  EXTRAS
    @State(value = ["Provider is up"], action = TEARDOWN)
    fun providerIsUpState() {
    }

    @State(value = ["Message with ID 1234 exists in the system"], action = SETUP)
    fun messageWithId1234ExistsState() {
        `when`(messageService.getMessage(eq(1234))).thenReturn(Message(1234, "John", "Lorem ipsum"))
    }

    // STEP 10
    @State("No message exists in the system")
    fun noMessageExistInTheSystemState() {
        val command = CreateMessageCommand("John", "Lorem ipsum")
        `when`(messageService.createMessage(anyLong(), eq(command))).thenReturn(Message(1234, "John", "Lorem ipsum"))
    }

    private fun Interaction.isNotActuatorInteraction() =
            (this as RequestResponseInteraction).request.path.startsWith("/actuator")

}
```

</details>
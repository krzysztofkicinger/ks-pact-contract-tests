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
  host: localhost
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
fun toGetState() {
}
```

### STEP 7: Verify whether the provider fulfills the contract

To publish pacts to pact broker, use the following command:

```shell
./gradlew :provider:test
```

### STEP 6: Request the contract from provider according to the definitions

**GET /api/messages/{messageId}**

| Name | Value |
| --- | --- |
| Method                    | GET |
| Path                      | /api/messages/{messageId} |
| Request Headers           | Accept: application/vnd.contract-tests-app.messages.v1+json |
| Response Headers          | Content-Type: application/vnd.pricing-facade.messages.v1+json |
| Response Schema           | ```{ "id": "<number>", "author": "<string>", "message": <string> }``` |

**POST /api/messages**

| Name | Value |
| --- | --- |
| Method                    | POST |
| Path                      | /api/messages |
| Request Headers           | Accept: application/vnd.contract-tests-app.messages.v1+json, Content-Type: application/vnd.pricing-facade.messages.v1+json |
| Response Schema           | ```{ "author": "<string>", "message": <string> }``` |
| Response Headers          | Content-Type: application/vnd.pricing-facade.messages.v1+json |
| Response Schema           | ```{ "id": "<number>", "author": "<string>", "message": <string> }``` |

### STEP 6: Provide the contract on the provider side (+ verify the contract)

### STEP 7: Verify the contract on producer side (Contracts in isolation)

### STEP 8: Verify the contract on producer side (Service (functionality) in isolation)

STEP 1: Add pact to the project

Add plugin definition to the main project:

STEP 2: Configure pact plugin in Consumer application

STEP 3: Publish contract requirements in Constructor




STEP 0: Run pact broker

Go to the terminal (ALT + F12)

```text
cd docker
docker-compose up
```

Browser: http://localhost:9292
Browser: http://<docker_ip>:9292

check initial pact broker

STEP 1: Add pact to the project

Add plugin definition to the main project:

```text
buildscript {
    ext {
        ...
        pactVersion = '4.0.10'
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

STEP 2: Configure pact plugin in Consumer application

```text
ext {
    broker_host = 'localhost'
    broker_port = '9292'
    broker_scheme = 'http'
    java_Version = JavaVersion.VERSION_11
    api_Verion = '1.3'
}

pact {
    publish {
        pactBrokerUrl = "$broker_scheme://$broker_host:$broker_port"
    }
}

task contractTest(type: Test) {
    description = "Consumer Driven Contract - PACT Based"
    group = "verification"

    systemProperty "pact.verifier.publishResults", "true"
    systemProperty "pactbroker.host", broker_host
    systemProperty "pactbroker.port", broker_port
    systemProperty "pactbroker.scheme", broker_scheme

    useJUnitPlatform()

    testLogging {
        events "passed", "skipped", "failed"
    }
}

dependencies {
    ...
    testImplementation "au.com.dius:pact-jvm-consumer-junit5:$pactVersion"
}
```



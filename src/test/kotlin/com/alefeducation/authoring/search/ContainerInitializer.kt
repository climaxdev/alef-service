package com.alefeducation.authoring.search

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer

class ContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {
        // Lazy because we only want it to be initialized when accessed
        private val elasticsearch: KGenericContainerByName by lazy {
            KGenericContainerByName("docker.elastic.co/elasticsearch/elasticsearch:8.7.1")
                .withExposedPorts(9200)
                .withEnv("xpack.security.enabled", "false")
                .withEnv("discovery.type", "single-node")
        }
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        elasticsearch.start()
        val host = elasticsearch.host
        val port = elasticsearch.getMappedPort(9200)
        TestPropertyValues.of(
            "spring.elasticsearch.uris=http://$host:$port"
        ).applyTo(applicationContext.environment)
    }
}

class KGenericContainerByName(image: String) : GenericContainer<KGenericContainerByName>(image)

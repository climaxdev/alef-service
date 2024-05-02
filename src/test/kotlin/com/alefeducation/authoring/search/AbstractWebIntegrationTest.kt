package com.alefeducation.authoring.search

import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = [ContainerInitializer::class])
abstract class AbstractWebIntegrationTest {
    @Autowired lateinit var reactiveOperations: ReactiveElasticsearchOperations
    @Autowired lateinit var webClient: WebTestClient
}

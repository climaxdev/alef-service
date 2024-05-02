package com.alefeducation.authoring.search.infra

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import com.alefeducation.authoring.search.ContainerInitializer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [ContainerInitializer::class])
@ExperimentalCoroutinesApi
@ImportAutoConfiguration(TestChannelBinderConfiguration::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BaseTestIT {

    @Autowired
    lateinit var reactiveOperations: ReactiveElasticsearchOperations

    @AfterEach
    fun afterEach() {
        reactiveOperations.delete(
            NativeQueryBuilder().withQuery(Query(QueryBuilders.matchAll().build())).build(),
            Any::class.java, IndexCoordinates.of("aat")
        ).block()
    }
}

fun Any.toMap(): Map<String, Any> {
    return this.javaClass.declaredFields.associate { field ->
        field.isAccessible = true
        field.name to field.get(this)
    }
}

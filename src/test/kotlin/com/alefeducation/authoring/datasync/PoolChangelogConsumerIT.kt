package com.alefeducation.authoring.datasync

import com.alefeducation.authoring.datasync.Constants.INDEX_NAME
import com.alefeducation.authoring.datasync.event.consumer.PoolChangeLogEventType
import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.infra.BaseTestIT
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.binder.test.InputDestination
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.messaging.support.MessageBuilder

@OptIn(ExperimentalCoroutinesApi::class)
class PoolChangelogConsumerIT(
    @Autowired
    private val inputDestination: InputDestination,
    @Autowired
    private val changeLogEventFactory: ChangeLogEventFactory
) : BaseTestIT() {

    @Test
    fun `should create pool`() {
        // Given
        val poolEvent = changeLogEventFactory.createPoolPersistedEvent()

        val message = MessageBuilder.withPayload(poolEvent).setHeader("eventType", PoolChangeLogEventType.PoolPersisted.name).build()
        inputDestination.send(message, Constants.POOL_CHANGELOG_DESTINATION)
        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(Constants.INDEX_NAME)).refresh().block()

        // When
        val result = reactiveOperations.get(poolEvent["id"].toString(), Map::class.java, IndexCoordinates.of("aat")).blockOptional().get()

        // Then
        result.run {
            this["id"] shouldBe poolEvent["id"]
            this["name"] shouldBe poolEvent["name"]
            this["_kind"] shouldBe KindType.POOLS.value
            this["_organisation"] shouldBe (poolEvent["organisations"] as List<*>).first()
        }
    }

    @Test
    fun `should update pool`() {
        // create a pool first
        val existingPool = changeLogEventFactory.createPoolPersistedEvent().toMutableMap()
        reactiveOperations.save(existingPool, IndexCoordinates.of(INDEX_NAME)).block()

        // Given
        existingPool["name"] = "updated name"
        val message = MessageBuilder.withPayload(existingPool).setHeader("eventType", PoolChangeLogEventType.PoolPersisted.name).build()
        inputDestination.send(message, Constants.POOL_CHANGELOG_DESTINATION)
        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(INDEX_NAME)).refresh().block()

        // When
        val result = reactiveOperations.get(existingPool["id"].toString(), Map::class.java, IndexCoordinates.of(INDEX_NAME)).blockOptional().get()

        // Then
        result.run {
            this["id"] shouldBe existingPool["id"]
            this["name"] shouldBe "updated name"
            this["_kind"] shouldBe KindType.POOLS.value
            this["_organisation"] shouldBe (existingPool["organisations"] as List<*>).first()
        }
    }

    @Test
    fun `should delete pool`() {
        // create a pool first
        val existingPool = changeLogEventFactory.createPoolPersistedEvent().toMutableMap()
        reactiveOperations.save(existingPool, IndexCoordinates.of(INDEX_NAME)).block()

        // Given
        val message = MessageBuilder.withPayload(mapOf("id" to existingPool["id"])).setHeader("eventType", PoolChangeLogEventType.PoolDeleted.name).build()
        inputDestination.send(message, Constants.POOL_CHANGELOG_DESTINATION)
        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(INDEX_NAME)).refresh().block()

        // When
        val result = reactiveOperations.get(existingPool["id"].toString(), Map::class.java, IndexCoordinates.of(INDEX_NAME)).blockOptional()
        result.isEmpty shouldBe true
    }
}

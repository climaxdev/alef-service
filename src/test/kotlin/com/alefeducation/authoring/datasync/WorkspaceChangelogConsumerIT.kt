package com.alefeducation.authoring.datasync

import com.alefeducation.authoring.datasync.Constants.INDEX_NAME
import com.alefeducation.authoring.datasync.event.consumer.WorkspaceChangeLogEventType
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
class WorkspaceChangelogConsumerIT(
    @Autowired
    private val inputDestination: InputDestination,
    @Autowired
    private val changeLogEventFactory: ChangeLogEventFactory
) : BaseTestIT() {

    @Test
    fun `should create workspace`() {
        // Given
        val event = changeLogEventFactory.createWorkspacePersistedEvent()

        val message = MessageBuilder.withPayload(event).setHeader("eventType", WorkspaceChangeLogEventType.WorkspacePersisted.name).build()
        inputDestination.send(message, Constants.WORKSPACE_CHANGELOG_DESTINATION)
        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(INDEX_NAME)).refresh().block()

        // When
        val result = reactiveOperations.get(event["id"].toString(), Map::class.java, IndexCoordinates.of("aat")).blockOptional().get()

        // Then
        result.run {
            this["id"] shouldBe event["id"]
            this["name"] shouldBe event["name"]
            this["_kind"] shouldBe KindType.WORKSPACES.value
            this["_organisation"] shouldBe (event["organisations"] as List<*>).first()
        }
    }

    @Test
    fun `should update workspace`() {
        // create a workspace first
        val existingWorkspace = changeLogEventFactory.createWorkspacePersistedEvent().toMutableMap()
        reactiveOperations.save(existingWorkspace, IndexCoordinates.of(INDEX_NAME)).block()

        // Given
        existingWorkspace["name"] = "updated name"
        val message = MessageBuilder.withPayload(existingWorkspace).setHeader("eventType", WorkspaceChangeLogEventType.WorkspacePersisted.name).build()
        inputDestination.send(message, Constants.WORKSPACE_CHANGELOG_DESTINATION)
        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(INDEX_NAME)).refresh().block()

        // When
        val result = reactiveOperations.get(existingWorkspace["id"].toString(), Map::class.java, IndexCoordinates.of(INDEX_NAME)).blockOptional().get()

        // Then
        result.run {
            this["id"] shouldBe existingWorkspace["id"]
            this["name"] shouldBe "updated name"
            this["_kind"] shouldBe KindType.WORKSPACES.value
            this["_organisation"] shouldBe (existingWorkspace["organisations"] as List<*>).first()
        }
    }

    @Test
    fun `should delete workspace`() {
        // create a workspace first
        val existingWorkspace = changeLogEventFactory.createWorkspacePersistedEvent().toMutableMap()
        reactiveOperations.save(existingWorkspace, IndexCoordinates.of(INDEX_NAME)).block()

        // Given
        val message = MessageBuilder.withPayload(mapOf("id" to existingWorkspace["id"])).setHeader("eventType", WorkspaceChangeLogEventType.WorkspaceDeleted.name).build()
        inputDestination.send(message, Constants.WORKSPACE_CHANGELOG_DESTINATION)
        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(INDEX_NAME)).refresh().block()

        // When
        val result = reactiveOperations.get(existingWorkspace["id"].toString(), Map::class.java, IndexCoordinates.of(INDEX_NAME)).blockOptional()
        result.isEmpty shouldBe true
    }
}

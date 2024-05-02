package com.alefeducation.authoring.datasync

import com.alefeducation.authoring.datasync.Constants.INDEX_NAME
import com.alefeducation.authoring.datasync.event.consumer.QuestionChangeLogEventType
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
class QuestionChangelogConsumerIT(
    @Autowired
    private val inputDestination: InputDestination,
    @Autowired
    private val changeLogEventFactory: ChangeLogEventFactory
) : BaseTestIT() {

    @Test
    fun `should create question`() {
        // Given
        val questionEvent = changeLogEventFactory.createQuestionPersistedEvent()

        val message = MessageBuilder.withPayload(questionEvent).setHeader("eventType", QuestionChangeLogEventType.QuestionPersisted.name).build()
        inputDestination.send(message, Constants.QUESTION_CHANGELOG_DESTINATION)
        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(Constants.INDEX_NAME)).refresh().block()

        // When
        val result = reactiveOperations.get(questionEvent["id"].toString(), Map::class.java, IndexCoordinates.of("aat")).blockOptional().get()

        // Then
        result.run {
            this["id"] shouldBe questionEvent["id"]
            this["code"] shouldBe questionEvent["code"]
            this["_kind"] shouldBe KindType.QUESTIONS.value
            this["_organisation"] shouldBe (questionEvent["organisations"] as List<*>).first()
        }
    }

    @Test
    fun `should update question`() {
        // create a question first
        val existingQuestion = changeLogEventFactory.createQuestionPersistedEvent().toMutableMap()
        reactiveOperations.save(existingQuestion, IndexCoordinates.of(INDEX_NAME)).block()

        // Given
        existingQuestion["code"] = "updated code"
        val message = MessageBuilder.withPayload(existingQuestion).setHeader("eventType", QuestionChangeLogEventType.QuestionPersisted.name).build()
        inputDestination.send(message, Constants.QUESTION_CHANGELOG_DESTINATION)
        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(INDEX_NAME)).refresh().block()

        // When
        val result = reactiveOperations.get(existingQuestion["id"].toString(), Map::class.java, IndexCoordinates.of(INDEX_NAME)).blockOptional().get()

        // Then
        result.run {
            this["id"] shouldBe existingQuestion["id"]
            this["code"] shouldBe "updated code"
            this["_kind"] shouldBe KindType.QUESTIONS.value
            this["_organisation"] shouldBe (existingQuestion["organisations"] as List<*>).first()
        }
    }

    @Test
    fun `should delete question`() {
        // create a question first
        val existingQuestion = changeLogEventFactory.createQuestionPersistedEvent().toMutableMap()
        reactiveOperations.save(existingQuestion, IndexCoordinates.of(INDEX_NAME)).block()

        // Given
        val message = MessageBuilder.withPayload(mapOf("id" to existingQuestion["id"])).setHeader("eventType", QuestionChangeLogEventType.QuestionDeleted.name).build()
        inputDestination.send(message, Constants.QUESTION_CHANGELOG_DESTINATION)
        // refresh index to make sure all the documents are searchable
        reactiveOperations.indexOps(IndexCoordinates.of(INDEX_NAME)).refresh().block()

        // When
        val result = reactiveOperations.get(existingQuestion["id"].toString(), Map::class.java, IndexCoordinates.of(INDEX_NAME)).blockOptional()
        result.isEmpty shouldBe true
    }
}

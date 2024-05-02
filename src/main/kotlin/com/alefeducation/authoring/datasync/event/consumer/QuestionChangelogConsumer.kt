package com.alefeducation.authoring.datasync.event.consumer

import com.alefeducation.authoring.datasync.infra.question.DeleteQuestion
import com.alefeducation.authoring.datasync.infra.question.PersistQuestion
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component
class QuestionChangelogConsumer(
    private val persistQuestion: PersistQuestion,
    private val deleteQuestion: DeleteQuestion
) : (Message<Map<String, Any>>) -> Unit {

    companion object {
        val log: Logger = LoggerFactory.getLogger(QuestionChangelogConsumer::class.java)
    }

    override fun invoke(message: Message<Map<String, Any>>): Unit =
        when (message.headers["eventType"]) {
            QuestionChangeLogEventType.QuestionPersisted.name -> persistQuestion.invoke(message.payload)
            QuestionChangeLogEventType.QuestionDeleted.name -> deleteQuestion.invoke(message.payload)
            else -> log.warn("Unknown event type ${message.headers["eventType"]}")
        }
}

enum class QuestionChangeLogEventType {
    QuestionPersisted,
    QuestionDeleted,
}

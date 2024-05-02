package com.alefeducation.authoring.datasync.event.consumer

import com.alefeducation.authoring.datasync.infra.pool.DeletePool
import com.alefeducation.authoring.datasync.infra.pool.PersistPool
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component
class PoolChangelogConsumer(
    private val persistPool: PersistPool,
    private val deletePool: DeletePool
) : (Message<Map<String, Any>>) -> Unit {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PoolChangelogConsumer::class.java)
    }
    override fun invoke(message: Message<Map<String, Any>>): Unit =
        when (message.headers["eventType"]) {
            PoolChangeLogEventType.PoolPersisted.name -> persistPool.invoke(message.payload)
            PoolChangeLogEventType.PoolDeleted.name -> deletePool.invoke(message.payload)
            else -> log.warn("Unknown event type ${message.headers["eventType"]}")
        }
}

enum class PoolChangeLogEventType {
    PoolPersisted,
    PoolDeleted,
}

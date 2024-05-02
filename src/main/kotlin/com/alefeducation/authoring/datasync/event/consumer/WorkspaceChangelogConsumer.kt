package com.alefeducation.authoring.datasync.event.consumer

import com.alefeducation.authoring.datasync.infra.workspace.DeleteWorkspace
import com.alefeducation.authoring.datasync.infra.workspace.PersistWorkspace
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component
class WorkspaceChangelogConsumer(
    private val persistWorkspace: PersistWorkspace,
    private val deleteWorkspace: DeleteWorkspace
) : (Message<Map<String, Any>>) -> Unit {

    companion object {
        val log: Logger = LoggerFactory.getLogger(WorkspaceChangelogConsumer::class.java)
    }

    override fun invoke(message: Message<Map<String, Any>>): Unit =
        when (message.headers["eventType"]) {
            WorkspaceChangeLogEventType.WorkspacePersisted.name -> persistWorkspace.invoke(message.payload)
            WorkspaceChangeLogEventType.WorkspaceDeleted.name -> deleteWorkspace.invoke(message.payload)
            else -> log.warn("Unknown event type ${message.headers["eventType"]}")
        }
}

enum class WorkspaceChangeLogEventType {
    WorkspacePersisted,
    WorkspaceDeleted,
}

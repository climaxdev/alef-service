package com.alefeducation.authoring.datasync.infra.workspace

import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.infra.config.Platform.indexNames
import com.alefeducation.authoring.search.infra.config.PlatformProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Component

fun interface DeleteWorkspace {
    fun invoke(workspace: Map<String, Any>)
}

@Component
class DeleteWorkspaceElasticSearch(
    private val reactiveElasticsearchTemplate: ReactiveElasticsearchTemplate,
    private val platformProperties: PlatformProperties
) : DeleteWorkspace {

    companion object {
        val log: Logger = LoggerFactory.getLogger(DeleteWorkspaceElasticSearch::class.java)
    }

    override fun invoke(workspace: Map<String, Any>) {
        require(!(workspace["id"] as? String).isNullOrEmpty()) { "workspace must have id" }
        log.info("Deleting workspace ${workspace["id"]}")
        reactiveElasticsearchTemplate.delete(workspace["id"].toString(), indexCoordinates()).blockOptional()
    }

    private fun indexCoordinates(): IndexCoordinates = IndexCoordinates.of(*platformProperties.indexNames(listOf(KindType.WORKSPACES)).toTypedArray())
}

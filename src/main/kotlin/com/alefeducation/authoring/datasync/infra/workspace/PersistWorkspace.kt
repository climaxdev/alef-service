package com.alefeducation.authoring.datasync.infra.workspace

import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.infra.config.Platform.indexNames
import com.alefeducation.authoring.search.infra.config.PlatformProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Component

fun interface PersistWorkspace {
    fun invoke(workspace: Map<String, Any>)
}

@Component
class PersistWorkspaceElasticSearch(
    private val reactiveElasticsearchTemplate: ReactiveElasticsearchTemplate,
    private val platformProperties: PlatformProperties
) : PersistWorkspace {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PersistWorkspaceElasticSearch::class.java)
    }

    override fun invoke(workspace: Map<String, Any>) {
        require(!(workspace["id"] as? String).isNullOrEmpty()) { "workspace must have id" }
        require(!(workspace["organisations"] as? List<*>).isNullOrEmpty()) { "workspace must have organisations" }
        log.info("Persisting workspace ${workspace["id"]}")
        reactiveElasticsearchTemplate.save(enrich(workspace), indexCoordinates()).blockOptional()
    }

    private fun enrich(workspace: Map<String, Any>): Map<String, Any?> = workspace.plus("_kind" to KindType.WORKSPACES.value)

    private fun indexCoordinates(): IndexCoordinates = IndexCoordinates.of(*platformProperties.indexNames(listOf(KindType.WORKSPACES)).toTypedArray())
}

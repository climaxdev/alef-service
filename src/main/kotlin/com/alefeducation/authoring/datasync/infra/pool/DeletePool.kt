package com.alefeducation.authoring.datasync.infra.pool

import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.infra.config.Platform.indexNames
import com.alefeducation.authoring.search.infra.config.PlatformProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Component

fun interface DeletePool {
    fun invoke(pool: Map<String, Any>)
}

@Component
class DeletePoolElasticSearch(
    private val reactiveElasticsearchTemplate: ReactiveElasticsearchTemplate,
    private val platformProperties: PlatformProperties
) : DeletePool {

    companion object {
        val log: Logger = LoggerFactory.getLogger(DeletePoolElasticSearch::class.java)
    }

    override fun invoke(pool: Map<String, Any>) {
        require(!(pool["id"] as? String).isNullOrEmpty()) { "pool must have id" }
        log.info("Deleting pool ${pool["id"]}")
        reactiveElasticsearchTemplate.delete(pool["id"].toString(), indexCoordinates()).blockOptional()
    }

    private fun indexCoordinates(): IndexCoordinates = IndexCoordinates.of(*platformProperties.indexNames(listOf(KindType.POOLS)).toTypedArray())
}

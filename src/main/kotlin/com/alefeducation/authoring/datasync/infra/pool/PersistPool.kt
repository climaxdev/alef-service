package com.alefeducation.authoring.datasync.infra.pool

import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.infra.config.Platform.indexNames
import com.alefeducation.authoring.search.infra.config.PlatformProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Component

fun interface PersistPool {
    fun invoke(pool: Map<String, Any>)
}

@Component
class PersistPoolElasticSearch(
    private val reactiveElasticsearchTemplate: ReactiveElasticsearchTemplate,
    private val platformProperties: PlatformProperties
) : PersistPool {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PersistPoolElasticSearch::class.java)
    }

    override fun invoke(pool: Map<String, Any>) {
        require(!(pool["id"] as? String).isNullOrEmpty()) { "pool must have id" }
        require(!(pool["organisations"] as? List<*>).isNullOrEmpty()) { "pool must have organisations" }
        log.info("Persisting pool ${pool["id"]}")
        reactiveElasticsearchTemplate.save(enrich(pool), indexCoordinates()).blockOptional()
    }

    private fun enrich(pool: Map<String, Any>): Map<String, Any?> = pool.plus("_kind" to KindType.POOLS.value)

    private fun indexCoordinates(): IndexCoordinates = IndexCoordinates.of(*platformProperties.indexNames(listOf(KindType.POOLS)).toTypedArray())
}

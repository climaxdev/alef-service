package com.alefeducation.authoring.datasync.infra.question

import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.infra.config.Platform.indexNames
import com.alefeducation.authoring.search.infra.config.PlatformProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Component

fun interface DeleteQuestion {
    fun invoke(question: Map<String, Any>)
}

@Component
class DeleteQuestionElasticSearch(
    private val reactiveElasticsearchTemplate: ReactiveElasticsearchTemplate,
    private val platformProperties: PlatformProperties
) : DeleteQuestion {

    companion object {
        val log: Logger = LoggerFactory.getLogger(DeleteQuestionElasticSearch::class.java)
    }

    override fun invoke(question: Map<String, Any>) {
        require(!(question["id"] as? String).isNullOrEmpty()) { "question must have id" }
        log.info("Deleting question ${question["id"]}")
        reactiveElasticsearchTemplate.delete(question["id"].toString(), indexCoordinates()).blockOptional()
    }

    private fun indexCoordinates(): IndexCoordinates = IndexCoordinates.of(*platformProperties.indexNames(listOf(KindType.QUESTIONS)).toTypedArray())
}

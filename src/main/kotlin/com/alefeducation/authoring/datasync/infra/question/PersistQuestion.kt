package com.alefeducation.authoring.datasync.infra.question

import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.infra.config.Platform.indexNames
import com.alefeducation.authoring.search.infra.config.PlatformProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Component

fun interface PersistQuestion {
    fun invoke(question: Map<String, Any>)
}

@Component
class PersistQuestionElasticSearch(
    private val reactiveElasticsearchTemplate: ReactiveElasticsearchTemplate,
    private val platformProperties: PlatformProperties
) : PersistQuestion {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PersistQuestionElasticSearch::class.java)
    }
    override fun invoke(question: Map<String, Any>) {
        require(!(question["id"] as? String).isNullOrEmpty()) { "question must have id" }
        require(!(question["organisations"] as? List<*>).isNullOrEmpty()) { "question must have organisations" }
        log.info("Persisting question ${question["id"]}")
        reactiveElasticsearchTemplate.save(enrich(question), indexCoordinates()).blockOptional()
    }

    private fun enrich(question: Map<String, Any>): Map<String, Any?> = question.plus("_kind" to KindType.QUESTIONS.value)

    private fun indexCoordinates(): IndexCoordinates = IndexCoordinates.of(*platformProperties.indexNames(listOf(KindType.QUESTIONS)).toTypedArray())
}

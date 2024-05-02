package com.alefeducation.authoring.search.infra.storage

import arrow.core.Either
import co.elastic.clients.elasticsearch.core.search.Suggester
import com.alefeducation.authoring.search.domain.FieldPriority
import com.alefeducation.authoring.search.domain.FieldPriority.POOL_CODE
import com.alefeducation.authoring.search.domain.FieldPriority.QUESTION_BODY
import com.alefeducation.authoring.search.domain.FieldPriority.QUESTION_CODE
import com.alefeducation.authoring.search.domain.FieldPriority.WORKSPACE
import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.domain.Suggestion
import com.alefeducation.authoring.search.infra.config.Platform.kindProperties
import com.alefeducation.authoring.search.infra.config.PlatformProperties
import com.alefeducation.authoring.search.infra.http.inputs.GetSuggestionInput
import com.alefeducation.authoring.search.infra.storage.suggester.PoolsCodeSuggester
import com.alefeducation.authoring.search.infra.storage.suggester.QuestionBodySuggester
import com.alefeducation.authoring.search.infra.storage.suggester.QuestionCodeSuggester
import com.alefeducation.authoring.search.infra.storage.suggester.SuggesterFactory
import com.alefeducation.authoring.search.infra.storage.suggester.WorkspaceSuggester
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.suggest.response.Suggest
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux

internal typealias StorageSuggester = com.alefeducation.authoring.search.infra.storage.suggester.Suggester

@Component
class SuggestionStorage(
    val reactiveElasticsearchTemplate: ReactiveElasticsearchTemplate,
    val platformProperties: PlatformProperties
) {
    suspend fun suggestions(input: GetSuggestionInput): Flow<Suggestion> {
        return SuggesterSession.Builder().given(input).given(platformProperties).build()
            .execute(reactiveElasticsearchTemplate)
    }
}

internal class SuggesterSession private constructor(
    private val suggester: Suggester,
    private val indices: IndexCoordinates
) {

    fun execute(reactiveElasticsearchTemplate: ReactiveElasticsearchTemplate): Flow<Suggestion> {
        val query = NativeQueryBuilder().withSuggester(suggester).build()
        return reactiveElasticsearchTemplate.suggest(query, Any::class.java, indices).flatMapMany { extract(it) }
            .asFlow()
    }

    private fun extract(suggest: Suggest): Flux<Suggestion> =
        getSuggestions(suggest, "question-body-suggest", QUESTION_BODY)
            .plus(getSuggestions(suggest, "question-code-suggest", QUESTION_CODE))
            .plus(getSuggestions(suggest, "pool-code-suggest", POOL_CODE))
            .plus(getSuggestions(suggest, "workspace-name-suggest", WORKSPACE))
            .toFlux()

    private fun getSuggestions(suggest: Suggest, name: String, priority: FieldPriority): List<Suggestion> =
        suggest.getSuggestion(name)?.entries?.flatMap { it.options }
            ?.map { Suggestion(it.text, it.score!!, priority) } ?: emptyList()

    internal class Builder {
        private var suggesterFactories: List<SuggesterFactory<StorageSuggester>> = listOf()
        private lateinit var input: GetSuggestionInput
        private lateinit var platformProperties: PlatformProperties

        private fun of(vararg suggesterFactories: SuggesterFactory<StorageSuggester>): Builder = apply {
            this.suggesterFactories = this.suggesterFactories.plus(suggesterFactories)
        }

        fun given(input: GetSuggestionInput): Builder =
            apply {
                this.input = input
            }.apply {
                val factories = input.kind.distinct().flatMap {
                    when (it) {
                        KindType.QUESTIONS -> listOf(QuestionCodeSuggester, QuestionBodySuggester)
                        KindType.WORKSPACES -> listOf(WorkspaceSuggester)
                        KindType.POOLS -> listOf(PoolsCodeSuggester)
                    }
                }
                of(*factories.toTypedArray())
            }

        fun given(platformProperties: PlatformProperties): Builder = apply {
            this.platformProperties = platformProperties
        }

        fun build(): SuggesterSession {
            assert(this::input.isInitialized) { "input is not given" }
            assert(this::platformProperties.isInitialized) { "platformProperties is not given" }

            val suggestersMap = suggesterFactories
                .map { factory -> factory(input) }.associate { it.name to it.build() }
            val suggester: Suggester = Suggester.Builder()
                .suggesters(suggestersMap)
                .build()

            platformProperties.spaces.firstOrNull { it.name == "aat" }
            val indexNames = input.kind.map { kindType ->
                when (val either = platformProperties.kindProperties(kindType)) {
                    is Either.Left -> throw IllegalArgumentException(either.value.joinToString())
                    is Either.Right -> either.value.index
                }
            }.distinct()
            val indexCoordinates = IndexCoordinates.of(*indexNames.toTypedArray())

            return SuggesterSession(suggester, indexCoordinates)
        }
    }
}

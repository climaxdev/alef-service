package com.alefeducation.authoring.search.infra.storage

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField
import com.alefeducation.authoring.search.domain.Constants
import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.domain.WorkspacePoolIdPair
import com.alefeducation.authoring.search.infra.config.Platform.indexNames
import com.alefeducation.authoring.search.infra.config.PlatformProperties
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Component
class GetWorkspacePoolIdPair(
    private val reactiveElasticsearchTemplate: ReactiveElasticsearchTemplate,
    private val platformProperties: PlatformProperties
) {
    suspend operator fun invoke(org: String, poolIds: List<String>): Flow<WorkspacePoolIdPair> {
        val nativeQuery = NativeQueryBuilder()
            .withQuery(poolsTermQuery(org, poolIds))
            .withFilter(workspace())
            .withSourceFilter(FetchSourceFilterBuilder().withIncludes("id", "pools").build())
            .build()

        return reactiveElasticsearchTemplate.search(nativeQuery, Any::class.java, indexCoordinates(listOf(KindType.WORKSPACES)))
            .map { it.content as Map<*, *> }
            .transform { toWorkspacePoolIdPair(poolIds, it.collectList()) }
            .asFlow()
    }

    private fun toWorkspacePoolIdPair(poolIds: List<String>, contentMono: Mono<MutableList<Map<*, *>>>): Flux<WorkspacePoolIdPair> =
        contentMono.flatMapMany { content ->
            poolIds.mapNotNull { poolId ->
                findContentByPoolId(content, poolId)?.let { WorkspacePoolIdPair(it["id"] as String, poolId) }
            }.toFlux()
        }

    private fun findContentByPoolId(content: MutableList<Map<*, *>>, poolId: String) = content.find { (it["pools"] as List<*>).contains(poolId) }

    private fun workspace() = Query(QueryBuilders.term().field(Constants.KIND).value(KindType.WORKSPACES.value).build())

    private fun poolsTermQuery(org: String, poolIds: List<String>) = Query(
        QueryBuilders.bool().must(
            listOf(
                Query(MatchQuery.Builder().field(Constants.Search.ORGANISATIONS_FIELD).query(org).build()),
                Query(QueryBuilders.terms().field("pools").terms(TermsQueryField.Builder().value(poolIds.map { FieldValue.of(it) }).build()).build())
            )
        ).build()
    )

    private fun indexCoordinates(kinds: List<KindType>): IndexCoordinates = IndexCoordinates.of(*platformProperties.indexNames(kinds).toTypedArray())
}

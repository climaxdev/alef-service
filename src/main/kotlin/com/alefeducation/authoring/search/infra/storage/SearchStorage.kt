package com.alefeducation.authoring.search.infra.storage

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import co.elastic.clients.elasticsearch.core.search.FieldCollapse
import co.elastic.clients.elasticsearch.core.search.InnerHits
import com.alefeducation.authoring.search.domain.Constants
import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.domain.SearchResult
import com.alefeducation.authoring.search.infra.config.Platform.indexNames
import com.alefeducation.authoring.search.infra.config.PlatformProperties
import com.alefeducation.authoring.search.infra.http.inputs.SearchInput
import com.alefeducation.authoring.search.infra.storage.mapper.toSearchResult
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Component

@Component
class SearchStorage(
    private val reactiveElasticsearchTemplate: ReactiveElasticsearchTemplate,
    private val platformProperties: PlatformProperties
) {

    suspend fun searchFor(searchInput: SearchInput): SearchResult {
        val nativeQuery = NativeQueryBuilder()
            .withQuery(buildQuery(searchInput))
            .withFilter(kind(searchInput.kinds))
            .withFieldCollapse(byKind(searchInput.offset, searchInput.limit)).build()

        return reactiveElasticsearchTemplate.searchForHits(nativeQuery, Any::class.java, indexCoordinates(searchInput.kinds))
            .flatMap { it.toSearchResult() }.awaitSingle()
    }

    private fun indexCoordinates(kinds: List<KindType>): IndexCoordinates = IndexCoordinates.of(*platformProperties.indexNames(kinds).toTypedArray())

    private fun buildQuery(searchInput: SearchInput): Query =
        Query(
            BoolQuery.Builder().must(
                listOf(
                    matchOrganizationQuery(searchInput.org),
                    queryStringQuery(searchInput.term)
                )
            ).build()
        )

    private fun byKind(offset: Int, limit: Int): FieldCollapse = FieldCollapse.Builder().field(Constants.Search.COLLAPSED_BY_FIELD).innerHits(
        listOf(InnerHits.Builder().name(Constants.Search.COLLAPSED_BY_NAME).from(offset).size(limit).build())
    ).build()

    private fun matchOrganizationQuery(org: String): Query {
        val matchQuery = MatchQuery.Builder().field(Constants.Search.ORGANISATIONS_FIELD).query(org).build()
        return Query(matchQuery)
    }

    private fun kind(kinds: List<KindType>): Query =
        Query(QueryBuilders.terms().field(Constants.KIND).terms(TermsQueryField.Builder().value(kinds.map { FieldValue.of(it.value) }).build()).build())

    private fun queryStringQuery(term: String): Query {
        val queryStringQuery = QueryStringQuery.Builder().fields(
            listOf(
                "id^10",
                "code^9",
                "name^9",
                "body.prompt_plain^8",
                "body.generalFeedback^7",
                "stage",
                "language",
                "variant"
            )
        )
            .type(TextQueryType.BestFields)
            .tieBreaker(0.0)
            .query(term)
            .build()
        return Query(queryStringQuery)
    }
}

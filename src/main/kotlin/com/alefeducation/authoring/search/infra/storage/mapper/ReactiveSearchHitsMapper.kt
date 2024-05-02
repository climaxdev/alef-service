package com.alefeducation.authoring.search.infra.storage.mapper

import com.alefeducation.authoring.search.domain.Constants
import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.domain.PoolsHit
import com.alefeducation.authoring.search.domain.PoolsResult
import com.alefeducation.authoring.search.domain.QuestionsHit
import com.alefeducation.authoring.search.domain.QuestionsResult
import com.alefeducation.authoring.search.domain.SearchResult
import com.alefeducation.authoring.search.domain.WorkspaceHit
import com.alefeducation.authoring.search.domain.WorkspacesResult
import org.springframework.data.elasticsearch.core.ReactiveSearchHits
import reactor.core.publisher.Mono

fun ReactiveSearchHits<Any>.toSearchResult(): Mono<SearchResult> {
    val searchResultBuilder = SearchResult.Builder().total(this.totalHits)
    return this.searchHits.map {
        val kindValue = (it.content as Map<*, *>)[Constants.KIND].toString()
        val innerHits = it.innerHits[Constants.Search.COLLAPSED_BY_NAME]
        val innerHitsTotal = innerHits?.totalHits ?: 0
        val innerHitsContent = innerHits?.searchHits?.map { hit -> hit.content as Map<*, *> } ?: emptyList()
        when (KindType.getKindTypeFor(kindValue)) {
            KindType.QUESTIONS -> buildQuestions(innerHitsContent, searchResultBuilder, innerHitsTotal)
            KindType.POOLS -> buildPools(innerHitsContent, searchResultBuilder, innerHitsTotal)
            KindType.WORKSPACES -> buildWorkspaces(innerHitsContent, searchResultBuilder, innerHitsTotal)
            else -> throw IllegalArgumentException("Unknown kind type: $kindValue")
        }
    }.collectList().map { searchResultBuilder.build() }
}

private fun buildWorkspaces(
    innerHitsContent: List<Map<*, *>>,
    searchResultBuilder: SearchResult.Builder,
    innerHitsTotal: Long
): SearchResult.Builder {
    val workspacesHits = innerHitsContent.map { WorkspaceHit(it["name"].toString(), it["id"].toString()) }.toList()
    return searchResultBuilder.workspaces(WorkspacesResult(workspacesHits, innerHitsTotal))
}

private fun buildPools(
    innerHitsContent: List<Map<*, *>>,
    searchResultBuilder: SearchResult.Builder,
    innerHitsTotal: Long
): SearchResult.Builder {
    val poolsHits = innerHitsContent.map { PoolsHit(it["name"].toString(), it["id"].toString()) }.toList()
    return searchResultBuilder.pools(PoolsResult(poolsHits, innerHitsTotal))
}

private fun buildQuestions(
    innerHitsContent: List<Map<*, *>>,
    searchResultBuilder: SearchResult.Builder,
    innerHitsTotal: Long
): SearchResult.Builder {
    val questionsHits = innerHitsContent.map { QuestionsHit(it["code"].toString(), it["id"].toString()) }.toList()
    return searchResultBuilder.questions(QuestionsResult(questionsHits, innerHitsTotal))
}

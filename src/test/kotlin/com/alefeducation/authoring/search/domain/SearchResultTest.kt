package com.alefeducation.authoring.search.domain

import com.alefeducation.authoring.search.factory.SearchResultFactory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SearchResultTest {

    @Test
    fun `should enrich search result with workspace id in the pool`() {
        val searchResult = SearchResultFactory.createSearchResult(5, 5, 5)
        val workspaceIdByPoolId = (1..5).map { WorkspacePoolIdPair("workspace_id_$it", "pool_id_$it") }.associateBy({ it.poolId }, { it.workspaceId })

        val expectedSearchResult = searchResult.copy(pools = searchResult.pools!!.copy(hits = searchResult.pools!!.hits.map { it.copy(workspaceId = workspaceIdByPoolId[it.id]) }))
        val actualSearchResult = searchResult.enrichPoolsWithWorkspaceId(workspaceIdByPoolId)

        actualSearchResult shouldBe expectedSearchResult
    }
}

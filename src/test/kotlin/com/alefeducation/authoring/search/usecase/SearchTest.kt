package com.alefeducation.authoring.search.usecase

import com.alefeducation.authoring.search.builders.SearchInputBuilder
import com.alefeducation.authoring.search.domain.WorkspacePoolIdPair
import com.alefeducation.authoring.search.factory.SearchResultFactory
import com.alefeducation.authoring.search.infra.storage.GetWorkspacePoolIdPair
import com.alefeducation.authoring.search.infra.storage.SearchStorage
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class SearchTest {

    private val searchStorage: SearchStorage = mockk()
    private val getWorkspacePoolIdPair: GetWorkspacePoolIdPair = mockk()
    private val search = Search(searchStorage, getWorkspacePoolIdPair)

    @Test
    fun `should successfully fetch the search result with workspace id`() = runTest {
        val searchInput = SearchInputBuilder().build()
        val searchResult = SearchResultFactory.createSearchResult(5, 5, 5)

        val workspacePoolIdPairs = (1..5).map { WorkspacePoolIdPair("workspace_id_$it", "pool_id_$it") }

        coEvery { searchStorage.searchFor(searchInput) } answers { searchResult }
        coEvery { getWorkspacePoolIdPair("shared", searchResult.pools!!.hits.map { it.id }) } answers { workspacePoolIdPairs.asFlow() }

        val expectedSearchResult = searchResult.enrichPoolsWithWorkspaceId(workspacePoolIdPairs.associateBy({ it.poolId }, { it.workspaceId }))

        val actualSearchResult = search(searchInput)

        actualSearchResult shouldBe expectedSearchResult
    }
}

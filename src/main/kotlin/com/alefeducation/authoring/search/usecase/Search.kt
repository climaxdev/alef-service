package com.alefeducation.authoring.search.usecase

import com.alefeducation.authoring.search.domain.SearchResult
import com.alefeducation.authoring.search.infra.http.inputs.SearchInput
import com.alefeducation.authoring.search.infra.storage.GetWorkspacePoolIdPair
import com.alefeducation.authoring.search.infra.storage.SearchStorage
import kotlinx.coroutines.flow.toList

class Search(private val searchStorage: SearchStorage, private val getWorkspacePoolIdPair: GetWorkspacePoolIdPair) {
    suspend operator fun invoke(searchInput: SearchInput): SearchResult {
        val searchResult = searchStorage.searchFor(searchInput)
        val workspaceIdByPoolId = getWorkspaceIdByPoolId(searchResult, searchInput.org)

        return searchResult.enrichPoolsWithWorkspaceId(workspaceIdByPoolId)
    }

    private suspend fun getWorkspaceIdByPoolId(
        searchResult: SearchResult,
        org: String
    ): Map<String, String>? =
        searchResult.pools?.hits?.map { it.id }
            ?.let { getWorkspacePoolIdPair(org, it) }
            ?.toList()
            ?.associateBy({ it.poolId }, { it.workspaceId })
}

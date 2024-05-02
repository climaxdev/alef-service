package com.alefeducation.authoring.search.infra.http

import com.alefeducation.authoring.search.domain.SearchResult
import com.alefeducation.authoring.search.infra.http.inputs.SearchInput
import com.alefeducation.authoring.search.infra.storage.GetWorkspacePoolIdPair
import com.alefeducation.authoring.search.infra.storage.SearchStorage
import com.alefeducation.authoring.search.usecase.Search
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/search")
class SearchController(searchStorage: SearchStorage, getWorkspacePoolIdPair: GetWorkspacePoolIdPair) {
    val search = Search(searchStorage, getWorkspacePoolIdPair)

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun searchFor(
        @RequestBody searchInput: SearchInput
    ): SearchResult {
        return search(searchInput)
    }
}

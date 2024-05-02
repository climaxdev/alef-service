package com.alefeducation.authoring.search.infra.http

import com.alefeducation.authoring.search.domain.Constants.Companion.MAX_SUGGESTION_LIMIT
import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.domain.SuggestionKindType
import com.alefeducation.authoring.search.domain.projections.SuggestionProjection
import com.alefeducation.authoring.search.infra.http.inputs.GetSuggestionInput
import com.alefeducation.authoring.search.infra.storage.SuggestionStorage
import com.alefeducation.authoring.search.usecase.GetSuggestions
import jakarta.validation.constraints.Max
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/suggestions")
class SuggestionController(suggestionStorage: SuggestionStorage) {

    val getSuggestions = GetSuggestions(suggestionStorage)

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getSuggestionsFor(
        @RequestParam organisation: String,
        @RequestParam term: String,
        @RequestParam("limit", defaultValue = "5") limit: @Max(MAX_SUGGESTION_LIMIT.toLong()) Int,
        @RequestParam(value = "kinds") kinds: List<KindType>,
        @RequestParam(required = false, defaultValue = "KEYWORD") suggestionKind: SuggestionKindType,
    ): Flow<SuggestionProjection> {
        return getSuggestions(GetSuggestionInput(term, limit, kinds, organisation, suggestionKind))
            .map { SuggestionProjection(it) }
    }
}

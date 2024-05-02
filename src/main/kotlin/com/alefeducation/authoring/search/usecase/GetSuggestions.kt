package com.alefeducation.authoring.search.usecase

import com.alefeducation.authoring.search.domain.Suggestion
import com.alefeducation.authoring.search.infra.http.inputs.GetSuggestionInput
import com.alefeducation.authoring.search.infra.storage.SuggestionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList

class GetSuggestions(private val suggestionStorage: SuggestionStorage) {
    suspend operator fun invoke(suggestionInput: GetSuggestionInput): Flow<Suggestion> {
        return suggestionStorage.suggestions(suggestionInput).toList()
            .sortedByDescending { it.score }.take(5).asFlow()
    }
}

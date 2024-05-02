package com.alefeducation.authoring.search.domain.projections

import com.alefeducation.authoring.search.domain.Suggestion

data class SuggestionProjection(val suggestionKind: String, val value: String) {
    companion object {
        operator fun invoke(suggestion: Suggestion) = SuggestionProjection("keyword", suggestion.text)
    }
}

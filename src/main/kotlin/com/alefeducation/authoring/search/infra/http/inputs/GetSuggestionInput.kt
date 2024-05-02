package com.alefeducation.authoring.search.infra.http.inputs

import com.alefeducation.authoring.search.domain.KindType
import com.alefeducation.authoring.search.domain.SuggestionKindType

data class GetSuggestionInput(
    val term: String,
    val limit: Int,
    val kind: List<KindType>,
    val organisation: String,
    val suggestionKind: SuggestionKindType
)
